package com.compomics.pride_asa_pipeline.core.repository.impl.combo;

import com.compomics.pride_asa_pipeline.core.exceptions.ParameterExtractionException;
import com.compomics.pride_asa_pipeline.core.logic.inference.InferenceStatistics;
import com.compomics.pride_asa_pipeline.core.model.modification.impl.AsapModificationAdapter;
import com.compomics.pride_asa_pipeline.core.model.modification.source.PRIDEModificationFactory;
import com.compomics.pride_asa_pipeline.core.repository.ModificationRepository;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileExperimentRepository;
import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.CachedDataAccessController;
import uk.ac.ebi.pride.utilities.data.core.SpectrumIdentification;

/**
 *
 * @author Kenneth Verheggen
 */
public class FileExperimentModificationRepository extends FileExperimentRepository implements ModificationRepository {

    /**
     * The identifier for the current repository (filename or assay accession)
     */
    private String experimentIdentifier;
    /**
     * A logger instance
     */
    private static final Logger LOGGER = Logger.getLogger(FileExperimentModificationRepository.class);
    /**
     * The modification adapter to return pride asap modificaitons
     */
    private final AsapModificationAdapter adapter = new AsapModificationAdapter();
    /**
     * The UniMod modification factory for all modifications used in PRIDE
     */
    private final PRIDEModificationFactory modFactory = PRIDEModificationFactory.getInstance();
    /**
     * In memory list of peptides and modifications
     */
    private final HashMap<Comparable, List<Modification>> modMapping = new HashMap<>();

    public FileExperimentModificationRepository(){
        
    }
    
    public FileExperimentModificationRepository(String experimentIdentifier) {
        this.experimentIdentifier = experimentIdentifier;
    }

    @Override
    public List<Identification> loadExperimentIdentifications(String experimentAccession) {
        List<Identification> identifications = new ArrayList<>();
        CachedDataAccessController parser = parserCache.getParser(experimentAccession, false);
        //get all the peptide ids for the proteins
        long proteinCount = parser.getProteinIds().size();
        double completeRatio = 0.0;
        double currentCount = 0;
        double currentPrint = 0;
        for (Comparable aProteinID : parser.getProteinIds()) {
            completeRatio = 100 * currentCount / proteinCount;
            if (completeRatio > currentPrint) {
                LOGGER.info(InferenceStatistics.round(completeRatio, 0) + "%");
                currentPrint += 10;
            }

            for (Comparable aPeptideID : parser.getPeptideIds(aProteinID)) {
                uk.ac.ebi.pride.utilities.data.core.Peptide aPeptide = parser.getPeptideByIndex(aProteinID, aPeptideID);
                // do the mods
                List<Modification> modificationList = modMapping.getOrDefault(aPeptideID, new ArrayList<>());
                for (uk.ac.ebi.pride.utilities.data.core.Modification aMod : aPeptide.getModifications()) {
                    try {
                        modificationList.add((Modification) modFactory.getModification(adapter, aMod.getName()));
                    } catch (ParameterExtractionException ex) {
                        LOGGER.error("Could not load " + aMod.getName() + " .Reason :" + ex);
                    }
                }
                //don't put empty lists in the mapping to lower stress on memory 
                if (!modificationList.isEmpty()) {
                    modMapping.put(aPeptideID, modificationList);
                }
                //do the identification
                SpectrumIdentification spectrumIdentification = aPeptide.getSpectrumIdentification();
                try {
                    int charge = spectrumIdentification.getChargeState();
                    double mz = spectrumIdentification.getExperimentalMassToCharge();
                    AminoAcidSequence aaSequence = new AminoAcidSequence(aPeptide.getPeptideSequence().getSequence());
                    Peptide peptide = new Peptide(charge, mz, aaSequence);
                    Identification identification = new Identification(
                            //@TODO is this correct?
                            peptide,
                            String.valueOf(spectrumIdentification.getSpectrum().getIndex()),
                            String.valueOf(spectrumIdentification.getSpectrum().getId()),
                            spectrumIdentification.getName());
                    identifications.add(identification);
                } catch (UnknownAAException ex) {
                    LOGGER.error(ex);
                }
                aPeptide = null;
            }
            currentCount++;
        }
        LOGGER.info("100% Completion!");
        //get all evidence for all peptide ids
        //     parser.close();
        return identifications;
    }

    @Override
    public List<Modification> getModificationsByPeptideId(long peptideID) {
        List<Modification> modificationList = new ArrayList<>();
        if (modMapping.isEmpty()) {
            CachedDataAccessController parser = parserCache.getParser(experimentIdentifier, true);
            for (Comparable proteinID : parser.getProteinIds()) {
                List<uk.ac.ebi.pride.utilities.data.core.Modification> mods = parser.getPTMs(proteinID, peptideID);
                for (uk.ac.ebi.pride.utilities.data.core.Modification aMod : mods) {
                    try {
                        modificationList.add((Modification) modFactory.getModification(adapter, aMod.getName()));
                    } catch (ParameterExtractionException ex) {
                        LOGGER.error("Could not load " + aMod.getName() + " .Reason :" + ex);
                    }
                }
            }
        } else {
            modificationList.addAll(modMapping.getOrDefault(peptideID, new ArrayList<Modification>()));
        }
        return modificationList;
    }

    @Override
    public List<Modification> getModificationsByExperimentId(String experimentId) {
        List<Modification> modificationList = new ArrayList<>();
        if (modMapping.isEmpty()) {
            CachedDataAccessController parser = parserCache.getParser(experimentId, true);
            for (Comparable proteinID : parser.getProteinIds()) {
                for (Comparable peptideID : parser.getPeptideIds(proteinID)) {
                    List<uk.ac.ebi.pride.utilities.data.core.Modification> mods = parser.getPTMs(proteinID, peptideID);
                    for (uk.ac.ebi.pride.utilities.data.core.Modification aMod : mods) {
                        if (aMod != null && aMod.getName() != null) {
                            Object modification = null;
                            try {
                                modification = modFactory.getModification(adapter, aMod.getName());
                            } catch (ParameterExtractionException ex) {
                                LOGGER.error("Could not load " + aMod.getName() + " .Reason :" + ex);
                            }
                            if (modification != null) {
                                modificationList.add((Modification) modification);
                            } else {
                                LOGGER.warn(aMod.getName() + " was not found in the modifications");
                            }
                        }
                    }
                }
            }
        } else {
            for (Map.Entry<Comparable, List<Modification>> aPeptide : modMapping.entrySet()) {
                modificationList.addAll(aPeptide.getValue());
            }
        }
        return modificationList;
    }

    public void setExperimentIdentifier(String assay) {
        this.experimentIdentifier = assay;
    }

}
