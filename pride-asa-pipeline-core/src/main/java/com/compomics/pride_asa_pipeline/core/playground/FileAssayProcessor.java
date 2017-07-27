package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.pride_asa_pipeline.core.data.extractor.FileParameterExtractor;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import java.io.File;
import java.util.ArrayList;

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
        File inputFile = new File("C:\\Users\\compomics\\Desktop\\Projecten\\PRIDE_TEST\\49514.mzid");
        File peakFile = new File("C:\\Users\\compomics\\Desktop\\Projecten\\PRIDE_TEST\\49514.mzML");
       
        File inputFile2 = new File("C:\\Users\\compomics\\Desktop\\Projecten\\PRIDE_TEST\\TEST.xml");
        
        ArrayList<File> peakFiles = new ArrayList<>();
        peakFiles.add(peakFile);
    //    SearchParameters parameters = new FileParameterExtractor(outputFolder).analyzeMzID(inputFile, peakFiles,"49514");
        SearchParameters parameters = new FileParameterExtractor(outputFolder).analyzePrideXML(inputFile2,"18266");
        
    System.out.println(parameters);
    }
}
