/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.parameters;

import com.compomics.pride_asa_pipeline.core.logic.parameters.PrideAsapExtractor;
import com.compomics.util.experiment.identification.SearchParameters;
import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
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
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private File getFileFromResources(String fileName) throws IOException {
        return new ClassPathResource(fileName).getFile();
    }

    /**
     * Test of getSearchParametersFileForProject method, of class
     * PrideAsapSearchParamExtractor.
     */
    public void testGetSearchParametersFileForProject3() throws Exception {
        System.out.println("getSearchParametersFileForProject");
        File testingFile = getFileFromResources("PRIDE_Exp_Complete_Ac_3.xml");
        File outputFile = new File(testingFile.getParentFile(), "PRIDE_Exp_Complete_Ac_3.parameters");
        outputFile.deleteOnExit();
        PrideAsapExtractor instance = new PrideAsapExtractor(testingFile, testingFile);
        instance.getSearchParametersFileForProject();
        instance.save(outputFile, true);
        SearchParameters identificationParameters = SearchParameters.getIdentificationParameters(outputFile);
        System.out.println(identificationParameters);
        assertEquals(0.247, identificationParameters.getPrecursorAccuracy());
        assertEquals(0.112, identificationParameters.getFragmentIonAccuracy());
        assertEquals(identificationParameters.getPrecursorAccuracyType(), SearchParameters.MassAccuracyType.DA);
        assertEquals(identificationParameters.getEnzyme().getName().toLowerCase(), "arg-c");
        assertEquals(identificationParameters.getMinChargeSearched().value, 1);
        assertEquals(identificationParameters.getMaxChargeSearched().value, 5);

        System.out.println(identificationParameters.getModificationProfile().getFixedModifications());
        System.out.println(identificationParameters.getModificationProfile().getAllNotFixedModifications());

        //assertTrue(identificationParameters.getModificationProfile().getFixedModifications().contains("itraq114 on k"));
        //assertTrue(identificationParameters.getModificationProfile().getFixedModifications().contains("itraq114 on nterm"));
        //assertTrue(identificationParameters.getModificationProfile().getVariableModifications().contains("itraq114 on y"));
        assertTrue(identificationParameters.getModificationProfile().getVariableModifications().contains("oxidation of m"));
        assertTrue(identificationParameters.getModificationProfile().getFixedModifications().contains("carbamidomethyl c"));

    }

    public void testGetSearchParametersFileForProject11954() throws Exception {
        System.out.println("getSearchParametersFileForProject");
        File testingFile = getFileFromResources("PRIDE_Exp_Complete_Ac_11954.xml");
        File outputFile = new File(testingFile.getParentFile(), "PRIDE_Exp_Complete_Ac_11954.parameters");
        PrideAsapExtractor instance = new PrideAsapExtractor(testingFile, testingFile);
        instance.getSearchParametersFileForProject();
        instance.save(outputFile, true);
        SearchParameters identificationParameters = SearchParameters.getIdentificationParameters(outputFile);
        assertEquals(0.005, identificationParameters.getPrecursorAccuracy());
        assertEquals(0.552, identificationParameters.getFragmentIonAccuracy());
        assertEquals(identificationParameters.getPrecursorAccuracyType(), SearchParameters.MassAccuracyType.DA);
        assertEquals(identificationParameters.getEnzyme().getName().toLowerCase(), "trypsin");
        assertEquals(1, identificationParameters.getMinChargeSearched().value);
        assertEquals(5, identificationParameters.getMaxChargeSearched().value);

        System.out.println(identificationParameters.getModificationProfile().getFixedModifications());
        System.out.println(identificationParameters.getModificationProfile().getAllNotFixedModifications());

        assertTrue(identificationParameters.getModificationProfile().getFixedModifications().contains("carbamidomethyl c"));
        assertTrue(identificationParameters.getModificationProfile().getVariableModifications().contains("oxidation of m"));
        assertTrue(identificationParameters.getModificationProfile().getVariableModifications().contains("phosphorylation with neutral loss on s"));

    }

    public void testGetSearchParametersFileForITRAQ() throws Exception {
        System.out.println("getSearchParametersFileForProject");
        File testingFile = getFileFromResources("PRIDE_Exp_Complete_Ac_3010.xml");
        File outputFile = new File(testingFile.getParentFile(), "PRIDE_Exp_Complete_Ac_3010.parameters");
        PrideAsapExtractor instance = new PrideAsapExtractor(testingFile, testingFile);
        instance.getSearchParametersFileForProject();
        instance.save(outputFile, true);

        SearchParameters identificationParameters = SearchParameters.getIdentificationParameters(outputFile);
        System.out.println(identificationParameters);

        assertEquals(0.005, identificationParameters.getPrecursorAccuracy());
        assertEquals(0.569, identificationParameters.getFragmentIonAccuracy());
        assertEquals(identificationParameters.getPrecursorAccuracyType(), SearchParameters.MassAccuracyType.DA);
        assertEquals("trypsin", identificationParameters.getEnzyme().getName().toLowerCase());
        assertEquals(identificationParameters.getMinChargeSearched().value, 1);
        assertEquals(identificationParameters.getMaxChargeSearched().value, 5);

        System.out.println(identificationParameters.getModificationProfile().getFixedModifications());
        System.out.println(identificationParameters.getModificationProfile().getAllNotFixedModifications());

        assertTrue(identificationParameters.getModificationProfile().getFixedModifications().contains("itraq114 on k"));
        assertTrue(identificationParameters.getModificationProfile().getFixedModifications().contains("itraq114 on nterm"));
        assertTrue(identificationParameters.getModificationProfile().getVariableModifications().contains("itraq114 on y"));
        assertTrue(identificationParameters.getModificationProfile().getVariableModifications().contains("oxidation of m"));
    }

    public void testGetSearchParametersFileForPeptideShaker() throws Exception {
        System.out.println("getSearchParametersFileForProject");
        File testingFile = getFileFromResources("PeptideShaker_Example.xml");
        File outputFile = new File(testingFile.getParentFile(), "PeptideShaker_Example.parameters");
        PrideAsapExtractor instance = new PrideAsapExtractor(testingFile, testingFile);
        instance.getSearchParametersFileForProject();
        instance.save(outputFile, true);
        SearchParameters identificationParameters = SearchParameters.getIdentificationParameters(outputFile);
        System.out.println(identificationParameters);
        assertEquals(0.017, identificationParameters.getPrecursorAccuracy());
        assertEquals(0.991, identificationParameters.getFragmentIonAccuracy());
        assertEquals(identificationParameters.getPrecursorAccuracyType(), SearchParameters.MassAccuracyType.DA);
        assertEquals("trypsin", identificationParameters.getEnzyme().getName().toLowerCase());
        assertEquals(identificationParameters.getMinChargeSearched().value, 1);
        assertEquals(identificationParameters.getMaxChargeSearched().value, 5);

        System.out.println(identificationParameters.getModificationProfile().getFixedModifications());
        System.out.println(identificationParameters.getModificationProfile().getAllNotFixedModifications());

        assertTrue(identificationParameters.getModificationProfile().getFixedModifications().contains("carbamidomethyl c"));
        assertTrue(identificationParameters.getModificationProfile().getVariableModifications().contains("oxidation of m"));
    }
}
