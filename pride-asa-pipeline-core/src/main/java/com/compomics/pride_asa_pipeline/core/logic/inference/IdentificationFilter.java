package com.compomics.pride_asa_pipeline.core.logic.inference;

import com.compomics.pride_asa_pipeline.model.ModificationHolder;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.IdentificationScore;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class IdentificationFilter {

    /**
     * The identifications to be filtered
     */
    private final Collection<Identification> identifications;
    /**
     * the logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(IdentificationFilter.class);

    /**
     * A filter for the identifications
     *
     * @param identifications to be filtered
     */
    public IdentificationFilter(Collection<Identification> identifications) {
        this.identifications = identifications;
    }

    /**
     * Get the precursor hits above the given percentile
     *
     * @param percentile the percentile value (in %)
     * @return the best precursor hits above the given percentile
     */
    public List<Identification> getTopPrecursorHits(double percentile) {
        List<Identification> topIdentifications = new ArrayList<>();
        if (identifications.size() > 30) {
            double threshold = getPrecursorThreshold(percentile);
            LOGGER.debug("Only retaining identifications above " + threshold);
            for (Identification anIdentification : identifications) {
                try {
                    IdentificationScore identificationScore = anIdentification.getAnnotationData().getIdentificationScore();
                    double value = identificationScore.getAverageAminoAcidScore();
                    if (threshold <= value) {
                        topIdentifications.add(anIdentification);
                    } else {
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

    /**
     * Get the fragment ion hits above the given percentile
     *
     * @param percentile the percentile value (in %)
     * @return the best fragment ion hits above the given percentile
     */
    public List<Identification> getTopFragmentIonHits(double percentile) {
        List<Identification> topIdentifications = new ArrayList<>();
        if (identifications.size() > 100) {
            double threshold = getFragmentIonThreshold(percentile);
            LOGGER.debug("Only retaining identifications above " + threshold);
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

    /**
     * Calculates the threshold score value for the given percentile
     *
     * @param percentile the percentile value in %
     * @return the threshold score value corresponding to the percentile
     */
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

    /**
     * Calculates the threshold score value for the given percentile
     *
     * @param percentile the percentile value in %
     * @return the threshold score value corresponding to the percentile
     */
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

    /**
     * Partitions a list into sublists
     *
     * @param parentList
     * @param partitions
     * @return
     */
    public static List<List<Identification>> partitionIdentifications(List<Identification> parentList, int partitions) {
        int subListSize = (parentList.size() / partitions) + 1;
        List<List<Identification>> subLists = new ArrayList<>();
        if (subListSize > parentList.size()) {
            subLists.add(parentList);
        } else {
            int remainingElements = parentList.size();
            int startIndex = 0;
            int endIndex = subListSize;
            do {
                List<Identification> subList = parentList.subList(startIndex, endIndex);
                subLists.add(subList);
                startIndex = endIndex;
                if (remainingElements - subListSize >= subListSize) {
                    endIndex = startIndex + subListSize;
                } else {
                    endIndex = startIndex + remainingElements - subList.size();
                }
                remainingElements -= subList.size();
            } while (remainingElements > 0);

        }
        return subLists;
    }

    /**
     * Filters a collection of identifications, retaining only those with
     * unexplained masses larger than the smallest mass shift in the
     * modification holder
     *
     * @param completeIdentifications the input identifications
     * @param modificationHolder the modification holder
     * @param sort boolean to state if the resulting list has to be sorted on
     * mass (smaller differences are easier to explain?)
     * @return a filtered list of identifications
     */
    public static List<Identification> filterIdentifications(List<Identification> completeIdentifications, ModificationHolder modificationHolder, boolean sort) {
        //first remove all identifications that have a mass error smaller than the smallest modification
        double smallestMass = modificationHolder.getAllModifications().iterator().next().getMonoIsotopicMassShift();
        for (Modification aMod : modificationHolder.getAllModifications()) {
            double deltaAbs = Math.abs(aMod.getMonoIsotopicMassShift());
            if (smallestMass > deltaAbs) {
                smallestMass = deltaAbs;
            }
        }
        //1 da = widest possible window
        smallestMass = smallestMass - 1;
        LOGGER.debug("Looking for unexplained masses that are larger than " + smallestMass);
        List<Identification> temp = new ArrayList<>();
        //split the complete identificion into smaller sets
        int nThreads = Runtime.getRuntime().availableProcessors();
        List<List<Identification>> chopIdentificationList = partitionIdentifications(completeIdentifications, nThreads);
        ExecutorService mtService = Executors.newFixedThreadPool(nThreads);
        List<Future<List<Identification>>> futureList = new ArrayList<>();
        for (List<Identification> aList : chopIdentificationList) {
            futureList.add(mtService.submit(new IdentificationFilterCallable(aList, smallestMass)));
        }
        mtService.shutdown();
        for (Future<List<Identification>> aFuture : futureList) {
            try {
                temp.addAll(aFuture.get());
            } catch (InterruptedException | ExecutionException ex) {
                LOGGER.warn("Could not include all the identifications in a mass filter...");
            }
        }
        if (sort) {
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
        }
        return temp;
    }

    private static class IdentificationFilterCallable implements Callable<List<Identification>> {

        private final List<Identification> completeIdentifications;
        private final double lowerMassThreshold;

        private IdentificationFilterCallable(List<Identification> completeIdentifications, double lowerMassThreshold) {
            this.completeIdentifications = completeIdentifications;
            this.lowerMassThreshold = lowerMassThreshold;
        }

        @Override
        public List<Identification> call() throws Exception {
            List<Identification> temp = new ArrayList<>();
            for (Identification ident : completeIdentifications) {
                try {
                    if (Math.abs(ident.getPeptide().calculateMassDelta()) >= lowerMassThreshold) {
                        temp.add(ident);
                    }
                } catch (AASequenceMassUnknownException ex) {
                    LOGGER.warn(ex);
                }
            }
            return temp;
        }

    }

}
