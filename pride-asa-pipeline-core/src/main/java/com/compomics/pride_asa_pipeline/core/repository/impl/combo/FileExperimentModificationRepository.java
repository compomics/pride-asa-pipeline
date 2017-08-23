package com.compomics.pride_asa_pipeline.core.repository.impl.combo;

import com.compomics.pride_asa_pipeline.core.logic.inference.InferenceStatistics;
import com.compomics.pride_asa_pipeline.model.modification.impl.AsapModificationAdapter;
import com.compomics.pride_asa_pipeline.model.modification.source.PRIDEModificationFactory;
import com.compomics.pride_asa_pipeline.core.repository.ModificationRepository;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileExperimentRepository;
import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.ParameterExtractionException;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.CachedDataAccessController;
import uk.ac.ebi.pride.utilities.data.core.SpectrumIdentification;

/**
 * This class combines a FileExperimentRepository and a
 * FileModificationRepository to help cache the file contents in memory and
 * reduce the need to reparse the file
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
     * In memory list of mod occurences
     */
    private final HashMap<uk.ac.ebi.pride.utilities.data.core.Modification, Integer> modMapping = new HashMap<>();
    /**
     * boolean indicating experiments have been loaded before
     */
    private boolean experimentLoaded = false;
    //make a singleton to fix multi passing

    public static FileExperimentModificationRepository INSTANCE = new FileExperimentModificationRepository();

    private List<Identification> identifications = new ArrayList<>();

    public static FileExperimentModificationRepository getInstance() {
        return INSTANCE;
    }

    protected FileExperimentModificationRepository() {

    }

    /*private FileExperimentModificationRepository(String experimentIdentifier) {
        this.experimentIdentifier = experimentIdentifier;
    }*/
    @Override
    public List<Identification> loadExperimentIdentifications(String experimentAccession) {
        if (!isExperimentLoaded(experimentAccession)) {
            setExperimentIdentifier(experimentAccession);
            identifications.clear();
            try {
                CachedDataAccessController parser = parserCache.getParser(experimentAccession, false);
                //get all the peptide ids for the proteins
                long proteinCount = parser.getProteinIds().size();
                double completeRatio = 0.0;
                double currentCount = 0;
                double currentPrint = 0;
                Collection<Comparable> proteinIds = parser.getProteinIds();
                for (Comparable aProteinID : proteinIds) {
                    completeRatio = 100 * currentCount / proteinCount;
                    if (completeRatio > currentPrint) {
                        LOGGER.info(InferenceStatistics.round(completeRatio, 0) + "%");
                        currentPrint += 10;
                    }
                    Collection<Comparable> peptideIds = parser.getPeptideIds(aProteinID);
                    for (Comparable aPeptideID : peptideIds) {
                        uk.ac.ebi.pride.utilities.data.core.Peptide aPeptide = parser.getPeptideByIndex(aProteinID, aPeptideID);
                        // do the mods
                        for (uk.ac.ebi.pride.utilities.data.core.Modification aMod : aPeptide.getModifications()) {
                            modMapping.put(aMod, modMapping.getOrDefault(aMod, 0) + 1);
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
                                    String.valueOf(aPeptideID),
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

            } catch (TimeoutException | InterruptedException | ExecutionException ex) {
                LOGGER.error("The parser timed out before it could deliver all identifications !");
            }
        }
        experimentLoaded = true;
        return identifications;
    }

    @Override
    public List<Modification> getModificationsByPeptideId(long peptideID) {
        List<Modification> modificationList = new ArrayList<>();
        try {
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
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            LOGGER.error("The parser timed out before it could deliver all the modifications");
        }
        return modificationList;
    }

    @Override
    public List<Modification> getModificationsByExperimentId(String experimentId) {

        List<Modification> modificationList = new ArrayList<>();
        if (!experimentLoaded) {
            loadExperimentIdentifications(experimentId);
        }
        for (uk.ac.ebi.pride.utilities.data.core.Modification aMod : modMapping.keySet()) {
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
        return modificationList;


        /*         if (modMapping.isEmpty()) {
            try {
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
            } catch (TimeoutException | InterruptedException | ExecutionException ex) {
                LOGGER.error("The parser timed out before it could deliver all the modifications");
            }
        } else {
            for (Map.Entry<Comparable, List<Modification>> aPeptide : modMapping.entrySet()) {
                for (Modification mod : aPeptide.getValue()) {
                    if (!modificationList.contains(mod)) {
                        modificationList.add(mod);
                    }
                }
            }
        }
                return modificationList;*/
    }

    public void setExperimentIdentifier(String assay) {
        if (experimentIdentifier != assay) {
            experimentLoaded = false;
            this.experimentIdentifier = assay;
            INSTANCE = this;
        }
    }

    public boolean isExperimentLoaded(String assay) {
        return assay == this.experimentIdentifier && experimentLoaded;
    }

}
