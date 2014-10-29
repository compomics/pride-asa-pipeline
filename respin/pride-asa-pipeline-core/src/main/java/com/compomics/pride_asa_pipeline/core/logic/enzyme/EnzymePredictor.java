/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.enzyme;

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
import java.util.LinkedHashMap;
import java.util.List;
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
    private final List<String> peptideSequences;
    private Enzyme bestGuess;
    private boolean suitedForSearching = true;
    private File tempEnzymeFile;

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
        this.peptideSequences = new ArrayList<>();
    }

    public EnzymePredictor(List<String> peptideSequences) throws IOException, FileNotFoundException, ClassNotFoundException, XmlPullParserException {
        LOGGER.info("Setting up factories");
        loadEnzymeFactory();
        this.peptideSequences = peptideSequences;
    }

    public void addPeptideObjects(List<Peptide> peptides) throws IOException, FileNotFoundException, ClassNotFoundException, XmlPullParserException {
        for (Peptide aPeptide : peptides) {
            peptideSequences.add(aPeptide.getSequenceString());
        }
    }

    public void addPeptides(List<String> peptides) throws IOException, FileNotFoundException, ClassNotFoundException, XmlPullParserException {
        for (String aPeptide : peptides) {
            peptideSequences.add(aPeptide);
        }
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

    public List<String> getPeptideSequences() {
        return peptideSequences;
    }

    public Enzyme getMainEnzyme(LinkedHashMap<Enzyme, Integer> enzymecounts) {
        int chymoTrypsin;
        int argC;
        int lysC;
        int pepsin;
        int trypsin;
        //if none of these, just pick the highest ranking

        Enzyme highestCount = enzymeFactory.getEnzyme("Trypsin");
        for (Enzyme anEnzyme : enzymecounts.keySet()) {
            int bestGuessCount = enzymecounts.get(highestCount);
            if (enzymecounts.get(anEnzyme) > bestGuessCount) {
                highestCount = anEnzyme;
            }
        }

        try {
            trypsin = enzymecounts.get(enzymeFactory.getEnzyme("Trypsin"));
        } catch (NullPointerException e) {
            trypsin = 0;
        }

        try {
            chymoTrypsin = enzymecounts.get(enzymeFactory.getEnzyme("Chymotrypsin (FYWL)"));
        } catch (NullPointerException e) {
            chymoTrypsin = 0;
        }
        try {
            pepsin = enzymecounts.get(enzymeFactory.getEnzyme("Pepsin A"));
        } catch (NullPointerException e) {
            pepsin = 0;
        }

        if (highestCount.getName().toUpperCase().contains("TRYPSIN +") && (double) chymoTrypsin < (double) (0.5 * trypsin)) {
            highestCount = enzymeFactory.getEnzyme("Trypsin");
        }

//check if it's not chymotrypsin or pepsin...
        if (highestCount.getName().toUpperCase().contains("CHYMOTRYP")) {
            double trypsinToChymoTrypsin = (double) ((double) trypsin / (double) chymoTrypsin);
            double trypsinToPepsin = (double) ((double) trypsin / (double) pepsin);
            double pepsinToChymoTrypsin = (double) ((double) pepsin / (double) chymoTrypsin);
            if (trypsinToChymoTrypsin <= 0.5 && pepsinToChymoTrypsin < 0.8) {
                highestCount = enzymeFactory.getEnzyme("Chymotrypsin (FYWL)");
            } else if (trypsinToPepsin < 0.5) {
                highestCount = enzymeFactory.getEnzyme("Pepsin A");
            }
        } else if (highestCount.getName().toLowerCase().contains("pepsin")) {
            try {
                Enzyme chymoTrypsinEnzyme = enzymeFactory.getEnzyme("Chymotrypsin (FYWL)");
                chymoTrypsin = enzymecounts.get(chymoTrypsinEnzyme);
            } catch (NullPointerException e) {
                chymoTrypsin = 0;
            }
            //AT THIS POINT, IT IS NOT TRYPSIN, or C 
            if ((pepsin / chymoTrypsin) >= 0.9) {
                return highestCount;
            }
            highestCount = enzymeFactory.getEnzyme("Chymotrypsin (FYWL)");
        }

//ELSE IT COULD VERY WELL BE TRYPSIN
        if (highestCount.getName().equalsIgnoreCase("trypsin")) {
            double trypsinToPepsin = (double) ((double) pepsin / (double) trypsin);

            try {
                argC = enzymecounts.get(enzymeFactory.getEnzyme("Arg-C"));
            } catch (NullPointerException e) {
                argC = 0;
            }
            try {
                lysC = enzymecounts.get(enzymeFactory.getEnzyme("Lys-C"));
            } catch (NullPointerException e) {
                lysC = 0;
            }
            //check if arg-c or lys-c
            double argToTryps = (double) (1 - ((double) argC / (double) trypsin));
            double lysToTryps = (double) (1 - ((double) lysC / (double) trypsin));
            if (trypsinToPepsin > 0.5) {
                highestCount = enzymeFactory.getEnzyme("Pepsin A");
            } else if (-0.1 < argToTryps && argToTryps < 0.1) {
                highestCount = enzymeFactory.getEnzyme("Arg-C");
            } else if (-0.1 < lysToTryps && lysToTryps < 0.1) {
                highestCount = enzymeFactory.getEnzyme("Lys-C");
            } else {
                highestCount = enzymeFactory.getEnzyme("Trypsin");
            }
        }

        bestGuess = highestCount;
        for (Enzyme anEnzyme : enzymecounts.keySet()) {
            System.out.println(anEnzyme.getName() + ":" + enzymecounts.get(anEnzyme));
        }
        return highestCount;
    }

    public LinkedHashMap<Enzyme, Integer> getEnzymeCounts() {
        LOGGER.info("Counting enzyme occurences");
        bestGuess = enzymeFactory.getEnzyme("Trypsin");
        HashMap<Character, Integer> AAbeforeMap = new HashMap<>();
        for (String sequence : peptideSequences) {
            char CharAAbeforeCleavage = sequence.charAt(sequence.length() - 1);
            int counter = 1;
            if (AAbeforeMap.containsKey(CharAAbeforeCleavage)) {
                counter = counter + (AAbeforeMap.get(CharAAbeforeCleavage));
            }
            AAbeforeMap.put(CharAAbeforeCleavage, counter);
        }
        LinkedHashMap<Enzyme, Integer> enzymeHits = new LinkedHashMap<>();
        ArrayList<Enzyme> enzymes = enzymeFactory.getEnzymes();
        int totalEnzymeCounter = 0;
        for (Enzyme anEnzyme : enzymes) {
            int enzymeCounter = 0;
            for (char AA : anEnzyme.getAminoAcidBefore()) {
                if (AAbeforeMap.get(AA) != null) {
                    enzymeCounter = enzymeCounter + AAbeforeMap.get(AA);
                }
            }
            if (enzymeCounter != 0) {
                LOGGER.debug("Checking " + anEnzyme.getName() + " - " + enzymeCounter);
                enzymeHits.put(anEnzyme, enzymeCounter);
            }
            totalEnzymeCounter += enzymeCounter;
        }
        //Sort on occurences 
        //Deal with special cases : 
        //default
        if (enzymeHits.isEmpty()) {
            enzymeHits.put(enzymeFactory.getEnzyme("Trypsin"), 9999);
        }
        return enzymeHits;
    }

    public Enzyme estimateEnzyme(List<String> peptideSequences) {
        LinkedHashMap<Enzyme, Integer> enzymeCounts = getEnzymeCounts();
        return getMainEnzyme(enzymeCounts);
    }

    public int estimateMissedCleavages(Enzyme enzyme) {
        int averageMissedCleavage = 2;
        for (String aSequence : peptideSequences) {
            averageMissedCleavage += enzyme.getNmissedCleavages(aSequence);
        }
        averageMissedCleavage = (int) Math.max(1, Math.ceil(averageMissedCleavage / peptideSequences.size()));
        return averageMissedCleavage;
    }

}
