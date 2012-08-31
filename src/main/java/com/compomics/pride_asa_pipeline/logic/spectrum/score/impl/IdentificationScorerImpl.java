package com.compomics.pride_asa_pipeline.logic.spectrum.score.impl;

import com.compomics.pride_asa_pipeline.logic.spectrum.score.IdentificationScorer;
import com.compomics.pride_asa_pipeline.model.*;
import com.compomics.pride_asa_pipeline.util.PeakUtils;
import java.util.*;

/**
 * Created by IntelliJ IDEA. User: niels Date: 27/10/11 Time: 17:03 To change
 * this template use File | Settings | File Templates.
 */
public class IdentificationScorerImpl implements IdentificationScorer {

    private double fragmentMassError;

    public IdentificationScorerImpl() {
    }

    public double getFragmentMassError() {
        return fragmentMassError;
    }

    @Override
    public void setFragmentMassError(double fragmentMassError) {
        this.fragmentMassError = fragmentMassError;
    }

    @Override
    public AnnotationData score(Peptide peptide, List<Peak> peaks) {                
        //check the Y- and B-ions
        Map<FragmentIonAnnotation.IonType, double[][]> ionLadderMasses = new EnumMap<FragmentIonAnnotation.IonType, double[][]>(FragmentIonAnnotation.IonType.class);
        //add charge ladders
        double[][] yMasses = {peptide.getYIonLadderMasses(1), peptide.getYIonLadderMasses(2)};
        double[][] bMasses = {peptide.getBIonLadderMasses(1), peptide.getBIonLadderMasses(2)};

        ionLadderMasses.put(FragmentIonAnnotation.IonType.Y_ION, yMasses);
        ionLadderMasses.put(FragmentIonAnnotation.IonType.B_ION, bMasses);

        //total intensity of the spectrum        
        long totalIntensity = getTotalIntensity(peaks);
        //total peak count of the spectrum
        int totalPeakCount = peaks.size();

        List<FragmentIonAnnotation> fragmentIonAnnotations = new ArrayList<FragmentIonAnnotation>();
        int matchingPeakCount = 0;
        long matchingIntensity = 0;

        //keep track of all the matched peaks in a set
        //to be sure that a peak is scored only once
        Set<Peak> matchedPeakSet = new HashSet<Peak>();

        //iterate over all fragment ion types
        for (FragmentIonAnnotation.IonType ionType : ionLadderMasses.keySet()) {
            double[][] ionLadderMassMatrix = ionLadderMasses.get(ionType);
            //iterate over all charge states
            for (int i = 0; i < ionLadderMassMatrix.length; i++) {
                //iterate over all ion ladder masses for one charge state
                for (int j = 0; j < ionLadderMassMatrix[i].length; j++) {
                    double ionLadderMass = ionLadderMassMatrix[i][j];
                    Peak matchingPeak = findMatchingPeak(ionLadderMass, peaks);
                    if (matchingPeak != null && !matchedPeakSet.contains(matchingPeak)) {                        
                        //add peak to matchedPeakSet
                        matchedPeakSet.add(matchingPeak);
                        //increment matched peak count
                        matchingPeakCount++;
                        //add matched peak intensity
                        matchingIntensity += matchingPeak.getIntensity();
                        //add fragment ion annotation to list
                        FragmentIonAnnotation fragmentIonAnnotation = new FragmentIonAnnotation(peptide.getPeptideId(), ionType, j + 1, matchingPeak.getMzRatio(), matchingPeak.getIntensity(), fragmentMassError, i + 1);
                        fragmentIonAnnotations.add(fragmentIonAnnotation);                        
                    }
                }
            }
        }

        //add fragment ion annotations and scoring result to annotationData                   
        AnnotationData annotationData = new AnnotationData();
        if (!fragmentIonAnnotations.isEmpty()) {
            annotationData.setFragmentIonAnnotations(fragmentIonAnnotations);
        }
        IdentificationScore identificationScore = new IdentificationScore(matchingPeakCount, totalPeakCount, matchingIntensity, totalIntensity, peptide.length());
        annotationData.setIdentificationScore(identificationScore);

        return annotationData;
    }

    /**
     * Find the matching spectrum with the highest intensity (if there are any).
     *
     * @param fragmentMz the m/z ratio of the fragment ion.
     * @param signalPeaks the signal peaks of the spectra.
     * @return the most intense signal spectrum matching the m/z of the fragment
     * ion.
     * @see this#isHit(double, double)
     */
    private Peak findMatchingPeak(double fragmentMz, List<Peak> signalPeaks) {
        Peak bestMatch = null;
        double highestIntensity = -1;

        //try to find the provided m/z (of the fragment ion) in the
        //list of signal peaks and if there are more than one matches
        //take the one with the highest intensity.
        for (Peak peak : signalPeaks) {
            if (PeakUtils.isHit(peak.getMzRatio(), fragmentMz, fragmentMassError)) {
                if (peak.getIntensity() > highestIntensity) {
                    bestMatch = peak;
                    highestIntensity = bestMatch.getIntensity();
                }
            }
        }
        return bestMatch;
    }

    /**
     * gets the total intensity of the spectrum
     *
     * @param signalPeaks the list of peaks
     * @return the total intensity
     */
    public long getTotalIntensity(List<Peak> signalPeaks) {
        long totalIntensity = 0;
        for (Peak peak : signalPeaks) {
            totalIntensity += peak.getIntensity();
        }
        return totalIntensity;
    }
}
