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
package com.compomics.pride_asa_pipeline.core.logic.spectrum.match.impl;

import com.compomics.pride_asa_pipeline.core.logic.spectrum.filter.NoiseFilter;
import com.compomics.pride_asa_pipeline.core.logic.spectrum.filter.NoiseThresholdFinder;
import com.compomics.pride_asa_pipeline.core.logic.spectrum.match.SpectrumMatcher;
import com.compomics.pride_asa_pipeline.core.logic.spectrum.score.IdentificationScorer;
import com.compomics.pride_asa_pipeline.model.AnnotationData;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.core.model.ModifiedPeptidesMatchResult;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.core.model.PeakFilterResult;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.core.util.PeakUtils;
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
    public AnnotationData matchPrecursor(Peptide unmodifiedPeptide, List<Peak> peaks, double fragmentMassError) {
        //filter spectrum
        PeakFilterResult peakFilterResult = filterPeaks(peaks, unmodifiedPeptide.calculateExperimentalMass());

        //match the peptide
        AnnotationData annotationData = matchPeptide(unmodifiedPeptide, peakFilterResult.getFilteredPeaks(), fragmentMassError);
        //set noise threshold
        annotationData.setNoiseThreshold(peakFilterResult.getNoiseThreshold());

        return annotationData;
    }

    @Override
    public ModifiedPeptidesMatchResult findBestModifiedPeptideMatch(Peptide peptide, Set<ModifiedPeptide> modifiedPeptides, List<Peak> peaks, double fragmentMassError) {
        ModifiedPeptidesMatchResult modifiedPeptidesMatchResult = null;

        //filter spectrum
        PeakFilterResult peakFilterResult = filterPeaks(peaks, peptide.calculateExperimentalMass());

        //now match each ModifiedPeptide and record its score
        ModifiedPeptide bestMatch = null;
        AnnotationData bestAnnotationData = null;
        AnnotationData annotationData = null;
        double bestScore = -1;
        for (ModifiedPeptide modifiedPeptide : modifiedPeptides) {
            if (modifiedPeptide == null) {
                LOGGER.info("null as modified peptide in variations collection for precursor!");
            }
            //score the modified peptide against the spectrum peaks
            annotationData = this.matchPeptide(modifiedPeptide, peakFilterResult.getFilteredPeaks(), fragmentMassError);
            //for the moment, consider the average fragment ion score
            double averageFragmentIonScore = annotationData.getIdentificationScore().getAverageFragmentIonScore();
            if (averageFragmentIonScore > bestScore) {
                bestMatch = modifiedPeptide;
                bestAnnotationData = annotationData;
                bestScore = bestAnnotationData.getIdentificationScore().getAverageFragmentIonScore();
            }
        }

        //return the best scoring ModifiedPeptide as the best match        
        if (bestMatch != null) {
            //LOGGER.info("Best matching peptide: " + bestMatch.getSequence() + " with score: " + bestScore);
            //set annotation data noise threshold
            annotationData.setNoiseThreshold(peakFilterResult.getNoiseThreshold());
            modifiedPeptidesMatchResult = new ModifiedPeptidesMatchResult(bestMatch, bestAnnotationData);
        } else {
            LOGGER.info("best match: null");
        }

        return modifiedPeptidesMatchResult;
    }

    /**
     * Filters the peaks.
     *
     * @param peaks the peaks
     * @param experimentalPrecursorMass the experimental precursor mass
     * @return the peak filter result (filtered peaks + noise filter threshold)
     */
    private PeakFilterResult filterPeaks(List<Peak> peaks, double experimentalPrecursorMass) {
        PeakFilterResult peakFilterResult = null;
        //don't filter if there are no peaks
        if (!peaks.isEmpty()) {
            double[] intensities = PeakUtils.getIntensitiesAsArray(peaks);
            double threshold = noiseThresholdFinder.findNoiseThreshold(intensities);
            List<Peak> filteredPeaks = noiseFilter.filterNoise(peaks, threshold, experimentalPrecursorMass);
            peakFilterResult = new PeakFilterResult(filteredPeaks, threshold);
        } else {
            peakFilterResult = new PeakFilterResult(peaks);
        }

        return peakFilterResult;
    }

    /**
     * Matches the peptide against a spectrum
     *
     * @param peptide the peptide to be matched
     * @param peaks the spectrum peaks
     * @return the annotation data (score result + fragment ion annotations)
     */
    private AnnotationData matchPeptide(Peptide peptide, List<Peak> peaks, double fragmentMassError) {
        return identificationScorer.score(peptide, peaks, fragmentMassError);
    }
}
