/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.inference.ionaccuracy.massdeficit.logic;

import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Kenneth
 */
public class ModifiedSequenceConverter {

    static String example = "NH2-HSHHHS<P>SSTPSAATPT<P>PTAGAR-COOH";
    static PTMFactory ptmFactory = PTMFactory.getInstance();

    private static final Pattern TAG_REGEX = Pattern.compile("<(.+?)>");

    public static void main(String[] args) {
        init();
        getModifiedPeptide(example);
    }

    public static Peptide getModifiedPeptide(String modifiedSequence) {
        //   init();
        String sequence = getNormalSequence(modifiedSequence);
        ArrayList<ModificationMatch> modMatches = getModificationLocations(modifiedSequence);
        return new Peptide(sequence, modMatches);
    }

    private static void init() {
        ArrayList<String> defaultModifications = ptmFactory.getDefaultModifications();
        Collections.sort(defaultModifications);
    }

    private static String eliminateTermini(final String str) {
        return str.replace("NH2-", "")
                .replace("-COOH", "")
                .replace("Ace-", "");
    }

    private static String getNormalSequence(final String str) {
        return eliminateTermini(str).replaceAll("<[^>]*>", "");
    }

    private static ArrayList<ModificationMatch> getModificationLocations(final String str) {
        ArrayList<ModificationMatch> modificationMatches = new ArrayList<>();
        String tempSeq = eliminateTermini(str);
        final Matcher matcher = TAG_REGEX.matcher(tempSeq);
        while (matcher.find()) {
            String mod = matcher.group().replace("<", "").replace(">", "");
            int modLocation = matcher.start();
            char modifiedAminoAcid = tempSeq.charAt(modLocation-1);
            modificationMatches.add(new ModificationMatch(getUtilitiesPTMName(String.valueOf(modifiedAminoAcid), mod), true, modLocation));
        }
        //check for termini
        if (str.startsWith("Ace-")) {
            modificationMatches.add(new ModificationMatch("acetylation of protein n-term", true, 1));
        }
        return modificationMatches;
    }

    private static String getUtilitiesPTMName(String modifiedAminoAcid, String modification) {
        //phosphorylations
        String utilitiesPTMName = "";
        if (modification.equals("P")) {
            switch (modifiedAminoAcid) {
                case "S":
                    utilitiesPTMName = "phosphorylation of s";
                    break;
                case "T":
                    utilitiesPTMName = "phosphorylation of t";
                    break;
                case "Y":
                    utilitiesPTMName = "phosphorylation of y";
                    break;
                case "H":
                    utilitiesPTMName = "phosphorylation of h";
                    break;
                default:
            }
        } else if (modification.equals("Mox")) {
            switch (modifiedAminoAcid) {
                case "M":
                    utilitiesPTMName = "oxidation of m";
                    break;
                default:
            }
            return utilitiesPTMName;
        } else if (modification.equals("Cmm*")) {
            switch (modifiedAminoAcid) {
                case "C":
                    utilitiesPTMName = "carbamidomethyl c";
                    break;
                default:
            }
            return utilitiesPTMName;
        } else if (modification.equals("Pyr")) {
            switch (modifiedAminoAcid) {
                case "Q":
                    utilitiesPTMName = "pyro-glu from n-term q";
                    break;
                case "E":
                    utilitiesPTMName = "pyro-glu from n-term e";
                    break;
                default:
            }
            return utilitiesPTMName;
        }
        return utilitiesPTMName;
    }

}
