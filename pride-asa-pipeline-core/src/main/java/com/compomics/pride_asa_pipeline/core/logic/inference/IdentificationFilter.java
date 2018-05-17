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
package com.compomics.pride_asa_pipeline.core.logic.inference;

import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.IdentificationScore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.math3.
stat.descriptive.DescriptiveStatistics;
import com.compomics.pride_asa_pipeline.core.gui.PipelineProgressMonitor;

/**
 *
 * @author Kenneth Verheggen
 */
public class IdentificationFilter {

    /*
    The collection of identifications that should be considered
    */
    private final Collection<Identification> identifications;

    /**
     * An identification filter to exclude identifications below a certain percentile
     * @param identifications 
     */
    public IdentificationFilter(Collection<Identification> identifications) {
        this.identifications = identifications;
    }

    /**
     * Returns the top ranked precursor hits
     * @param percentile the threshold percentile value
     * @return a list of identifications that are above this percentile
     */
    public List<Identification> getTopPrecursorHits(double percentile) {
        List<Identification> topIdentifications = new ArrayList<>();
        if (identifications.size() > 30) {
            double threshold = getPrecursorThreshold(percentile);
            PipelineProgressMonitor.info("Only retaining identifications above " + threshold);
            for (Identification anIdentification : identifications) {
                try {
                    IdentificationScore identificationScore = anIdentification.getAnnotationData().getIdentificationScore();
                    double value = identificationScore.getAverageAminoAcidScore();
                    if (threshold <= value) {
                        topIdentifications.add(anIdentification);
                    }
                } catch (NullPointerException e) {
                    //then there is no scoring information known?
                }
            }
        }
        if (topIdentifications.isEmpty()) {
            topIdentifications.addAll(identifications);
        }
        return topIdentifications;
    }

    
     /**
     * Returns the top ranked precursor hits
     * @param percentile the threshold percentile value
     * @return a list of identifications that are above this percentile
     */
    public List<Identification> getTopFragmentIonHits(double percentile) {
        List<Identification> topIdentifications = new ArrayList<>();
        if (identifications.size() > 30) {
            double threshold = getFragmentIonThreshold(percentile);
            PipelineProgressMonitor.info("Only retaining identifications above " + threshold);
            for (Identification anIdentification : identifications) {
                try {
                    IdentificationScore identificationScore = anIdentification.getAnnotationData().getIdentificationScore();
                    double value = identificationScore.getAverageFragmentIonScore();
                    if (threshold <= value) {
                        topIdentifications.add(anIdentification);
                    }
                } catch (NullPointerException e) {
                    //then there is no scoring information known?
                }
            }
        }
        if (topIdentifications.isEmpty()) {
            topIdentifications.addAll(identifications);
        }
        return topIdentifications;
    }

    private double getPrecursorThreshold(double percentile) {
        DescriptiveStatistics statistics = new DescriptiveStatistics();
        for (Identification anIdentification : identifications) {
            try {
                IdentificationScore identificationScore = anIdentification.getAnnotationData().getIdentificationScore();
                double value = identificationScore.getAverageAminoAcidScore();
                if (value > 0) {
                    statistics.addValue(value);
                }
            } catch (NullPointerException e) {
                //then there is no scoring information known?
            }
        }
        if (statistics.getN() == 0) {
            return 0.0;
        }
        return statistics.getPercentile(percentile);
    }

    private double getFragmentIonThreshold(double percentile) {
        DescriptiveStatistics statistics = new DescriptiveStatistics();
        for (Identification anIdentification : identifications) {
            try {
                IdentificationScore identificationScore = anIdentification.getAnnotationData().getIdentificationScore();
                double value = identificationScore.getAverageFragmentIonScore();
                if (value > 0) {
                    statistics.addValue(value);
                }
            } catch (NullPointerException e) {
                //then there is no scoring information known?
                // PipelineProgressMonitor.warn(e);
            }
        }
        if (statistics.getN() == 0) {
            return 0.0;
        }
        return statistics.getPercentile(percentile);
    }

    public List<Identification> ScanTrustworthyIdentifications(double precursorPercentile, double fragmentPercentile) {
        List<Identification> rankedIdentifications = new ArrayList<>();
        //determine the original starting point
        DescriptiveStatistics precursorScoreStatistics = new DescriptiveStatistics();
        DescriptiveStatistics fragmentScoreStatistics = new DescriptiveStatistics();
        for (Identification identification : identifications) {
            if (identification.getAnnotationData() != null && identification.getAnnotationData().getIdentificationScore() != null) {
                precursorScoreStatistics.addValue(identification.getAnnotationData().getIdentificationScore().getAverageAminoAcidScore());
                fragmentScoreStatistics.addValue(identification.getAnnotationData().getIdentificationScore().getAverageFragmentIonScore());
            }
        }
        //We need the 90th percentile in theory, but if there is not enough data we need to lower the threshold...this means we need to sort the identifications to their precursor and fragment ion scores...
        double precursorThreshold = precursorScoreStatistics.getPercentile(precursorPercentile);
        double fragmentThreshold = fragmentScoreStatistics.getPercentile(fragmentPercentile);

        for (Identification identification : identifications) {
            if (identification.getAnnotationData() != null && identification.getAnnotationData().getIdentificationScore() != null) {
                IdentificationScore score = identification.getAnnotationData().getIdentificationScore();
                if (score.getAverageAminoAcidScore() >= precursorThreshold && score.getAverageFragmentIonScore() >= fragmentThreshold) {
                    rankedIdentifications.add(identification);
                }
            }
        }

        return rankedIdentifications;
    }

    private Comparator<Identification> CreateComparator() {
        return new Comparator<Identification>() {
            @Override
            public int compare(Identification o1, Identification o2) {
                if (o1.getAnnotationData() == null || o1.getAnnotationData().getIdentificationScore() == null) {
                    return -1;
                } else if (o2.getAnnotationData() == null || o2.getAnnotationData().getIdentificationScore() == null) {
                    return 1;
                } else {
                    //pick the one with the best score? matched peaks? peptide length? (so invert the order, we want high to low...)
                    return -Double.compare(calculateQuality(o1), calculateQuality(o2));
                }
            }

            private double calculateQuality(Identification ident) {
                IdentificationScore score = ident.getAnnotationData().getIdentificationScore();
                return score.getPeptideLength() * score.getAverageAminoAcidScore() * score.getAverageFragmentIonScore() * score.getTotalPeaks();
            }
        };
    }

}
