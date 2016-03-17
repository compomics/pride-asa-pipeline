package com.compomics.pride_asa_pipeline.core.repository.impl.file;

import com.compomics.pride_asa_pipeline.core.exceptions.ParameterExtractionException;
import com.compomics.pride_asa_pipeline.core.model.ParserCacheConnector;
import com.compomics.pride_asa_pipeline.core.model.modification.impl.AsapModificationAdapter;
import com.compomics.pride_asa_pipeline.core.model.modification.source.PRIDEModificationFactory;
import com.compomics.pride_asa_pipeline.core.repository.ModificationRepository;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.CachedDataAccessController;

/**
 *
 * @author Kenneth Verheggen
 */
public class FileModificationRepository extends ParserCacheConnector implements ModificationRepository {

    /**
     * The identifier for the current repository (filename or assay accession)
     */
    private String experimentIdentifier;
    /**
     * A logger instance
     */
    private static final Logger LOGGER = Logger.getLogger(FileModificationRepository.class);
    /**
     * The modification adapter to return pride asap modificaitons
     */
    private final AsapModificationAdapter adapter = new AsapModificationAdapter();
    /**
     * The UniMod modification factory for all modifications used in PRIDE
     */
    private final PRIDEModificationFactory modFactory = PRIDEModificationFactory.getInstance();

    public FileModificationRepository(String experimentIdentifier) {
        this.experimentIdentifier = experimentIdentifier;
    }

    public FileModificationRepository() {

    }

    public String getExperimentIdentifier() {
        return experimentIdentifier;
    }

    public void setExperimentIdentifier(String experimentIdentifier) {
        this.experimentIdentifier = experimentIdentifier;
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
            //@TODO handle peptideshaker projects
            //peptideshaker projects are a special case as it stores some mods in the user params...how can we deal with this? do a complete text parsing of the file?
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            LOGGER.error("The parser timed out before it could deliver all the peptides");
        }
        return modificationList;
    }

}
