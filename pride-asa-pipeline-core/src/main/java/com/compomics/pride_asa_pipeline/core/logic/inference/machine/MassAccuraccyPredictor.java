package com.compomics.pride_asa_pipeline.core.logic.inference.machine;

import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.logic.MassDeltaExplainer;
import com.compomics.pride_asa_pipeline.core.logic.inference.InferenceStatistics;
import com.compomics.pride_asa_pipeline.core.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.model.FragmentIonAnnotation;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.util.experiment.biology.Atom;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class MassAccuraccyPredictor {

    /**
     * A logger
     */
    private static final Logger LOGGER = Logger.getLogger(MassAccuraccyPredictor.class);
    /**
     * The most likely inferred precursor error (default to 0.6)
     */
    private double mostLikelyPrecursorError = 0.6;
    /**
     * The most likely inferred fragment ion error (default to 0.6)
     */
    private double mostLikelyFragIonAcc = 0.6;
    /**
     * Boolean indicating to use absolute mass errors in the statistical
     * inference
     */
    private final boolean useAbsoluteMassDelta = true;
    /**
     * An statistics object for the fragment ions
     */
    private InferenceStatistics fragmentIonStats = new InferenceStatistics(useAbsoluteMassDelta);
    /**
     * An statistics object for the precursor ion stats
     */
    private InferenceStatistics precursorStats = new InferenceStatistics(useAbsoluteMassDelta);
    /*
     * The used SpectrumAnnotatorResult
     */
    private final SpectrumAnnotatorResult spectrumAnnotatorResult;
    /*
     * The used massDeltaExplainer
     */
    private final MassDeltaExplainer massDeltaExplainer;
    /**
     * An ordened set of the encountered charges
     */
    private final TreeSet<Integer> encounteredCharges = new TreeSet<>();

    public MassAccuraccyPredictor(SpectrumAnnotatorResult spectrumAnnotatorResult, MassDeltaExplainer massDeltaExplainer) {
        this.spectrumAnnotatorResult = spectrumAnnotatorResult;
        this.massDeltaExplainer = massDeltaExplainer;
        inferMachineSettings();
    }

    private void inferMachineSettings() {
        //use the known unmodified precorsors
        List<Identification> experimentIdentifications = spectrumAnnotatorResult.getUnmodifiedPrecursors();
        HashSet<Identification> alreadyProcessedIdentifications = new HashSet<>();
        precursorStats.clear();
        fragmentIonStats.clear();
        for (Identification anIdentification : experimentIdentifications) {
            //if the identification was processed before, don't repeat it
            if (!alreadyProcessedIdentifications.contains(anIdentification)) {
                encounteredCharges.add(anIdentification.getPeptide().getCharge());
                alreadyProcessedIdentifications.add(anIdentification);
                if (anIdentification.getPipelineExplanationType() != null) {
                    try {
                        List<FragmentIonAnnotation> fragmentIonAnnotations = anIdentification.getAnnotationData().getFragmentIonAnnotations();
                        for (FragmentIonAnnotation anAnnotation : fragmentIonAnnotations) {
                            double frag_mass_error = anAnnotation.getMass_error();
                            //C13 peaks
                            if (frag_mass_error < Atom.C.getMonoisotopicMass() / 12) {
                                fragmentIonStats.addValue(frag_mass_error);
                            }
                        }
                    } catch (NullPointerException e) {
                        //this can happen if there are no unmodified and identified peptides...
                        //  LOGGER.error("Not able to extract for " + anIdentification);
                    }
                }
            }
        }
        LOGGER.info("Attempting to find best suited precursor accuraccy (both sides)...");
        precursorStats = massDeltaExplainer.getExplainedMassDeltas();
        mostLikelyPrecursorError = InferenceStatistics.round(precursorStats.calculateOptimalMassError(), 3);
        if (mostLikelyPrecursorError == Double.NaN
                || mostLikelyPrecursorError == Double.NEGATIVE_INFINITY
                || mostLikelyPrecursorError == Double.NEGATIVE_INFINITY) {
            mostLikelyPrecursorError = PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.default_error_tolerance");
        }
        LOGGER.info("Most likely precursor accuraccy found at " + mostLikelyPrecursorError);

        LOGGER.info("Attempting to find best suited fragment ion accuraccy");

        if (fragmentIonStats.getValues().length == 0) {
            fragmentIonStats = precursorStats;
        } else {
            //calculate the optimal mass ?
            mostLikelyFragIonAcc = InferenceStatistics.round(fragmentIonStats.calculateOptimalMassError(), 3);
        }
        if (mostLikelyFragIonAcc == Double.NaN
                || mostLikelyFragIonAcc == Double.NEGATIVE_INFINITY
                || mostLikelyFragIonAcc == Double.POSITIVE_INFINITY) {
            mostLikelyFragIonAcc = PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.default_error_tolerance");
        }
        LOGGER.info("Most likely fragment ion accuraccy found at " + mostLikelyFragIonAcc);
    }

    public double getRecalibratedPrecursorAccuraccy() {
        return mostLikelyPrecursorError;
    }

    public double getRecalibratedFragmentIonAccuraccy() {
        return mostLikelyFragIonAcc;
    }

    public int getRecalibratedMaxCharge() {
        return encounteredCharges.last();
    }

    public int getRecalibratedMinCharge() {
        return encounteredCharges.first();
    }

}
