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

import com.compomics.pride_asa_pipeline.core.cache.ParserCache;
import com.compomics.pride_asa_pipeline.core.model.ParserCacheConnector;
import com.compomics.pride_asa_pipeline.core.repository.ExperimentRepository;
import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.CachedDataAccessController;
import uk.ac.ebi.pride.utilities.data.core.SpectrumIdentification;

/**
 *
 * @author Niels Hulstaert
 */
/**
 *
 * @author Kenneth Verheggen
 */
public class FileExperimentRepository extends ParserCacheConnector implements ExperimentRepository {

    /**
     * A logger instance
     */
    private static final Logger LOGGER = Logger.getLogger(FileExperimentRepository.class);

    /**
     * Creates a new File experiment repository
     */
    public FileExperimentRepository() {

    }

    @Override
    public Map<String, String> findAllExperimentAccessions() {
        return ParserCache.getInstance().getLoadedFiles();
    }

    @Override
    public Map<String, String> findExperimentAccessionsByTaxonomy(int taxonomyId) {
        throw new UnsupportedOperationException("Taxonomy filtration is not yet possible."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Identification> loadExperimentIdentifications(String experimentAccession) {
        List<Identification> identifications = new ArrayList<>();
        CachedDataAccessController parser = parserCache.getParser(experimentAccession, true);
        //get all the peptide ids for the proteins
        for (Comparable aProteinID : parser.getProteinIds()) {
            for (Comparable aPeptideID : parser.getPeptideIds(aProteinID)) {
                uk.ac.ebi.pride.utilities.data.core.Peptide aPeptide = parser.getPeptideByIndex(aProteinID, aPeptideID);
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
            }
        }
        //get all evidence for all peptide ids
        return identifications;
    }

    @Override
    public long getNumberOfSpectra(String experimentAccession) {
        return parserCache.getParser(experimentAccession, true).getSpectrumIds().size();
    }

    @Override
    public List<String> getProteinAccessions(String experimentAccession) {
        List<String> proteinAccessions = new ArrayList<>();
        CachedDataAccessController parser = parserCache.getParser(experimentAccession, true);
        for (Comparable aProteinId : parser.getProteinIds()) {
            proteinAccessions.add(parser.getProteinAccession(aProteinId));
        }
        return proteinAccessions;
    }

    @Override
    public long getNumberOfPeptides(String experimentAccession) {
        return parserCache.getParser(experimentAccession, true).getNumberOfPeptides();
    }

    @Override
    public List<Map<String, Object>> getSpectraMetadata(String experimentAccession) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
