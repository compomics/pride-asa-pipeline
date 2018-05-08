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
package com.compomics.pride_asa_pipeline.core.logic.impl;

import com.compomics.pride_asa_pipeline.core.logic.CombinationGenerator;
import com.compomics.pride_asa_pipeline.core.logic.ZenArcher;
import com.compomics.pride_asa_pipeline.core.util.MathUtils;
import java.util.*;

/**
 * @author Jonathan Rameseder Date: 15-Jan-2008
 * @since 0.1
 */
public class ZenArcherImpl implements ZenArcher {

    private double minimalSum;
    private double maximalSum;

    public ZenArcherImpl() {
    }    
    
    //Wonders of Numbers (Oxford: Oxford University Press, 2001), pp. 275-276
    @Override
    public Set<List<Double>> computeCombinations(double[] values, int sizeCombination) {
        CombinationGenerator generator = new CombinationGenerator(values.length, sizeCombination);
        Set<List<Double>> results = new HashSet<>();
        while (generator.hasMore()) {
            List<Double> combination = new ArrayList<>();
            int[] indices = generator.getNext();
            for (int index : indices) {
                combination.add(values[index]);
            }
            double sum = MathUtils.calcSum(combination);
            if (sum >= minimalSum && sum <= maximalSum) {
                Collections.sort(combination);
                if (!results.contains(combination)) {
                    results.add(combination);
                }
            }
        }
        return results;
    }

    @Override
    public void setMinimalSum(double minimalSum) {
        this.minimalSum = minimalSum;
    }

    @Override
    public void setMaximalSum(double maximalSum) {
        this.maximalSum = maximalSum;
    }
}