package com.compomics.pride_asa_pipeline.core.logic.recalibration.impl;

import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.logic.recalibration.AbstractMassRecalibrator;
import com.compomics.pride_asa_pipeline.core.logic.recalibration.MassRecalibrationResult;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.core.util.MathUtils;
import java.util.Collection;
import java.util.List;

/**
 * @author Florian Reisinger Date: 11-Aug-2009
 * @since 0.1
 */
public class MassRecalibratorImpl extends AbstractMassRecalibrator {
            
    //ToDo: update, so modified peptides are taken into account (not just skipped)
    @Override
    public MassRecalibrationResult recalibrate(AnalyzerData analyzerData, Collection<Peptide> peptides) throws AASequenceMassUnknownException {
        checkSetup();
        if (peptides == null) {
            throw new IllegalStateException("Can not recalibrate null list of peptides!");
        }

        //without systematic error the mass errors should concentrate around zero
        double centre = 0.0D;

        //init the mass calibration result with the default values
        MassRecalibrationResult result = initMassRecalibrationResult(analyzerData);

        //calculate for each charge state separately
        for (Integer charge : consideredChargeStates) {
            //first find all the peptides with the same (current) charge state
            List<Peptide> peptidesByCharge = findPrecursorsWithCharge(peptides, charge);
            if (peptidesByCharge == null || peptidesByCharge.size() < PropertiesConfigurationHolder.getInstance().getInt("massrecalibrator.minimum_peptide_count")) {
                //none or to little peptides with the current charge: jump to the next
                continue;
            }
            //then find the usable peptides          
            List<Peptide> usablePeptidesByCharge = findUsablePeptides(peptidesByCharge);
            if (usablePeptidesByCharge.size() < PropertiesConfigurationHolder.getInstance().getInt("massrecalibrator.minimum_peptide_count")) {
                //there is no point in trying to calculate a systematic mass error on that few values
                continue;
            }
            //then get all the mass errors of these peptides
            List<Double> massErrors = getMassErrors(usablePeptidesByCharge);
            //determine the mass error window around zero (we don't take mass errors into account
            //that are due to modifications and there for may group around a different centre)
            massWindowFinder.setCentre(centre);
            double massWindow = massWindowFinder.findMassWindow(massErrors);
            //get the mass errors that lie in the mass error window
            List<Double> massErrorsInWindow = filterMassErrors(massErrors, (centre - massWindow), (centre + massWindow));
            if (massErrorsInWindow == null || massErrorsInWindow.isEmpty()) {
                //if we did not find any mass errors around the given centre, we have to continue
                //maybe all mass errors with the current charge state concentrate around another mass
                //=> possible modification
                continue;
            }
            //calculate the systematic mass error for those masses
            double systematicError = MathUtils.calculateMedian(massErrorsInWindow);
            //calculate the error window around the systematic error (new centre)
            massWindowFinder.setCentre(systematicError);
            double systematicErrorWindow = massWindowFinder.findMassWindow(massErrorsInWindow);
            //store the results
            result.addMassError(charge, systematicError, systematicErrorWindow);
        }

        return result;
    }
}
