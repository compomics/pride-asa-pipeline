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
        if (psi_mod_file == null) {
            psi_mod_file = File.createTempFile("PSI-MOD", ".obo");
            InputStream inputStream = new ClassPathResource("PSI-MOD.obo").getInputStream();
            OutputStream outputStream = new FileOutputStream(psi_mod_file);
            IOUtils.copy(inputStream, outputStream);
            importSynonyms(psi_mod_file.getAbsolutePath());
            psi_mod_file.deleteOnExit();
        }
        if (unimod_file == null) {
            unimod_file = File.createTempFile("unimod", ".obo");
            InputStream inputStream = new ClassPathResource("unimod.obo").getInputStream();
            OutputStream outputStream = new FileOutputStream(unimod_file);
            IOUtils.copy(inputStream, outputStream);
            importSynonyms(unimod_file.getAbsolutePath());
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
        LOGGER.info("Extending library with known mods...");
        //add additional known mods
        synonymMapping.put("oxidation of m", "oxidation");
    }
    
    public ArrayList<String> lookupRealModNames(String modTerm) {
        ArrayList<String> realMods = new ArrayList<>();
        //check special cases
        if (modTerm.toLowerCase().equals("carboxyamidomethylation")) {
            realMods.add("carbamidomethyl");
        } else if (modTerm.toLowerCase().contains("carboxy")) {
            realMods.add("carboxymethyl c");
        } else if (modTerm.equalsIgnoreCase("oxidation")) {
            realMods.add("oxidation of m");
        } else if (modTerm.toLowerCase().contains("4traq")) {
            realMods.add("iTRAQ4plex");
        } else if (modTerm.toLowerCase().contains("8traq")) {
            realMods.add("iTRAQ8plex");
        } else if (modTerm.toLowerCase().contains("tmt")) {
            realMods.add("tmtduplex");
        } else {
            //if not special case, look for a synonym
            String realModName = synonymMapping.get(modTerm.trim());
            if (realModName != null) {
                realMods.add(realModName);
            }
        }
        return realMods;
        
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
        //set the variable mods that should be fixed as FIXED
        HashMap<String, Boolean> temp = new HashMap<>();
        for (String aMod : modificationMap.keySet()) {
            temp.put(aMod, isFixedPtm(aMod));
        }
        //continue
        for (String aModificationName : temp.keySet()) {
            if (!addToProfile(modProfile, temp, unknownPtms, aModificationName)) {
                ArrayList<String> synonyms = lookupRealModNames(aModificationName);
                if (!synonyms.isEmpty()) {
                    for (String anAlternativeModName : lookupRealModNames(aModificationName)) {
                        addToProfile(modProfile, temp, unknownPtms, anAlternativeModName);
                    }
                }
            }
        }
        //check
        ArrayList<String> tempMods = modProfile.getAllModifications();
        for (String aVariableModification : tempMods) {
            if (isFixedPtm(aVariableModification)) {
                modProfile.removeVariableModification(aVariableModification);
                modProfile.addFixedModification(modProfile.getPtm(aVariableModification));
            }
        }
        return modProfile;
    }
    
    private boolean addToProfile(ModificationProfile modProfile, HashMap<String, Boolean> modificationMap, ArrayList<String> unknownPtms, String aModificationName) {
        boolean added = false;
        if (factory.containsPTM(aModificationName)) {
            PTM loadedCorrespondingPtm = factory.getPTM(aModificationName);
            if (loadedCorrespondingPtm != null) {
                LOGGER.info(aModificationName + " was found in utilities!");
                if (isFixedPtm(loadedCorrespondingPtm.getName())) {
                    modProfile.addFixedModification(loadedCorrespondingPtm);
                } else {
                    modProfile.addVariableModification(loadedCorrespondingPtm);
                }
                added = true;
            }
        } else {
            String convertPridePtm = factory.convertPridePtm(aModificationName, modProfile, unknownPtms, modificationMap.getOrDefault(aModificationName, false));
            if (convertPridePtm != null && !convertPridePtm.isEmpty()) {
                LOGGER.info(aModificationName + " was found in utilities after conversion!");
                added = true;
            } else {
                LOGGER.info(aModificationName + " was not found in utilities!");
            }
        }
        return added;
    }

    /**
     * Returns true if the PTM is assumed to be fixed, false otherwise.
     *
     * @param pridePtmName the PTM to check
     * @return true if the PTM is assumed to be fixed, false otherwise.
     */
    private boolean isFixedPtm(String pridePtmName) {
        boolean fixedPtm = false;
        if (pridePtmName.toLowerCase().contains("carbamidomethyl")
                || pridePtmName.equalsIgnoreCase("S-carboxamidomethyl-L-cysteine")
                || pridePtmName.equalsIgnoreCase("iodoacetamide - site C")
                || pridePtmName.equalsIgnoreCase("iodoacetamide derivatized residue")
                || pridePtmName.equalsIgnoreCase("Iodoacetamide derivative")
                || pridePtmName.equalsIgnoreCase("Carboxymethyl")
                || pridePtmName.equalsIgnoreCase("S-carboxymethyl-L-cysteine")
                || pridePtmName.equalsIgnoreCase("iodoacetic acid derivatized residue")) {
            fixedPtm = true;
        }
        
        return fixedPtm;
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
     * Tries to determine the correct amino acid position for certain
     * modificationtypes in pride asap
     *
     * @param modification the modification rendered by pride asap
     * @param type modificationtype (example : oxidation)
     * @return a list with the corrected modification names per amino acid
     */
    public List<String> correctPrideAsapPositions(Modification modification, RepositioningModificationType type) {
        LOGGER.info("Correcting positions for " + modification.getName());
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
    
    public ArrayList<String> correctPrideAsapModName(Modification modification) {
        ArrayList<String> newNames = lookupRealModNames(modification.getName());
        return newNames;
    }
    
    public ModificationProfile removeDuplicateMasses(ModificationProfile modProfile, double precursorMassAcc) {
        TreeMap<Double, String> massToModMap = new TreeMap<>();
        for (String aModName : modProfile.getAllModifications()) {
            massToModMap.put(modProfile.getPtm(aModName).getMass(), aModName);
        }
        if (!massToModMap.isEmpty()) {
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
        }

        //special cases that are impossible !
        if (modProfile.getFixedModifications().contains("carbamidomethyl c") && modProfile.getVariableModifications().contains("carboxymethyl c")) {
            modProfile.removeVariableModification("carboxymethyl c");
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
        
        Modification modification = new Modification(accessionValue, monoIsotopicMassShift, averageMassShift, location, EnumSet.noneOf(AminoAcid.class
        ), modificationItem.getModAccession(), accessionValue);
        modification.getAffectedAminoAcids()
                .add(AminoAcid.getAA(peptideSequence.substring(sequenceIndex, sequenceIndex + 1)));
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
        
        double monoIsotopicMassShift = modificationItem.getMonoisotopicMassDelta();
        //if average mass shift is empty, use the monoisotopic mass.
        double averageMassShift;
        try {
            averageMassShift = modificationItem.getAvgMassDelta();
        } catch (NullPointerException e) {
            LOGGER.warn("Average mass shift not found, setting to mono isotopic mass shift");
            averageMassShift = monoIsotopicMassShift;
        }
        String accession = modificationItem.getCvParam().get(0).getAccession();
        String accessionValue = modificationItem.getCvParam().get(0).getValue();
        Modification modification = new Modification(accessionValue,
                monoIsotopicMassShift,
                averageMassShift,
                location,
                EnumSet.noneOf(AminoAcid.class
                ),
                accession, accessionValue);
        modification.getAffectedAminoAcids()
                .add(AminoAcid.getAA(peptideSequence.substring(sequenceIndex, sequenceIndex + 1)));
        modification.setOrigin(Modification.Origin.PRIDE);
        return modification;
    }
    
    public void clear() {
        synonymMapping.clear();
    }
    
}
