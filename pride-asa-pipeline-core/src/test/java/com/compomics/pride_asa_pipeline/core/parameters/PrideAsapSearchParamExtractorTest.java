/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.parameters;

import com.compomics.pride_asa_pipeline.core.logic.parameters.PrideAsapExtractor;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import java.io.File;
import junit.framework.TestCase;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

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

    /**
     * Test of getSearchParametersFileForProject method, of class
     * PrideAsapSearchParamExtractor.
     */
    public void testGetSearchParameters() throws Exception {
        System.out.println("testGetSearchParameters");
        File temp = File.createTempFile("temp", ".tmp");
        temp.deleteOnExit();
        File outputFolder = File.createTempFile("temp", ".tmp").getParentFile();
        File outputMGFFolder = new File(outputFolder, "mgf");
        PrideAsapExtractor instance = new PrideAsapExtractor("3", outputFolder);
        SearchParameters identificationParameters = instance.inferSearchParameters();
        instance.save(outputMGFFolder, true, true);

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

}
