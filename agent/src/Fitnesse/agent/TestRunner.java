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
import Fitnesse.agent.ResultReader.ResultReader;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Fitnesse Test runner. Launches test and returns it's result
 *
 * @author: elgris
 * @date 05.10.12
 */
public class TestRunner {
    @NotNull
    private String FitnessePath;
    @NotNull
    private ResultReader Reader;

    public void setFitnessePath(@NotNull String path) {
        FitnessePath = path;
    }

    public void setReader(@NotNull ResultReader reader) {
        Reader = reader;
    }

    public Result run(final String testName) throws Exception {
        URL url = new URL(String.format("%s/%s?test&format=%s", FitnessePath, testName, getFormat()));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream stream = connection.getInputStream();
        return Reader.getResult(stream);
    }

    /**
     * Method picks up format in which FitNesse should produce output
     *
     * @todo Need support of 'text' format
     * @return String
     */
    private String getFormat() {
        return "xml";
    }
}
