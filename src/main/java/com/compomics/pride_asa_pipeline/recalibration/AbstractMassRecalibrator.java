package com.compomics.pride_asa_pipeline.recalibration;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.MassRecalibrationResult;
import com.compomics.pride_asa_pipeline.model.Peptide;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: niels
 * Date: 26/10/11
 * Time: 10:21
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractMassRecalibrator implements MassRecalibrator {

    protected MassWindowFinder massWindowFinder;
    protected Set<Integer> consideredChargeStates;
    //the default systematic error to use if none could be established automatically
    protected double defaultSystematicMassError = PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.default_systematic_mass_error"); 
    //the default error tolerance (one side of a error window) to use if none could be established automatically
    protected double defaultErrorTolerance = PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.default_error_tolerance");  

    @Override
    public void setMassWindowFinder(MassWindowFinder massWindowFinder) {
        this.massWindowFinder = massWindowFinder;
    }

    @Override
    public MassWindowFinder getMassWindowFinder() {
        return massWindowFinder;
    }

    @Override
    public void setConsideredChargeStates(Set<Integer> consideredChargeStates) {
        this.consideredChargeStates = consideredChargeStates;
    }

    @Override
    public double getDefaultSystematicMassError() {
        return defaultSystematicMassError;
    }

    @Override
    public void setDefaultSystematicMassError(double defaultSystematicMassError) {
        this.defaultSystematicMassError = defaultSystematicMassError;
    }

    @Override
    public double getDefaultErrorTolerance() {
        return defaultErrorTolerance;
    }

    @Override
    public void setDefaultErrorTolerance(double defaultErrorTolerance) {
        this.defaultErrorTolerance = defaultErrorTolerance;
    }

    @Override
    public abstract MassRecalibrationResult recalibrate(Collection<Peptide> peptides) throws AASequenceMassUnknownException;

    protected void checkSetup() {
        if (massWindowFinder == null) {
            throw new IllegalStateException("MassRecalibrator needs a MassWindowFinder! Please set one.");
        }
        if (consideredChargeStates == null) {
            throw new IllegalStateException("MassRecalibrator needs a list of charge states! " +
                    "Please specify the charge states to consider.");
        }
    }

    /**
     * Inits the mass recalibration result; 
     *      -checks if there are charge states defined
     *      -add the default systematic mass error for each charge state
     * @return the initialized mass recalibration result
     */
    protected MassRecalibrationResult initMassRecalibrationResult() {
        MassRecalibrationResult result = new MassRecalibrationResult();
        if (consideredChargeStates == null) {
            throw new IllegalStateException("No charge states defined! Can not initialise calibration result.");
        }

        for (Integer charge : consideredChargeStates) {
            result.addMassError(charge, defaultSystematicMassError, (defaultErrorTolerance / charge));
        }

        return result;
    }
    
    /**
     * Returns the peptides with a specific charge
     * 
     * @param peptides the peptide collection
     * @param charge the charge value
     * @return 
     */
    protected List<Peptide> findPrecursorsWithCharge(Collection<Peptide> peptides, int charge) {
        List<Peptide> precs = new ArrayList<Peptide>();
        for (Peptide peptide : peptides) {
            if (peptide.getCharge() == charge) {
                precs.add(peptide);
            }
        }
        return precs;
    }
    
    /**
     * Returns the 'usable' peptides; those that exceed a given mass threshold
     * 
     * @param peptidesByCharge the peptides with a specific charge
     * @return the usable peptide with a specific charge
     */
    protected List<Peptide> findUsablePeptides(List<Peptide> peptidesByCharge) {
        List<Peptide> peptides = new ArrayList<Peptide>();
        //get threshold value from configuration file
        double threshold = PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.usable_peptides_threshold");
        for (Peptide peptide : peptidesByCharge) {
            try {
                double massDelta = peptide.calculateMassDelta();
                if (Math.abs(massDelta) < threshold) {
                    peptides.add(peptide);
                }
            } catch (AASequenceMassUnknownException e) {
                //should not happen here, since we are supposed to deal with 'complete' peptides only
                throw new IllegalStateException("AminoAcidSequence with AA of unknown mass!");
            }
        }

        return peptides;
    }
    
    /**
     * Gets the mass errors of the peptides
     * 
     * @param peptides the peptides
     * @return the mass errors
     * @throws AASequenceMassUnknownException 
     */
    protected List<Double> getMassErrors(List<Peptide> peptides) throws AASequenceMassUnknownException {
        List<Double> errors = new ArrayList<Double>();
        for (Peptide peptide : peptides) {
            errors.add(peptide.calculateMassDelta());
        }
        return errors;
    }
    
    /**
     * Filters the mass errors with a window [lower bound, upper bound].
     * Only the mass errors in the window pass the filter.
     * 
     * @param massErrors the mass errors
     * @param lower the lower bound value
     * @param upper the upper bound value
     * @return the filtered mass errors
     */    
    protected List<Double> filterMassErrors(List<Double> massErrors, double lower, double upper) {
        List<Double> errors = new ArrayList<Double>();
        for (Double massError : massErrors) {
            if (massError >= lower && massError <= upper) {
                errors.add(massError);
            }
        }
        return errors;
    }

}
