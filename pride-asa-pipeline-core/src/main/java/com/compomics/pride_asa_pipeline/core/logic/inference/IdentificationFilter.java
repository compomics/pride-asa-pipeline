package com.compomics.pride_asa_pipeline.core.logic.inference;

import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.IdentificationScore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class IdentificationFilter {

    private final Collection<Identification> identifications;
    private static final Logger LOGGER = Logger.getLogger(IdentificationFilter.class);

    public IdentificationFilter(Collection<Identification> identifications) {
        this.identifications = identifications;
    }

    public List<Identification> getTopPrecursorHits(double percentile) {
        List<Identification> topIdentifications = new ArrayList<>();
        if (identifications.size() > 30) {
            double threshold = getPrecursorThreshold(percentile);
            LOGGER.info("Only retaining identifications above " + threshold);
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

    public List<Identification> getTopFragmentIonHits(double percentile) {
        List<Identification> topIdentifications = new ArrayList<>();
        if (identifications.size() > 30) {
            double threshold = getFragmentIonThreshold(percentile);
            LOGGER.info("Only retaining identifications above " + threshold);
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
        InferenceStatistics statistics = new InferenceStatistics(true);
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
        InferenceStatistics statistics = new InferenceStatistics(true);
        for (Identification anIdentification : identifications) {
            try {
                IdentificationScore identificationScore = anIdentification.getAnnotationData().getIdentificationScore();
                double value = identificationScore.getAverageFragmentIonScore();
                if (value > 0) {
                    statistics.addValue(value);
                }
            } catch (NullPointerException e) {
                //then there is no scoring information known?
               // LOGGER.warn(e);
            }
        }
        if (statistics.getN() == 0) {
            return 0.0;
        }
        return statistics.getPercentile(percentile);
    }

}
