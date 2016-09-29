package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.pride_asa_pipeline.core.data.extractor.WebServiceParameterExtractor;
import com.compomics.pride_asa_pipeline.model.MGFExtractionException;
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
 * @author Kenneth Verheggen This class is used to automatically download and
 * process an assay through the PRIDE WS
 */
public class WebServiceAssayProcessor {

    public static void main(String[] args) throws IOException, ParseException, MGFExtractionException, MzXMLParsingException, JMzReaderException, XmlPullParserException, ClassNotFoundException, GOBOParseException, InterruptedException, Exception {
  //      String inputAssay = "51178";
        String inputAssay = "11954";
      // String inputAssay="3";
   
        File outputFolder = new File("C:\\Users\\Kenneth\\Desktop\\Complete\\" + inputAssay);
        if (outputFolder.exists()) {
            outputFolder.delete();
        }
        outputFolder.mkdirs();
        new File(outputFolder+"/temp").mkdir();
        WebServiceParameterExtractor               
                webServiceParameterExtractor = new WebServiceParameterExtractor(outputFolder);
        SearchParameters analyze = webServiceParameterExtractor.analyze(inputAssay);
        System.out.println(analyze);
     }
}
