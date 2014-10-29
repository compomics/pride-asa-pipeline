/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.repository.impl;

import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.Peptide;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import static junit.framework.TestCase.assertEquals;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 *
 * @author Kenneth
 */
public class PrideXmlParserImplTest extends TestCase {

    private PrideXmlParser instance;

    public PrideXmlParserImplTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //turn of hideous debug statements from prideUnmarshallerFactory
        Logger.getRootLogger().setLevel(Level.WARN);
        File testingFile = new ClassPathResource("PRIDE_Exp_Complete_Ac_3.xml").getFile();
        instance = new PrideXmlParser();
        instance.init(testingFile);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getExperimentIdentifications method, of class PrideXmlParserImpl.
     */
    public void testGetExperimentIdentifications() throws IOException {
        System.out.println("getExperimentIdentifications");
        List<Identification> result = instance.getExperimentIdentifications();
        assertEquals(1958, result.size());
        Peptide testingPeptide1888 = result.get(1888).getPeptide();
        assertEquals("PPKVTSELLR", testingPeptide1888.getSequenceString());
    }

    /**
     * Test of getNumberOfSpectra method, of class PrideXmlParserImpl.
     */
    public void testGetNumberOfSpectra() throws IOException {
        System.out.println("getNumberOfSpectra");
        long expResult = 1958;
        long result = instance.getNumberOfSpectra();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNumberOfPeptides method, of class PrideXmlParserImpl.
     */
    public void testGetNumberOfPeptides() throws IOException {
        System.out.println("getNumberOfPeptides");
        long result = instance.getNumberOfPeptides();
        assertEquals(1958, result);

    }

    /**
     * Test of getNumberOfPeptides method, of class PrideXmlParserImpl.
     */
    public void testGetNumberOfUniquePeptides() throws IOException {
        System.out.println("getNumberOfPeptides");
        long result = instance.getNumberUniquePeptides();
        assertEquals(530, result);
    }

    /**
     * Test of getModifications method, of class PrideXmlParserImpl.
     */
    public void testGetModifications() throws IOException, ClassNotFoundException, MzXMLParsingException, JMzReaderException {
        System.out.println("getModifications");
        File testingFile = new ClassPathResource("PRIDE_Experiment_11954.xml").getFile();
        PrideXmlParser instance = new PrideXmlParser();
        instance.init(testingFile);
        List<Modification> result = instance.getModifications();
        //convert to strings
        List<String> resultAsStrings = new ArrayList<>();
        for (Modification mod : result) {
            resultAsStrings.add(mod.getName().toLowerCase());
        }
        assertTrue(resultAsStrings.contains("o-phospho-l-serine"));
        assertTrue(resultAsStrings.contains("2-pyrrolidone-5-carboxylic acid (gln)"));
    }

    /**
     * Test of getProteinAccessions method, of class PrideXmlParserImpl.
     */
    public void testGetProteinAccessions() {
        System.out.println("getProteinAccessions");
        List<String> result = instance.getProteinAccessions();
        assertEquals(345, result.size());
    }

    /**
     * Test of getProtocolPTMs method, of class PrideXmlParserImpl.
     *
     * public void testGet11954ProtocolPTMs() throws IOException {
     * System.out.println("getProtocolPTMs : 11954"); File testingFile = new
     * ClassPathResource("PRIDE_Exp_Complete_Ac_11954.xml").getFile();
     * PrideXmlParserImpl instance = new PrideXmlParserImpl();
     * instance.init(testingFile); HashMap<PTM, Boolean> result =
     * instance.getProtocolPTMs(); List<String> resultAsStrings = new
     * ArrayList<>(); for (PTM mod : result.keySet()) {
     * resultAsStrings.add(mod.getName().toLowerCase()); }
     * assertTrue(resultAsStrings.contains("deamidation of n and q")); }
     */
    /**
     * Test of getProtocolPTMs method, of class PrideXmlParserImpl.
     *
     * public void testGet2694ProtocolPTMs() throws IOException {
     * System.out.println("getProtocolPTMs : 2694"); File testingFile = new
     * ClassPathResource("PRIDE_Exp_Complete_Ac_2694.xml").getFile();
     * PrideXmlParserImpl instance = new PrideXmlParserImpl();
     * instance.init(testingFile); HashMap<PTM, Boolean> result =
     * instance.getProtocolPTMs(); HashMap<String, Boolean> resultStrings = new
     * HashMap<>(); List<String> resultAsStrings = new ArrayList<>(); for (PTM
     * mod : result.keySet()) {
     * resultAsStrings.add(mod.getName().toLowerCase());
     * resultStrings.put(mod.getName().toLowerCase(), result.get(mod)); }
     * assertTrue(resultAsStrings.contains("carboxymethyl"));
     * assertTrue(resultAsStrings.contains("oxidation of m"));
     * assertTrue(resultAsStrings.contains("monohydroxylated residue"));
     * assertTrue(resultStrings.get("carboxymethyl"));
     * assertFalse(resultStrings.get("oxidation of m"));
     * assertFalse(resultStrings.get("deamidation of n and q")); }
     */
    /**
     * Test of getAnalyzerData method for the peptideshaker reference project.
     */
    public void testGetAnalyzerData() throws IOException, ClassNotFoundException, MzXMLParsingException, JMzReaderException {
        System.out.println("getAnalyzerData");
        File testingFile = new ClassPathResource("PeptideShaker_Example.xml").getFile();
        PrideXmlParser instance = new PrideXmlParser();
        instance.init(testingFile);
        List<AnalyzerData> analyzerDataList = instance.getAnalyzerData();
        AnalyzerData analyzerData = analyzerDataList.get(0);
        System.out.println("Found " + analyzerData.getAnalyzerFamily());
        assertEquals(AnalyzerData.ANALYZER_FAMILY.ORBITRAP, analyzerData.getAnalyzerFamily());
        assertEquals(0.02, analyzerData.getFragmentMassError());
        assertEquals(0.1, analyzerData.getPrecursorMassError());
    }

    /*
     /**
     * Test of getGellFreePTMs method, of class PrideXmlParserImpl.
     public void testGetGellFreePTMs() throws IOException {
     System.out.println("getGellFreePTMs");
     File testingFile = new ClassPathResource("PRIDE_Exp_Complete_Ac_3643.xml").getFile();
     PrideXmlParserImpl instance = new PrideXmlParserImpl();
     instance.init(testingFile);
     HashMap<PTM, Boolean> result = instance.getGellFreePTMs();
     System.out.println("lol");
     }
     */
}
