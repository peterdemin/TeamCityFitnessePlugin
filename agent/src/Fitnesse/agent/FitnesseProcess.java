package Fitnesse.agent;

import Fitnesse.agent.ResultReader.Result;
import Fitnesse.agent.ResultReader.ResultReaderFactory;
import Fitnesse.agent.ResultReader.ResultReaderType;
import Fitnesse.common.Util;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.jetbrains.annotations.NotNull;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * Class describes running Fitnesse process. Process is responsible for:
 * - starting (unpacking if necessary) Fitnesse
 * - starting Fitnesse tests
 * - collecting tests results
 *
 * @author Advard, elgris
 */
public class FitnesseProcess extends FutureBasedBuildProcess {

    private final static String LOCAL_URL = "http://localhost";

    @NotNull
    private final AgentRunningBuild Build;
    @NotNull
    private final BuildRunnerContext Context;
    @NotNull
    private final BuildProgressLogger Logger;
    @NotNull
    private final ResultLogger ResultLogger;
    @NotNull
    private TestRunner TestRunner;

    public FitnesseProcess(@NotNull final AgentRunningBuild build, @NotNull final BuildRunnerContext context) {
        Build = build;
        Context = context;
        Logger = build.getBuildLogger();
        ResultLogger = new ResultLogger(Logger);
        TestRunner = new TestRunner();
    }

    public BuildFinishedStatus call() throws Exception {
        BuildFinishedStatus result = BuildFinishedStatus.FINISHED_FAILED;
        try {
            ensureFitnesseAvailable();
            initTestRunner();
            result = runTests();
        } catch(InterruptedException e) {
            result = BuildFinishedStatus.INTERRUPTED;
            Logger.error("Fitnesse process has been interrupted");
        }
        return result;
    }

    private void initTestRunner() throws Exception {
        TestRunner.setFitnessePath(getFitnesseUrl().toString());
        // TODO here reader is being set up every time... That's bad
        TestRunner.setReader(ResultReaderFactory.getStreamReader(ResultReaderType.Xml));
    }

    // TODO here we only check that FitNesse can be launched as webserver. It can be launched for single command though
    // TODO Implement FitNesse as command executor
    private void ensureFitnesseAvailable() {
        try {
            Logger.progressMessage(new String("Checking if FitNesse is already up'n'running"));
            checkExistingFitnesseConnection();
        } catch(IOException e) {
            Logger.progressMessage(new String("Launching new FitNesse server"));
            createFitnesseConnection();
        }
    }

    private void checkExistingFitnesseConnection() throws IOException {
        URL url = getFitnesseUrl();
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.disconnect();
        Logger.progressMessage(String.format("FitNesse is running at %s", url.toString()));
    }

    private void createFitnesseConnection() {
        try {
            Process fitnesseProcess = runFitnesseInstance();
            waitUntilFitnesseUnpacks(fitnesseProcess);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not create FitNesse connection: %s", e.getMessage()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Process runFitnesseInstance() throws IOException {
        String cmdFitnesse = getFitNesseCmd();
        String rootFolder = getFitnesseRoot();
        Logger.progressMessage(String.format("Running fitnesse use cmd '%s' in '%s'", cmdFitnesse, rootFolder));
        return Runtime.getRuntime().exec(cmdFitnesse, null, new File(rootFolder));
    }

    // TODO we can encapsulate it to some FitnesseUnpacker or FitnesseInstance
    private void waitUntilFitnesseUnpacks(Process fitnesseProcess) throws Exception {
        BufferedReader is = new BufferedReader(new InputStreamReader(fitnesseProcess.getInputStream()));

        int timeout = 60;
        int count = 0;
        String line = "";
        do {
            line = is.readLine();
            if (line == null) {
                Thread.sleep(1000);
                count++;
            }
        } while (
            (line == null || !line.contains("page version expiration set to"))
                && count < timeout
                && !isInterrupted()
        );
        if(isInterrupted()) {
            throw new InterruptedException();
        }
        if(line == null || line.contains("page version expiration set to")) {
            throw new Exception("Incorrect FitNesse output detected!");
        }
    }

    private BuildFinishedStatus runTests() throws Exception {
        String [] testNames = getTestNames();
        for(String testName : testNames) {
            try {
                Logger.logTestStarted(testName, new Date());
                Result result = TestRunner.run(testName);
                ResultLogger.print(testName, result);
            } catch(Exception e) {
                Logger.logTestFailed(testName, e);
            } finally {
                Logger.logTestFinished(testName, new Date());
            }
        }
        return BuildFinishedStatus.FINISHED_SUCCESS;
    }

    private String[] getTestNames() {
        return getParameter(Util.PROPERTY_FITNESSE_TEST).split(";");
    }

    @NotNull
    private String getParameter(@NotNull final String parameterName) {
        final String value = Context.getRunnerParameters().get(parameterName);
        if (value == null) {
            throw new RuntimeException(String.format("Unknown parameter: %s", parameterName));
        }
        String result = value.trim();
        return result;
    }

    private String getFitNesseCmd() {
        return String.format("java -jar %s -p %d -d %s", getRunCommand(), getPort(), getFitnesseRoot());
    }

    private String getRunCommand() {
        File jarFitnesse = new File(getParameter("fitnesseJarPath"));
        return isWindows()
            ? getCommandForWindows(jarFitnesse)
            : getCommandForNix(jarFitnesse);
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("win") >= 0);
    }

    private String getCommandForWindows(File jarFile) {
        return String.format("\"%s\"", jarFile.getAbsolutePath(), getPort());
    }

    private String getCommandForNix(File jarFile) {
        String fileName = jarFile.getName();
        if (fileName.indexOf(" ") >= 0) {
            Logger.progressMessage(
                String.format(
                    "Trying to run jar file '%s' which contains spaces in name. Spaces can brake process",
                    fileName
                )
            );
        }
        return fileName;
    }

    private String getFitnesseRoot() {
        File jarFitnesse = new File(getParameter("fitnesseJarPath"));
        return jarFitnesse.getParent();
    }

    private int getPort() {
        return Integer.parseInt(getParameter(Util.PROPERTY_FITNESSE_PORT));
    }

    private URL getFitnesseUrl() {
        String urlString = String.format("%s:%d/", LOCAL_URL, getPort());
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            // Log that we have an error in our URL providing 'urlString'
            throw new RuntimeException(e);
        }
    }
}