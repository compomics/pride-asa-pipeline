package com.compomics.pride_asa_pipeline.core.data.extractor;

import com.compomics.pride_asa_pipeline.core.playground.*;
import com.compomics.pride_asa_pipeline.core.cache.ParserCache;
import com.compomics.pride_asa_pipeline.core.data.extractor.ParameterExtractor;
import com.compomics.pride_asa_pipeline.core.model.MGFExtractionException;
import com.compomics.pride_asa_pipeline.core.repository.impl.combo.WebServiceFileExperimentRepository;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileSpectrumRepository;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.io.compression.ZipUtils;
import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geneontology.oboedit.dataadapter.GOBOParseException;
import org.xmlpull.v1.XmlPullParserException;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 *
 * @author Kenneth Verheggen This class is used to automatically download and
 * process an assay through the PRIDE WS
 */
public class WebServiceParameterExtractor {

    /**
     * The output folder for the extraction
     */
    private final File outputFolder;
    /**
     * The Logger instance
     */
    private static final Logger LOGGER = Logger.getLogger(WebServiceParameterExtractor.class);
    /**
     * The temporary folder where files should be downloaded to 
     */
    private final File tempFolder;

    /**
     * Default constructor for a WebProjectExtractor
     * @param outputFolder the folder where the results will be stored in
     * @throws IOException
     */
    public WebServiceParameterExtractor(File outputFolder) throws IOException {
        ParserCache.getInstance().clear();
        this.outputFolder = outputFolder;
        this.tempFolder = new File(outputFolder, "temp");
        tempFolder.mkdirs();
    }

    /**
     * Downloads, extracts and zips the results for the given assay
     * @param assayAccession the assay identifier that needs to be run
     * @return the inferred SearchParameters object
     * @throws Exception
     */
    public SearchParameters analyze(String assayAccession) throws Exception {
        LOGGER.info("Setting up experiment repository for assay " + assayAccession);
        WebServiceFileExperimentRepository experimentRepository = new WebServiceFileExperimentRepository(tempFolder);
        experimentRepository.addAssay(assayAccession);
        if (!ParserCache.getInstance().getLoadedFiles().containsKey(assayAccession)) {
            throw new MGFExtractionException("There is no suited parser in the cache !");
        }
        //write an MGF with all peakfile information?
        LOGGER.info("Getting related spectrum files from the cache");
        FileSpectrumRepository spectrumRepository = new FileSpectrumRepository(assayAccession);
        File mgf = spectrumRepository.writeToMGF(outputFolder);
        //zip the MGF file
        File zip = new File(mgf.getAbsolutePath() + ".zip");
        ZipUtils.zip(mgf, zip, new WaitingHandlerCLIImpl(), mgf.length());
        mgf.delete();
        //do the extraction
        LOGGER.info("Attempting to infer searchparameters");
        ParameterExtractor extractor = new ParameterExtractor(assayAccession);
        SearchParameters parameters;
        try {
            parameters = extractor.getParameters();
        } catch (Exception e) {
            extractor.useDefaults(assayAccession);
            parameters = extractor.getParameters();
        }
        extractor.printReports(outputFolder);
        //remediate error

        SearchParameters.saveIdentificationParameters(parameters, new File(outputFolder, assayAccession + ".par"));
        return parameters;
    }

    /** 
     * Deletes the temp folder
     * @throws IOException the folder can not be deleted
     */
    public void clearTempFolder() throws IOException {
        FileUtils.deleteDirectory(tempFolder);
    }

}
