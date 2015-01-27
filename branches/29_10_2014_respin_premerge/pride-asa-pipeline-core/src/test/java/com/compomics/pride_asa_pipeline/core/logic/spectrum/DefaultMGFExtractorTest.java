/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.spectrum;

import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import junit.framework.TestCase;
import org.springframework.core.io.ClassPathResource;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 *
 * @author Kenneth
 */
public class DefaultMGFExtractorTest extends TestCase {

    private static DefaultMGFExtractor instance3 = null;
    private static DefaultMGFExtractor instance1696 = null;

    public DefaultMGFExtractorTest(String testName) throws IOException, ClassNotFoundException, MzXMLParsingException, JMzReaderException {
        super(testName);
        if (DefaultMGFExtractorTest.instance1696 == null) {
            File testingFile16969 = new ClassPathResource("PRIDE_Exp_Complete_Ac_1696.xml").getFile();
            DefaultMGFExtractorTest.instance1696 = new DefaultMGFExtractor(testingFile16969);
        }
        if (DefaultMGFExtractorTest.instance3 == null) {
            File testingFile3 = new ClassPathResource("PRIDE_Exp_Complete_Ac_3.xml").getFile();
            DefaultMGFExtractorTest.instance3 = new DefaultMGFExtractor(testingFile3);
        }
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
     * Test of getSpectraMetadata method, of class DefaultMGFExtractor2.
     *
     * public void testGetSpectraMetadata() { System.out.println("get
     * SpectraMetadata"); List<Map<String, Object>> spectraMetadata =
     * instance.getSpectraMetadata(); List<Map<String, Object>> expResult =
     * null; List<Map<String, Object>> result = instance.getSpectraMetadata();
     * assertEquals(expResult, result); // TODO review the generated test code
     * and remove the default call to fail. fail("The test case is a
     * prototype."); }
     *
     */
    /**
     * Test of getSpectrumIds method, of class DefaultMGFExtractor2.
     */
    public void testGetSpectrumIds() throws IOException {
        System.out.println("getSpectrumIds");
        int expResult = 1958;
        int result = DefaultMGFExtractorTest.instance3.getSpectrumIds().size();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSpectrumPeakMapBySpectrumId method, of class
     * DefaultMGFExtractor2.
     */
    public void testGetSpectrumPeakMapBySpectrumId() throws IOException {
        System.out.println("getSpectrumPeakMapBySpectrumId");
        String spectrumId = "25";
        HashMap<Double, Double> result = DefaultMGFExtractorTest.instance3.getSpectrumPeakMapBySpectrumId(spectrumId);
        assertEquals(result.size(), 210);
        assertTrue(result.get(962.4208) != null);
    }

    /**
     * Test of extractMGF method, of class DefaultMGFExtractor2.
     */
    public void testExtractMGF() throws Exception {
        System.out.println("extractMGF");

        File outputFile = new File("pride_project_3.mgf");
        outputFile.deleteOnExit();
        File result = DefaultMGFExtractorTest.instance3.extractMGF(outputFile);
        SpectrumFactory factory = SpectrumFactory.getInstance();
        factory.clearFactory();
        factory.addSpectra(outputFile, null);
        //TODO verify that charge is really not required, as suggested by Harald ...
        //  Integer maxCharge = factory.getMaxCharge(result.getName());
        Double maxIntensity = factory.getMaxIntensity(result.getName());
        Double maxMz = factory.getMaxMz(result.getName());
        assertEquals(58177.0547, maxIntensity);
        assertEquals(1198.1465, maxMz);
        assertEquals(1958, factory.getSpectrumTitles(result.getName()).size());
    }

    public void testExtractMGF1696() throws IOException {
        //TODO check if these can be saved and / or are frequent
        System.out.println("extractMGF 1696");
        File outputFile = new File("pride_project_1696.mgf");
        outputFile.deleteOnExit();
        try {
            File result = DefaultMGFExtractorTest.instance1696.extractMGF(outputFile);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            assertTrue(true);
        }
        assertFalse(false);
    }

}
