package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.pride_asa_pipeline.core.cache.ParserCache;
import com.compomics.pride_asa_pipeline.core.data.extractor.ParameterExtractor;
import com.compomics.pride_asa_pipeline.core.model.MGFExtractionException;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileExperimentRepository;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileModificationRepository;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileSpectrumRepository;
import com.compomics.pride_asa_pipeline.core.spring.ApplicationContextProvider;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.io.compression.ZipUtils;
import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.geneontology.oboedit.dataadapter.GOBOParseException;
import org.xmlpull.v1.XmlPullParserException;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 *
 * @author Kenneth Verheggen
 */
public class FileProjectExtractor {

    /**
     * The output folder for the extraction
     */
    private final File outputFolder;
    /**
     * The Logger instance
     */
    private static final Logger LOGGER = Logger.getLogger(FileProjectExtractor.class);

    public static void main(String[] args) throws IOException, ParseException, MGFExtractionException, MzXMLParsingException, JMzReaderException, XmlPullParserException, ClassNotFoundException, GOBOParseException, InterruptedException, Exception {
        File outputFolder = new File("C:\\Users\\compomics\\Documents\\Example_Files\\download");
        File inputFile = new File("C:\\Users\\compomics\\Documents\\Example_Files\\PRIDE_Exp_Complete_Ac_3.xml");
        //File inputFile = new File("C:\\Users\\compomics\\Documents\\Example_Files\\PeptideShaker_Example.xml");
        System.out.println(new FileProjectExtractor(outputFolder).analyze(inputFile, inputFile.getName()));
    }

    public FileProjectExtractor(File outputFolder) throws IOException {
        this.outputFolder = outputFolder;
        //add a logger specific to this file
        String targetLog = outputFolder.getAbsolutePath() + "extraction.log";

        FileAppender apndr = new FileAppender(new PatternLayout("%d %-5p [%c{1}] %m%n"), targetLog, true);
        Logger.getRootLogger().addAppender(apndr);
        Logger.getRootLogger().setLevel((Level) Level.DEBUG);
    }

    public SearchParameters analyze(File inputFile, String assay) throws IOException, MGFExtractionException, MzXMLParsingException, JMzReaderException, XmlPullParserException, ClassNotFoundException, GOBOParseException, Exception {
        LOGGER.info("Setting up experiment repository for assay " + assay);
        //load into the spring setup
        ApplicationContextProvider.getInstance().setDefaultApplicationContext();
        FileExperimentRepository experimentRepository = (FileExperimentRepository) ApplicationContextProvider.getInstance().getBean("experimentRepository");
        FileSpectrumRepository spectrumRepository = (FileSpectrumRepository) ApplicationContextProvider.getInstance().getBean("spectrumRepository");
        FileModificationRepository modificationRepository = (FileModificationRepository) ApplicationContextProvider.getInstance().getBean("modificationRepository");

        //load the file into the repository
        experimentRepository.addPrideXMLFile(assay, inputFile);
        spectrumRepository.setExperimentIdentifier(assay);
        modificationRepository.setExperimentIdentifier(assay);

        //the cache should only have one for now?
        String entry = ParserCache.getInstance().getLoadedFiles().keySet().iterator().next();
        LOGGER.info(entry + " was found in the parser cache");

        LOGGER.info("Getting related spectrum files from the cache");
        //    FileSpectrumRepository spectrumRepository = new FileSpectrumRepository(entry);
        File mgf = spectrumRepository.writeToMGF(outputFolder);
        //zip the MGF file
        File zip = new File(mgf.getAbsolutePath() + ".zip");
        ZipUtils.zip(mgf, zip, new WaitingHandlerCLIImpl(), mgf.length());
        mgf.delete();
        //do the extraction
        LOGGER.info("Attempting to infer searchparameters");
        ParameterExtractor extractor = new ParameterExtractor(assay);
        SearchParameters parameters = extractor.getParameters();
        extractor.printReports(outputFolder);
        SearchParameters.saveIdentificationParameters(parameters, new File(outputFolder, assay + ".par"));
        return parameters;
    }
}
