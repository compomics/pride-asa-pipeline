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

    public static void main(String[] args) throws IOException, ParseException, MGFExtractionException, MzXMLParsingException, JMzReaderException, XmlPullParserException, ClassNotFoundException, GOBOParseException, InterruptedException, Exception {
        File outputFolder = new File("C:\\Users\\compomics\\Desktop\\TEST_ASAP\\output");
        if (outputFolder.exists()) {
            outputFolder.delete();
        }
        outputFolder.mkdirs();
        File inputFile = new File("C:\\Users\\compomics\\Desktop\\TEST_ASAP\\TCGA-AA-A00N-01A-32_W_VU_20121027_A0218_5D_R_FR02.mzid");
        File peakFile = new File("C:\\Users\\compomics\\Desktop\\TEST_ASAP\\TCGA-AA-A00N-01A-32_W_VU_20121027_A0218_5D_R_FR02.mzml");
        ArrayList<File> peakFiles = new ArrayList<>();
        peakFiles.add(peakFile);
        SearchParameters parameters = new FileParameterExtractor(outputFolder).analyzeMzID(inputFile, peakFiles,"51098");
        System.out.println(parameters);
    }
}
