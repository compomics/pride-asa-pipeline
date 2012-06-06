package com.compomics.pride_asa_pipeline.spectrum.match;

import com.compomics.pride_asa_pipeline.model.AnnotationData;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.spectrum.filter.NoiseThresholdFinder;
import com.compomics.pride_asa_pipeline.spectrum.filter.NoiseFilter;
import com.compomics.pride_asa_pipeline.spectrum.score.IdentificationScorer;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: niels Date: 27/10/11 Time: 17:34 To change
 * this template use File | Settings | File Templates.
 */
public interface SpectrumMatcher {
    
    /**
     * Matches an unmodified peptide against a spectrum. The score result and
     * fragment ion annotation are returned as annotation data.
     *
     * @param peptide the unmodified peptide
     * @param peaks the spectrum peak list
     * @return the annotation data (score result and fragment ion annotations)
     */
    AnnotationData matchUnmodifiedPeptide(Peptide peptide, List<Peak> peaks);

    /**
     * Finds the best matching modified peptide against a spectrum the scores
     * and fragment annotation are added to the peptide
     *
     * @see IdentificationScorer#score(uk.ac.ebi.pride.asa.model.Peptide,
     * java.util.List)
     *
     * @param modifiedPeptides the set of modified peptides
     * @param peptide the identified peptide
     * @param peaks the spectrum peak list
     * @return the best matching modified peptide
     */
    ModifiedPeptide findBestModifiedPeptideMatch(Peptide peptide, Set<ModifiedPeptide> modifiedPeptides, List<Peak> peaks);
    
    /**
     * Gets the identification scorer
     * 
     * @return the identification scorer
     */
    IdentificationScorer getIdentificationScorer();
}
