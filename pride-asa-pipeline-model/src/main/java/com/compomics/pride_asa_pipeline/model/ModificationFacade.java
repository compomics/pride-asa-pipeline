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
package com.compomics.pride_asa_pipeline.model;

/**
 * @author Florian Reisinger
 *         Date: 04-Jun-2010
 * @since $version
 */
public interface ModificationFacade {

    /**
     * Gets the modification mass shift
     * 
     * @return the mass shift value
     */
    public double getMassShift();

    /**
     * Gets the modification name
     * 
     * @return the modification name
     */
    public String getName();
    
    /**
     * Gets the modification type enum (MS1 or MS2)
     * 
     * @return the modification type
     */
    public Modification.Type getType();


}
