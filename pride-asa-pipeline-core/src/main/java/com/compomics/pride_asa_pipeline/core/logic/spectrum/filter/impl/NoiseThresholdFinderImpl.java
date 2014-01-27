package com.compomics.pride_asa_pipeline.core.logic.spectrum.filter.impl;

import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.logic.spectrum.filter.NoiseThresholdFinder;
import com.compomics.pride_asa_pipeline.core.util.MathUtils;
import com.google.common.primitives.Doubles;

/**
 * @author Florian Reisinger Date: 10-Sep-2009
 * @since 0.1
 */
public class NoiseThresholdFinderImpl implements NoiseThresholdFinder {        

    /**
     * There are different 3 spectrum scenarios this noise threshold finder
     * considers: 1. a spectrum with few signal peaks compared to the noise
     * peaks 2. a spectrum consisting out of signal peaks only 3. a spectrum
     * consisting out of noise peaks only
     *
     * @param signalValues the double array of signal values
     * @return the double threshold value
     */
    @Override
    public double findNoiseThreshold(double[] signalValues) {
        /**
         * the value to multiply the standard deviation with to define the
         * outlier
         */
        double outlierLimit = PropertiesConfigurationHolder.getInstance().getDouble("winsorisation.outlier_limit");
        double meanRatioThreshold = PropertiesConfigurationHolder.getInstance().getDouble("noisethresholdfinder.mean_ratio_threshold");
        double densityThreshold = PropertiesConfigurationHolder.getInstance().getDouble("noisethresholdfinder.density_threshold");
        double noiseThreshold = 0.0;

        //calculate mean and standard deviation
        double mean = MathUtils.calculateMean(signalValues);
        double standardDeviation = Math.sqrt(MathUtils.calcVariance(signalValues, mean));

        //first use a winsonrisation approach (with the preset configuration) to find
        //the noise treshold for the privided signal values.
        double[] winsorisedValues = winsorise(signalValues);

        double winsorisedMedian = MathUtils.calculateMedian(winsorisedValues);
        double winsorisedMean = MathUtils.calculateMean(winsorisedValues);
        double winsorisedStandardDeviation = Math.sqrt(MathUtils.calcVariance(winsorisedValues, winsorisedMean));

        //check if the winsorised mean to mean ratio exceeds a given threshold
        double meanRatio = ((winsorisedMean - 0.0) < 0.001) ? 0.0 : winsorisedMean / mean;
//        double standardDeviationRatio = ((winsorisedStandardDeviation - 0.0) < 0.001) ? 0.0 : winsorisedStandardDeviation / standardDeviation;

        if (meanRatio < meanRatioThreshold) {
            //scenario 1, the winsorisation has significantly decreased the mean
            //calculate the noise threshold for the spectrum (based on the winsorisation result)
            noiseThreshold = winsorisedMedian + outlierLimit * winsorisedStandardDeviation;
        } //scenario 2 or 3
        //to make a distinction between the only signal or only noise spectrum, check the peak density
        else {
            double minimumValue = Doubles.min(signalValues);
            double maximumValue = Doubles.max(signalValues);
            //peak density: number of peaks / dalton
            double density = signalValues.length / (maximumValue - minimumValue);
            if (density < densityThreshold) {
                //scenario 2
                noiseThreshold = Math.max(mean - (1.5 * standardDeviation), 0.0);
            } else {
                //scenario 3
                noiseThreshold = mean + 1.5 * standardDeviation;
            }
        }

        return noiseThreshold;
    }

    ///// ///// ///// ///// ////////// ///// ///// ///// /////
    // private methods
    private double[] winsorise(double[] signalValues) {
        double winsorisationConstant = PropertiesConfigurationHolder.getInstance().getDouble("winsorisation.constant");
        double convergenceCriterium = PropertiesConfigurationHolder.getInstance().getDouble("winsorisation.convergence_criterion");

        double median = MathUtils.calculateMedian(signalValues);
        double currMAD = calcIntensityMAD(signalValues, median);
        double prevMAD = 3d * currMAD; //initial start value
        double[] correctedIntensities = new double[signalValues.length];

        while (((prevMAD - currMAD) / prevMAD) >= convergenceCriterium) {
            correctedIntensities = reduceOutliers(signalValues, median + (winsorisationConstant * currMAD));
            prevMAD = currMAD;
            currMAD = calcIntensityMAD(correctedIntensities, median);

            //ToDo: history reporting?
            //if we want to record the history, we can here capture the current treshold (possibly together with the iteration, if not: give e.g. a list)
            //double hypotheticalNoiseThreshold = median + OUTLIER_TH * currMAD;
        }

        return correctedIntensities;
    }

    private double calcIntensityMAD(double[] values, double median) {
        double[] diffs = new double[values.length];
        int cnt = 0;
        for (double p : values) {
            diffs[cnt++] = (Math.abs(p - median));
        }

        return MathUtils.calculateMedian(diffs);
    }

    private double[] reduceOutliers(double[] intensities, double maxIntensityLimit) {
        double[] correctedIntensities = new double[intensities.length];
        //sets all the values above the limit (outliers) to the limit
        //and therefore effectively eliminating outliers
        for (int i = 0; i < intensities.length; i++) {
            if (intensities[i] <= maxIntensityLimit) {
                correctedIntensities[i] = intensities[i];
            } else {
                correctedIntensities[i] = maxIntensityLimit;
            }
        }
        return correctedIntensities;
    }
}
