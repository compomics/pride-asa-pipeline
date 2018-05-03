/* 
 * Copyright 2018 compomics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.compomics.pride_asa_pipeline.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author Niels Hulstaert
 */
public class IOUtils {

    /**
     * Unzip and write to file.
     *
     * @param inputFile the input file
     * @param outputFile the output file
     *
     * @throws java.io.IOException
     */
    public static void unzip(File inputFile, File outputFile) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                GZIPInputStream gZIPInputStream = new GZIPInputStream(new FileInputStream(inputFile))) {
            //unzip
            //this method uses a buffer internally
            org.apache.commons.io.IOUtils.copy(gZIPInputStream, fileOutputStream);
        }
    }

}
