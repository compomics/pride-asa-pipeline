/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.spectrum;

import com.compomics.pride_asa_pipeline.core.repository.factory.FileParserType;
import java.io.File;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.tools.dta_parser.DtaFile;
import uk.ac.ebi.pride.tools.jmzreader.JMzReader;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;
import uk.ac.ebi.pride.tools.ms2_parser.Ms2File;
import uk.ac.ebi.pride.tools.mzdata_parser.MzDataFile;
import uk.ac.ebi.pride.tools.mzml_wrapper.MzMlWrapper;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.pkl_parser.PklFile;
import uk.ac.ebi.pride.tools.pride_wrapper.PRIDEXmlWrapper;


/**
 *
 * @author Kenneth Verheggen
 */
public class SpectrumParserFactory {

    private static final Logger LOGGER = Logger.getLogger(SpectrumParserFactory.class);
    private static File currentInputFile;
    private static FileParserType currentType;

    public static JMzReader getJMzReader(File inputFile) throws ClassNotFoundException, MzXMLParsingException, JMzReaderException {
        currentInputFile = inputFile;
        JMzReader parser = null;
            String extension = FilenameUtils.getExtension(inputFile.getAbsolutePath());
            switch (extension.toLowerCase()) {
                case "mzxml":
                    LOGGER.info("Detected mzXml file extension.");
                    currentType = FileParserType.MZXML;
                    parser = new MzXMLFile(inputFile);
                    break;
                case "mzml":
                    LOGGER.info("Detected mzml file extension.");
                    currentType = FileParserType.MZML;
                    parser = new MzMlWrapper(inputFile);
                    break;
                case "dta":
                    LOGGER.info("Detected dta file extension.");
                    currentType = FileParserType.DTA;
                    parser = new DtaFile(inputFile);
                    break;
                case "mgf":
                    LOGGER.info("Detected mgf file extension.");
                    currentType = FileParserType.MGF;
                    parser = new MgfFile(inputFile);
                    break;
                case "ms2":
                    LOGGER.info("Detected ms2 file extension.");
                    currentType = FileParserType.MS2;
                    parser = new Ms2File(inputFile);
                    break;
                case "mzData":
                    LOGGER.info("Detected mzData file extension.");
                    currentType = FileParserType.MZDATA;
                    parser = new MzDataFile(inputFile);
                    break;
                case "xml":
                    LOGGER.info("Detected xml file extension.");
                    currentType = FileParserType.PRIDEXML;
                    parser = new PRIDEXmlWrapper(inputFile);
                    break;
                case "pkl":
                    LOGGER.info("Detected pkl file extension.");
                    currentType = FileParserType.PKL;
                    parser = new PklFile(inputFile);
                    break;
                default:
                    throw new ClassNotFoundException("No suitable parser was found for the inputfile.");
            }
        return parser;
    }

    public static File getCurrentInputFile() {
        return currentInputFile;
    }

    public static void setCurrentInputFile(File currentInputFile) {
        SpectrumParserFactory.currentInputFile = currentInputFile;
    }

    public static FileParserType getCurrentType() {
        return currentType;
    }

    public static void setCurrentType(FileParserType currentType) {
        SpectrumParserFactory.currentType = currentType;
    }

}
