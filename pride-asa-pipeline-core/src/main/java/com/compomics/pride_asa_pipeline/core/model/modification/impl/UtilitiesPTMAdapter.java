package com.compomics.pride_asa_pipeline.core.model.modification.impl;

import com.compomics.pride_asa_pipeline.core.exceptions.ParameterExtractionException;
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
import java.util.TreeMap;
import java.util.logging.Level;
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
    private static final Pattern pattern1 = Pattern.compile("([0-9]*)?\\s?[A-Z][a-z]*?(\\s?[0-9]*)?(\\(-?[0-9]*\\)?)?");
    /**
     * A standard pattern for unresolved atoms
     */
    private static final Pattern pattern2 = Pattern.compile("[A-Z][a-z]?\\s[A-Z][a-z]?");
    /**
     * A logger
     */
    private static final Logger LOGGER = Logger.getLogger(UtilitiesPTMAdapter.class);

    public static void main(String[] args) {
        //testPTM("2-methyl-L-glutamine");
        // testPTM("1-thioglycine (internal)");
        //testPTM("Biotin");
        // testPTM("PEO-Iodoacetyl-LC-Biotin");
        //   testPTM("L-3',4',5'-trihydroxyphenylalanine");
        testCoverage();
    }

    public static void testPTM(String testMod) {
        System.out.println(testMod);
        UtilitiesPTMAdapter adapter = new UtilitiesPTMAdapter();
        PRIDEModificationFactory instance = PRIDEModificationFactory.getInstance();
        LinkedHashMap<String, PRIDEModification> modificationMap = instance.getModificationMap();
        PRIDEModification get = modificationMap.get(testMod);
        System.out.println(get.getFormula());
        PTM convertModification;
        try {
            convertModification = adapter.convertModification(modificationMap.get(testMod));
            LOGGER.info(testMod + "\t" + modificationMap.get(testMod).getMonoDeltaMass() + "\tvs\t" + convertModification.getMass());
        } catch (ParameterExtractionException ex) {
            LOGGER.error(ex);
        }

    }

    public static void testCoverage() {
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
                    LOGGER.info(aMod.getKey() + "\t" + aMod.getValue().getFormula() + "\tUniMod mass:" + aMod.getValue().getAveDeltaMass() + "\tCalculatedMass\t" + convertModification.getRoundedMass());
                    coverage++;
                } catch (ParameterExtractionException | UnsupportedOperationException | IllegalArgumentException | NullPointerException e) {
                    LOGGER.error(aMod.getKey() + "\t" + aMod.getValue().getFormula() + "error : " + e.getMessage()
                    );
                }
            } else {
                System.out.println(aMod.getKey() + "\t" + aMod.getValue().getFormula() + " can not be parsed");
            }
        }
        System.out.println(100 * coverage / modificationMap.size() + " %");
    }

    @Override
    public PTM convertModification(PRIDEModification mod) throws ParameterExtractionException {
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
        //check that the modifications are not just substitutions and remove the amino acid mass if applicable
        checkSubstitutions(mod, ptm);
        //check for reported differences with the original unimod mass (for example loss of H2O, addition of CO, ...)
        checkChemicalLosses(mod, ptm);
        double massDifference = Math.abs(ptm.getMass() - mod.getMonoDeltaMass());
        if (massDifference > 1) {
            throw new ParameterExtractionException(ptm.getName() + " differs more than 1 da from the reported mono-isotopic mass value (" + massDifference + ")");
        }
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
        //check for amino acid names in the mod name...
        if (pridePTM.getSpecificityCollection().size() == 1) {
            //this will only work if there is but a single amino acid target
            String prideAminoAcidName = pridePTM.getSpecificityCollection().get(0).getName().toString();
            com.compomics.util.experiment.biology.AminoAcid aminoAcid = com.compomics.util.experiment.biology.AminoAcid.getAminoAcid(prideAminoAcidName);
            if (pridePTM.getName().toLowerCase().contains(aminoAcid.name.toLowerCase()) || pridePTM.getName().toLowerCase().contains(aminoAcid.threeLetterCode.toLowerCase())) {
                if (aminoAcid.getMonoisotopicMass() < convertedPTM.getMass()) {
                    AtomChain atomAA = aminoAcid.getMonoisotopicAtomChain();
                    subtractAtomChain(convertedPTM, atomAA);
                }
            }
        }
    }

    private void checkChemicalLosses(PRIDEModification pridePTM, PTM convertedPTM) {
        //in case of a missmatch
        double mass_delta = (convertedPTM.getRoundedMass() - pridePTM.getMonoDeltaMass());

        if (Math.abs(mass_delta) > 1) {
            //check the losses for the closest one?
            TreeMap<Double, CommonMassLoss> smallestDifferenceMap = new TreeMap<>();
            for (CommonMassLoss loss : CommonMassLoss.values()) {
                smallestDifferenceMap.put(Math.abs(mass_delta - loss.getAtomChain().getMass()), loss);
            }
            //check the compatible losses...
            for (Map.Entry<Double, CommonMassLoss> loss : smallestDifferenceMap.entrySet()) {
                if (Math.abs(mass_delta) < 1) {
                    return;
                } else if (loss.getValue().isCompatible(convertedPTM.getAtomChainAdded())) {
                    double lostMass = loss.getValue().getAtomChain().getMass();
                    if (Math.abs(lostMass - mass_delta) < 1) {
                        subtractAtomChain(convertedPTM, loss.getValue().getAtomChain());
                    }
                }
            }
        }
    }

    private void subtractAtomChain(PTM convertedPTM, AtomChain atomChain) {

        AtomChain atomChainAdded = convertedPTM.getAtomChainAdded();
        HashMap<String, Integer> atomCount = new HashMap<>();
        for (AtomImpl anAtom : atomChainAdded.getAtomChain()) {
            String letter = anAtom.getAtom().getLetter();
            atomCount.put(letter, atomCount.getOrDefault(letter, 0) + 1);
        }
        //deduct the amino acid from the atom count
        for (AtomImpl anAtom : atomChain.getAtomChain()) {
            String letter = anAtom.getAtom().getLetter();
            atomCount.put(letter, atomCount.getOrDefault(letter, 0) - 1);
        }
        //put the new hashmap into new atom chains and set them in the PTM
        AtomChain newlyAdded = new AtomChain();
        AtomChain newlyRemoved = new AtomChain();
        for (Map.Entry<String, Integer> aLetter : atomCount.entrySet()) {
            AtomImpl atom = new AtomImpl(Atom.getAtom(aLetter.getKey()), 0);
            if (aLetter.getValue() < 0) {
                newlyRemoved.append(atom, Math.abs(aLetter.getValue()));
            } else {
                newlyAdded.append(atom, Math.abs(aLetter.getValue()));
            }
        }
        convertedPTM.setAtomChainAdded(newlyAdded);
        convertedPTM.setAtomChainRemoved(newlyRemoved);
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
        char[] toCharArray = atomSection.trim().toCharArray();
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
                AtomImpl impl = new AtomImpl(utilitiesAtom, isotope);
                //   System.out.println(atomString + " " + (impl.getMass()) * count);
                if (count > 0) {
                    increaseMassChain.append(impl, count);
                } else {
                    decreaseMassChain.append(impl, Math.abs(count));
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
