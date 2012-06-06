/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.logic;

import java.util.List;
import java.util.Set;

/**
 *
 * @author Niels Hulstaert
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
