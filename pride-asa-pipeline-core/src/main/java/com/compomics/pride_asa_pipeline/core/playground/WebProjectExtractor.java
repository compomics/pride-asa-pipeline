package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.pride_asa_pipeline.core.cache.ParserCache;
import com.compomics.pride_asa_pipeline.core.logic.inference.ParameterExtractor;
import com.compomics.pride_asa_pipeline.core.model.MGFExtractionException;
import com.compomics.pride_asa_pipeline.core.repository.impl.combo.WebServiceFileExperimentRepository;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import java.io.File;
import java.io.IOException;
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
public class WebProjectExtractor {

    /**
     * The output folder for the extraction
     */
    private final File outputFolder;
    /**
     * The Logger instance
     */
    private static final Logger LOGGER = Logger.getLogger(WebProjectExtractor.class);

    public static void main(String[] args) throws IOException, ParseException, MGFExtractionException, MzXMLParsingException, JMzReaderException, XmlPullParserException, ClassNotFoundException, GOBOParseException, InterruptedException, Exception {
        File outputFolder = new File("C:\\Users\\Kenneth\\Desktop\\MzID_Test\\download");
        String inputAssay = "8173";
        SearchParameters analyze = new WebProjectExtractor(outputFolder).analyze(inputAssay);
        System.out.println(analyze);
    }

    public WebProjectExtractor(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    public SearchParameters analyze(String assayAccession) throws IOException, MGFExtractionException, MzXMLParsingException, JMzReaderException, XmlPullParserException, ClassNotFoundException, GOBOParseException, Exception {
        LOGGER.info("Setting up experiment repository for assay " + assayAccession);
        WebServiceFileExperimentRepository experimentRepository = new WebServiceFileExperimentRepository();
        experimentRepository.addAssay(assayAccession);
        //the cache should only have one for now?
        String entry = ParserCache.getInstance().getLoadedFiles().keySet().iterator().next();
        LOGGER.info(entry + " was found in the parser cache");
        //write an MGF with all peakfile information?
        LOGGER.info("Getting related spectrum files from the cache");
       /* FileSpectrumRepository spectrumRepository = new FileSpectrumRepository(entry);
        File mgf = spectrumRepository.writeToMGF(outputFolder);
        //zip the MGF file
        File zip = new File(mgf.getAbsolutePath() + ".zip");
        ZipUtils.zip(mgf, zip, new WaitingHandlerCLIImpl(), mgf.length());
        mgf.delete();*/
        //do the extraction
        LOGGER.info("Attempting to infer searchparameters");
        ParameterExtractor extractor = new ParameterExtractor(assayAccession);
        return extractor.getParameters();
    }
}
