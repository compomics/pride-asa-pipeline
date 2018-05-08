/* 
 * Copyright 2018 compomics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.compomics.pride_asa_pipeline.core.logic.enzyme;

import com.compomics.pride_asa_pipeline.core.logic.inference.enzyme.EnzymePredictor;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.io.compression.ZipUtils;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Kenneth Verheggen
 */
public class EnzymePredictorTest {

    private Enzyme testingEnzyme;
    private Enzyme testingEnzyme2;
    private Enzyme testingEnzyme3;
    private Enzyme testingEnzyme4;
    private SequenceFactory sequenceFactory;
    private EnzymeFactory enzymeFactory;
    private Enzyme testingEnzyme5;

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

    private File getFileFromResources(String fileName) throws IOException, URISyntaxException {
        File testResource = new File(EnzymePredictorTest.class.getClassLoader().getResource(fileName).toURI());
        if (testResource.getName().endsWith(".zip")) {
            ZipUtils.unzip(testResource, testResource.getParentFile(), null);
            testResource = new File(testResource.getAbsolutePath().replace(".zip", ""));
            testResource.deleteOnExit();
        }
        return testResource;
    }

    @Before
    public void setUp() throws Exception {
        File fastaFile = getFileFromResources("uniprot-human_reviewed_december_13_concatenated_target_decoy.fasta.zip");
        File enzymeFile = new File(EnzymePredictorTest.class.getClassLoader().getResource("searchGUI_enzymes.xml").toURI());
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

    @Test
    public void testTrypsin() throws Exception {
        System.out.println("Test Trypsin");
        List<String> peptides = mockUpPeptideDigestion(testingEnzyme, 300);
        EnzymePredictor predictor = new EnzymePredictor(peptides);
        Enzyme bestGuess = predictor.getMostLikelyEnzyme();
        System.out.println("Outcome = " + bestGuess.getName());
        Assert.assertTrue(bestGuess.getName().toUpperCase().contains("TRYP"));
    }

    @Test
    public void testArgC() throws Exception {
        System.out.println("Test ARG-C");
        List<String> peptides = mockUpPeptideDigestion(testingEnzyme2, 300);
        EnzymePredictor predictor = new EnzymePredictor(peptides);
        Enzyme bestGuess = predictor.getMostLikelyEnzyme();
        System.out.println("Outcome = " + bestGuess.getName());
        Assert.assertTrue(bestGuess.getName().toUpperCase().contains("ARG-C"));
    }

    @Test
    public void testLysC() throws Exception {
        System.out.println("Test LYS-C");
        List<String> peptides = mockUpPeptideDigestion(testingEnzyme3, 300);
        EnzymePredictor predictor = new EnzymePredictor(peptides);
        Enzyme bestGuess = predictor.getMostLikelyEnzyme();
        System.out.println("Outcome = " + bestGuess.getName());
        Assert.assertTrue(bestGuess.getName().toUpperCase().contains("LYS-C"));

    }

    @Test
    public void testPepsin() throws Exception {
        System.out.println("Test PEPSIN");
        List<String> peptides = mockUpPeptideDigestion(testingEnzyme4, 300);
        EnzymePredictor predictor = new EnzymePredictor(peptides);
        Enzyme bestGuess = predictor.getMostLikelyEnzyme();
        System.out.println("Outcome = " + bestGuess.getName());
        Assert.assertTrue(bestGuess.getName().toUpperCase().contains("PEPSIN"));
    }

    @Test
    public void testChymoTrypsin() throws Exception {
        System.out.println("Test ChymoTrypsin");
        List<String> peptides = mockUpPeptideDigestion(testingEnzyme5, 300);
        EnzymePredictor predictor = new EnzymePredictor(peptides);
        Enzyme bestGuess = predictor.getMostLikelyEnzyme();
        System.out.println("Outcome = " + bestGuess.getName());
        Assert.assertTrue(bestGuess.getName().toUpperCase().contains("CHYMOTRYP"));
    }



}
