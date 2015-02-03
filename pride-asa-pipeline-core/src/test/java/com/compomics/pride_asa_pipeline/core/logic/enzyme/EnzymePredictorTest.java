/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.enzyme;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.SequenceFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.springframework.core.io.ClassPathResource;
import uk.ac.ebi.pride.jaxb.model.PeptideItem;
import uk.ac.ebi.pride.jaxb.xml.PrideXmlReader;

/**
 *
 * @author Kenneth Verheggen
 */
public class EnzymePredictorTest extends TestCase {

    private Enzyme testingEnzyme;
    private Enzyme testingEnzyme2;
    private Enzyme testingEnzyme3;
    private Enzyme testingEnzyme4;
    private SequenceFactory sequenceFactory;
    private EnzymeFactory enzymeFactory;
    private Enzyme testingEnzyme5;

    public EnzymePredictorTest(String testName) {
        super(testName);
    }

    private List<String> mockUpPeptideDigestion(Enzyme enzyme, int sampleSize) throws Exception {
        List<String> peptideList = new ArrayList<>();
        System.out.println("Mocking cleaving proteins with " + enzyme.getName());
        Protein aProtein;
        int counter = 0;
        for (String aProteinKey : sequenceFactory.getAccessions()) {
            aProtein = sequenceFactory.getProtein(aProteinKey);
            if (!aProtein.isDecoy()) {
                peptideList.addAll(enzyme.digest(aProtein.getSequence(), 2, 6, 15));
                counter++;
            }
            if (counter == sampleSize) {
                break;
            }
        }
        return peptideList;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File fastaFile = new ClassPathResource("uniprot-human_reviewed_december_13_concatenated_target_decoy.fasta").getFile();
        File enzymeFile = new ClassPathResource("searchGUI_enzymes.xml").getFile();
        sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(fastaFile, null);
        enzymeFactory = EnzymeFactory.getInstance();
        enzymeFactory.importEnzymes(enzymeFile);
        testingEnzyme = enzymeFactory.getEnzyme("Trypsin");
        testingEnzyme2 = enzymeFactory.getEnzyme("Arg-C");
        testingEnzyme3 = enzymeFactory.getEnzyme("Lys-C");
        testingEnzyme4 = enzymeFactory.getEnzyme("Pepsin A");
        testingEnzyme5 = enzymeFactory.getEnzyme("Chymotrypsin (FYWL)");
    }

      public void testTrypsin() throws Exception {
     System.out.println("Test Trypsin");
     List<String> peptides = mockUpPeptideDigestion(testingEnzyme, 300);
     EnzymePredictor predictor = new EnzymePredictor(peptides);
     Enzyme bestGuess = predictor.estimateEnzyme(peptides);
     System.out.println("Outcome = " + bestGuess.getName());
     assertTrue(bestGuess.getName().toUpperCase().contains("TRYP"));
     }

     public void testArgC() throws Exception {
     System.out.println("Test ARG-C");
     List<String> peptides = mockUpPeptideDigestion(testingEnzyme2, 300);
     EnzymePredictor predictor = new EnzymePredictor(peptides);
     Enzyme bestGuess = predictor.estimateEnzyme(peptides);
     System.out.println("Outcome = " + bestGuess.getName());
     assertTrue(bestGuess.getName().toUpperCase().contains("ARG-C"));
     }

     public void testLysC() throws Exception {
     System.out.println("Test LYS-C");
     List<String> peptides = mockUpPeptideDigestion(testingEnzyme3, 300);
     EnzymePredictor predictor = new EnzymePredictor(peptides);
     Enzyme bestGuess = predictor.estimateEnzyme(peptides);
     System.out.println("Outcome = " + bestGuess.getName());
     assertTrue(bestGuess.getName().toUpperCase().contains("LYS-C"));

     }

     public void testPepsin() throws Exception {
     System.out.println("Test PEPSIN");
     List<String> peptides = mockUpPeptideDigestion(testingEnzyme4, 300);
     EnzymePredictor predictor = new EnzymePredictor(peptides);
     Enzyme bestGuess = predictor.estimateEnzyme(peptides);
     System.out.println("Outcome = " + bestGuess.getName());
     assertTrue(bestGuess.getName().toUpperCase().contains("PEPSIN"));
     }

     public void testChymoTrypsin() throws Exception {
     System.out.println("Test ChymoTrypsin");
     List<String> peptides = mockUpPeptideDigestion(testingEnzyme5, 300);
     EnzymePredictor predictor = new EnzymePredictor(peptides);
     Enzyme bestGuess = predictor.estimateEnzyme(peptides);
     System.out.println("Outcome = " + bestGuess.getName());
     assertTrue(bestGuess.getName().toUpperCase().contains("CHYMOTRYP"));
     }

