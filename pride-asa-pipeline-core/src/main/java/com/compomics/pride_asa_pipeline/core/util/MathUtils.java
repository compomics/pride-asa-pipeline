package com.compomics.pride_asa_pipeline.core.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Florian Reisinger Date: 10-Aug-2009
 * @since $version
 */
public class MathUtils {

    public static final Integer NUMBER_OF_DECIMALS = 4;

    /**
     * Utility method to calculate the median of a given list of values. Note:
     * this will throw an IllegalStateException if the provided argument is null
     * or empty.
     *
     * @param values the double values for which to calculate the median.
     * @return the median of the provided values
     * @throws IllegalStateException if the provided argument is null or empty.
     */
    public static double calculateMedian(List<Double> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalStateException("Can not calculate median on empty list or null!");
        }

        // if there is only one value, then it automatically is the median
        if (values.size() == 1) {
            return values.get(0);
        }

        double[] valueArray = toArray(values);
        return calculateMedian(valueArray);
    }

    public static double calculateMedian(double[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Can not calculate median of null or empty array!");
        }

        // if there is only one value, then it automatically is the median
        if (values.length == 1) {
            return values[0];
        }

        double result; // the median we are going to calculate
        Arrays.sort(values);

        int middle = values.length / 2; // determine the middle of the list
        if (values.length % 2 == 0) { // even number of elements (middle is between two values)
            // build the average between the two values next to the middle
            result = (values[middle] + values[middle - 1]) / 2d;
        } else { // uneven number of elements (middle is one value of the list)
            result = values[middle];
        }

        return result;
    }

    public static double calculateMean(Collection<Double> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Can not calculate mean of null or empty collection!");
        }

        return (calcSum(values) / values.size());

    }

    public static double calculateMean(double[] values) {
        if (values == null) {
            throw new IllegalArgumentException("Can not calculate mean of null!");
        }

        return (calcSum(values) / values.length);

    }

    public static double calcVariance(double[] population) {
        long n = 0;
        double mean = 0;
        double s = 0.0;

        for (double x : population) {
            n++;
            double delta = x - mean;
            mean += delta / n;
            s += delta * (x - mean);
        }
        //if you want to calculate std deviation
        //of a sample change this to (s/(n-1))
        return (s / n);
    }

    public static double calcStdDeviation(double[] population) {
        return Math.sqrt(calcVariance(population));
    }

    public static double calcSum(Collection<Double> values) {
        if (values == null) {
            throw new IllegalArgumentException("Can not calculate sum of null!");
        }
        double sum = 0D;
        for (double d : values) {
            sum += d;
        }
        return sum;
    }

    public static double calcSum(double[] values) {
        if (values == null) {
            throw new IllegalArgumentException("Can not calculate sum of null!");
        }
        double sum = 0D;
        for (double d : values) {
            sum += d;
        }
        return sum;
    }

    public static double calcVariance(double[] values, double mean) {
        double squares = 0D;
        for (double value : values) {
            squares += Math.pow((value - mean), 2);
        }
        return squares / (double) values.length;
    }

    public static double[] toArray(Collection<Double> values) {
        if (values == null) {
            throw new IllegalArgumentException("No null value allowed!");
        }
        double[] result = new double[values.size()];
        int cnt = 0;
        for (Double value : values) {
            result[cnt++] = value;
        }
        return result;
    }

    public static List<Double> toList(double[] values) {
        if (values == null) {
            throw new IllegalArgumentException("No null value allowed!");
        }
        List<Double> result = new ArrayList<Double>(values.length);
        for (double value : values) {
            result.add(value);
        }
        return result;
    }

    /**
     * Utility method for computing the factorial n! of a number n. NOTE: the
     * resulting factorial is only valid in the scope of the primitive type
     * 'long', so the largest factorial is 20!.
     *
     * @param n The integer number of which to compute the factorial.
     * @return The factorial number n!.
     */
    public static BigInteger factorial(int n) {
        BigInteger f = BigInteger.ONE;              // long f = 1;
        BigInteger g = BigInteger.valueOf(n);       // int n

        while (g.compareTo(BigInteger.ONE) == 1) {  // while (n > 1) {
            f = f.multiply(g);                      // f *= n;
            g = g.subtract(BigInteger.ONE);         // n--;
        }

        return f;

    }

    /**
     * Utility method for computing the factorial n! of a number n. NOTE: this
     * method uses BigInteger, so is in principle abel to calculate arbitrary
     * precision numbers. However, to compute the factorial of large numbers may
     * take up considerable resources and time.
     *
     * @param n The integer number of which to compute the factorial.
     * @return The factorial number n! as BigInteger.
     */
    public static BigInteger bigFactorial(int n) {
        // check that we have a number we can actually calculate the factorial for.
        if (n < 0) {
            throw new IllegalArgumentException("The factorial can only "
                    + "be calculated positive numbers.");
        }
        // handle the trivial cases
        if (n == 0 || n == 1) {
            return BigInteger.ONE;
        }
        // calculate the factorial
        BigInteger f = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            f = f.multiply(BigInteger.valueOf(i));
        }
        return f;
    }

    /**
     * This method allows to check two double values if they are equal within a
     * certain error margin. E.g. it will check if the difference between the
     * two values is smaller than the specified allowedError.
     *
     * @param a the first double to compare.
     * @param b the second double to compare.
     * @param allowedError the allowed error (or difference) between the two
     * values.
     * @return true if the two given values are equal within the allowed error.
     */
    public static boolean equalValues(double a, double b, double allowedError) {
        // the difference between the tow values a and b has to be smaller than the allowed error.
        return Math.abs(a - b) < allowedError;
    }

    /**
     * Rounds a given double to the default number of decimals.
     *
     * @param d the double
     * @return the rounded double
     */
    public static double roundDouble(double d) {
        return roundDouble(d, NUMBER_OF_DECIMALS);
    }

    /**
     * Rounds a given double with the given number of decimals.
     *
     * @param d the double
     * @param numberOfDecimals the number of decimals
     * @return the rounded double
     */
    public static double roundDouble(double d, int numberOfDecimals) {
        return roundDoubleAsBigDecimal(d, numberOfDecimals).doubleValue();
    }

    /**
     * Rounds a double with the given number of decimals and returns a
     * BigDecimal.
     *
     * @param d the double
     * @param numberOfDecimals the number of decimals
     * @return the rounded BigDecimal
     */
    public static BigDecimal roundDoubleAsBigDecimal(double d, int numberOfDecimals) {
        BigDecimal bigDecimal = new BigDecimal(d).setScale(numberOfDecimals, BigDecimal.ROUND_HALF_UP);
        return bigDecimal;
    }

    /**
     * Calculate required samplesize
     */
    public static int calculateRequiredSampleSize(double confidence, double marginOfError) {
        double z = getZValue(confidence);
        double stdev = 0.5;
        int sampleSize = (int) ((z * z) * stdev * (1 - stdev) / (marginOfError * marginOfError));
        return sampleSize;
    }

    public static double getZValue(double confidence) {
        return getZValue((int) (confidence * 100));

    }

    public static float getZValue(int confidence) {
        float z;

        switch (confidence) {
            case 70:
                z = 1.04f;
                break;
            case 75:
                z = 1.15f;
                break;
            case 80:
                z = 1.28f;
                break;
            case 85:
                z = 1.44f;
                break;
            case 90:
                z = 1.645f;
                break;
            case 92:
                z = 1.75f;
                break;
            case 95:
                z = 1.96f;
                break;
            case 96:
                z = 2.05f;
                break;
            case 98:
                z = 2.33f;
                break;
            case 99:
                z = 2.58f;
                break;
            default:
                z = 1.96f;
        }
        return z;
    }

}
