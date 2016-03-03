package com.compomics.pride_asa_pipeline.core.logic.inference;

import com.compomics.pride_asa_pipeline.core.model.ModificationHolder;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.IdentificationScore;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
                    }else{
                        //@ToDo what can we do with the others?
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
        if (identifications.size() > 100) {
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

    public double getPrecursorThreshold(double percentile) {
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

    public double getFragmentIonThreshold(double percentile) {
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

    public static List<Identification> filterIdentifications(List<Identification> completeIdentifications, ModificationHolder modificationHolder) {
        //first remove all identifications that have a mass error smaller than the smallest modification
        double smallestMass = modificationHolder.getAllModifications().iterator().next().getMonoIsotopicMassShift();
        for (Modification aMod : modificationHolder.getAllModifications()) {
            double deltaAbs = Math.abs(aMod.getMonoIsotopicMassShift());
            if (smallestMass > deltaAbs) {
                smallestMass = deltaAbs;
            }
        }
        LOGGER.info("Looking for unexplained masses that are larger than " + smallestMass);
        List<Identification> temp = new ArrayList<>();
        for (Identification ident : completeIdentifications) {
            try {
                if (Math.abs(ident.getPeptide().calculateMassDelta()) < smallestMass) {
                    temp.add(ident);
                }
            } catch (AASequenceMassUnknownException ex) {
                LOGGER.warn(ex);
            }
        }
        //sort identifications from worst to best, more chance to find a mod in a bad match !
        Comparator comparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 instanceof Identification && o2 instanceof Identification) {
                    Identification ident1 = (Identification) o1;
                    Identification ident2 = (Identification) o2;
                    try {
                        double mr1 = ident1.getPeptide().calculateMassDelta();
                        double mr2 = ident2.getPeptide().calculateMassDelta();
                        if (mr1 > mr2) {
                            return 1;
                        } else if (mr1 < mr2) {
                            return -1;
                        }
                    } catch (AASequenceMassUnknownException e) {
                        LOGGER.warn(e);
                        return -1;
                    }
                }
                return 0;
            }
        };
        Collections.sort(temp, comparator);
        return temp;
    }
    
}
