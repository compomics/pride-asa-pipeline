package com.compomics.pride_asa_pipeline.logic.impl;

import com.compomics.pride_asa_pipeline.logic.CombinationGenerator;
import com.compomics.pride_asa_pipeline.logic.CombinationGenerator;
import com.compomics.pride_asa_pipeline.logic.ZenArcher;
import com.compomics.pride_asa_pipeline.util.MathUtils;
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
        Set<List<Double>> results = new HashSet<List<Double>>();
        while (generator.hasMore()) {
            List<Double> combination = new ArrayList<Double>();
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