     public void testProject3() throws Exception {
     System.out.println("Test PROJECT 3");
     File testingFile = new ClassPathResource("PRIDE_Exp_Complete_Ac_3.xml").getFile();
     PrideXmlReader reader = new PrideXmlReader(testingFile);
     List<String> peptides = new ArrayList<>();
     for (String anID : reader.getIdentIds()) {
     for (PeptideItem anItem : reader.getPeptides(anID)) {
     peptides.add(anItem.getSequence());
     }
     }

     EnzymePredictor predictor = new EnzymePredictor();
     Enzyme bestGuess = predictor.estimateEnzyme(peptides);
     System.out.println("Outcome = " + bestGuess.getName());
     assertTrue(bestGuess.getName().toUpperCase().contains("ARG-C"));
     }

     public void testProject11954() throws Exception {
     System.out.println("Test PROJECT 11954");
     File testingFile = new ClassPathResource("PRIDE_Exp_Complete_Ac_11954.xml").getFile();
     PrideXmlReader reader = new PrideXmlReader(testingFile);
     List<String> peptides = new ArrayList<>();
     for (String anID : reader.getIdentIds()) {
     for (PeptideItem anItem : reader.getPeptides(anID)) {
     peptides.add(anItem.getSequence());
     }
     }

     EnzymePredictor predictor = new EnzymePredictor();
     Enzyme bestGuess = predictor.estimateEnzyme(peptides);
     System.out.println("Outcome = " + bestGuess.getName());
     assertTrue(bestGuess.getName().toUpperCase().contains("TRYP"));
     }

     public void testProject1644() throws Exception {
     System.out.println("Test PROJECT 1644");
     File testingFile = new ClassPathResource("PRIDE_Exp_Complete_Ac_1644.xml").getFile();
     PrideXmlReader reader = new PrideXmlReader(testingFile);
     List<String> peptides = new ArrayList<>();
     for (String anID : reader.getIdentIds()) {
     for (PeptideItem anItem : reader.getPeptides(anID)) {
     peptides.add(anItem.getSequence());
     }
     }
     EnzymePredictor predictor = new EnzymePredictor();
     Enzyme bestGuess = predictor.estimateEnzyme(peptides);
     System.out.println("Outcome = " + bestGuess.getName());
     assertTrue(bestGuess.getName().toUpperCase().contains("TRYP"));
     }

     public void testMissedCleavagePredictorExperiment11954() throws Exception {
     System.out.println("Test Missed Cleavage Project 1644");
     File testingFile = new ClassPathResource("PRIDE_Exp_Complete_Ac_11954.xml").getFile();
     PrideXmlReader reader = new PrideXmlReader(testingFile);
     List<String> peptides = new ArrayList<>();
     for (String anID : reader.getIdentIds()) {
     for (PeptideItem anItem : reader.getPeptides(anID)) {
     System.out.println(anItem.getSequence());
     peptides.add(anItem.getSequence());
     }
     }
     EnzymePredictor predictor = new EnzymePredictor();
     int estimatedMissedCleavages = predictor.estimateMaxMissedCleavages(testingEnzyme);
     System.out.println("Outcome = " + estimatedMissedCleavages);
     assertEquals(2, estimatedMissedCleavages);
     }

     public void testMissedCleavagePredictorExperiment3() throws Exception {
     System.out.println("Test Missed Cleavage Project 3");
     File testingFile = new ClassPathResource("PRIDE_Exp_Complete_Ac_3.xml").getFile();
     PrideXmlReader reader = new PrideXmlReader(testingFile);
     List<String> peptides = new ArrayList<>();
     for (String anID : reader.getIdentIds()) {
     for (PeptideItem anItem : reader.getPeptides(anID)) {
     peptides.add(anItem.getSequence());
     }
     }
     EnzymePredictor predictor = new EnzymePredictor();
     int estimatedMissedCleavages = predictor.estimateMaxMissedCleavages(testingEnzyme);
     System.out.println("Outcome = " + estimatedMissedCleavages);
     assertEquals(2, estimatedMissedCleavages);
     }
     
    public void testMissedCleavagePredictorPeptideShakerExample() throws Exception {
        System.out.println("Test Missed Cleavage PeptideShaker Example");
        File testingFile = new ClassPathResource("PeptideShaker_Example.xml").getFile();
        PrideXmlReader reader = new PrideXmlReader(testingFile);
        List<String> peptides = new ArrayList<>();
        for (String anID : reader.getIdentIds()) {
            for (PeptideItem anItem : reader.getPeptides(anID)) {
                peptides.add(anItem.getSequence());
            }
        }
        EnzymePredictor predictor = new EnzymePredictor(peptides);
        int estimatedMissedCleavages = predictor.estimateMaxMissedCleavages(testingEnzyme);
        double estimatedMissedRatio = predictor.getMissedCleavageRatio(testingEnzyme);
        System.out.println("Outcome = " + estimatedMissedCleavages);
        assertEquals(4, estimatedMissedCleavages);
        assertEquals(0.423, estimatedMissedRatio);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
