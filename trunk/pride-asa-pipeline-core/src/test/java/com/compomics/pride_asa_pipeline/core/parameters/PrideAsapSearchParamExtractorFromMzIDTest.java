/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.parameters;

import java.io.File;
import junit.framework.TestCase;

/**
 *
 * @author Kenneth
 */
public class PrideAsapSearchParamExtractorFromMzIDTest extends TestCase {

    private static final File inputPeaksFile = new File("C:\\Users\\Kenneth\\Desktop\\MzID_Test\\F064601.mgf");
    private static final File inputIdentificationsFile = new File("C:\\Users\\Kenneth\\Desktop\\MzID_Test\\F064601.mzid");
    // private static final File inputIdentificationsFile = new File("C:\\Users\\Kenneth\\Desktop\\MzID_Test\\F064601.mzid");

    public PrideAsapSearchParamExtractorFromMzIDTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getSearchParametersFileForProject method, of class
     * PrideAsapSearchParamExtractor.
     *
     * @throws java.lang.Exception
     */
    public void testGetSearchParametersFileForMzIdentMl() throws Exception {
        System.out.println("MZIdentML Testing can only be done locally for now");
        /*  System.out.println("getSearchParametersFileForProject");
         File outputFile = new File(inputIdentificationsFile.getParentFile(), "test.parameters");     
         PrideAsapSearchParamExtractor instance = new PrideAsapSearchParamExtractor(inputIdentificationsFile, inputPeaksFile);
         instance.getSearchParametersFileForProject();
         instance.save(outputFile, true);
         SearchParameters identificationParameters = SearchParameters.getIdentificationParameters(outputFile);
         System.out.println(identificationParameters);
         assertEquals(0.01343, identificationParameters.getPrecursorAccuracy());
         assertEquals(0.3, identificationParameters.getFragmentIonAccuracy());
         assertEquals(identificationParameters.getPrecursorAccuracyType(), SearchParameters.PrecursorAccuracyType.DA);
         assertEquals(identificationParameters.getEnzyme().getName().toLowerCase(), "trypsin");
         assertEquals(1, identificationParameters.getMinChargeSearched().value);
         assertEquals(5, identificationParameters.getMaxChargeSearched().value);
         assertTrue(identificationParameters.getModificationProfile().getVariableModifications().contains("carbamidomethyl c"));
         //        assertTrue(identificationParameters.getModificationProfile().getVariableModifications().contains("pyro-glu from n-term q"));
         //     assertTrue(identificationParameters.getModificationProfile().getVariableModifications().contains("oxidation of m"));
         */
    }
}
