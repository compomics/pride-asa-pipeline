package com.compomics.pride_asa_pipeline.core.logic.inference.enzyme;

import com.compomics.pride_asa_pipeline.core.logic.inference.InferenceStatistics;
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

    /**
     * The enzyme factory
     */
    private static EnzymeFactory enzymeFactory;
    /**
     * A logger
     */
    private static final Logger LOGGER = Logger.getLogger(EnzymePredictor.class);
    /**
     * boolean indicating if this project is fit for searches
     */
    private boolean suitedForSearching = true;
    /**
     * The temporary enzyme file
     */
    private File tempEnzymeFile;
    /**
     * the count of amino acid occurences from the N-terminus
     */
    private HashMap<Character, Integer> N_TerminiCount;
    /**
     * the count of amino acid occurences from the C-terminus
     */
    private HashMap<Character, Integer> C_TerminiCount;
    /**
     * The maximal amount of allowed misscleavages
     */
    private int maxMissedCleavages = 2;
    /**
     * the peptide sequences that need to be considered
     */
    private ArrayList<String> peptideSequences = new ArrayList<>();
    /*
     * The most likely enzyme
     */
    private Enzyme mostLikelyEnzyme;

    public boolean isSuitedForSearching() {
        return suitedForSearching;
    }

    public void setSuitedForSearching(boolean suitedForSearching) {
        this.suitedForSearching = suitedForSearching;
    }

    public EnzymePredictor() throws IOException, FileNotFoundException, ClassNotFoundException, XmlPullParserException {
        loadEnzymeFactory();
    }

    public EnzymePredictor(List<String> peptideSequences) throws IOException, XmlPullParserException {
        loadEnzymeFactory();
        this.peptideSequences.addAll(peptideSequences);
        estimateBestEnzyme();
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

    /**
     * Estimates the best suited enzyme for a given collection of sequences
     *
     */
    private void estimateBestEnzyme() {
        mostLikelyEnzyme = enzymeFactory.getEnzyme("Trypsin");
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
                int currentMissedCleavages = Math.max(anEnzyme.getNmissedCleavages(aSequence), missedCleavagesMap.getOrDefault(anEnzyme, 0));
                missedCleavagesMap.put(anEnzyme, currentMissedCleavages);
            }
        }
        //select the best Enzyme
        double bestCorrectness = 0;

        for (Map.Entry<Enzyme, Double> anEnzyme : correctnessMap.entrySet()) {
            double correctNess = anEnzyme.getValue() / (missedCleavagesMap.get(anEnzyme.getKey()) + 1);
            if (correctNess > bestCorrectness) {
                mostLikelyEnzyme = anEnzyme.getKey();
                maxMissedCleavages = missedCleavagesMap.get(anEnzyme.getKey());
                bestCorrectness = correctNess;
            }
        }
    }

    /**
     *
     * @param enzyme the used enzyme
     * @return the maximum amount of misscleavages found in the provided
     * peptidesequences
     */
    public int getMissedCleavages() {
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
            totalMissed += mostLikelyEnzyme.getNmissedCleavages(aSequence);
        }
        return InferenceStatistics.round((double) totalMissed / (double) peptideSequences.size(), 3);
    }

    public Enzyme getMostLikelyEnzyme() {
        return mostLikelyEnzyme;
    }

}
