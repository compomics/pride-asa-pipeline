package com.compomics.pride_asa_pipeline.core.logic.modification.conversion.impl;

import com.compomics.pride_asa_pipeline.core.logic.modification.conversion.ModificationAdapter;
import com.compomics.pride_asa_pipeline.core.logic.modification.conversion.UniModModification;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.AtomChain;
import com.compomics.util.experiment.biology.AtomImpl;
import com.compomics.util.experiment.biology.PTM;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import uk.ac.ebi.pridemod.model.Specificity;
import uk.ac.ebi.pridemod.model.Specificity.AminoAcid;
import uk.ac.ebi.pridemod.model.Specificity.Position;

/**
 *
 * @author Kenneth Verheggen
 */
public class UtilitiesPTMAdapter implements ModificationAdapter<PTM> {

    /**
     * the targeted residues as a string
     */
    private String targetResiduesString;
    /**
     * the atom chain for masses that contribute to the total mass
     */
    private AtomChain increaseMassChain = new AtomChain();
    /**
     * the atom chain for masses that deduct from the total mass
     */
    private AtomChain decreaseMassChain = new AtomChain();
    /**
     * The regex pattern for atoms
     */
    private static final Pattern atomPattern = Pattern.compile("[A-Z][a-z]*\\d*");
    /**
     * The regex pattern for isotopes
     */
    private static final Pattern isotopePattern = Pattern.compile("[0-9]*[^A-Z]*[0-9]");
    /**
     * The regex pattern for atypical pride formulatae
     */
    private static final Pattern prideFormatPattern = Pattern.compile("([A-Z][a-z]?[0-9]?\\s[0-9]*)");
    /**
     * A logger
     */
    private static final Logger LOGGER = Logger.getLogger(UtilitiesPTMAdapter.class);

    @Override
    public PTM convertModification(UniModModification mod) {
        increaseMassChain = new AtomChain();
        decreaseMassChain = new AtomChain();
        decreaseMassChain.setAddition(false);
        LOGGER.debug("Getting target residues");
        fetchTargetResidues(mod.getPtm());
        LOGGER.debug("Parsing PTM composition");
        parseFormula(mod.getPtm().getFormula(), " ");
        AminoAcidPattern pattern = new AminoAcidPattern(targetResiduesString);
        LOGGER.debug("Inferring modification type");
        int type = getModType(mod.getPtm());
        return new PTM(type, mod.getPtm().getName(), mod.getPtm().getAccession(), increaseMassChain, decreaseMassChain, pattern);
    }

    private void fetchTargetResidues(uk.ac.ebi.pridemod.model.PTM ptm) {
        HashSet<AminoAcid> targetResidues = new HashSet<>();
        StringBuilder targetResiduesStringBuilder = new StringBuilder();
        for (Specificity specificity : ptm.getSpecificityCollection()) {
            if (specificity != null) {
                if (specificity.getName() != null) {
                    if (!specificity.getName().equals(AminoAcid.NONE) && !targetResidues.contains(specificity.getName())) {
                        targetResidues.add(specificity.getName());
                        targetResiduesStringBuilder.append(specificity.getName());
                    }
                }
            }
        }
        this.targetResiduesString = targetResiduesStringBuilder.toString();
    }

    private int getModType(uk.ac.ebi.pridemod.model.PTM ptm) {
        HashMap<Position, Integer> positionMap = new HashMap<>();
        for (Specificity specificity : ptm.getSpecificityCollection()) {
            positionMap.put(specificity.getPosition(), positionMap.getOrDefault(specificity.getPosition(), 0) + 1);
        }
        int nTermPeptides = positionMap.getOrDefault(Position.NTERM, 0);
        int cTermPeptides = positionMap.getOrDefault(Position.NTERM, 0);
        int nTermProteins = positionMap.getOrDefault(Position.PNTERM, 0);
        int cTermProteins = positionMap.getOrDefault(Position.PCTERM, 0);
        int noTerm = positionMap.getOrDefault(Position.NONE, 0);

        //TODO review this
        //if one is none specific, all should be set to none specific to get all of them covered...
        boolean aSpecific = targetResiduesString.isEmpty();
        if (nTermPeptides > 0 && cTermPeptides == 0) {
            if (aSpecific) {
                return PTM.MODNP;
            } else {
                return PTM.MODNPAA;
            }
        } else if (cTermPeptides > 0 && nTermPeptides == 0) {
            if (aSpecific) {
                return PTM.MODCP;
            } else {
                return PTM.MODCPAA;
            }
        } else if (nTermProteins > 0 && cTermProteins == 0) {
            if (aSpecific) {
                return PTM.MODN;
            } else {
                return PTM.MODNAA;
            }
        } else if (cTermProteins > 0 && nTermProteins == 0) {
            if (aSpecific) {
                return PTM.MODC;
            } else {
                return PTM.MODCAA;
            }
        } else if (!aSpecific) {
            return PTM.MODAA;
        } else {
            throw new IllegalArgumentException("Could not map modification correctly :"
                    + ptm.getAccession());
        }
    }

