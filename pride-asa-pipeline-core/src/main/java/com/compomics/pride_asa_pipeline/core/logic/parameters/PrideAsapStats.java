/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.parameters;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class PrideAsapStats extends DescriptiveStatistics {

    private static final Logger LOGGER = Logger.getLogger(PrideAsapStats.class);
    private boolean absolute = false;

    public PrideAsapStats(Collection<Double> values, boolean absolute) {
        addValues(values);
    }

    PrideAsapStats(boolean absolute) {
        this.absolute = absolute;
    }

    @Override
    public void addValue(double value) {
        if (absolute) {
            value = Math.abs(value);
        }
        super.addValue(value);
    }

    public double getBinnedMode(double binSize) {
        HashMap<Double, Integer> binCount = new HashMap<>();
        //fill in bins...
        double cursor = getMin();
        while (cursor <= getMax()) {
            binCount.put(cursor, 0);
            cursor += binSize;
        }
        //make an iterator (fastest way)
        Iterator<Map.Entry<Double, Integer>> iterator = binCount.entrySet().iterator();
        Map.Entry<Double, Integer> next = iterator.next();
        double mode = 0;
        //fill up the hashmap on the sorted values = only 1 iteration needed
        for (Double aDouble : getSortedValues()) {
            if (aDouble > next.getKey() && iterator.hasNext()) {
                //if the double is on a new bin, switch to it
                if (next.getValue() > mode) {
                    //if this bin has a higher count than the last one, use it
                    //this way, the largest mode wins!
                    mode = next.getKey();
                }
                next = iterator.next();
            }
            binCount.put(next.getKey(), next.getValue() + 1);
        }
        return mode;
    }

    public void addValues(Collection<Double> values) {     // Add the data from the array
        for (double aWindow : values) {
            addValue(aWindow);
        }
    }

    public long getOutOfConfidence(double confidence) {
        //confidenceth percentile = kept, so 1-confidenceth percent is lost
        return Math.round((double) getValues().length * (1 - confidence));
    }

    public static double round(double value) {
        //TODO check if this is correct 
        BigDecimal bd;
        try {
            bd = new BigDecimal(value).setScale(5, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            bd = new BigDecimal(1);
        }
        return bd.doubleValue();
    }

    public double getDropOffValue() {
        double[] temps = getSortedValues();
        double precAcc = 1.0;
        if (temps.length > 1) {
            double maxDifference = Math.abs(temps[0] - temps[1]);
            for (int i = 0; i < temps.length - 1; i++) {
                double currentDifference = Math.abs(temps[i] - temps[i + 1]);
                if (currentDifference > maxDifference) {
                    maxDifference = currentDifference;
                    precAcc = temps[i];
                }
            }
        }
        return precAcc;
    }

    /**
     *
     * @param parameter (1 for mean, 2 for variance, 3 for max)
     * @return
     */
    public double getPostDropStatistic(int parameter) {
        double[] temps = getSortedValues();
        DescriptiveStatistics stats = new DescriptiveStatistics();
        double dropoff = getDropOffValue();
        if (temps.length > 1) {
           for(int counter=temps.length - 1; counter >= 0;counter--){
                if (temps[counter] == dropoff) {
                    break;
                }
                stats.addValue(temps[counter]);
            }
        }
        switch (parameter) {
            case 1:
                return stats.getMean();
            case 2:
                return stats.getVariance();
            case 3:
                return stats.getMax();
            default:
                throw new UnsupportedOperationException("Please use options 1-3");
        }

    }

}
