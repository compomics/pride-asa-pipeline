package com.compomics.pride_asa_pipeline.core.model.modification.impl;

import com.compomics.pride_asa_pipeline.core.model.modification.ModificationAdapter;
import com.compomics.pride_asa_pipeline.core.model.modification.PRIDEModification;
import com.compomics.pride_asa_pipeline.core.model.modification.source.PRIDEModificationFactory;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.AtomChain;
import com.compomics.util.experiment.biology.AtomImpl;
import com.compomics.util.experiment.biology.PTM;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
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
     * A standard pattern for chemical formula
     */
    private static final Pattern pattern1 = Pattern.compile("\\s?\\(?[1-9]*\\)?[A-Z][a-z]?\\s?\\(?-?[1-9]*\\)?[1-9]*?.");
    /**
     * A standard pattern for unresolved atoms
     */
    private static final Pattern pattern2 = Pattern.compile("[A-Z][a-z]?\\s[A-Z][a-z]?");
    /**
     * A logger
     */
    private static final Logger LOGGER = Logger.getLogger(UtilitiesPTMAdapter.class);

    @Override
    public PTM convertModification(PRIDEModification mod) {
        increaseMassChain = new AtomChain();
        decreaseMassChain = new AtomChain();
        decreaseMassChain.setAddition(false);
        LOGGER.debug("Getting target residues");
        fetchTargetResidues(mod);
        LOGGER.debug("Parsing PTM composition");
        parseFormula(mod.getFormula());
        AminoAcidPattern pattern = new AminoAcidPattern(targetResiduesString);
        LOGGER.debug("Inferring modification type");
        int type = getModType(mod);
        //check if the mass can be correctly retrieved, if not then throw a conversionexception???
        PTM ptm = new PTM(type, mod.getName(), mod.getAccession(), increaseMassChain, decreaseMassChain, pattern);
        ptm.getMass();
        //check that the modifications are not just substitutions and remove the amino acid mass if applicable
        checkSubstitutions(mod, ptm);
        return ptm;
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

    private void checkSubstitutions(PRIDEModification pridePTM, PTM convertedPTM) {
        if (pridePTM.getSpecificityCollection().size() == 1) {
            //this will only work if there is but a single amino acid target
            String prideAminoAcidName = pridePTM.getSpecificityCollection().get(0).getName().toString();
            com.compomics.util.experiment.biology.AminoAcid aminoAcid = com.compomics.util.experiment.biology.AminoAcid.getAminoAcid(prideAminoAcidName);
            AtomChain newChain = new AtomChain();
            AtomChain atomPTM = convertedPTM.getAtomChainAdded();
            AtomChain atomAA = aminoAcid.getMonoisotopicAtomChain();
            HashMap<String, Integer> occurenceMap = new HashMap<>();
            for (AtomImpl atom : atomAA.getAtomChain()) {
                occurenceMap.put(atom.getAtom().getLetter(), occurenceMap.getOrDefault(atom.getAtom().getLetter(), 0) - 1);
            }
            for (AtomImpl atom : atomPTM.getAtomChain()) {
                occurenceMap.put(atom.getAtom().getLetter(), occurenceMap.getOrDefault(atom.getAtom().getLetter(), 0) + 1);
            }
            //then you're left with only the positive ones...(in theory)
            for (Map.Entry<String, Integer> atom : occurenceMap.entrySet()) {
                if (atom.getValue() > 0) {
                    AtomImpl temp = new AtomImpl(Atom.getAtom(atom.getKey()), 0);
                    newChain.append(temp, atom.getValue());
                }
            }
            convertedPTM.setAtomChainAdded(newChain);
        }
    }

    public static void main(String[] args) {
        UtilitiesPTMAdapter adapter = new UtilitiesPTMAdapter();
        PRIDEModificationFactory instance = PRIDEModificationFactory.getInstance();
        LinkedHashMap<String, PRIDEModification> modificationMap = instance.getModificationMap();
        double coverage = 0;
        for (Map.Entry<String, PRIDEModification> aMod : modificationMap.entrySet()) {
            if (aMod.getValue().getFormula().equals("none")) {
                coverage++;
            } else if (pattern1.matcher(aMod.getValue().getFormula()).find()) {
                try {
                    PTM convertModification = adapter.convertModification(aMod.getValue());
                    System.out.println(aMod.getKey() + "\t" + aMod.getValue().getFormula() + "\tUniMod mass:" + aMod.getValue().getAveDeltaMass() + "\tCalculatedMass\t" + convertModification.getRoundedMass());
                    coverage++;
                } catch (UnsupportedOperationException | IllegalArgumentException | NullPointerException e) {
                    System.out.println(aMod.getKey() + "\t" + aMod.getValue().getFormula() + " could be parsed, but is not compatible with utilities : " + e.getMessage()
                    );
                }
            } else {
                System.out.println(aMod.getKey() + "\t" + aMod.getValue().getFormula() + " can not be parsed");
            }
        }
        System.out.println(100 * coverage / modificationMap.size() + " %");
    }

    private void parseFormula(String formula) {
        //replace the sugars in the formula?
        formula = formula.toUpperCase()
                .replace("METH", " C(1) H(3)")
                .replace("ETH", " C(2) H(5)")
                .replace("AC", " C(2) H(5)")
                .replace("NAC", " N C(2) H(5)")
                .replace("PROP", " C(3) H(7)")
                .replace("BUT", " C(4) H(9)")
                .replace("PENT", " C(5) H(11)")
                .replace("HEX", " C(6) H(13)")
                .replace("HEPT", " C(7) H(15)")
                .replace("OCT", " C(8) H(17)")
                .replace("NON", " C(9) H(19)")
                .replace("DEC", " C(10) H(21)");
        Matcher matcher = pattern1.matcher(formula);
        while (matcher.find()) {
            String atomSection = matcher.group(0);
            Matcher subMatcher = pattern2.matcher(atomSection);
            if (subMatcher.find()) {
                String[] atomSections = subMatcher.group(0).split(" ");
                for (String anAtomSection : atomSections) {
                    constructMassForPTM(formula, anAtomSection);
                }
            } else {
                constructMassForPTM(formula, atomSection);
            }
        }
    }

    private void constructMassForPTM(String formula, String atomSection) {
        //check this atomSection against the other regex, split them up if it is the case...
        int index = 0;
        //any integer in front of the atom is an isotope
        char[] toCharArray = atomSection.toCharArray();
        int isotope = 0;
        if (Character.isDigit(toCharArray[0]) || toCharArray[0] == '(') {
            String isotopeString = "";
            while (index < toCharArray.length && Character.isDigit(toCharArray[index])) {
                isotopeString += toCharArray[index];
                index++;
            }
            isotope = Integer.parseInt(isotopeString.trim().replace("(", ""));
        }

        //The atom can be multiple letters...
        String atomString = "";
        while (index < toCharArray.length && Character.isAlphabetic(toCharArray[index])) {
            atomString += toCharArray[index];
            index++;
        }
        //check for deuterium
        if (atomString.equalsIgnoreCase("D")) {
            atomString = "H";
            isotope = 2;
        }
        //any number following (between brackets or after a space?) is the count
        int count = 1;
        if (index < toCharArray.length && (toCharArray[index] == ' ' || toCharArray[index] == '(')) {
            String occurenceString = "";
            index++;
            if (index < toCharArray.length) {
                if (toCharArray[index] == '-') {
                    occurenceString += '-';
                    index++;
                }
                while (index < toCharArray.length && Character.isDigit(toCharArray[index])) {
                    occurenceString += toCharArray[index];
                    index++;
                }
                if (!occurenceString.isEmpty()) {
                    count = Integer.parseInt(occurenceString);
                }
            }
        }
        if (!atomString.isEmpty()) {
            //append to the modification
            //reformat atom after the replacement for sugars and organic short names
            atomString = atomString.substring(0, 1).toUpperCase() + atomString.substring(1).toLowerCase();

            Atom utilitiesAtom = Atom.getAtom(atomString);
            //the utilities isotope is the difference of the noted isotope with the monoisotopic mass...
            if (isotope != 0) {
                isotope = Math.abs((int) (isotope - utilitiesAtom.getMonoisotopicMass()));
            }
            ArrayList<Integer> implementedIsotopes = utilitiesAtom.getImplementedIsotopes();
            //check if the isotope is in the mapping?
            if (isotope == 0 || implementedIsotopes.contains(isotope)) {
                if (utilitiesAtom == null) {
                    //how to handle this?
                    System.out.println("Whoops");
                } else {
                    AtomImpl impl = new AtomImpl(utilitiesAtom, isotope);
                    if (count > 0) {
                        increaseMassChain.append(impl, count);
                    } else {
                        decreaseMassChain.append(impl, Math.abs(count));
                    }
                }
            } else {
                String isotopeMessage = "";
                for (int s : implementedIsotopes) {
                    isotopeMessage += s + ",\t";
                }
                throw new UnsupportedOperationException(formula + "\t : " + isotope + " is not a known isotope in the implementation :" + isotopeMessage.substring(0, isotopeMessage.length() - 2));
            }
        }
    }

}
