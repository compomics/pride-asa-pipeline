/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.modification;

import com.compomics.pride_asa_pipeline.model.AminoAcid;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.preferences.ModificationProfile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.geneontology.oboedit.dataadapter.DefaultGOBOParser;
import org.geneontology.oboedit.dataadapter.GOBOParseEngine;
import org.geneontology.oboedit.dataadapter.GOBOParseException;
import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBOSession;
import org.springframework.core.io.ClassPathResource;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Kenneth Verheggen
 */
public class PTMMapper {

    private static PTMFactory factory;
    private static PTMMapper ptmMapper;
    private static final HashMap<String, String> synonymMapping = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger(PTMMapper.class);
    private static File tempSearchGuiModFile;
    private File psi_mod_file;
    private File unimod_file;

    private PTMMapper() {

    }

    public HashMap<String, String> getSynonymMapping() {
        return synonymMapping;
    }

    public static PTMMapper getInstance() throws XmlPullParserException, IOException, GOBOParseException {
        if (ptmMapper == null) {
            loadSearchGUImodificationFile();
            factory = PTMFactory.getInstance();
            factory.clearFactory();
            factory.importModifications(tempSearchGuiModFile, false);
            //factory.importModifications(respProps.getUserModFile(), true, true);
            ptmMapper = new PTMMapper();
            ptmMapper.loadDictionaries();
        }
        return ptmMapper;
    }

    private static void loadSearchGUImodificationFile() throws IOException {
        if (tempSearchGuiModFile == null) {
            tempSearchGuiModFile = File.createTempFile("searchGUI_mods", ".xml");
            InputStream inputStream = new ClassPathResource("searchGUI_mods.xml").getInputStream();
            OutputStream outputStream = new FileOutputStream(tempSearchGuiModFile);
            IOUtils.copy(inputStream, outputStream);
            tempSearchGuiModFile.deleteOnExit();
        }
    }

    private void loadDictionaries() throws IOException, GOBOParseException {
        LOGGER.info("Loading modification mapping dictionaries");
        if (psi_mod_file == null) {
            psi_mod_file = File.createTempFile("PSI-MOD", ".obo");
            InputStream inputStream = new ClassPathResource("PSI-MOD.obo").getInputStream();
            OutputStream outputStream = new FileOutputStream(psi_mod_file);
            IOUtils.copy(inputStream, outputStream);
            importSynonyms(psi_mod_file.getAbsolutePath());
            LOGGER.info("Loading psimod dictionary");
            psi_mod_file.deleteOnExit();
        }
        if (unimod_file == null) {
            unimod_file = File.createTempFile("unimod", ".obo");
            InputStream inputStream = new ClassPathResource("unimod.obo").getInputStream();
            OutputStream outputStream = new FileOutputStream(unimod_file);
            IOUtils.copy(inputStream, outputStream);
            importSynonyms(unimod_file.getAbsolutePath());
            LOGGER.info("Loading unimod dictionary");
            unimod_file.deleteOnExit();
        }
    }

    private void importSynonyms(String path) throws IOException, GOBOParseException {
        DefaultGOBOParser parser = new DefaultGOBOParser();
        GOBOParseEngine engine = new GOBOParseEngine(parser);
        Collection paths = new LinkedList();
        paths.add(path);
        engine.setPaths(paths);
        engine.parse();
        OBOSession session = parser.getSession();
        Map allTermsHash = session.getAllTermsHash();
        for (Object aTerm : allTermsHash.keySet()) {
            OBOClass term = session.getTerm(aTerm.toString());
            for (Object aSynonym : term.getSynonyms()) {
                synonymMapping.put(aSynonym.toString(), term.getName());
            }
        }
    }

    public String lookupRealModName(String modTerm) {
        String realModName = synonymMapping.get(modTerm);
        if (realModName == null) {
            realModName = modTerm;
        }
        return realModName;
    }

    /**
     * Processes all mods before they are set to the modification profile
     * modification profile. Unknown PTMs are added to the unknown PTMs
     * arraylist.
     *
     * @param modificationMap hashmap containing modification names and their
     * fixed/variable status
     * @param modProfile the modification profile to add the PTmMs to
     * @param unknownPtms the list of unknown PTMS, updated during this method
     * @return the filled up modification profile
     */
    public ModificationProfile buildTotalModProfile(HashMap<String, Boolean> modificationMap, ArrayList<String> unknownPtms) {
        ModificationProfile modProfile = new ModificationProfile();
        for (String aModificationName : modificationMap.keySet()) {
            if (factory.containsPTM(aModificationName)) {
                PTM loadedCorrespondingPtm = factory.getPTM(aModificationName);
                if (modificationMap.get(aModificationName)) {
                    modProfile.addFixedModification(loadedCorrespondingPtm);
                } else {
                    modProfile.addVariableModification(loadedCorrespondingPtm);
                }
            } else {
                convertPridePtm(aModificationName, modProfile, unknownPtms, modificationMap.get(aModificationName));
            }
        }
        return modProfile;
    }

