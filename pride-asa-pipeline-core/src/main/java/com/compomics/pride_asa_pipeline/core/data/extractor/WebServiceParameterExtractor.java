package com.compomics.pride_asa_pipeline.core.data.extractor;

import com.compomics.pride_asa_pipeline.core.cache.ParserCache;
import com.compomics.pride_asa_pipeline.core.exceptions.ParameterExtractionException;
import com.compomics.pride_asa_pipeline.core.model.MGFExtractionException;
import com.compomics.pride_asa_pipeline.core.repository.impl.combo.WebServiceFileExperimentRepository;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileSpectrumRepository;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.io.compression.ZipUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParserException;

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
     *
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
     *
     * @param assayAccession the assay identifier that needs to be run
     * @return the inferred SearchParameters object
     * @throws Exception
     */
    public SearchParameters analyze(String assayAccession) throws FileNotFoundException, IOException, XmlPullParserException, ParameterExtractionException {
        LOGGER.info("Setting up experiment repository for assay " + assayAccession);
        ParameterExtractor extractor = null;
        SearchParameters parameters = null;
        try {
            WebServiceFileExperimentRepository experimentRepository = new WebServiceFileExperimentRepository(tempFolder);
            experimentRepository.addAssay(assayAccession);
            extractor = new ParameterExtractor(assayAccession);
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
            parameters = extractor.getParameters();
            extractor.printReports(outputFolder);
            //remediate error
            SearchParameters.saveIdentificationParameters(parameters, new File(outputFolder, assayAccession + ".par"));
        } catch (Exception e) {
            File errorFile = new File(outputFolder, "extraction_error.log");
            errorFile.getParentFile().mkdirs();
            try (PrintStream ps = new PrintStream(errorFile)) {
                e.printStackTrace(ps);
                if (extractor != null) {
                    extractor.useDefaults(assayAccession);
                    parameters = extractor.getParameters();
                } else {
                    throw new ParameterExtractionException("Extractor failed for unknown reason");
                }
            }
        } finally {
            if (extractor != null) {
                extractor.clear();
            }
        }
        return parameters;
    }

    /**
     * Deletes the temp folder
     *
     * @throws IOException the folder can not be deleted
     */
    public void clearTempFolder() throws IOException {
        FileUtils.deleteDirectory(tempFolder);
    }

}
