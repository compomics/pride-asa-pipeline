/* 
 * Copyright 2018 compomics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.compomics.pride_asa_pipeline.core.repository.impl.file;

import com.compomics.pride_asa_pipeline.core.model.ParserCacheConnector;
import com.compomics.pride_asa_pipeline.core.model.modification.impl.AsapModificationAdapter;
import com.compomics.pride_asa_pipeline.core.model.modification.source.PRIDEModificationFactory;
import com.compomics.pride_asa_pipeline.core.repository.ModificationRepository;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.ArrayList;
import java.util.List;
import com.compomics.pride_asa_pipeline.core.gui.PipelineProgressMonitor;
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
        CachedDataAccessController parser = parserCache.getParser(experimentIdentifier, true);
        for (Comparable proteinID : parser.getProteinIds()) {
            List<uk.ac.ebi.pride.utilities.data.core.Modification> mods = parser.getPTMs(proteinID, peptideID);
            for (uk.ac.ebi.pride.utilities.data.core.Modification aMod : mods) {
                modificationList.add((Modification) modFactory.getModification(adapter, aMod.getName()));
            }
        }
        return modificationList;
    }

    @Override
    public List<Modification> getModificationsByExperimentId(String experimentId) {
        List<Modification> modificationList = new ArrayList<>();
        CachedDataAccessController parser = parserCache.getParser(experimentId, true);
        for (Comparable proteinID : parser.getProteinIds()) {
            for (Comparable peptideID : parser.getPeptideIds(proteinID)) {
                List<uk.ac.ebi.pride.utilities.data.core.Modification> mods = parser.getPTMs(proteinID, peptideID);
                for (uk.ac.ebi.pride.utilities.data.core.Modification aMod : mods) {
                    modificationList.add((Modification) modFactory.getModification(adapter, aMod.getName()));
                }
            }
        }
        //@TODO handle peptideshaker projects
        //peptideshaker projects are a special case as it stores some mods in the user params...how can we deal with this? do a complete text parsing of the file?
        return modificationList;
    }

}
