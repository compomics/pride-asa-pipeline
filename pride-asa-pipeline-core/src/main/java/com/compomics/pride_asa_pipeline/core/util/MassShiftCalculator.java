/*
 * Copyright 2016 Kenneth Verheggen <kenneth.verheggen@gmail.com>.
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
package com.compomics.pride_asa_pipeline.core.util;

import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.FragmentIonAnnotation;
import com.compomics.pride_asa_pipeline.model.Identification;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class MassShiftCalculator {

    private static final FastFourierTransformer FFT = new FastFourierTransformer(DftNormalization.STANDARD);

    public static double findOptimalPrecursorShift(Collection<Identification> identifications) {
        //Load the masses
        DescriptiveStatistics stat = new DescriptiveStatistics();
        for (Identification ident : identifications) {
            if (ident.getAnnotationData() != null && ident.getAnnotationData().getFragmentIonAnnotations() != null) {
                try {
                    double ppm = (ident.getPeptide().calculateMassDelta() * 1000000) / ident.getPeptide().getMzRatio();
                    stat.addValue((ppm));
                } catch (AASequenceMassUnknownException ex) {
                    Logger.getLogger(MassShiftCalculator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        //multiply by two to cover the fact we've used absolutes?
        double shift = Math.abs(stat.getPercentile(50))* (Math.sqrt(3) / 2);
        if (shift > 50) {
            shift = 50;
        } else if (shift < 0.01) {
            shift = 0.5;
        }
        return shift;
    }

    public static double findOptimalFragmentShift(Collection<Identification> identifications) {
        DescriptiveStatistics stat = new DescriptiveStatistics();
        for (Identification ident : identifications) {
            if (ident.getAnnotationData() != null) {
                List<FragmentIonAnnotation> fragmentIonAnnotations = ident.getAnnotationData().getFragmentIonAnnotations();
                if (fragmentIonAnnotations != null) {
                    double[] experimental = new double[fragmentIonAnnotations.size()];
                    double[] theoretical = new double[fragmentIonAnnotations.size()];
                    int i = 0;
                    for (FragmentIonAnnotation fia : fragmentIonAnnotations) {
                        if (fia != null) {
                            theoretical[i] = fia.getMz() * fia.getIon_charge();
                            experimental[i] = theoretical[i] + fia.getMass_error();
                        } else {
                            theoretical[i] = 0;
                            experimental[i] = 0;
                        }
                        i++;
                    }
                    stat.addValue(findOptimalShift(theoretical, experimental));
                }
            }
        }
        return 2 * stat.getPercentile(50) * (Math.sqrt(3) / 2);
    }

    private static double findOptimalShift(double[] theoreticalMasses, double[] experimentalMasses) {
        double shift = 0.0;
        if (theoreticalMasses.length > 0) {
            shift = getOptimalShift(theoreticalMasses, experimentalMasses);
        }
        return shift;
    }

    private static Complex[] calculateXCorr(double[] first, double[] second) {

        //check for powers of 2
        int padding = first.length + second.length;
        if (!isPowerOf2(padding)) {
            first = doZeroPadding(first, padding);
            second = doZeroPadding(second, padding);
        }
        //1. FFT first series
        Complex[] firstFrequencies = FFT.transform(first, TransformType.FORWARD);
        Complex[] secondFrequencies = FFT.transform(second, TransformType.FORWARD);
        //2. Multiply 
        Complex[] fftXCorrelation = new Complex[firstFrequencies.length];
        for (int i = 0; i < firstFrequencies.length; i++) {
            fftXCorrelation[i] = firstFrequencies[i].multiply(secondFrequencies[i]);
        }
        //3. Inverse the transformation
        return FFT.transform(fftXCorrelation, TransformType.INVERSE);
    }

    private static double getOptimalShift(double[] first, double[] second) {
        //the optimal shift is found where the maximal correlation is
        Complex[] corr = calculateXCorr(first, second);
        int maxIndex = 0;
        double tempValue = 0;
        for (int i = 0; i < corr.length; i++) {
            if (corr[i].getReal() >= tempValue) {
                tempValue = corr[i].getReal();
                maxIndex = i;
            }
        }
        return (second[maxIndex] - first[maxIndex]);
    }

    private static double getBestCorrelation(double[] first, double[] second) {
        //the optimal shift is found where the maximal correlation is
        Complex[] corr = calculateXCorr(first, second);
        DescriptiveStatistics stat = new DescriptiveStatistics();
        for (int i = 0; i < corr.length; i++) {
            stat.addValue(corr[i].getReal());
        }
        System.out.println(stat);
        return stat.getMax();
    }

    private static double[] doZeroPadding(double[] values, int padding) {
        int nextPowerOf2 = nextPowerOf2(padding);
        double[] paddedValues = new double[nextPowerOf2];
        for (int i = 0; i < nextPowerOf2; i++) {
            if (i < values.length) {
                paddedValues[i] = values[i];
            } else {
                paddedValues[i] = 0;
            }
        }
        return paddedValues;
    }

    private static int nextPowerOf2(final int a) {
        int b = 1;
        while (b < a) {
            b = b << 1;
        }
        return b;
    }

    private static boolean isPowerOf2(final int num) {
        return (num & -num) == num;
    }

}
