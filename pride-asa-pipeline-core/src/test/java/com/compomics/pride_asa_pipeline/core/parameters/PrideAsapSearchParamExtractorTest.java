/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.parameters;

import com.compomics.pride_asa_pipeline.core.logic.parameters.PrideAsapExtractor;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.io.compression.ZipUtils;
import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author Kenneth
 */
public class PrideAsapSearchParamExtractorTest extends TestCase {

    public PrideAsapSearchParamExtractorTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Logger.getRootLogger().setLevel(Level.DEBUG);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

      private File getFileFromResources(String fileName) throws IOException {
        File testResource = new ClassPathResource(fileName).getFile();
        if(testResource.getName().endsWith(".zip")){
        ZipUtils.unzip(testResource,testResource.getParentFile(), null);
        testResource = new File(testResource.getAbsolutePath().replace(".zip",""));
        testResource.deleteOnExit();
        }
        return testResource;
    }

    /**
     * Test of getSearchParametersFileForProject method, of class
     * PrideAsapSearchParamExtractor.
     */
    public void testGetSearchParametersFileForProjectPeptideShaker() throws Exception {
        System.out.println("getSearchParametersFileForProject");
        File testingFile = getFileFromResources("peptideshaker_example.xml.zip");
        File outputFile = new File(testingFile.getParentFile(), "peptideshaker_example.par");
        outputFile.deleteOnExit();
        PrideAsapExtractor instance = new PrideAsapExtractor(testingFile, testingFile);
        instance.getSearchParametersFileForProject();
        instance.save(outputFile, true);

        SearchParameters identificationParameters = SearchParameters.getIdentificationParameters(outputFile);
        System.out.println(identificationParameters.getPrecursorAccuracy());
        System.out.println(identificationParameters.getFragmentIonAccuracy());

        assertEquals(0.005, identificationParameters.getPrecursorAccuracy());
        assertEquals(0.019, identificationParameters.getFragmentIonAccuracy());

        assertEquals(identificationParameters.getPrecursorAccuracyType(), SearchParameters.MassAccuracyType.DA);

        assertTrue(identificationParameters.getEnzyme().getName().toLowerCase().contains("tryp"));
        assertEquals(identificationParameters.getMinChargeSearched().value, 1);
        assertEquals(identificationParameters.getMaxChargeSearched().value, 5);

        System.out.println(identificationParameters.getPtmSettings().getFixedModifications());
        System.out.println(identificationParameters.getPtmSettings().getAllNotFixedModifications());

        assertTrue(identificationParameters.getPtmSettings().getVariableModifications().contains("Phosphorylation of S"));
        assertTrue(identificationParameters.getPtmSettings().getVariableModifications().contains("Oxidation of M"));
        assertTrue(identificationParameters.getPtmSettings().getFixedModifications().contains("Carbamidomethylation of C"));
    }

    /**
     * Test of getSearchParametersFileForProject method, of class
     * PrideAsapSearchParamExtractor.
     */
    public void testGetSearchParametersFileForProjectPeptideShakerMzID() throws Exception {
        System.out.println("getSearchParametersFileForProject");
        File testingFile = getFileFromResources("peptideshaker_example.mzid.zip");
        File peakFile = getFileFromResources("peptideshaker_example.mgf.zip");
        File outputFile = new File(testingFile.getParentFile(), "peptideshaker_example.par");
        outputFile.deleteOnExit();
        PrideAsapExtractor instance = new PrideAsapExtractor(testingFile, peakFile);
        instance.getSearchParametersFileForProject();
        instance.save(outputFile, true);
        SearchParameters identificationParameters = SearchParameters.getIdentificationParameters(outputFile);
        System.out.println(identificationParameters.getPrecursorAccuracy());
        System.out.println(identificationParameters.getFragmentIonAccuracy());

        assertEquals(0.006, identificationParameters.getPrecursorAccuracy());
        //THIS DIFFERENCE WITH THE PRIDEXML IS DUE TO PROTEIN INFERENCE PROBLEMS
        assertEquals(0.075, identificationParameters.getFragmentIonAccuracy());

        assertEquals(identificationParameters.getPrecursorAccuracyType(), SearchParameters.MassAccuracyType.DA);
        assertTrue(identificationParameters.getEnzyme().getName().toLowerCase().contains("tryp"));
        assertEquals(identificationParameters.getMinChargeSearched().value, 1);
        assertEquals(identificationParameters.getMaxChargeSearched().value, 5);

        System.out.println(identificationParameters.getPtmSettings().getFixedModifications());
        System.out.println(identificationParameters.getPtmSettings().getAllNotFixedModifications());

        assertTrue(identificationParameters.getPtmSettings().getVariableModifications().contains("Phosphorylation of S"));
        assertTrue(identificationParameters.getPtmSettings().getVariableModifications().contains("Oxidation of M"));
        assertTrue(identificationParameters.getPtmSettings().getFixedModifications().contains("Carbamidomethylation of C"));

    }

}
