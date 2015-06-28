/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.enzyme;

import com.compomics.pride_asa_pipeline.core.logic.parameters.PrideAsapStats;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Kenneth Verheggen
 */
public class EnzymePredictor {

    private static EnzymeFactory enzymeFactory;
    private static final Logger LOGGER = Logger.getLogger(EnzymePredictor.class);
    private Enzyme bestGuess;
    private boolean suitedForSearching = true;
    private File tempEnzymeFile;
    private HashMap<Character, Integer> N_TerminiCount;
    private HashMap<Character, Integer> C_TerminiCount;
    private int maxMissedCleavages = 2;
    private ArrayList<String> peptideSequences = new ArrayList<>();

    public Enzyme getBestGuess() {
        return bestGuess;
    }

    public void setBestGuess(Enzyme bestGuess) {
        this.bestGuess = bestGuess;
    }

    public boolean isSuitedForSearching() {
        return suitedForSearching;
    }

    public void setSuitedForSearching(boolean suitedForSearching) {
        this.suitedForSearching = suitedForSearching;
    }

    public EnzymePredictor() throws IOException, FileNotFoundException, ClassNotFoundException, XmlPullParserException {
        LOGGER.info("Setting up factories");
        loadEnzymeFactory();
        this.bestGuess = enzymeFactory.getEnzyme("Trypsin");
    }

    public EnzymePredictor(List<String> peptideSequences) throws IOException, FileNotFoundException, ClassNotFoundException, XmlPullParserException {
        LOGGER.info("Setting up factories");
        loadEnzymeFactory();
        this.bestGuess = enzymeFactory.getEnzyme("Trypsin");
        this.peptideSequences.addAll(peptideSequences);
    }

    /**
     * This method adds peptide objects to the predictor
     *
     * @param peptides
     * @throws IOException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     * @throws XmlPullParserException
     */
    public void addPeptideObjects(List<Peptide> peptides) throws IOException, FileNotFoundException, ClassNotFoundException, XmlPullParserException {
        for (Peptide aPeptide : peptides) {
            peptideSequences.add(aPeptide.getSequenceString());
        }
    }

    /**
     * This method adds peptide sequences to the predictor
     *
     * @param peptides
     * @throws IOException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     * @throws XmlPullParserException
     */
    public void addPeptides(List<String> peptides) throws IOException, FileNotFoundException, ClassNotFoundException, XmlPullParserException {
        peptideSequences.addAll(peptides);
    }

    private void loadEnzymeFactory() throws IOException, XmlPullParserException {
        if (tempEnzymeFile == null) {
            tempEnzymeFile = File.createTempFile("searchGUI_enzymes", ".xml");
            InputStream inputStream = new ClassPathResource("searchGUI_enzymes.xml").getInputStream();
            OutputStream outputStream = new FileOutputStream(tempEnzymeFile);
            IOUtils.copy(inputStream, outputStream);
            tempEnzymeFile.deleteOnExit();
        }
        enzymeFactory = EnzymeFactory.getInstance();
        enzymeFactory.importEnzymes(tempEnzymeFile);
    }

    public Enzyme estimateBestEnzyme() {
        N_TerminiCount = new HashMap<>();
        C_TerminiCount = new HashMap<>();
        for (String sequence : peptideSequences) {
            //C_TERM
            char charAAbeforeCleavage = sequence.charAt(sequence.length() - 1);
            char charAAAfterCleavage = sequence.charAt(0);
            C_TerminiCount.put(charAAbeforeCleavage, (C_TerminiCount.getOrDefault(charAAbeforeCleavage, 0) + 1));
            //N_TERM
            N_TerminiCount.put(charAAAfterCleavage, (N_TerminiCount.getOrDefault(charAAAfterCleavage, 0) + 1));
        }
        HashMap<Enzyme, Double> correctnessMap = new HashMap<>();
        //calculate "correctness" for all enzymes
        for (Enzyme anEnzyme : enzymeFactory.getEnzymes()) {
            double correctHits = 0;
            ArrayList<Character> aminoAcidAfter = anEnzyme.getAminoAcidAfter();
            ArrayList<Character> aminoAcidBefore = anEnzyme.getAminoAcidBefore();
            for (Character anAminoAcid : aminoAcidAfter) {
                correctHits += N_TerminiCount.getOrDefault(anAminoAcid, 0);
            }
            for (Character anAminoAcid : aminoAcidBefore) {
                correctHits += C_TerminiCount.getOrDefault(anAminoAcid, 0);
            }
            correctnessMap.put(anEnzyme, correctHits);
        }

        //check for the best misscleavages
        HashMap<Enzyme, Integer> missedCleavagesMap = new HashMap<>();
        for (Enzyme anEnzyme : enzymeFactory.getEnzymes()) {
            for (String aSequence : peptideSequences) {
                missedCleavagesMap.put(anEnzyme, Math.max(missedCleavagesMap.getOrDefault(anEnzyme, 0), anEnzyme.getNmissedCleavages(aSequence)));
            }
        }
        //select the best Enzyme
        double bestCorrectness = 0;

        for (Map.Entry<Enzyme, Double> anEnzyme : correctnessMap.entrySet()) {
            double correctNess = anEnzyme.getValue() / (missedCleavagesMap.get(anEnzyme.getKey()) + 1);
            if (correctNess > bestCorrectness) {
                bestGuess = anEnzyme.getKey();
                maxMissedCleavages = missedCleavagesMap.get(anEnzyme.getKey());
                bestCorrectness=correctNess;
            }
        }
        return bestGuess;
    }

    /**
     *
     * @param enzyme the used enzyme
     * @return the maximum amount of misscleavages found in a peptidesequence
     */
    public int getMissCleavages() {
        return maxMissedCleavages;
    }

    /**
     *
     * @param enzyme the used enzyme
     * @return the miss cleavage ratio (# Misscleavages / #Sequences)
     */
    public double getMissedCleavageRatio() {
        int totalMissed = 0;
        for (String aSequence : peptideSequences) {
            totalMissed += bestGuess.getNmissedCleavages(aSequence);
        }
        return PrideAsapStats.round((double) totalMissed / (double) peptideSequences.size(), 3);
    }

    public void clear() {
        enzymeFactory = null;
        bestGuess = null;
        if (peptideSequences != null) {
            peptideSequences.clear();
        }
    }

}
