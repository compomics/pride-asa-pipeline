package com.compomics.pride_asa_pipeline.core.repository.impl.file;

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
