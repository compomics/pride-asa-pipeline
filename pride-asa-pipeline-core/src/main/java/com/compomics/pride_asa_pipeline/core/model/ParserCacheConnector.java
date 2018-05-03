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
package com.compomics.pride_asa_pipeline.core.model;

import com.compomics.pride_asa_pipeline.core.cache.ParserCache;
import java.io.File;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * A class that has to be extended by classes that communicate with the parser
 * cache to add new files
 *
 * @author Kenneth Verheggen
 */
public abstract class ParserCacheConnector {

    /**
     * The parser cache
     */
    protected ParserCache parserCache = ParserCache.getInstance();
    /**
     * The Logger instance
     */
    private static final Logger LOGGER = Logger.getLogger(ParserCacheConnector.class);

    /**
     * Adds a prideXML file to the cache
     *
     * @param identificationsFile the prideXML file
     */
    public void addPrideXMLFile(String experimentAccession,File identificationsFile) {
        parserCache.getParser(experimentAccession,identificationsFile, true).getName();
    }

    /**
     * Adds a mzID and related peak files to the cache
     *
     * @param identificationsFile the mzID
     * @param peakFiles the peak files
     */
    public void addMzID(String experimentAccession,File identificationsFile, List<File> peakFiles) {
        parserCache.getParser(experimentAccession,identificationsFile, true).getName();
        parserCache.addPeakFiles(experimentAccession,identificationsFile, peakFiles);
    }



}
