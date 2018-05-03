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

import java.util.List;

/**
 * @author Florian Reisinger
 *         Date: 11-Aug-2009
 * @since 0.1
 */
public interface MassWindowFinder {
    
    /**
     * Sets the centre
     * 
     * @param centre the centre value
     */    
    public void setCentre(double centre);

    /**
     * Sets the maximium window size. 
     * The default value is retrieved from the properties file.
     * 
     * @param maxWindowSize the maximum window size value
     */
    public void setMaxWindow(double maxWindowSize);

    /**
     * Finds the window size necessary around the given centre to include
     * the values provided.
     * 
     * @param values the double values to find the window for.
     * @return the window size (in one direction) around the centre.
     *         (e.g. the whole window = centre +/- window size. 
     */
    public double findMassWindow(List<Double> values);

}
