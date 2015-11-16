package com.compomics.pride_asa_pipeline.core.parameters;

import com.compomics.pride_asa_pipeline.core.playground.FileProjectExtractor;
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
    
    public static void main(String[]args) throws Exception{
        new PrideAsapSearchParamExtractorTest("My Test").testGetSearchParameters();
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
        FileProjectExtractor instance = new FileProjectExtractor(outputFolder);
   
        File inputFile = getFileFromResources("PeptideShaker_Example.xml.zip");
        
        SearchParameters identificationParameters = instance.analyze(inputFile, "3");
  
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

