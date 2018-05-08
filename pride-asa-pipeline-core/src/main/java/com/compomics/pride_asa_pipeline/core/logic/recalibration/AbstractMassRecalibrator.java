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
package com.compomics.pride_asa_pipeline.core.logic.recalibration;

import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.model.MassRecalibrationResult;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Peptide;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: niels Date: 26/10/11 Time: 10:21 To change
 * this template use File | Settings | File Templates.
 */
public abstract class AbstractMassRecalibrator implements MassRecalibrator {

    protected MassWindowFinder massWindowFinder;
    protected Set<Integer> consideredChargeStates;    

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
        return PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.default_systematic_mass_error");
    }

    @Override
    public void setDefaultSystematicMassError(double defaultSystematicMassError) {
        PropertiesConfigurationHolder.getInstance().setProperty("massrecalibrator.default_systematic_mass_error", defaultSystematicMassError);
    }

    @Override
    public double getDefaultErrorTolerance() {
        return PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.default_error_tolerance");
    }

    @Override
    public void setDefaultErrorTolerance(double defaultErrorTolerance) {
        PropertiesConfigurationHolder.getInstance().setProperty("massrecalibrator.default_error_tolerance", defaultErrorTolerance);
    }

    @Override
    public abstract MassRecalibrationResult recalibrate(AnalyzerData analyzerData, Collection<Peptide> peptides) throws AASequenceMassUnknownException;

    protected void checkSetup() {
        if (massWindowFinder == null) {
            throw new IllegalStateException("MassRecalibrator needs a MassWindowFinder! Please set one.");
        }
        if (consideredChargeStates == null) {
            throw new IllegalStateException("MassRecalibrator needs a list of charge states! "
                    + "Please specify the charge states to consider.");
        }
    }

    /**
     * Inits the mass recalibration result; 
     *  -checks if there are charge states defined 
     *  -add the default systematic mass error for each charge state; takes the precursor mass error from the analyzer data
     *
     * @param analyzerData the analyzer data
     * @return the initialized mass recalibration result
     */
    protected MassRecalibrationResult initMassRecalibrationResult(AnalyzerData analyzerData) {
        MassRecalibrationResult result = new MassRecalibrationResult();
        if (consideredChargeStates == null) {
            throw new IllegalStateException("No charge states defined! Can not initialise calibration result.");
        }

        double errorTolerance = (analyzerData == null) ? PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.default_error_tolerance") : analyzerData.getPrecursorMassError();
        for (Integer charge : consideredChargeStates) {
            result.addMassError(charge, PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.default_systematic_mass_error"), (errorTolerance / charge));
        }

        return result;
    }

    /**
     * Returns the peptides with the spefied charge
     *
     * @param peptides the peptide collection
     * @param charge the charge value
     * @return the list of peptides
     */
    public List<Peptide> findPrecursorsWithCharge(Collection<Peptide> peptides, int charge) {
        List<Peptide> precs = new ArrayList<>();
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
    public List<Peptide> findUsablePeptides(List<Peptide> peptidesByCharge) {
        List<Peptide> peptides = new ArrayList<>();
        //get threshold value from configuration file
        double threshold = PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.mass_delta_threshold");
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
    public List<Double> getMassErrors(List<Peptide> peptides) throws AASequenceMassUnknownException {
        List<Double> errors = new ArrayList<>();
        for (Peptide peptide : peptides) {
            errors.add(peptide.calculateMassDelta());
        }
        return errors;
    }

    /**
     * Filters the mass errors with a window [lower bound, upper bound]. Only
     * the mass errors in the window pass the filter.
     *
     * @param massErrors the mass errors
     * @param lower the lower bound value
     * @param upper the upper bound value
     * @return the filtered mass errors
     */
    protected List<Double> filterMassErrors(List<Double> massErrors, double lower, double upper) {
        List<Double> errors = new ArrayList<>();
        for (Double massError : massErrors) {
            if (massError >= lower && massError <= upper) {
                errors.add(massError);
            }
        }
        return errors;
    }
}
