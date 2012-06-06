/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.utils;

import com.compomics.pride_asa_pipeline.util.MathUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import static junit.framework.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Niels Hulstaert
 */
public class MathUtilsTest {

    private static double[] values_1 = new double[]{10.5, 27.9, 30.0, 85.7, 23.6};
    private static double[] values_2 = new double[]{10.5, 27.9, 30.0, 23.6};
    private static double[] values_3 = new double[]{6.48889631997186, 11.9457046284258, 8.37896610112283, 9.94108092615248,
        7.36294758637606, 11.0792730197557, 10.6855887324832, 8.85200215121014,
        11.6798518015736, 9.85178491475201};
    private static List<Double> values_4;
    private static List<Double> values_5;
    
    @BeforeClass
    public static void setUponce(){
        values_4 = new ArrayList<Double>();
        for(double d : values_1){
            values_4.add(d);
        }
        values_5 = new ArrayList<Double>();
        for(double d : values_2){
            values_5.add(d);
        }        
    }
    
    @Test
    public void testCalculateMedian() {
        double median_1 = MathUtils.calculateMedian(values_1);
        assertEquals(27.9, median_1, 0.01);
        double median_2 = MathUtils.calculateMedian(values_2);
        assertEquals(25.75, median_2, 0.01);
        double median_3 = MathUtils.calculateMedian(values_4);
        assertEquals(27.9, median_3, 0.01);
        double median_4 = MathUtils.calculateMedian(values_5);
        assertEquals(25.75, median_4, 0.01);
        
    }

    @Test
    public void testCalculateMean() {
        double mean_1 = MathUtils.calculateMean(values_1);
        assertEquals(35.54, mean_1, 0.01);
        double mean_2 = MathUtils.calculateMean(values_4);
        assertEquals(35.54, mean_2, 0.01);
    }

    @Test
    public void testCalculateVariance_1() {
        double variance = MathUtils.calcVariance(values_3);
        assertEquals(3.010, variance, 0.01);
    }
    
    @Test
    public void testCalculateVariance_2() {
        double mean = MathUtils.calculateMean(values_3);
        double variance = MathUtils.calcVariance(values_3, mean);
        assertEquals(3.010, variance, 0.01);
    }

    @Test
    public void testStdVariation() {
        double stdVariation = MathUtils.calcStdDeviation(values_3);
        assertEquals(1.7349, stdVariation, 0.01);
    }
    
    @Test
    public void testCalculateSum() {
        double sum_1 = MathUtils.calcSum(values_1);
        assertEquals(177.7, sum_1, 0.01);
        double sum_2 = MathUtils.calcSum(values_4);
        assertEquals(177.7, sum_2, 0.01);
    }
    
    @Test
    public void testToArray() {        
        double[] values = MathUtils.toArray(values_4);
        for(int i = 0; i < values.length; i++){
            assertEquals(values_4.get(i), values[i], 0.01);
        }        
    }
    
    @Test
    public void testToList() {
        List<Double> values = MathUtils.toList(values_1);
        for(int i = 0; i < values.size(); i++){
            assertEquals(values_1[i], values.get(i), 0.01);
        }
    }

    @Test
    public void testFactorial() {
        long factorial = MathUtils.factorial(8);
        assertEquals(40320, factorial);
    }

    @Test
    public void testBigFactorial() {
        BigInteger factorial = MathUtils.bigFactorial(8);
        assertEquals(BigInteger.valueOf(40320), factorial);
    }
}
