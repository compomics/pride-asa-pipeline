package com.compomics.pride_asa_pipeline.pipeline;


import com.compomics.pride_asa_pipeline.model.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: niels
 * Date: 9/11/11
 * Time: 17:16
 * To change this template use File | Settings | File Templates.
 */
public abstract class PrideSpectrumAnnotatorTemplate {
    
    /**
     * Loads the experiment identifications.
     *
     * @param experimentAccession the experiment accession number
     */
    public abstract void loadExperimentIdentifications(String experimentAccession);
    
    /**
     * Annotates the experiment spectra
     *
     * @param experimentAccession the experiment accession number
     * @throws SQLException
     */
    public abstract void annotate(String experimentAccession) throws SQLException;
    
    /**
     * finds the systematic mass errors per charge state
     *
     * @param consideredChargeStates a set of considered charge states
     * @param completePeptides list of complete peptides (i.e. all sequence AA masses known)
     * @return the mass recalibration result (systemic mass error) per charge state
     * @throws SQLException
     */
    public abstract MassRecalibrationResult findSystematicMassError(Set<Integer> consideredChargeStates, List<Peptide> completePeptides);

    /**
     * finds all possible modifications that could explain a mass delta -> Zen Archer
     *
     * @param massRecalibrationResult the mass recalibration result (systemic mass error) per charge state
     * @param identifications the identifications
     * @return the possible modifications map (key: the identification data, value the set of modification combinations)
     * @throws SQLException
     */
    public abstract Map<Identification, Set<ModificationCombination>> findModificationCombinations(MassRecalibrationResult massRecalibrationResult, Identifications identifications);

    /**
     * find all possible precursor variations (taking all
     * the possible modification combinations into account)
     *
     * @param massDeltaExplanationsMap the possible modifications map (key: the identification data, value the set of modification combinations)
     * @return the precursor variations map (key: the identification data, value the set of modification combinations)
     * @throws SQLException
     */
    public abstract Map<Identification, Set<ModifiedPeptide>> findPrecursorVariations(Map<Identification, Set<ModificationCombination>> massDeltaExplanationsMap);

    /**
     * creates theoretical fragment ions for all precursors
     *        match them onto the peaks in the spectrum and decide
     *        which one is the best 'explanation'
     *
     * @param experimentAccession the experiment accession number
     * @param modifiedPrecursorVariationsMap the modified precursor variations map (key: the identification data, value the set of modification combinations)
     * @return the result map (keys: the identification data, values: the modified peptides)
     * @throws SQLException
     */
    public abstract Map<Identification, ModifiedPeptide> findBestMatches(String experimentAccession, Map<Identification, Set<ModifiedPeptide>> modifiedPrecursorVariationsMap) throws SQLException;

    /**
     * processes the results of the spectra matching
     *
     * @param experimentAccession the experiment accession number
     * @param unmodifiedPeptidesList the unmodified precursors list
     * @param modifiedPeptidesMap the modified precursors map (keys: the identification data, values: the modified peptides)
     * @throws SQLException
     */
    public abstract void handleResult(String experimentAccession, List<Identification> unmodifiedPeptidesList, Map<Identification, ModifiedPeptide> modifiedPeptidesMap) throws SQLException;

    /**
     * processes the results of the spectra matching without taking into account any modifications
     *
     * @param experimentAccession the experiment accession
     * @throws SQLException
     */
    public abstract void annotateWithoutModifications(String experimentAccession) throws SQLException;
}
