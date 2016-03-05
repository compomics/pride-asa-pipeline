package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.pride_asa_pipeline.core.data.extractor.FileParameterExtractor;
import com.compomics.pride_asa_pipeline.core.model.MGFExtractionException;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.cli.ParseException;
import org.geneontology.oboedit.dataadapter.GOBOParseException;
import org.xmlpull.v1.XmlPullParserException;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 *
 * @author Kenneth Verheggen
 */
public class FileAssayProcessor {

    public static void main(String[] args) throws Exception {
        File outputFolder = new File("C:\\Users\\compomics\\Desktop\\TEST_ASAP\\output");
        if (outputFolder.exists()) {
            outputFolder.delete();
        }
        outputFolder.mkdirs();
        File inputFile = new File("C:\\Users\\compomics\\Desktop\\Complete\\42095\\temp\\iTRAQ86_20120928b.mzid");
        File peakFile = new File("C:\\Users\\compomics\\Desktop\\Complete\\42095\\temp\\iTRAQ86_20120928b.mzid_Mudpit_iTRAQ86_CoutureP_1a14_completeproteome_MGFPeaklist_(F046704).MGF");
        ArrayList<File> peakFiles = new ArrayList<>();
        peakFiles.add(peakFile);
        SearchParameters parameters = new FileParameterExtractor(outputFolder).analyzeMzID(inputFile, peakFiles,"42095");
        System.out.println(parameters);
    }
}
