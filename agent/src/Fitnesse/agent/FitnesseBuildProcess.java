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
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * Class describes running Fitnesse process. Process is responsible for:
 * - starting (unpacking if necessary) Fitnesse
 * - starting Fitnesse tests
 * - collecting tests results
 *
 * @todo FitNesse can be launched not only as server process but as a single command processor ('-c' parameter)
 * @todo Make some initializer that can checke whether FitNesse already working or launch new instance of it
 *
 * @author Advard, elgris
 */
public class FitnesseBuildProcess extends FutureBasedBuildProcess {

    private final static String DEFAULT_HOSTNAME = "localhost";

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
        URL url = new URL(getFitnesseUrl());
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("FitNesse is not available!");
        }
        connection.disconnect();
    }

    private BuildFinishedStatus runTests() throws Exception {
        String [] testNames = getTestNames();
        for(String testName : testNames) {
            try {
                Logger.logTestStarted(testName, new Date());
                Logger.progressMessage(String.format("Test URL: %s%s?test", getFitnesseUrl(), testName));

                URL url = new URL(String.format("%s%s?test&format=xml", getFitnesseUrl(), testName));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream stream = connection.getInputStream();
                Result result = ResultReader.getResult(stream);
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

    private int getPort() {
        return Integer.parseInt(getParameter(Util.PROPERTY_FITNESSE_PORT));
    }

    private String getFitnesseUrl() throws UnknownHostException {
        String hostname = "";
        //TODO temporary decision. Need to implement "fitnesseHost" parameter
        try {
            hostname = this.getParameter(Util.PROPERTY_FITNESSE_HOST);
        } catch (RuntimeException e) {
            hostname = InetAddress.getLocalHost().getHostName();
            if (hostname.length() == 0) {
                hostname = DEFAULT_HOSTNAME;
            }
        }
        return String.format("http://%s:%d/", hostname, getPort());
    }
}