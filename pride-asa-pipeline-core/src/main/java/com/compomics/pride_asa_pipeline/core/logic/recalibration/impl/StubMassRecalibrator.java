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
