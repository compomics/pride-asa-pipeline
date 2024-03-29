package com.compomics.pride_asa_pipeline.core.logic.inference;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class InferenceStatistics extends DescriptiveStatistics {

    private static final Logger LOGGER = Logger.getLogger(InferenceStatistics.class);
    private boolean absolute = false;

    public InferenceStatistics(Collection<Double> values, boolean absolute) {
        addValues(values);
    }

    public InferenceStatistics(boolean absolute) {
        this.absolute = absolute;
    }

    @Override
    public void addValue(double value) {
        if (absolute) {
            value = Math.abs(value);
        }
        super.addValue(value);
    }

    public void addValues(Collection<Double> values) {     // Add the data from the array
        for (double aWindow : values) {
            addValue(aWindow);
        }
    }

    public static double round(double value, int decimals) {
        //TODO check if this is correct 
        BigDecimal bd;
        try {
            bd = new BigDecimal(value).setScale(decimals, RoundingMode.HALF_UP);
            if (bd.equals(BigDecimal.ZERO)) {
                String number = "0.";
                for (int i = 0; i < decimals; i++) {
                    number += "0";
                }
                number += "1";
                return new BigDecimal(number).doubleValue();
            }
        } catch (NumberFormatException e) {
            bd = new BigDecimal(1);
        }
        return bd.doubleValue();
    }

    /**
     * The mass errors follow an elbowed curve. The elbow point reflects the
     * best mass error, even in case modifications or mass deviations are not
     * being picked up. Suppose a line is drawn between the two terminal points
     * on the sorted values, then the distance to this line determines where the
     * elbow point is
     *
     * @return the most optimal mass error
     */
    public double calculateOptimalMassError() {
        double optimalMassError;
        optimalMassError = getOutlierThreshold();
        return optimalMassError;
    }

    private double getOutlierThreshold() {
        if (getN() >= 30) {
            //sort values the other way?
            for (int i = getSortedValues().length - 1; i > -1; i--) {
                double aValue = getSortedValues()[i];
                //has to be bigger than the value that can be set...
                //if (Math.abs(aValue) > 0.001) {
                double zScore = (aValue - getMean()) / getStandardDeviation();
                //half of all values should be within 0.67
                if (Math.abs(zScore) <= 0.67) {
                    //return the correct value
                    return aValue;
                }
                //}
            }
        }
        LOGGER.info("Returning threshold using elbow point cutoff value");
        return getPartialMedian(getElbowPoint());
    }

    private double getPartialMedian(double cutOffValue) {
        DescriptiveStatistics temp = new DescriptiveStatistics();
        for (double aDouble : getSortedValues()) {
            if (aDouble <= cutOffValue) {
                temp.addValue(aDouble);
            } else {
                break;
            }
        }
        return temp.getMean();
    }

    private double getElbowPoint() {
        //done by triangulation of the curve ---> the point with the furthest distance to the line connecting the lowest and the highest value is the elbow point
        double[] sortedValues = getSortedValues();
        double[] point1 = new double[]{1, sortedValues[0]};
        double[] point2 = new double[]{sortedValues.length, sortedValues[sortedValues.length - 1]};

        double maxTriangleArea = Double.MIN_VALUE;
        double elbowMassError = point2[1];

        for (int x = 0; x < sortedValues.length; x++) {
            double[] point3 = new double[]{x, sortedValues[x]};
            double triangleArea = (0.5) * Math.abs((point1[0] - point3[0]) * (point2[1] - point1[1]) - (point1[0] - point2[0]) * (point3[1] - point1[1]));
            if (maxTriangleArea == -1 | triangleArea > maxTriangleArea) {
                maxTriangleArea = triangleArea;

                elbowMassError = point3[1];
            }
        }
        return elbowMassError;
    }

    public void dump(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            for (double aValue : getSortedValues()) {
                writer.append(String.valueOf(aValue)).append(System.lineSeparator());
            }
        }
    }

}
