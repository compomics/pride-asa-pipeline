/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.spectrum;

import com.compomics.pride_asa_pipeline.core.data.extractor.MGFExtractor;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.io.compression.ZipUtils;
import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
import org.springframework.core.io.ClassPathResource;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 *
 * @author Kenneth
 */
public class DefaultMGFExtractorTest extends TestCase {

    private static MGFExtractor instance3 = null;

    private File getFileFromResources(String fileName) throws IOException {
        File testResource = new ClassPathResource(fileName).getFile();
        if (testResource.getName().endsWith(".zip")) {
            ZipUtils.unzip(testResource, testResource.getParentFile(), null);
            testResource = new File(testResource.getAbsolutePath().replace(".zip", ""));
            testResource.deleteOnExit();
        }
        return testResource;
    }

    public DefaultMGFExtractorTest(String testName) throws IOException, ClassNotFoundException, MzXMLParsingException, JMzReaderException {
        super(testName);
        if (DefaultMGFExtractorTest.instance3 == null) {
            File testingFile3 = getFileFromResources("PRIDE_Exp_Complete_Ac_3.xml.zip");
            DefaultMGFExtractorTest.instance3 = new MGFExtractor(testingFile3);
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

}