    private void parseFormula(String formula, String atomSeparator) {
        if (formula != null && !formula.equalsIgnoreCase("none")) {
            if (formula.length() == 1) {
                AtomImpl atom;
                atom = new AtomImpl(Atom.getAtom(formula), 0);
                increaseMassChain.append(atom, 1);
            } else if (!formula.contains("(")) {
                parseFormulaPRIDEFormat(formula);
            } else {
                //preprocess formula...
                formula = formula.toUpperCase()
                        .replace("METH", "C(1)H(3)")
                        .replace("ETH", "C(2)H(5)")
                        .replace("PROP", "C(3)H(7)")
                        .replace("BUT", "C(4)H(9)")
                        .replace("PENT", "C(5)H(11)")
                        .replace("HEX", "C(6)H(13)")
                        .replace("HEPT", "C(7)H(15)")
                        .replace("OCT", "C(8)H(17)")
                        .replace("NON", "C(9)H(19)")
                        .replace("DEC", "C(10)H(21)");
                String[] compounds = formula.split(atomSeparator);
                for (String aCompound : compounds) {
                    Matcher atomMatcher = atomPattern.matcher(aCompound);
                    Matcher isotopeMatcher = isotopePattern.matcher(aCompound);
                    if (atomMatcher.find()) {
                        String atomSymbol = atomMatcher.group();
                        //check if it contains an isotope? (for example C1)
                        int isotope = 0;
                        if (isotopeMatcher.matches()) {
                            isotope = Integer.parseInt(isotopeMatcher.group());
                        }
                        int compoundOccurence = 1;
                        if (aCompound.contains("(")) {
                            compoundOccurence = Integer.parseInt(aCompound.split("\\(")[1].replace(")", ""));
                        }
                        AtomImpl atom;
                        atom = new AtomImpl(Atom.getAtom(atomSymbol), isotope);
                        if (compoundOccurence > 0) {
                            increaseMassChain.append(atom, compoundOccurence);
                        } else {
                            decreaseMassChain.append(atom, Math.abs(compoundOccurence));
                        }
                    } else {
                        throw new IllegalArgumentException("Can not parse chemical formula :" + formula);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Could not find chemical formula for this modification");
        }
    }

    private void parseFormulaPRIDEFormat(String formula) {
        //preprocess formula...
        formula = formula.toUpperCase()
                .replace("METH", "C 1 H 3")
                .replace("ETH", "C 2 H 5")
                .replace("PROP", "C 3 H 7")
                .replace("BUT", "C 4 H 9")
                .replace("PENT", "C 5 H 11")
                .replace("HEX", "C 6 H 13")
                .replace("HEPT", "C 7 H 15")
                .replace("OCT", "C 8 H 17")
                .replace("NON", "C 9 H 19")
                .replace("DEC", "C 10 H 21");
        Matcher atomMatcher = prideFormatPattern.matcher(formula);
        if (atomMatcher.find()) {
            String aCompound = atomMatcher.group();
            String[] split = aCompound.split(" ");
            Matcher isotopeMatcher = isotopePattern.matcher(split[0]);
//check if it contains an isotope? (for example C1 or 2H)
            int isotope = 0;
            if (isotopeMatcher.matches()) {
                isotope = Integer.parseInt(isotopeMatcher.group());
            }
            int compoundOccurence = 1;
            if (aCompound.contains("(")) {
                compoundOccurence = Integer.parseInt(split[1]);
            }
            AtomImpl atom;
            atom = new AtomImpl(Atom.getAtom(split[0]), isotope);
            if (compoundOccurence > 0) {
                increaseMassChain.append(atom, compoundOccurence);
            } else {
                decreaseMassChain.append(atom, Math.abs(compoundOccurence));
            }
        } else {
            throw new IllegalArgumentException("Can not parse chemical formula :" + formula);
        }
    }
}
