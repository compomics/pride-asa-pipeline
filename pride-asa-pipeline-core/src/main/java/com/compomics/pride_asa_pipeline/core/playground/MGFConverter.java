/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.pride_asa_pipeline.core.exceptions.MGFExtractionException;
import com.compomics.pride_asa_pipeline.core.logic.spectrum.DefaultMGFExtractor;
import java.io.File;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 *
 * @author Kenneth
 */
public class MGFConverter {

    public static void main(String[] args) {
        try {
            //    File input = new File("C:\\Users\\Kenneth\\Desktop\\mzML-test\\A0214_WHIM16-P6-3_bhplc_20120605_r_klc_X_A3_t1_fr01.mzML");
            File input = new File("C:\\\\Users\\\\Kenneth\\\\Desktop\\\\mzML-test\\\\A0214_WHIM16-P6-3_bhplc_20120605_r_klc_X_A3_t1_fr06.mzML");
            File output = new File("C:\\Users\\Kenneth\\Desktop\\mzML-test\\output.mgf");
            DefaultMGFExtractor extractor = new DefaultMGFExtractor(input);
            extractor.extractMGF(output,1000);
        } catch (ClassNotFoundException | MGFExtractionException | JMzReaderException | MzXMLParsingException ex) {
            ex.printStackTrace();
        } finally {
            System.out.println("Done !");
        }
    }
}
