package com.compomics.pride_asa_pipeline.model.modification.impl;

import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.AtomChain;
import com.compomics.util.experiment.biology.AtomImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Kenneth Verheggen
 */
public enum CommonNeutralMassLoss {
    //   H35Cl(R-Cl),
    //   H37Cl(R-Cl),
    //  HF(R-F),
    C2H2O("methyl ketone, aromatic acetate"),
    CH3OH("methyl ester"),
    C2H5OH("ethyl ester"),
    C2H4("ethyl ester,aldehyde,ketone"),
    C2H6(""),
    C2HO("aromatic methyl ether"),
    C3H6("butyl ketone,propyl ether"),
    C4H10(""),
    // C4H8("Ar-n-C5H11, ArO-n-C4H9, Ar-iso-C5H11, ArO-iso-C4H9, pentyl ketone"),
    CH3COOH("acetate"),
    CH4(""),
    CO("aldehyde, ketone, carboxylic acid, ester, amide, phenol"),
    CO2("carboxylic acid, ester, anhydride"),
    H2(""),
    H2O("alcohol, ol"),
    S("thio"),
    HCCH("aromatic"),
    HCN("aromatic nitrile"),
    NO(""),
    NO2(""),
    SO("sulphoxide,sulphox"),
    H("");

    private AtomChain monoisotopicAtomChain;

    private final Pattern sectionPattern = Pattern.compile("([1-9]*)?\\s?[A-Z][a-z]?\\s?(\\(?[1-9]*\\)?)?");
    private String identifiers;

    private CommonNeutralMassLoss(String identifiers) {
        this.identifiers = identifiers;
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

    public String getIdentifiers() {
        return identifiers;
    }

}
