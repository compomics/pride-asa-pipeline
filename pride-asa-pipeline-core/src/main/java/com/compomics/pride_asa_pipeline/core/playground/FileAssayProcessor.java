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
        File inputFile = new File("C:\\Users\\compomics\\Desktop\\test\\peptideshaker_example.mzid");
        File peakFile = new File("C:\\Users\\compomics\\Desktop\\test\\peptideshaker_example.mgf");
        ArrayList<File> peakFiles = new ArrayList<>();
        peakFiles.add(peakFile);
        SearchParameters parameters = new FileParameterExtractor(outputFolder).analyzeMzID(inputFile, peakFiles,"peptideshaker");
        System.out.println(parameters);
    }
}
