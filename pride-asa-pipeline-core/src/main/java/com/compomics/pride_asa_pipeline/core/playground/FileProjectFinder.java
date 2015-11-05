/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.pride_asa_pipeline.core.model.MGFExtractionException;
import com.compomics.pride_asa_pipeline.core.inference.parameters.PrideAsapExtractor;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileExperimentRepository;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileSpectrumRepository;
import com.compomics.pride_asa_pipeline.core.cache.ParserCache;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.io.compression.ZipUtils;
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
        new FileProjectFinder(outputFolder).analyze(inputFile);
    }

    public FileProjectFinder(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    public void analyze(File inputFile) throws IOException, MGFExtractionException, MzXMLParsingException, JMzReaderException, XmlPullParserException, ClassNotFoundException, GOBOParseException, Exception {
        String assay = "3";
        LOGGER.info("Setting up experiment repository for assay " + assay);
        FileExperimentRepository experimentRepository = new FileExperimentRepository();
        experimentRepository.addPrideXMLFile(assay,inputFile);
        experimentRepository.loadExperimentIdentifications(assay);
        //the cache should only have one for now?
        String entry = ParserCache.getInstance().getLoadedFiles().keySet().iterator().next();
        LOGGER.info(entry + " was found in the parser cache");
        //write an MGF with all peakfile information?
        LOGGER.info("Getting related spectrum files from the cache");
        FileSpectrumRepository spectrumRepository = new FileSpectrumRepository(entry);
        File mgf = spectrumRepository.writeToMGF(outputFolder);
        //zip the MGF file
        File zip = new File(mgf.getAbsolutePath() + ".zip");
        ZipUtils.zip(mgf, zip, new WaitingHandlerCLIImpl(), mgf.length());
        mgf.delete();
        //do the extraction
        LOGGER.info("Attempting to infer searchparameters");
        PrideAsapExtractor extractor = new PrideAsapExtractor(entry, outputFolder);
        SearchParameters inferSearchParameters = extractor.inferSearchParameters();
        SearchParameters.saveIdentificationParameters(inferSearchParameters, new File(outputFolder, assay + ".asap.par"));
    }
}
