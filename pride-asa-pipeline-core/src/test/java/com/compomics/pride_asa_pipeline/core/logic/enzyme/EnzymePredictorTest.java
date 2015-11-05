/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.enzyme;

import com.compomics.pride_asa_pipeline.core.inference.enzyme.EnzymePredictor;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.io.compression.ZipUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.springframework.core.io.ClassPathResource;

/**
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

    private File getFileFromResources(String fileName) throws IOException {
        File testResource = new ClassPathResource(fileName).getFile();
        if (testResource.getName().endsWith(".zip")) {
            ZipUtils.unzip(testResource, testResource.getParentFile(), null);
            testResource = new File(testResource.getAbsolutePath().replace(".zip", ""));
            testResource.deleteOnExit();
        }
        return testResource;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File fastaFile = getFileFromResources("uniprot-human_reviewed_december_13_concatenated_target_decoy.fasta.zip");
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
        Enzyme bestGuess = predictor.estimateBestEnzyme();
        System.out.println("Outcome = " + bestGuess.getName());
        assertTrue(bestGuess.getName().toUpperCase().contains("TRYP"));
    }

    public void testArgC() throws Exception {
        System.out.println("Test ARG-C");
        List<String> peptides = mockUpPeptideDigestion(testingEnzyme2, 300);
        EnzymePredictor predictor = new EnzymePredictor(peptides);
        Enzyme bestGuess = predictor.estimateBestEnzyme();
        System.out.println("Outcome = " + bestGuess.getName());
        assertTrue(bestGuess.getName().toUpperCase().contains("ARG-C"));
    }

    public void testLysC() throws Exception {
        System.out.println("Test LYS-C");
        List<String> peptides = mockUpPeptideDigestion(testingEnzyme3, 300);
        EnzymePredictor predictor = new EnzymePredictor(peptides);
        Enzyme bestGuess = predictor.estimateBestEnzyme();
        System.out.println("Outcome = " + bestGuess.getName());
        assertTrue(bestGuess.getName().toUpperCase().contains("LYS-C"));

    }

    public void testPepsin() throws Exception {
        System.out.println("Test PEPSIN");
        List<String> peptides = mockUpPeptideDigestion(testingEnzyme4, 300);
        EnzymePredictor predictor = new EnzymePredictor(peptides);
        Enzyme bestGuess = predictor.estimateBestEnzyme();
        System.out.println("Outcome = " + bestGuess.getName());
        assertTrue(bestGuess.getName().toUpperCase().contains("PEPSIN"));
    }

    public void testChymoTrypsin() throws Exception {
        System.out.println("Test ChymoTrypsin");
        List<String> peptides = mockUpPeptideDigestion(testingEnzyme5, 300);
        EnzymePredictor predictor = new EnzymePredictor(peptides);
        Enzyme bestGuess = predictor.estimateBestEnzyme();
        System.out.println("Outcome = " + bestGuess.getName());
        assertTrue(bestGuess.getName().toUpperCase().contains("CHYMOTRYP"));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
