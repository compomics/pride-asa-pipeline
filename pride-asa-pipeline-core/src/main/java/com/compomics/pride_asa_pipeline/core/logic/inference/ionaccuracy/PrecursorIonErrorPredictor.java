package com.compomics.pride_asa_pipeline.core.logic.inference.ionaccuracy;

import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.logic.inference.InferenceStatistics;
import com.compomics.pride_asa_pipeline.core.logic.inference.additional.contaminants.MassScanResult;
import com.compomics.pride_asa_pipeline.core.logic.inference.report.impl.TotalReportGenerator;
import com.compomics.pride_asa_pipeline.core.model.modification.source.PRIDEModificationFactory;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.util.experiment.biology.Atom;
import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeSet;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class PrecursorIonErrorPredictor {

    /**
     * A logger
     */
    private static final Logger LOGGER = Logger.getLogger(PrecursorIonErrorPredictor.class);
    /**
     * The most likely inferred precursor error (default to 1.0)
     */
    private double mostLikelyPrecursorError = 1.0;
    /**
     * Boolean indicating to use absolute mass errors in the statistical
     * inference
     */
    private final boolean useAbsoluteMassDelta = true;
    /**
     * An statistics object for the fragment ions
     */
    private final InferenceStatistics precursorIonStats = new InferenceStatistics(useAbsoluteMassDelta);
    /**
     * An ordened set of the encountered charges
     */
    private final TreeSet<Integer> encounteredCharges = new TreeSet<>();
    /**
     * The mass difference of a carbon isotope
     */
    private final double C13IsotopeMass = Atom.C.getMonoisotopicMass() / 12;
    /**
     * The mass difference of a carbon isotope
     */
    private final Collection<Identification> experimentIdentifications;

    public PrecursorIonErrorPredictor(Collection<Identification> identifications) {
        this.experimentIdentifications = identifications;
        inferMachineSettings();
    }

    private void inferMachineSettings() {
        precursorIonStats.clear();
        LinkedList<Modification> asapMods = PRIDEModificationFactory.getAsapMods();

        for (Identification anIdentification : experimentIdentifications) {
            try {
                //only use the top 25% identifications?
                anIdentification.getAnnotationData().getIdentificationScore().getAverageAminoAcidScore();
                int charge = anIdentification.getPeptide().getCharge();
                encounteredCharges.add(anIdentification.getPeptide().getCharge());
                if (anIdentification.getPipelineExplanationType() != null) {
                    double precursor_mass_error;
                    try {
                        precursor_mass_error = Math.abs(anIdentification.getPeptide().calculateMassDelta());
                        if (precursor_mass_error <= C13IsotopeMass) {
                            boolean add = true;
                            for (Modification aMod : asapMods) {
                                //if it matches to 2 digits...
                                if (Math.abs(aMod.getAverageMassShift() - precursor_mass_error) < 0.01) {
                                    add = false;
                                    break;
                                }
                            }
                            if (precursor_mass_error > 0 && add) {
                                precursorIonStats.addValue(precursor_mass_error);
                            }
                        }
                    } catch (AASequenceMassUnknownException ex) {
                        LOGGER.warn(anIdentification.getPeptide().getSequenceString() + " contains unknown amino acids and will be skipped");
                    }
                    //C13 peaks
                }
            } catch (NullPointerException e) {
                //this can happen if there are no unmodified and identified peptides?
                //  LOGGER.error("Not able to extract for " + anIdentification);
            }
        }

        double acc = InferenceStatistics.round(precursorIonStats.calculateOptimalMassError(), 3);
        mostLikelyPrecursorError = Math.min(1.0, acc);

        TotalReportGenerator.setPrecursorAcc(mostLikelyPrecursorError);
        TotalReportGenerator.setPrecursorAccMethod(precursorIonStats.getMethodUsed());

        if (mostLikelyPrecursorError == Double.NaN
                || mostLikelyPrecursorError == Double.NEGATIVE_INFINITY
                || mostLikelyPrecursorError == Double.POSITIVE_INFINITY) {
            mostLikelyPrecursorError = PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.default_error_tolerance");
            TotalReportGenerator.setPrecursorAccMethod("Default value from properties");
        }
        LOGGER.info("Estimated precursor accuracy at " + mostLikelyPrecursorError);
        if (mostLikelyPrecursorError == 0 || mostLikelyPrecursorError >= 1.0) {
            mostLikelyPrecursorError = MassScanResult.estimatePrecursorIonToleranceBasedOnContaminants();
            TotalReportGenerator.setPrecursorAccMethod("Estimated based on known mass spectrometry related contaminants");
        }

    }

    public double getRecalibratedPrecursorAccuraccy() {
        return mostLikelyPrecursorError;
    }

    public int getRecalibratedMaxCharge() {
        return encounteredCharges.last();
    }

    public int getRecalibratedMinCharge() {
        return encounteredCharges.first();
    }

    public InferenceStatistics getPrecursorIonStats() {
        return precursorIonStats;
    }

    public TreeSet<Integer> getEncounteredCharges() {
        return encounteredCharges;
    }

    public Collection<Identification> getExperimentIdentifications() {
        return experimentIdentifications;
    }

    public double getC13IsotopeMass() {
        return C13IsotopeMass;
    }

}
