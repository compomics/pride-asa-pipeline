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
package com.compomics.pride_asa_pipeline.core.logic;

import java.util.List;
import java.util.Set;

/**
 *
 * @author Niels Hulstaert Hulstaert
 */
public interface ZenArcher {

    /**
     * Computes all combinations with a given length for a given array of values
     *
     * @param values the values the values array
     * @param sizeCombination the combination size value
     * @return the set of combinations
     */
    Set<List<Double>> computeCombinations(double[] values, int sizeCombination);

    /**
     * Sets the minimal sum
     *
     * @param minimalSum the minimal sum value
     */
    void setMinimalSum(double minimalSum);

    /**
     * Sets the maximal sum
     *
     * @param maximalSum the maximal sum value
     */
    void setMaximalSum(double maximalSum);
}
