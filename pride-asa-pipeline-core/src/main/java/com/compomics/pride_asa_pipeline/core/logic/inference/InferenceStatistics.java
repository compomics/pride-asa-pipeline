package com.compomics.pride_asa_pipeline.core.logic.inference;

import com.compomics.pride_asa_pipeline.core.logic.inference.report.impl.TotalReportGenerator;
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
    private String methodUsed;

    public InferenceStatistics(Collection<Double> values, boolean absolute) {
        addValues(values);
    }

    public InferenceStatistics(boolean absolute) {
        this.absolute = absolute;
    }

    public String getMethodUsed() {
        return methodUsed;
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
        //a lot of values are required to assume a normal distribution
        if (getN() > 1000) {
            methodUsed = ("Z-score derived outlier threshold");
            optimalMassError = getThresholdBasedOnZScore();
        } else {
            optimalMassError = getValueThroughSkewness();
        }
        return optimalMassError;
    }

    private double getThresholdBasedOnZScore() {
        double mean = getMean();
        double stdev = getStandardDeviation();
        //calculate the zScores till it becomes larger than 0.05
        for (double aValue : getSortedValues()) {
            double zScore = (aValue - mean) / stdev;
            if (zScore > 0.05) {
                return aValue;
            }
        }
        //return the mean error otherwise
        return getMean();
    }

    private double getValueThroughSkewness() {
        //check if the values are skewed to one side (ergo there is a big slope - more than 5% increase- between the min and max)
        double basicskewness = Math.abs(getMean() - getPercentile(50)) / (getMean());
        double threshold;
        LOGGER.info("The mean differs from the median by " + (100 * basicskewness) + "%");
        //if it's more than 5% difference, then there are more values that are higher...
        if (basicskewness > 0.05) {
            methodUsed = ("Elbow point to determine outlier threshold");
            threshold = getElbowPoint();
        } else {
            methodUsed = ("90th percentile to determine outlier threshold");
            threshold = getPercentile(90);
        }
        if (threshold == 0) {
            //then they either have a REALLY accurate machine, or the submission was tailored to perfect hits?
            return getMax();
        }
        return getMean();
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

    @Override
    public String toString() {
        return "N\t" + getN() + System.lineSeparator()
                + "Mean\t" + getMean() + System.lineSeparator()
                + "Min\t" + getMin() + System.lineSeparator()
                + "25p\t" + getPercentile(25) + System.lineSeparator()
                + "Median\t" + getPercentile(50) + System.lineSeparator()
                + "75p\t" + getPercentile(75) + System.lineSeparator()
                + "Max\t" + getMax();
    }

}
