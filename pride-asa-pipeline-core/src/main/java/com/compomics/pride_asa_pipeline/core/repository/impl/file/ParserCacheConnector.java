package com.compomics.pride_asa_pipeline.core.repository.impl.file;

import com.compomics.util.pride.PrideWebService;
import com.compomics.util.pride.prideobjects.webservice.file.FileDetail;
import com.compomics.util.pride.prideobjects.webservice.file.FileDetailList;
import com.compomics.util.pride.prideobjects.webservice.file.FileType;
import java.io.File;
import java.io.IOException;
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
    public void addPrideXMLFile(File identificationsFile) {
        parserCache.getParser(identificationsFile, true).getName();
    }

    /**
     * Adds a mzID and related peak files to the cache
     *
     * @param identificationsFile the mzID
     * @param peakFiles the peak files
     */
    public void addMzID(File identificationsFile, List<File> peakFiles) {
        parserCache.getParser(identificationsFile, true).getName();
        parserCache.addPeakFiles(identificationsFile, peakFiles);
    }

    /**
     * Converts the given experiment accession to the correct file name
     *
     * @param experimentAccession the input experiment accession
     * @return the given experiment accession to the correct file name
     */
    protected String getCorrectExperimentIdentifier(String experimentAccession) {
        String temp = experimentAccession;
        try {
            FileDetailList assayFileDetails = PrideWebService.getAssayFileDetails(experimentAccession);
            //try to find existing result files online
            for (FileDetail assayFile : assayFileDetails.getList()) {
                if (assayFile.getFileType().equals(FileType.RESULT.toString())) {
                    temp = assayFile.getFileName();
                }
            }
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
        return temp;
    }

}
