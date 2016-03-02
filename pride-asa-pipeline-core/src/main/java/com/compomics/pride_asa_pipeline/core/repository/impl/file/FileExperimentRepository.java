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
import java.util.Queue;
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
        CachedDataAccessController parser = parserCache.getParser(experimentAccession, false);
        //get all the peptide ids for the proteins
        long proteinCount = parser.getProteinIds().size();
        double completeRatio = 0.0;
        double currentCount = 0;
        for (Comparable aProteinID : parser.getProteinIds()) {
            completeRatio = 100 * currentCount / proteinCount;
            if (completeRatio % 10 < 1) {
                LOGGER.info(completeRatio + "%");
            }

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
                currentCount++;
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
