package com.compomics.pride_asa_pipeline.core.logic.spectrum.match;

import com.compomics.pride_asa_pipeline.model.AnnotationData;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.core.model.ModifiedPeptidesMatchResult;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.core.logic.spectrum.score.IdentificationScorer;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: niels Date: 27/10/11 Time: 17:34 To change
 * this template use File | Settings | File Templates.
 */
public interface SpectrumMatcher {

    /**
     * Matches an peptide against a spectrum. The score result and fragment ion
     * annotation are returned as annotation data.
     *
     * @param peptide the peptide
     * @param peaks the spectrum peak list
     * @param fragmentMassError the fragment mass error
     * @return the annotation data (score result and fragment ion annotations)
     */
    AnnotationData matchPrecursor(Peptide peptide, List<Peak> peaks, double fragmentMassError);

    /**
     * Finds the best matching modified peptide against a spectrum the scores
     * and fragment annotation are added to the peptide.
     *
     * @see
     * IdentificationScorer#score(com.compomics.pride_asa_pipeline.model.Peptide,
     * java.util.List)
     *
     * @param modifiedPeptides the set of modified peptides
     * @param peptide the identified peptide
     * @param peaks the spectrum peak list
     * @param fragmentMassError the fragment mass error
     * @return the best matching modified peptides result
     */
    ModifiedPeptidesMatchResult findBestModifiedPeptideMatch(Peptide peptide, Set<ModifiedPeptide> modifiedPeptides, List<Peak> peaks, double fragmentMassError);

    /**
     * Gets the identification scorer
     *
     * @return the identification scorer
     */
    IdentificationScorer getIdentificationScorer();
}
