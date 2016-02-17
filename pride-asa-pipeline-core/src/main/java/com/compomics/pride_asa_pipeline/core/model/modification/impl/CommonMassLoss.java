package com.compomics.pride_asa_pipeline.core.model.modification.impl;

import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.AtomChain;
import com.compomics.util.experiment.biology.AtomImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author compomics
 */
public enum CommonMassLoss {
    H,
    CH3,
    OH,
    H2O,
    //  F,
    //  HF,
    C2H3,
    HCN,
    C2H4,
    CO,
    CH2O,
    CH3O,
    CH4O,
    S,
    CH3_H2O,
    HS,
    H2S,
    //  Cl,
    //  HCl,
    C3H6,
    C2H2O,
    C2H4N,
    C3H7,
    CH3CO,
    CO2O,
    CONH2,
    C2H5O,
    C4H7,
    C4H9,
    C2H3O2,
    C2H4O2,
    SO2,
    // Br,
    // HBr,
    I,
    HI,;

    private AtomChain monoisotopicAtomChain;
    private final Pattern sectionPattern = Pattern.compile("([1-9]*)?\\s?[A-Z][a-z]?\\s?(\\(?[1-9]*\\)?)?");

    private CommonMassLoss() {
        monoisotopicAtomChain = new AtomChain();
        String formula = this.name().replace("_", "");
        Matcher matcher = sectionPattern.matcher(formula);
        while (matcher.find()) {
            String temp = matcher.group(0);
            char[] toCharArray = temp.toCharArray();
            String tempAtom = "";
            String tempCount = "";
            int index = 0;
            while ((index < toCharArray.length) && Character.isAlphabetic(toCharArray[index])) {
                tempAtom += toCharArray[index];
                index++;
            }
            while ((index < toCharArray.length) && Character.isDigit(toCharArray[index])) {
                tempCount += toCharArray[index];
                index++;
            }
            if (tempCount.isEmpty()) {
                tempCount += 1;
            }
            monoisotopicAtomChain.append(new AtomImpl(Atom.getAtom(tempAtom), 0), Integer.parseInt(tempCount));
        }
    }

    public AtomChain getAtomChain() {
        return monoisotopicAtomChain;
    }

    public boolean isCompatible(AtomChain atomChain) {
        //if the difference is large enough to be explained by this...
        HashMap<String, Integer> atomCount = new HashMap<>();
        for (AtomImpl atom : getAtomChain().getAtomChain()) {
            atomCount.put(atom.getAtom().getLetter(), atomCount.getOrDefault(atom.getAtom().getLetter(), 0) - 1);
        }
        for (AtomImpl atom : atomChain.getAtomChain()) {
            if (atomCount.containsKey(atom.getAtom().getLetter())) {
                atomCount.put(atom.getAtom().getLetter(), atomCount.get(atom.getAtom().getLetter()) + 1);
            }
        }
        //check if everything is not negative
        for (Map.Entry<String, Integer> atom : atomCount.entrySet()) {
            if (atom.getValue() < 0) {
                return false;
            }
        }
        return true;
    }
}
