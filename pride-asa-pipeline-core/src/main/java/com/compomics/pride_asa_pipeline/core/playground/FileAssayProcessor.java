package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.pride_asa_pipeline.core.data.extractor.FileParameterExtractor;
import com.compomics.pride_asa_pipeline.core.model.MGFExtractionException;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import java.io.File;
import java.io.IOException;
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
        File outputFolder = new File("C:\\Users\\compomics\\Documents\\Example_Files\\download");
        if (outputFolder.exists()) {
            outputFolder.delete();
        }
        outputFolder.mkdirs();
        File inputFile = new File("C:\\Users\\compomics\\Desktop\\3\\temp\\PRIDE_Exp_Complete_Ac_3.xml");
        SearchParameters parameters = new FileParameterExtractor(outputFolder).analyzePrideXML(inputFile, "3");
        System.out.println(parameters);
    }
}