    /**
     * Processes all mods before they are set to the modification profile
     * modification profile. Unknown PTMs are added to the unknown PTMs
     * arraylist.
     *
     * @param modificationMap hashmap containing modification names and their
     * fixed/variable status
     * @param modProfile the modification profile to add the PTmMs to
     * @param precursorAcc the precursor accuraccy to consider the possible
     * duplicates in
     * @param unknownPtms the list of unknown PTMS, updated during this method
     * @return the filled up modification profile
     */
    public ModificationProfile buildUniqueMassModProfile(HashMap<String, Boolean> modificationMap, ArrayList<String> unknownPtms, double precursorAcc) {
        return removeDuplicateMasses(buildTotalModProfile(modificationMap, unknownPtms), precursorAcc);
    }

    /**
     * Tries to convert a PRIDE PTM to utilities PTM name, and add it to the
     * modification profile. Unknown PTMs are added to the unknown PTMs
     * arraylist.
     *
     * @param pridePtmName the PRIDE PTM name
     * @param modProfile the modification profile to add the PTmMs to
     * @param unknownPtms the list of unknown PTMS, updated during this method
     * @param isFixed if true, the PTM will be added as a fixed modification
     */
    private void convertPridePtm(String pridePtmName, ModificationProfile modProfile, ArrayList<String> unknownPtms, boolean isFixed) {

        // special cases for when multiple ptms are needed
        if (pridePtmName.toLowerCase().indexOf("itraq") != -1) {
            if (pridePtmName.indexOf("8") != -1) {
                modProfile.addFixedModification(factory.getPTM("itraq8plex:13c(6)15n(2) on k"));
                modProfile.addFixedModification(factory.getPTM("itraq8plex:13c(6)15n(2) on nterm"));
                modProfile.addVariableModification(factory.getPTM("itraq8plex:13c(6)15n(2) on y"));
            } else {
                //this is itraq4
                modProfile.addFixedModification(factory.getPTM("itraq114 on k"));
                modProfile.addFixedModification(factory.getPTM("itraq114 on nterm"));
                modProfile.addVariableModification(factory.getPTM("itraq114 on y"));
            }
        } else if (pridePtmName.toLowerCase().indexOf("tmt") != -1) {
            if (pridePtmName.indexOf("6") != -1) {
                modProfile.addFixedModification(factory.getPTM("tmt 6-plex on k"));
                modProfile.addFixedModification(factory.getPTM("tmt 6-plex on n-term peptide"));
            } else {
                modProfile.addFixedModification(factory.getPTM("tmt duplex on k"));
                modProfile.addFixedModification(factory.getPTM("tmt duplex on n-term peptide"));
            }
        } else if (pridePtmName.toLowerCase().indexOf("phospho") != -1) {
            modProfile.addVariableModification(factory.getPTM("phosphorylation of s"));
            modProfile.addVariableModification(factory.getPTM("phosphorylation of t"));
            modProfile.addVariableModification(factory.getPTM("phosphorylation of y"));
        } else if (pridePtmName.toLowerCase().indexOf("palmitoylation") != -1) {
            modProfile.addVariableModification(factory.getPTM("palmitoylation of c"));
            modProfile.addVariableModification(factory.getPTM("palmitoylation of k"));
            modProfile.addVariableModification(factory.getPTM("palmitoylation of s"));
            modProfile.addVariableModification(factory.getPTM("palmitoylation of t"));
        } else if (pridePtmName.toLowerCase().indexOf("formylation") != -1) {
            modProfile.addVariableModification(factory.getPTM("formylation of k"));
            modProfile.addVariableModification(factory.getPTM("formylation of peptide n-term"));
            modProfile.addVariableModification(factory.getPTM("formylation of protein c-term"));
        } else if (pridePtmName.toLowerCase().indexOf("carbamylation") != -1) {
            modProfile.addVariableModification(factory.getPTM("carbamylation of k"));
            modProfile.addVariableModification(factory.getPTM("carbamylation of n-term peptide"));
        } else {
            // single ptm mapping
            String utilitiesPtmName = convertPridePtmToUtilitiesPtm(pridePtmName);
            if (utilitiesPtmName.equalsIgnoreCase("unknown")) {
                utilitiesPtmName = convertPridePtmToUtilitiesPtm(lookupRealModName(pridePtmName));
            }
            if (utilitiesPtmName.equalsIgnoreCase("unknown")) {
                unknownPtms.add(pridePtmName);
            }
            if (!modProfile.contains(utilitiesPtmName)) {
                if (isFixed) {
                    modProfile.addFixedModification(factory.getPTM(utilitiesPtmName));
                } else {
                    modProfile.addVariableModification(factory.getPTM(utilitiesPtmName));
                }
            }
        }
    }

