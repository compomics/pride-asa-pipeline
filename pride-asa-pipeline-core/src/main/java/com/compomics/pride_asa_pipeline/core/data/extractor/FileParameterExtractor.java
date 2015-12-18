package com.compomics.pride_asa_pipeline.core.data.extractor;

import com.compomics.pride_asa_pipeline.core.exceptions.ParameterExtractionException;
import com.compomics.pride_asa_pipeline.core.model.MGFExtractionException;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileExperimentRepository;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileModificationRepository;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileSpectrumRepository;
import com.compomics.pride_asa_pipeline.core.spring.ApplicationContextProvider;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.io.compression.ZipUtils;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.log4j.Logger;
import org.geneontology.oboedit.dataadapter.GOBOParseException;
import org.xmlpull.v1.XmlPullParserException;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 * This class is used to process an assay from an input file
 *
 * @author Kenneth Verheggen
 */
public class FileParameterExtractor {

    /**
     * The output folder for the extraction
     */
    private final File outputFolder;
    /**
     * The Logger instance
     */
    private static final Logger LOGGER = Logger.getLogger(FileParameterExtractor.class);

    private FileExperimentRepository experimentRepository;
    private FileSpectrumRepository spectrumRepository;
    private FileModificationRepository modificationRepository;
    private AnalyzerData analyzerData;

    public FileParameterExtractor(File outputFolder) throws IOException {
        this.outputFolder = outputFolder;
        this.analyzerData = AnalyzerData.getAnalyzerDataByAnalyzerType("");
        init();
    }

    public FileParameterExtractor(File outputFolder, AnalyzerData analyzerData) throws IOException {
        this.outputFolder = outputFolder;
        this.analyzerData = analyzerData;
        init();
    }

    private void init() {
        //load into the spring setup
        ApplicationContextProvider.getInstance().setDefaultApplicationContext();
        experimentRepository = (FileExperimentRepository) ApplicationContextProvider.getInstance().getBean("experimentRepository");
        spectrumRepository = (FileSpectrumRepository) ApplicationContextProvider.getInstance().getBean("spectrumRepository");
        modificationRepository = (FileModificationRepository) ApplicationContextProvider.getInstance().getBean("modificationRepository");
        //add a logger specific to this file
    }

    private SearchParameters inferParameters(String assay) throws ParameterExtractionException, IOException {
        LOGGER.info("Attempting to infer searchparameters");
        ParameterExtractor extractor = new ParameterExtractor(assay, analyzerData);
        SearchParameters parameters = extractor.getParameters();
        extractor.printReports(outputFolder);
        SearchParameters.saveIdentificationParameters(parameters, new File(outputFolder, assay + ".par"));
        return parameters;
    }

    private void processSpectra() throws IOException, MGFExtractionException {
        LOGGER.info("Getting related spectrum files from the cache");
        File mgf = spectrumRepository.writeToMGF(outputFolder);
        //zip the MGF file
        File zip = new File(mgf.getAbsolutePath() + ".zip");
        ZipUtils.zip(mgf, zip, new WaitingHandlerCLIImpl(), mgf.length());
        mgf.delete();
    }

    public SearchParameters analyzePrideXML(File inputFile, String assay) throws IOException, MGFExtractionException, MzXMLParsingException, JMzReaderException, XmlPullParserException, ClassNotFoundException, GOBOParseException, Exception {
        LOGGER.info("Setting up experiment repository for assay " + assay);
        //load the file into the repository
        experimentRepository.addPrideXMLFile(assay, inputFile);
        spectrumRepository.setExperimentIdentifier(assay);
        modificationRepository.setExperimentIdentifier(assay);
        processSpectra();
        return inferParameters(assay);
    }

    public SearchParameters analyzeMzID(File inputFile, List<File> peakFiles, String assay) throws MGFExtractionException, ParameterExtractionException, IOException {
        LOGGER.info("Setting up experiment repository for assay " + assay);
        experimentRepository.addMzID(assay, inputFile, peakFiles);
        spectrumRepository.setExperimentIdentifier(assay);
        modificationRepository.setExperimentIdentifier(assay);
        processSpectra();
        return inferParameters(assay);
    }

}
