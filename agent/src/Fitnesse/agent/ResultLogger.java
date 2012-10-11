/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package Fitnesse.agent;

import Fitnesse.agent.ResultReader.Result;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.jetbrains.annotations.NotNull;

/**
 * Logger class to print formatted messages about results using TeamCity logger
 *
 * @author: elgris
 * @date 05.10.12
 */
public class ResultLogger {
    @NotNull
    private final BuildProgressLogger Logger;

    public ResultLogger(BuildProgressLogger progressLogger) {
        Logger = progressLogger;
    }

    public void print(String testName, String testUrl, Result result) {
        int rights = result.getRightsCount();
        int wrongs = result.getWrongsCount();
        int exceptions = result.getExceptionsCount();
        int ignores = result.getIgnoresCount();
        int timeConsumed = result.getTimeConsumed();

        String resultString = String.format(
            "right: %d\twrong: %d\texceptions: %d\t ignored: %d\nTest runtime: %d msec\nTest URL: %s",
            rights, wrongs, exceptions, ignores, timeConsumed, testUrl
        );
        if(rights == 0 && wrongs == 0 && exceptions == 0 && ignores == 0) {
            Logger.logTestIgnored(testName, "Empty test");
        } else if (wrongs > 0 || exceptions > 0) {
            Logger.logTestFailed(testName, resultString, "");
        } else {
            Logger.logTestFinished(resultString);
        }
    }

}
