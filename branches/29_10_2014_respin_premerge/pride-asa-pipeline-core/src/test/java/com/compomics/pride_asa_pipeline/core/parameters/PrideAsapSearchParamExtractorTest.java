/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.parameters;

import com.compomics.pride_asa_pipeline.core.logic.parameters.PrideAsapSearchParamExtractor;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.SearchParameters;
import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
import org.springframework.core.io.ClassPathResource;
import org.xmlpull.v1.XmlPullParserException;

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
        PrideAsapSearchParamExtractor instance = new PrideAsapSearchParamExtractor(testingFile, testingFile);
        instance.getSearchParametersFileForProject();
        instance.save(outputFile, true);
        SearchParameters identificationParameters = SearchParameters.getIdentificationParameters(outputFile);
        System.out.println(identificationParameters);
        assertEquals(1.12071, identificationParameters.getPrecursorAccuracy());
        assertEquals(0.28589, identificationParameters.getFragmentIonAccuracy());
        assertEquals(identificationParameters.getPrecursorAccuracyType(), SearchParameters.PrecursorAccuracyType.DA);
        assertEquals(identificationParameters.getEnzyme().getName().toLowerCase(), "arg-c");
        assertEquals(identificationParameters.getMinChargeSearched().value, 1);
        assertEquals(identificationParameters.getMaxChargeSearched().value, 5);

    }
    
     public void testGetSearchParametersFileForProject11954() throws Exception {
     System.out.println("getSearchParametersFileForProject");
     File testingFile = getFileFromResources("PRIDE_Exp_Complete_Ac_11954.xml");
     File outputFile = new File(testingFile.getParentFile(), "PRIDE_Exp_Complete_Ac_11954.parameters");
     PrideAsapSearchParamExtractor instance = new PrideAsapSearchParamExtractor(testingFile,testingFile);
     instance.getSearchParametersFileForProject();
     instance.save(outputFile, true);
     SearchParameters identificationParameters = SearchParameters.getIdentificationParameters(outputFile);
     System.out.println(identificationParameters);
     assertEquals(1.00329, identificationParameters.getPrecursorAccuracy());
     assertEquals(0.59825, identificationParameters.getFragmentIonAccuracy());
     assertEquals(identificationParameters.getPrecursorAccuracyType(), SearchParameters.PrecursorAccuracyType.DA);
     assertEquals(identificationParameters.getEnzyme().getName().toLowerCase(), "trypsin");
     assertEquals(1, identificationParameters.getMinChargeSearched().value);
     assertEquals(5, identificationParameters.getMaxChargeSearched().value);
     assertTrue(identificationParameters.getModificationProfile().getVariableModifications().contains("carbamidomethyl c"));
     //        assertTrue(identificationParameters.getModificationProfile().getVariableModifications().contains("pyro-glu from n-term q"));
    //     assertTrue(identificationParameters.getModificationProfile().getVariableModifications().contains("oxidation of m"));
     }

     public void testPtmFactory() throws XmlPullParserException, IOException {
     PTMFactory factory = PTMFactory.getInstance();
     factory.clearFactory();
     factory.importModifications(getFileFromResources("searchGUI_mods.xml"), false);
     factory.importModifications(getFileFromResources("searchGUI_usermods.xml"), true, true);
     System.out.println("DEFAULT MODS");
     for (String aMod : factory.getDefaultModifications()) {
     System.out.println(aMod);
     }
     System.out.println("USER MODS");
     for (String aMod : factory.getDefaultModifications()) {
     System.out.println(aMod);
     }
     }

     public void testGetSearchParametersFileForPeptideShaker() throws Exception {
     System.out.println("getSearchParametersFileForProject");
     File testingFile = getFileFromResources("PeptideShaker_Example.xml");
     File outputFile = new File(testingFile.getParentFile(), "PeptideShaker_Example.parameters");
     PrideAsapSearchParamExtractor instance = new PrideAsapSearchParamExtractor(testingFile,testingFile);
     instance.getSearchParametersFileForProject();
     instance.save(outputFile, true);
     SearchParameters identificationParameters = SearchParameters.getIdentificationParameters(outputFile);
     System.out.println(identificationParameters);
     assertEquals(2.02142, identificationParameters.getPrecursorAccuracy());
     assertEquals(0.99999, identificationParameters.getFragmentIonAccuracy());
     assertEquals(identificationParameters.getPrecursorAccuracyType(), SearchParameters.PrecursorAccuracyType.DA);
     assertEquals("trypsin", identificationParameters.getEnzyme().getName().toLowerCase());
     assertEquals(identificationParameters.getMinChargeSearched().value, 1);
     assertEquals(identificationParameters.getMaxChargeSearched().value, 5);
     System.out.println(identificationParameters.getModificationProfile().getFixedModifications());
     System.out.println(identificationParameters.getModificationProfile().getAllNotFixedModifications());
     assertTrue(identificationParameters.getModificationProfile().getVariableModifications().contains("carbamidomethyl c"));
     assertTrue(identificationParameters.getModificationProfile().getVariableModifications().contains("acetylation of k"));
     }
}
