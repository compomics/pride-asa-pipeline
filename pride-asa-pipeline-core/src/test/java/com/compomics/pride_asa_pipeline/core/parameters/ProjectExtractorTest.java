package com.compomics.pride_asa_pipeline.core.parameters;

import com.compomics.pride_asa_pipeline.core.data.extractor.FileParameterExtractor;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.io.compression.ZipUtils;
import com.compomics.util.io.json.JsonMarshaller;
import com.compomics.util.io.json.marshallers.IdentificationParametersMarshaller;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import junit.framework.TestCase;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author Kenneth
 */
public class ProjectExtractorTest extends TestCase {

    public ProjectExtractorTest(String testName) {
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
        if (testResource.getName().endsWith(".zip")) {
            ZipUtils.unzip(testResource, testResource.getParentFile(), null);
            testResource = new File(testResource.getAbsolutePath().replace(".zip", ""));
            testResource.deleteOnExit();
        }
        return testResource;
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
        FileParameterExtractor instance = new FileParameterExtractor(outputFolder, AnalyzerData.getAnalyzerDataByAnalyzerType("orbitrap"));

        // File inputFile = getFileFromResources("PRIDE_Exp_Complete_Ac_3.xml.zip");
        File inputFile = getFileFromResources("PeptideShaker_example.xml.zip");

        SearchParameters identificationParameters = instance.analyzePrideXML(inputFile, "test_" + inputFile.getName().replace(".xml", ""));

        assertTrue(identificationParameters.getPrecursorAccuracy() > 0 && identificationParameters.getPrecursorAccuracy() <= 0.007);
        assertEquals(0.027, identificationParameters.getFragmentIonAccuracy());

        assertEquals(identificationParameters.getPrecursorAccuracyType(), SearchParameters.MassAccuracyType.DA);

        //TODO check if this can be handled better
        assertTrue(identificationParameters.getDigestionPreferences().getEnzymes().get(0).getName().toLowerCase().contains("tryp"));
        assertEquals(2, identificationParameters.getMinChargeSearched().value);
        assertEquals(4, identificationParameters.getMaxChargeSearched().value);

        ArrayList<String> modifications = identificationParameters.getPtmSettings().getAllModifications();

        assertTrue(modifications.contains("Phosphorylation of S"));
        assertTrue(modifications.contains("Phosphorylation of T"));
        assertTrue(modifications.contains("Phosphorylation of Y"));
        assertTrue(modifications.contains("Oxidation of M"));
        assertTrue(modifications.contains("Acetylation of K"));
        assertTrue(modifications.contains("Acetylation of peptide N-term"));
        assertTrue(modifications.contains("Carbamidomethylation of C"));
        assertTrue(modifications.contains("Pyrolidone from Q"));
        System.out.println();
        System.out.println("# ------------------------------------------------------------------");
        System.out.println("FINAL VERDICT");
        System.out.println("# ------------------------------------------------------------------");
        JsonMarshaller marsh = new IdentificationParametersMarshaller();
        System.out.println(marsh.toJson(identificationParameters));

    }

}
