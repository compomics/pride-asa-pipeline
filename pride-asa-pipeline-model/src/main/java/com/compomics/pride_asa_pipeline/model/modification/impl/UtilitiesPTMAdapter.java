package com.compomics.pride_asa_pipeline.model.modification.impl;


import com.compomics.pride_asa_pipeline.model.ParameterExtractionException;
import com.compomics.pride_asa_pipeline.model.modification.ModificationAdapter;
import com.compomics.pride_asa_pipeline.model.modification.PRIDEModification;
import com.compomics.pride_asa_pipeline.model.modification.source.PRIDEModificationFactory;
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
    private static final Pattern pattern1 = Pattern.compile("([0-9]*)?\\s?([A-Z][a-z]?)(\\s?[0-9]*)?(\\(?-?[0-9]*\\)?)?");
    /**
     * A standard pattern for unresolved atoms
     */
    private static final Pattern pattern2 = Pattern.compile("[A-Z][a-z]?\\s[A-Z][a-z]?");
    //private static final Pattern pattern2 = Pattern.compile("[A-Z][a-z]?\\s?(\\(\\-[0-9]*\\))?");
    /**
     * A logger
     */
    private static final Logger LOGGER = Logger.getLogger(UtilitiesPTMAdapter.class);
    /**
     * a map of amino acid synonyms according to IUPAC nomenclature
     */
    private HashMap<String, HashSet<String>> synonyms;

    public static void main(String[] args) {
        System.out.println((100 * getPTMCoverage()) + "% of the ptms could be correctly extracted");
    }

    public UtilitiesPTMAdapter() {
        initAminoAcidSynonymHashMap();
    }

    public static double getPTMCoverage() {
        UtilitiesPTMAdapter adapter = new UtilitiesPTMAdapter();
        PRIDEModificationFactory instance = PRIDEModificationFactory.getInstance();
        LinkedHashMap<String, PRIDEModification> modificationMap = instance.getModificationMap();
        double coverage = 0;
        double atomNI = 0;
        double unparseable = 0;
        double wrongMass = 0;
        for (Map.Entry<String, PRIDEModification> aMod : modificationMap.entrySet()) {
            if (aMod.getValue().getFormula().equals("none")) {
                coverage++;
            } else if (pattern1.matcher(aMod.getValue().getFormula()).find()) {
                try {
                    PTM convertModification = adapter.convertModification(aMod.getValue());
                    LOGGER.info(aMod.getKey() + "\t" + aMod.getValue().getFormula() + "\tUniMod mass:" + aMod.getValue().getAveDeltaMass() + "\tCalculatedMass\t" + convertModification.getRoundedMass());
                    coverage++;
                } catch (ParameterExtractionException e) {
                    LOGGER.error(aMod.getKey() + "\t" + aMod.getValue().getFormula() + "error : " + e.getMessage()
                    );
                    wrongMass++;
                } catch (UnsupportedOperationException e2) {
                    LOGGER.error(aMod.getKey() + "\t" + aMod.getValue().getFormula() + "error : " + e2.getMessage()
                    );
                    atomNI++;
                } catch (NullPointerException | IllegalArgumentException e3) {
                    LOGGER.error(aMod.getKey() + "\t" + aMod.getValue().getFormula() + "error : " + e3.getMessage()
                    );
                    unparseable++;
                }
            } else {
                LOGGER.error(aMod.getKey() + "\t" + aMod.getValue().getFormula() + " can not be parsed");
                unparseable++;
            }
        }
        LOGGER.info("Atoms not implemented : " + (100 * atomNI / modificationMap.size()));
        LOGGER.info("Wrong mass inference : " + (100 * wrongMass / modificationMap.size()));
        LOGGER.info("Unparseable : " + (100 * unparseable / modificationMap.size()));
        LOGGER.info("Total correct : " + (100 * coverage / modificationMap.size()));
        LOGGER.info("Total incorrect :" + (100 * (atomNI + wrongMass + unparseable) / modificationMap.size()));
        return (coverage / modificationMap.size());
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
        //     System.out.println("Ptm mass is now " + ptm.getMass());
        checkSubstitutions(mod, ptm);
        //  System.out.println("Ptm mass is now " + ptm.getMass());
        //check for reported differences with the original unimod mass (for example loss of H2O, addition of CO, ...)
        checkChemicalLosses(mod, ptm);
        // System.out.println("Ptm mass is now " + ptm.getMass());
        double massDifference = calculateMassDelta(ptm, mod);
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
            //check if the name contains some synonyms?
            boolean suspectedSubstitution = pridePTM.getName().toLowerCase().contains(aminoAcid.name.toLowerCase())
                    || pridePTM.getName().toLowerCase().contains(aminoAcid.threeLetterCode.toLowerCase());
            //if the amino acid name itself was not present, check for its synonyms and IUPAC names
            if (!suspectedSubstitution) {
                //check if the name contains some synonyms?
                HashSet<String> aminoAcidSynonyms = synonyms.get(aminoAcid.singleLetterCode.toUpperCase());
                for (String aSynonym : aminoAcidSynonyms) {
                    if (pridePTM.getName().toLowerCase().contains(aSynonym)) {
                        suspectedSubstitution = true;
                        break;
                    }
                }
            }
            //if there now is a suspected substitution, remove the amino acid
            if (suspectedSubstitution) {
                if (aminoAcid.getMonoisotopicMass() < convertedPTM.getMass()) {
                    AtomChain atomAA = aminoAcid.getMonoisotopicAtomChain();
                    subtractAtomChain(convertedPTM, atomAA);
                }
            }
        }
    }

    private void initAminoAcidSynonymHashMap() {
        synonyms = new HashMap<>();
//Alanine
        HashSet<String> alanineSynonyms = new HashSet<>();
        alanineSynonyms.add("2-Aminopropanoic");
        synonyms.put("A", alanineSynonyms);
//Arginine
        HashSet<String> arginineSynonyms = new HashSet<>();
        alanineSynonyms.add("2-Amino-5-guanidinopentanoic");
        synonyms.put("R", arginineSynonyms);
//Asparagine
        HashSet<String> asparagineSynonyms = new HashSet<>();
        alanineSynonyms.add("2-Amino-3-carbamoylpropanoic");
        synonyms.put("N", asparagineSynonyms);
//Aspartic
        HashSet<String> asparticAcidSynonyms = new HashSet<>();
        asparticAcidSynonyms.add("2-Aminobutanedioic");
        synonyms.put("D", asparticAcidSynonyms);
//Cysteine
        HashSet<String> cysteineSynonyms = new HashSet<>();
        cysteineSynonyms.add("2-Amino-3-mercaptopropanoic");
        synonyms.put("C", cysteineSynonyms);
//Glutamine
        HashSet<String> glutamineSynonyms = new HashSet<>();
        glutamineSynonyms.add("2-Amino-4-carbamoylbutanoic");
        synonyms.put("Q", glutamineSynonyms);
//Glutamic
        HashSet<String> glutamicAcidSynonyms = new HashSet<>();
        glutamicAcidSynonyms.add("2-Aminopentanedioic");
        synonyms.put("E", glutamicAcidSynonyms);
//Glycine
        HashSet<String> glycineSynonyms = new HashSet<>();
        glycineSynonyms.add("Aminoethanoic");
        synonyms.put("G", glycineSynonyms);
//Histidine
        HashSet<String> histidineSynonyms = new HashSet<>();
        histidineSynonyms.add("2-Amino-3-(1H-imidazol-4-yl)-propanoic");
        synonyms.put("H", histidineSynonyms);
//Isoleucine
        HashSet<String> isoLeucineSynonyms = new HashSet<>();
        isoLeucineSynonyms.add("2-Amino-3-methylpentanoic");
        synonyms.put("I", isoLeucineSynonyms);
//Leucine
        HashSet<String> leucineSynonyms = new HashSet<>();
        leucineSynonyms.add("2-Amino-4-methylpentanoic");
        synonyms.put("L", leucineSynonyms);
//Lysine
        HashSet<String> lysineSynonyms = new HashSet<>();
        lysineSynonyms.add("2,6-Diaminohexanoic");
        synonyms.put("K", lysineSynonyms);
//Methionine
        HashSet<String> methionineSynonyms = new HashSet<>();
        methionineSynonyms.add("2-Amino-4-(methylthio)butanoic");
        synonyms.put("M", methionineSynonyms);
//Phenylalanine
        HashSet<String> phenylAlanineSynonyms = new HashSet<>();
        phenylAlanineSynonyms.add("2-Amino-3-phenylpropanoic");
        phenylAlanineSynonyms.add("hydroxyphenylanaline");
        synonyms.put("F", phenylAlanineSynonyms);
//Proline
        HashSet<String> prolineSynonyms = new HashSet<>();
        prolineSynonyms.add("Pyrrolidine-2-carboxylic");
        synonyms.put("P", prolineSynonyms);
//Serine
        HashSet<String> serineSynonyms = new HashSet<>();
        serineSynonyms.add("2-Amino-3-hydroxypropanoic");
        synonyms.put("S", serineSynonyms);
//Threonine
        HashSet<String> threonineSynonyms = new HashSet<>();
        threonineSynonyms.add("2-Amino-3-hydroxybutanoic");
        synonyms.put("T", threonineSynonyms);
//Tryptophan
        HashSet<String> tryptophanSynonyms = new HashSet<>();
        tryptophanSynonyms.add("2-Amino-3-(lH-indol-3-yl)-propanoic");
        synonyms.put("W", tryptophanSynonyms);
//Tyrosine
        HashSet<String> TyrosineSynonyms = new HashSet<>();
        TyrosineSynonyms.add("2-Amino-3-(4-hydroxyphenyl)-propanoic");
        TyrosineSynonyms.add("hydroxyphenylalanine");
        synonyms.put("Y", TyrosineSynonyms);
//Valine
        HashSet<String> valineSynonyms = new HashSet<>();
        valineSynonyms.add("2-Amino-3-methylbutanoic");
        synonyms.put("V", valineSynonyms);

//add all "allowed" alternatives according to IUPAC as well
        HashMap<String, HashSet<String>> finalMap = new HashMap<>();
        for (Map.Entry<String, HashSet<String>> synonymMap : synonyms.entrySet()) {
            HashSet<String> tempSet = new HashSet<>();
            HashSet<String> currentSet = synonymMap.getValue();
            tempSet.addAll(currentSet);
            for (String aSynonym : currentSet) {
                String temp = aSynonym
                        .replace("3-carbamoylpropanoic", "succinamic")
                        .replace("pentanedioic", "glutaric")
                        .replace("4-carbamoylbutanoic", "glutaramic")
                        .replace("ethanoic", "acetic")
                        .replace("propanoic", "propionic")
                        .replace("butanoic", "butyric")
                        .replace("pentanoic", "valeric")
                        .replace("butanedioic", "succinic")
                        .replace("3-carbamoylpropanoic", "succinamic")
                        .replace("pentanedioic", "glutaric")
                        .replace("4-carbamoylbutanoic", "glutaramic");
                tempSet.add(temp);
            }
            finalMap.put(synonymMap.getKey(), tempSet);
        }

    }

    private double calculateMassDelta(PTM convertedPTM, PRIDEModification pridePTM) {
        double delta = pridePTM.getMonoDeltaMass() - (convertedPTM.getAtomChainAdded().getMass() + convertedPTM.getAtomChainRemoved().getMass());
        return delta;
    }

    private void checkChemicalLosses(PRIDEModification pridePTM, PTM convertedPTM) {
        //in case of a missmatch
        //double mass_delta = (convertedPTM.getRoundedMass() - pridePTM.getMonoDeltaMass());
        double mass_delta = calculateMassDelta(convertedPTM, pridePTM);
        if (Math.abs(mass_delta) > 1) {
            //check the compatible losses...
            String ptm = pridePTM.getName();
            TreeMap<Double, CommonNeutralMassLoss> massMap = new TreeMap<>();
            for (CommonNeutralMassLoss loss : CommonNeutralMassLoss.values()) {
                String[] identifiers = loss.getIdentifiers().split(",");
                for (String identifier : identifiers) {
                    if (!identifier.trim().isEmpty() && ptm.toLowerCase().contains(identifier.trim())) {
                        if (loss.isCompatible(convertedPTM.getAtomChainAdded())) {
                            subtractAtomChain(convertedPTM, loss.getAtomChain());
                            mass_delta = calculateMassDelta(convertedPTM, pridePTM);
                            if (mass_delta <= 1) {
                                return;
                            }
                        }
                    }
                }
            }

            //if there was no return yet , maybe check all of them?
            //check the losses for the closest one?
            for (CommonNeutralMassLoss loss : CommonNeutralMassLoss.values()) {
                massMap.put(loss.getAtomChain().getMass(), loss);
            }
            for (Map.Entry<Double, CommonNeutralMassLoss> loss : massMap.descendingMap().entrySet()) {
                if (Math.abs(mass_delta) < 1) {
                    return;
                } else if (loss.getValue().isCompatible(convertedPTM.getAtomChainAdded())) {
                    double lostMass = loss.getValue().getAtomChain().getMass();
                    if ((lostMass - Math.abs(mass_delta)) < 1) {
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
        formula = formula
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
                .replace("DEC", " C(10) H(21)")
                .replace("meth", " C(1) H(3)")
                .replace("eth", " C(2) H(5)")
                .replace("ac", " C(2) H(5)")
                .replace("NAc", " N C(2) H(5)")
                .replace("prop", " C(3) H(7)")
                .replace("but", " C(4) H(9)")
                .replace("pent", " C(5) H(11)")
                .replace("hex", " C(6) H(13)")
                .replace("hept", " C(7) H(15)")
                .replace("oct", " C(8) H(17)")
                .replace("non", " C(9) H(19)")
                .replace("dec", " C(10) H(21)");
        Matcher matcher = pattern1.matcher(formula);
        while (matcher.find()) {
            //find the longest match of atoms
            String atomSection = "";
            for (int group = 0; group <= matcher.groupCount(); group++) {
                if (matcher.group(group).trim().length() > atomSection.length()) {
                    atomSection = matcher.group(group).trim();
                }
            }
            if (!atomSection.isEmpty()) {
                Matcher subMatcher = pattern2.matcher(atomSection.trim());
                if (subMatcher.find()) {
                    String[] atomSections = subMatcher.group(0).split(" ");
                    for (String anAtomSection : atomSections) {
                        constructMassForPTM(formula, anAtomSection);
                    }
                } else if (!atomSection.trim().isEmpty()) {
                    constructMassForPTM(formula, atomSection);
                }
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
