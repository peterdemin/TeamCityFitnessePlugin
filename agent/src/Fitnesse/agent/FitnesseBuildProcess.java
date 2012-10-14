package Fitnesse.agent;

import Fitnesse.agent.ResultReader.Result;
import Fitnesse.agent.ResultReader.ResultReader;
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
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * Class describes running Fitnesse process. Process is responsible for:
 * - starting Fitnesse tests
 * - collecting tests results
 *
 * @author Advard, elgris
 */
public class FitnesseBuildProcess extends FutureBasedBuildProcess {

    //TODO This is stub for future testsuites support. Run mode can be either 'test' or 'suite' or something else
    private final static String TEST_RUN_MODE = "test";

    //TODO This is stub for future output formats support. Can be either 'xml' or 'text'
    private final static String OUTPUT_FORMAT = "xml";

    @NotNull
    private final AgentRunningBuild Build;
    @NotNull
    private final BuildRunnerContext Context;
    @NotNull
    private final BuildProgressLogger Logger;
    @NotNull
    private final ResultLogger ResultLogger;
    @NotNull
    private ResultReader ResultReader;

    public FitnesseBuildProcess(@NotNull final AgentRunningBuild build, @NotNull final BuildRunnerContext context) {
        Build = build;
        Context = context;
        Logger = build.getBuildLogger();
        ResultLogger = new ResultLogger(Logger);
        ResultReader = ResultReaderFactory.getStreamReader(ResultReaderType.Xml);
    }

    public BuildFinishedStatus call() throws Exception {
        BuildFinishedStatus result = BuildFinishedStatus.FINISHED_FAILED;
        try {
            checkExistingFitnesseConnection();
            result = runTests();
        } catch(IOException e) {
            Logger.error("Fitnesse process was not started. Please, start Fitnesse on th agent.\nAutomatic launch coming soon");
        }
        return result;
    }

    private void checkExistingFitnesseConnection() throws IOException {
        URL url = new URL(getFitnesseHost());
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("FitNesse is not available!");
        }
        connection.disconnect();
    }

    private BuildFinishedStatus runTests() throws Exception {
        String [] testNames = getTestNames();
        for(String testName : testNames) {
            String testUrl = String.format("%s%s?%s", getFitnesseHost(), testName, TEST_RUN_MODE);
            try {
                Logger.logTestStarted(testName, new Date());
                URL url = new URL(String.format("%s&format=%s", testUrl, OUTPUT_FORMAT));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream stream = connection.getInputStream();
                Result result = ResultReader.getResult(stream);
                ResultLogger.print(testName, testUrl, result);
            } catch(Exception e) {
                Logger.logTestFailed(
                    testName,
                    String.format("Exception encountered:%s\nTest URL: %s", e.getMessage(), testUrl), ""
                );
            } finally {
                Logger.logTestFinished(testName, new Date());
            }
        }
        return BuildFinishedStatus.FINISHED_SUCCESS;
    }

    private String[] getTestNames() {
        return getParameter(Util.PROPERTY_FITNESSE_TESTS).split(";");
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

    private String getFitnesseHost() throws UnknownHostException {
        String hostname = this.getParameter(Util.PROPERTY_FITNESSE_HOST);
        if(hostname.length() == 0) {
            hostname = Util.DEFAULT_HOST;
        }
        return hostname;
    }
}