    /**
     * Tries to convert a PRIDE PTM name to utilities PTM name.
     *
     * @param pridePtmName the PRIDE PTM name
     * @return the utilities PTM name, or null if there is no mapping
     */
    private String convertPridePtmToUtilitiesPtm(String pridePtmName) {

        if (pridePtmName.toLowerCase().indexOf("carbamidomethyl") != -1
                || pridePtmName.toLowerCase().indexOf("iodoacetamide") != -1) {
            return "carbamidomethyl c";
        } else if (pridePtmName.equalsIgnoreCase("oxidation")) {
            return "oxidation of m";
        } else if (pridePtmName.toLowerCase().indexOf("acetylation") != -1) {
            return "acetylation of k";
        } else if (pridePtmName.toLowerCase().indexOf("amidation") != -1) {
            return "amidation of peptide c-term";
        } else if (pridePtmName.toLowerCase().indexOf("carboxymethyl") != -1) {
            return "carboxymethyl c";
        } else if (pridePtmName.toLowerCase().indexOf("farnesylation") != -1) {
            return "farnesylation of c";
        } else if (pridePtmName.toLowerCase().indexOf("geranyl-geranyl") != -1) {
            return "geranyl-geranyl";
        } else if (pridePtmName.toLowerCase().indexOf("guanidination") != -1) {
            return "guanidination of k";
        } else if (pridePtmName.toLowerCase().indexOf("homoserine") != -1) {
            if (pridePtmName.toLowerCase().indexOf("lacton") != -1) {
                return "homoserine lactone";
            } else {
                return "homoserine";
            }
            //TODO ---> CHECK THE ICAT
        } else if (pridePtmName.equalsIgnoreCase("ICAT-C")) {
            return "icat light";
        } else if (pridePtmName.equalsIgnoreCase("ICAT-C:13C(9)")) {
            return "icat heavy";
        } else if (pridePtmName.toLowerCase().indexOf("lipoyl") != -1) {
            return "lipoyl k";
        } else if (pridePtmName.toLowerCase().indexOf("methylthio") != -1) {
            return "beta-methylthiolation of d (duplicate of 13)";
        } else if (pridePtmName.toLowerCase().indexOf("nipcam") != -1) {
            return "nipcam";
        } else if (pridePtmName.toLowerCase().indexOf("phosphopantetheine") != -1) {
            return "phosphopantetheine s";
        } else if (pridePtmName.toLowerCase().indexOf("propionamide") != -1) {
            return "propionamide c";
        } else if (pridePtmName.toLowerCase().indexOf("pyridylethyl") != -1) {
            return "s-pyridylethylation of c";
        } else if (pridePtmName.toLowerCase().indexOf("sulfo") != -1) {
            return "sulfation of y"; // not completely sure about this one...
        } else if (pridePtmName.toLowerCase().indexOf("dehydratation") != -1) {
            return "dehydro of s and t";
        } else if (pridePtmName.toLowerCase().indexOf("deamination") != -1) {
            return "deamidation of n and q"; // not that this does not separate between deamidation on only n and deamidation on n and q
        } else if (pridePtmName.toLowerCase().indexOf("dioxidation") != -1) {
            return "sulphone of m";
        } else {
            return "unknown";
        }
    }

    /**
     * Tries to determine the correct amino acid position for certain
     * modificationtypes in pride asap
     *
     * @param modification the modification rendered by pride asap
     * @param type modificationtype (example : oxidation)
     * @return a list with the corrected modification names per amino acid
     */
    public List<String> correctPrideAsapPositions(Modification modification, RepositioningModificationType type) {
        List<String> asapMods = new ArrayList<>();
        //Get the affected amino acids
        Set<AminoAcid> affectedAminoAcids = modification.getAffectedAminoAcids();
        //See what the amino acid would bes
        for (AminoAcid anAffectedAminoAcid : affectedAminoAcids) {
            String correctModName = type.getPrefix() + anAffectedAminoAcid.letter();
            asapMods.add(correctModName);
        }
        return asapMods;
    }

    public String correctPrideAsapModName(Modification modification) {
        String newName = lookupRealModName(modification.getName());
        return newName;
    }

