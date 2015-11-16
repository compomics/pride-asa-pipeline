package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.pride_asa_pipeline.core.cache.ParserCache;
import com.compomics.pride_asa_pipeline.core.logic.inference.ParameterExtractor;
import com.compomics.pride_asa_pipeline.core.logic.inference.massdeficit.FragmentIonErrorPredictor;
import com.compomics.pride_asa_pipeline.core.model.MGFExtractionException;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileExperimentRepository;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileModificationRepository;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileSpectrumRepository;
import com.compomics.pride_asa_pipeline.core.spring.ApplicationContextProvider;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.geneontology.oboedit.dataadapter.GOBOParseException;
import org.xmlpull.v1.XmlPullParserException;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 *
 * @author Kenneth Verheggen
 */
public class FileProjectFinder {

    /**
     * The output folder for the extraction
     */
    private final File outputFolder;
    /**
     * The Logger instance
     */
    private static final Logger LOGGER = Logger.getLogger(FileProjectFinder.class);

    public static void main(String[] args) throws IOException, ParseException, MGFExtractionException, MzXMLParsingException, JMzReaderException, XmlPullParserException, ClassNotFoundException, GOBOParseException, InterruptedException, Exception {
        File outputFolder = new File("C:\\Users\\Kenneth\\Desktop\\MzID_Test\\download");
        File inputFile = new File("C:\\Users\\Kenneth\\Desktop\\MzID_Test\\download\\PRIDE_Exp_Complete_Ac_3.xml");
        //File inputFile = new File("C:\\Users\\Kenneth\\Desktop\\MzID_Test\\download\\PeptideShaker_Example.xml");
        new FileProjectFinder(outputFolder).analyze(inputFile,inputFile.getName());
    }

    public FileProjectFinder(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    public void analyze(File inputFile,String assay) throws IOException, MGFExtractionException, MzXMLParsingException, JMzReaderException, XmlPullParserException, ClassNotFoundException, GOBOParseException, Exception {
        LOGGER.info("Setting up experiment repository for assay " + assay);
        //load into the spring setup
        ApplicationContextProvider.getInstance().setDefaultApplicationContext();
        FileExperimentRepository experimentRepository = (FileExperimentRepository) ApplicationContextProvider.getInstance().getBean("experimentRepository");
        FileSpectrumRepository spectrumRepository = (FileSpectrumRepository) ApplicationContextProvider.getInstance().getBean("spectrumRepository");
        FileModificationRepository modificationRepository = (FileModificationRepository) ApplicationContextProvider.getInstance().getBean("modificationRepository");

        //load the file into the repository
        experimentRepository.addPrideXMLFile(assay, inputFile);
        experimentRepository.loadExperimentIdentifications(assay);
        spectrumRepository.setExperimentIdentifier(assay);
        modificationRepository.setExperimentIdentifier(assay);

        //the cache should only have one for now?
        String entry = ParserCache.getInstance().getLoadedFiles().keySet().iterator().next();
        LOGGER.info(entry + " was found in the parser cache");

        //determine a very crude fragment acc
        List<Identification> experimentIdentifications = experimentRepository.loadExperimentIdentifications(assay);
        HashMap<Peptide, double[]> mzValueMap = new HashMap<>();
        for (Identification anExpIdentification : experimentIdentifications) {
            double[] mzValuesBySpectrumId = spectrumRepository.getMzValuesBySpectrumId(anExpIdentification.getSpectrumId());
            Peptide peptide = anExpIdentification.getPeptide();
            mzValueMap.put(peptide, mzValuesBySpectrumId);
        }
        FragmentIonErrorPredictor errorPredictor = new FragmentIonErrorPredictor(mzValueMap);

        //write an MGF with all peakfile information?
        /* 
         LOGGER.info("Getting related spectrum files from the cache");
         File mgf = spectrumRepository.writeToMGF(outputFolder);
         //zip the MGF file
         File zip = new File(mgf.getAbsolutePath() + ".zip");
         ZipUtils.zip(mgf, zip, new WaitingHandlerCLIImpl(), mgf.length());
         mgf.delete();
         */
        //do the extraction
        LOGGER.info("Attempting to infer searchparameters");
        ParameterExtractor extractor = new ParameterExtractor(assay, errorPredictor.getFragmentIonAccuraccy());
        //ParameterExtractor extractor = new ParameterExtractor(assay);
        SearchParameters inferSearchParameters = extractor.getParameters();
        System.out.println("MASS_OUTPUT : " + inferSearchParameters.getPrecursorAccuracyDalton() + "\t" + inferSearchParameters.getFragmentIonAccuracy());
        System.out.println("MOD FIXED: " + inferSearchParameters.getPtmSettings().getFixedModifications());
        System.out.println("MOD VAR: " + inferSearchParameters.getPtmSettings().getAllNotFixedModifications());

        //SearchParameters.saveIdentificationParameters(inferSearchParameters, new File(outputFolder, assay + ".asap.par"));
    }
}
