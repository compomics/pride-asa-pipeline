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

import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.core.model.MassRecalibrationResult;
import com.compomics.pride_asa_pipeline.model.Peptide;
import java.util.Collection;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: niels
 * Date: 26/10/11
 * Time: 9:35
 * To change this template use File | Settings | File Templates.
 */
public interface MassRecalibrator {
    
    /**
     * Sets the mass window finder
     * 
     * @param massWindowFinder the mass window finder 
     */    
    void setMassWindowFinder(MassWindowFinder massWindowFinder);
    
    /**
     * Gets the mass window finder
     * 
     * @return the mass window finder
     */
    MassWindowFinder getMassWindowFinder();
    
    /**
     * Sets the set of considered charge states
     * 
     * @param consideredChargeStates the considered charge states
     */
    void setConsideredChargeStates(Set<Integer> consideredChargeStates);
    
    /**
     * Recalibrates for each charge state; 
     *      -calculates the mass error
     *      -calculates the mass error window
     * 
     * @param peptides the peptide collection
     * @param analyzerData the analyzer data
     * @return the mass recalibration result
     * @throws AASequenceMassUnknownException 
     */
    MassRecalibrationResult recalibrate(AnalyzerData analyzerData, Collection<Peptide> peptides) throws AASequenceMassUnknownException;
    
    /**
     * Gets the default systematic mass error
     * 
     * @return the default systematic mass error
     */
    double getDefaultSystematicMassError();
    
    /**
     * Sets the default systematic error
     * 
     * @param defaultSystematicMassError 
     */
    void setDefaultSystematicMassError(double defaultSystematicMassError);
    
    /**
     * Gets the default error tolerance
     * 
     * @return the default error tolerance
     */    
    double getDefaultErrorTolerance();
    
    /**
     * Sets the default error tolerance
     * 
     * @param defaultErrorTolerance the default error tolerance
     */
    void setDefaultErrorTolerance(double defaultErrorTolerance);

}