    public ModificationProfile removeDuplicateMasses(ModificationProfile modProfile, double precursorMassAcc) {
        TreeMap<Double, String> massToModMap = new TreeMap<>();
        for (String aModName : modProfile.getAllModifications()) {
            massToModMap.put(modProfile.getPtm(aModName).getMass(), aModName);
        }
        double previousMass = massToModMap.firstKey() - precursorMassAcc;
        for (Double aModMass : massToModMap.keySet()) {
            if (Math.abs(aModMass - previousMass) < precursorMassAcc) {
                String originalModification = massToModMap.get(previousMass);
                String duplicateModification = massToModMap.get(aModMass);
                if (originalModification != null) {
                    System.out.println("Duplicate masses found : " + originalModification + "(" + previousMass + ")"
                            + " vs " + duplicateModification + "(" + aModMass + ")");
                    if (modProfile.getFixedModifications().contains(duplicateModification)) {
                        modProfile.removeFixedModification(duplicateModification);
                    } else {
                        modProfile.removeVariableModification(duplicateModification);
                    }
                }
            }
            previousMass = aModMass;
        }

        return modProfile;
    }

    /**
     * Map a ModificationItem onto the pipeline Modification
     *
     * @param modificationItem the ModificationItem
     * @param peptideSequence the peptide sequence
     * @return the mapped modification
     */
    public static Modification mapModification(uk.ac.ebi.pride.jaxb.model.ModificationItem modificationItem, String peptideSequence) {

        Integer modificationLocation = modificationItem.getModLocation().intValue();
        Modification.Location location;
        int sequenceIndex;
        if (modificationLocation == 0) {
            location = Modification.Location.N_TERMINAL;
            sequenceIndex = 0;
        } else if (0 < modificationLocation && modificationLocation < (peptideSequence.length() + 1)) {
            location = Modification.Location.NON_TERMINAL;
            sequenceIndex = modificationLocation - 1;
        } else if (modificationLocation == (peptideSequence.length() + 1)) {
            location = Modification.Location.C_TERMINAL;
            sequenceIndex = peptideSequence.length() - 1;
        } else {
            //in this case, return null for the modification
            return null;
        }

        double monoIsotopicMassShift = (modificationItem.getModMonoDelta().isEmpty()) ? 0.0 : Double.parseDouble(modificationItem.getModMonoDelta().get(0));
        //if average mass shift is empty, use the monoisotopic mass.
        double averageMassShift = (modificationItem.getModAvgDelta().isEmpty()) ? monoIsotopicMassShift : Double.parseDouble(modificationItem.getModAvgDelta().get(0));
        String accessionValue = (modificationItem.getAdditional().getCvParamByAcc(modificationItem.getModAccession()) == null) ? modificationItem.getModAccession() : modificationItem.getAdditional().getCvParamByAcc(modificationItem.getModAccession()).getName();

        Modification modification = new Modification(accessionValue, monoIsotopicMassShift, averageMassShift, location, EnumSet.noneOf(AminoAcid.class), modificationItem.getModAccession(), accessionValue);
        modification.getAffectedAminoAcids().add(AminoAcid.getAA(peptideSequence.substring(sequenceIndex, sequenceIndex + 1)));
        modification.setOrigin(Modification.Origin.PRIDE);
        return modification;
    }

    /**
     * Map a Modification from mzIdentMl onto the pipeline Modification
     *
     * @param modificationItem the ModificationItem
     * @param peptideSequence the peptide sequence
     * @return the mapped modification
     */
    public static Modification mapModificationWithParameters(uk.ac.ebi.jmzidml.model.mzidml.Modification modificationItem, String peptideSequence) {
        Integer modificationLocation = modificationItem.getLocation();
        Modification.Location location;
        int sequenceIndex;

        if (modificationLocation == 0) {
            location = Modification.Location.N_TERMINAL;
            sequenceIndex = 0;
        } else if (0 < modificationLocation && modificationLocation < (peptideSequence.length() + 1)) {
            location = Modification.Location.NON_TERMINAL;
            sequenceIndex = modificationLocation - 1;
        } else if (modificationLocation == (peptideSequence.length() + 1)) {
            location = Modification.Location.C_TERMINAL;
            sequenceIndex = peptideSequence.length() - 1;
        } else {
            //in this case, return null for the modification
            return null;
        }

        double monoIsotopicMassShift = (modificationItem.getMonoisotopicMassDelta());
        //if average mass shift is empty, use the monoisotopic mass.
        double averageMassShift;
        try {
            averageMassShift = modificationItem.getAvgMassDelta();
        } catch (NullPointerException e) {
            LOGGER.error("Average mass shift not found, setting to mono isotopic mass shift");
            averageMassShift = monoIsotopicMassShift;
        }
        String accession = modificationItem.getCvParam().get(0).getAccession();
        String accessionValue = modificationItem.getCvParam().get(0).getValue();
        Modification modification = new Modification(accessionValue,
                monoIsotopicMassShift,
                averageMassShift,
                location,
                EnumSet.noneOf(AminoAcid.class),
                accession, accessionValue);
        modification.getAffectedAminoAcids().add(AminoAcid.getAA(peptideSequence.substring(sequenceIndex, sequenceIndex + 1)));
        modification.setOrigin(Modification.Origin.PRIDE);
        return modification;
    }

}
