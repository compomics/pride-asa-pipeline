package com.compomics.pride_asa_pipeline.core.logic.inference.ionaccuracy;

import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.logic.inference.InferenceStatistics;
import com.compomics.pride_asa_pipeline.core.logic.inference.additional.contaminants.MassScanResult;
import com.compomics.pride_asa_pipeline.core.util.report.impl.TotalReportGenerator;
import com.compomics.pride_asa_pipeline.model.modification.source.PRIDEModificationFactory;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.AtomChain;
import com.compomics.util.experiment.biology.AtomImpl;
import java.util.ArrayList;
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
    private final Collection<Identification> experimentIdentifications;

    public PrecursorIonErrorPredictor(Collection<Identification> identifications) {
        this.experimentIdentifications = identifications;
        inferMachineSettings();
    }

    private void inferMachineSettings() {
        inferMachineSettings(experimentIdentifications);
    }

    private void inferMachineSettings(Collection<Identification> identifications) {
        precursorIonStats.clear();
        LinkedList<Modification> asapMods = PRIDEModificationFactory.getAsapMods();
        double defaultPrecursorError = PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.default_error_tolerance");
        for (Identification anIdentification : identifications) {
            try {
                encounteredCharges.add(anIdentification.getPeptide().getCharge());
                if (anIdentification.getPipelineExplanationType() != null) {
                    double precursor_mass_error;
                    try {
                        precursor_mass_error = Math.abs(anIdentification.getPeptide().calculateMassDelta());

                        //eliminate precursor errors that are due to neutral losses?
                        for (Double aNeutralLossMass : getCommonNeutralLosses()) {
                            //if it matches to 2 digits...
                            if (Math.abs(aNeutralLossMass - precursor_mass_error) < 0.01) {
                                precursor_mass_error -= Math.abs(aNeutralLossMass);
                                break;
                            }
                        }

                        //eliminate precursor errors that are due to potential modification shifts?
                        for (Modification aMod : asapMods) {
                            if (Math.abs(aMod.getMonoIsotopicMassShift() - precursor_mass_error) < 0.01) {
                                precursor_mass_error -= Math.abs(aMod.getMonoIsotopicMassShift());
                                break;
                            }
                        }

                        /*    //try to account for a C13 isotope?
                        double C13_isotopeMass = new AtomImpl(Atom.C, 0).getMass() - new AtomImpl(Atom.C, 1).getMass();
                        if (precursor_mass_error >= C13_isotopeMass) {
                            precursor_mass_error -= C13_isotopeMass;
                        }*/
                        //if it is still bigger, then there's no use...
                        if (Math.abs(precursor_mass_error) <= defaultPrecursorError) {
                            //add this real error to the pool
                            precursorIonStats.addValue(Math.abs(precursor_mass_error));
                            // System.out.println(precursor_mass_error);
                        }

                    } catch (AASequenceMassUnknownException ex) {
                        LOGGER.warn(anIdentification.getPeptide().getSequenceString() + " contains unknown amino acids and will be skipped");
                    }
                }
            } catch (NullPointerException e) {
                //this can happen if there are no unmodified and identified peptides?
                //  LOGGER.error("Not able to extract for " + anIdentification);
            }
        }
        double acc = InferenceStatistics.round(precursorIonStats.calculateOptimalMassError(), 3);
        //System.out.println("Current prec accuracy is " + acc + " da");
        mostLikelyPrecursorError = Math.min(1.0, acc);
        TotalReportGenerator.setPrecursorAcc(mostLikelyPrecursorError);
        TotalReportGenerator.setPrecursorAccMethod(precursorIonStats.getMethodUsed());

        //if the error is still larger than the max tolerance of 1 dalton, try to infer it through known contaminants
        if (mostLikelyPrecursorError == 0 || mostLikelyPrecursorError >= 1.0) {
            MassScanResult.inspectIdentifications(identifications);
            mostLikelyPrecursorError = MassScanResult.getContaminantBasedMassError();
            TotalReportGenerator.setPrecursorAccMethod("Estimated based on known mass spectrometry related contaminants");
        }
        // System.out.println("Current prec accuracy is " + mostLikelyPrecursorError + " da");
        if (mostLikelyPrecursorError == Double.NaN
                || mostLikelyPrecursorError == Double.NEGATIVE_INFINITY
                || mostLikelyPrecursorError == Double.POSITIVE_INFINITY
                || mostLikelyPrecursorError > defaultPrecursorError) {
            mostLikelyPrecursorError = defaultPrecursorError;
            TotalReportGenerator.setPrecursorAccMethod("Default value from properties");
        }
        LOGGER.info("Estimated precursor accuracy at " + mostLikelyPrecursorError + "(" + TotalReportGenerator.getPrecursorAccMethod().toLowerCase() + ")");
    }

    public double getRecalibratedPrecursorAccuraccy() {
        return mostLikelyPrecursorError;
    }

    public int getRecalibratedMaxCharge() {
        if (encounteredCharges.size() > 0) {
            return encounteredCharges.last();
        } else {
            return 5;
        }
    }

    public int getRecalibratedMinCharge() {
        if (encounteredCharges.size() > 0) {
            return encounteredCharges.first();
        } else {
            return 2;
        }

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

    public void clear() {
        if (encounteredCharges != null) {
            encounteredCharges.clear();
        }
        if (experimentIdentifications != null) {
            experimentIdentifications.clear();
        }
        if (precursorIonStats != null) {
            precursorIonStats.clear();
        }

    }

    private Collection<Double> getCommonNeutralLosses() {
        ArrayList<Double> neutralLosses = new ArrayList<>();
        //ammonia
        AtomChain NH3chain = new AtomChain();
        NH3chain.append(new AtomImpl(Atom.H, 0), 3);
        NH3chain.append(new AtomImpl(Atom.N, 0), 1);
        neutralLosses.add(NH3chain.getMass());
        //water
        AtomChain H2Ochain = new AtomChain();
        H2Ochain.append(new AtomImpl(Atom.H, 0), 2);
        H2Ochain.append(new AtomImpl(Atom.O, 0), 1);
        neutralLosses.add(H2Ochain.getMass());
        //phosphate
        AtomChain H3PO4Chain = new AtomChain();
        H3PO4Chain.append(new AtomImpl(Atom.H, 0), 3);
        H3PO4Chain.append(new AtomImpl(Atom.P, 0), 1);
        H3PO4Chain.append(new AtomImpl(Atom.O, 0), 4);
        neutralLosses.add(H3PO4Chain.getMass());
        //sulphate
        AtomChain H2SO4Chain = new AtomChain();
        H2SO4Chain.append(new AtomImpl(Atom.H, 0), 2);
        H2SO4Chain.append(new AtomImpl(Atom.S, 0), 1);
        H2SO4Chain.append(new AtomImpl(Atom.O, 0), 4);
        neutralLosses.add(H2SO4Chain.getMass());
        //add the masses
        return neutralLosses;
    }

}
