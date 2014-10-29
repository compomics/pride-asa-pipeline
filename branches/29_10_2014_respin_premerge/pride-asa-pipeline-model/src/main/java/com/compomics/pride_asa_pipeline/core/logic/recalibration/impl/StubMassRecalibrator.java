package com.compomics.pride_asa_pipeline.core.logic.recalibration.impl;

import com.compomics.pride_asa_pipeline.core.logic.recalibration.AbstractMassRecalibrator;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.core.model.MassRecalibrationResult;
import com.compomics.pride_asa_pipeline.model.Peptide;
import java.util.Collection;

/**
 * @author Florian Reisinger Date: 11-Aug-2009
 * @since 0.1
 */
public class StubMassRecalibrator extends AbstractMassRecalibrator {
            
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

        return result;
    }
}
