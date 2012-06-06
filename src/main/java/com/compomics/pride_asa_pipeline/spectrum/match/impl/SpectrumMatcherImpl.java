package com.compomics.pride_asa_pipeline.spectrum.match.impl;

import com.compomics.pride_asa_pipeline.model.AnnotationData;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.service.SpectrumService;
import com.compomics.pride_asa_pipeline.spectrum.filter.NoiseFilter;
import com.compomics.pride_asa_pipeline.spectrum.filter.NoiseThresholdFinder;
import com.compomics.pride_asa_pipeline.spectrum.match.SpectrumMatcher;
import com.compomics.pride_asa_pipeline.spectrum.score.IdentificationScorer;
import com.compomics.pride_asa_pipeline.util.PeakUtils;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * @author Florian Reisinger Date: 08-Oct-2009
 * @since 0.1
 */
public class SpectrumMatcherImpl implements SpectrumMatcher {

    private static final Logger LOGGER = Logger.getLogger(SpectrumMatcherImpl.class);
    
    /**
     * The identiciation scorer; scores the peptide spectrum match
     */
    private IdentificationScorer identificationScorer;
    private NoiseThresholdFinder noiseThresholdFinder;
    private NoiseFilter noiseFilter;
            
    @Override
    public IdentificationScorer getIdentificationScorer() {
        return identificationScorer;
    }

    public void setIdentificationScorer(IdentificationScorer identificationScorer) {
        this.identificationScorer = identificationScorer;
    }

    public NoiseFilter getNoiseFilter() {
        return noiseFilter;
    }

    public void setNoiseFilter(NoiseFilter noiseFilter) {
        this.noiseFilter = noiseFilter;
    }

    public NoiseThresholdFinder getNoiseThresholdFinder() {
        return noiseThresholdFinder;
    }

    public void setNoiseThresholdFinder(NoiseThresholdFinder noiseThresholdFinder) {
        this.noiseThresholdFinder = noiseThresholdFinder;
    }

    @Override
    public AnnotationData matchUnmodifiedPeptide(Peptide unmodifiedPeptide, List<Peak> peaks) {
        //filter spectrum
        List<Peak> signalPeaks = filterPeaks(peaks, unmodifiedPeptide.calculateExperimentalMass());

        return matchPeptide(unmodifiedPeptide, signalPeaks);
    }

    @Override
    public ModifiedPeptide findBestModifiedPeptideMatch(Peptide peptide, Set<ModifiedPeptide> modifiedPeptides, List<Peak> peaks) {
        //filter spectrum
        List<Peak> filteredPeaks = filterPeaks(peaks, peptide.calculateExperimentalMass());

        //now match each ModifiedPeptide and record its score
        ModifiedPeptide bestMatch = null;
        AnnotationData annotationData = null;
        double bestScore = -1;
        for (ModifiedPeptide modifiedPeptide : modifiedPeptides) {
            if (modifiedPeptide == null) {
                LOGGER.info("null as modified peptide in variations collection for precursor!");
            }
            //score the modified peptide against the spectrum peaks
            annotationData = this.matchPeptide(modifiedPeptide, filteredPeaks);
            //take the average fragment ion score
            double averageFragmentIonScore = annotationData.getIdentificationScore().getAverageFragmentIonScore();
            if (averageFragmentIonScore > bestScore) {
                bestMatch = modifiedPeptide;
                bestScore = averageFragmentIonScore;
            }
        }

        //return the best scoring ModifiedPeptide as the best match
        if (bestMatch != null) {
            LOGGER.info("Best matching peptide: " + bestMatch.getSequence() + " with score: " + bestScore);
        } else {
            LOGGER.info("best match: null");
        }

        return bestMatch;
    }

    private List<Peak> filterPeaks(List<Peak> peaks, double experimentalPrecursorMass) {
        //first get the signal peaks we have to use for the matching
        //these are either the unfiltered or filtered spectra peaks
        List<Peak> filteredPeaks = peaks;
        if (noiseFilter != null && noiseThresholdFinder != null) {
            //don't filter if there are no peaks
            if (!peaks.isEmpty()) {
                double[] intensities = PeakUtils.getIntensitiesAsArray(peaks);
                double threshold = noiseThresholdFinder.findNoiseThreshold(intensities);
                filteredPeaks = noiseFilter.filterNoise(peaks, threshold, experimentalPrecursorMass);
            }
        }

        return filteredPeaks;
    }

    /**
     * Matches the peptide against a spectrum
     *
     * @param peptide the peptide to be matched
     * @param peaks the spectrum peaks
     * @return the annotation data (score result + fragment ion annotations)
     */
    private AnnotationData matchPeptide(Peptide peptide, List<Peak> peaks) {
        return identificationScorer.score(peptide, peaks);
    }
}
