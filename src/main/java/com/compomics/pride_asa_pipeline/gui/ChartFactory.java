/*
 */
package com.compomics.pride_asa_pipeline.gui;

import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.util.GuiUtils;
import com.compomics.pride_asa_pipeline.util.MathUtils;
import com.google.common.primitives.Doubles;
import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

/**
 *
 * @author Niels Hulstaert
 */
public class ChartFactory {
    
    private static final String MODIFIED_LABEL = "modified";
    private static final String UNMODIFIED_LABEL = "unmodified";
    private static final String UNEXPLAINED_LABEL = "unexplained";
    private static final int NUMBER_OF_PRECURSOR_MASS_DELTA_BINS = 100;
    private static final int NUMBER_OF_ION_FRAGMENT_MASS_DELTA_BINS = 50;
    private static final int NUMBER_OF_ION_COVERAGE_BINS = 100;
    private static final int NUMBER_OF_SCORE_BINS = 100;
    private static final Color[] PIE_COLORS = new Color[]{Color.GREEN, Color.ORANGE, Color.RED};
    
    public static JFreeChart getPrecursorMassDeltasChart(double[] precursorMassDeltaValues) {
        JFreeChart precursorMassDeltasChart = null;

        //create new precursor mass delta dataset
        HistogramDataset precMassDeltasDataset = new HistogramDataset();
        precMassDeltasDataset.setType(HistogramType.FREQUENCY);

        //sort array in order to find min and max values
        Arrays.sort(precursorMassDeltaValues);
        precMassDeltasDataset.addSeries("precursorMassDeltaSeries", precursorMassDeltaValues, NUMBER_OF_PRECURSOR_MASS_DELTA_BINS, precursorMassDeltaValues[0] - 0.5, precursorMassDeltaValues[precursorMassDeltaValues.length - 1] + 0.5);
        precursorMassDeltasChart = org.jfree.chart.ChartFactory.createHistogram(
                "Precursor mass delta", "mass delta (d.)", "frequency", precMassDeltasDataset,
                PlotOrientation.VERTICAL, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
        precursorMassDeltasChart.getPlot().setBackgroundPaint(Color.WHITE);
        GuiUtils.setShadowVisible(precursorMassDeltasChart, Boolean.FALSE);
        
        return precursorMassDeltasChart;
    }
    
    public static JFreeChart getFragmentMassDeltasChart(List<Double> fragmentMassDeltaValues) {
        JFreeChart fragmentMassDeltasChart = null;

        //create new fragment ion mass delta dataset
        HistogramDataset fragMassDeltasDataset = new HistogramDataset();
        fragMassDeltasDataset.setType(HistogramType.FREQUENCY);

        //sort array in order to find min and max values
        Collections.sort(fragmentMassDeltaValues);
        fragMassDeltasDataset.addSeries("precursorMassDeltaSeries", Doubles.toArray(fragmentMassDeltaValues), NUMBER_OF_ION_FRAGMENT_MASS_DELTA_BINS, fragmentMassDeltaValues.get(0) - 0.5, fragmentMassDeltaValues.get(fragmentMassDeltaValues.size() - 1) + 0.5);
        fragmentMassDeltasChart = org.jfree.chart.ChartFactory.createHistogram(
                "Fragment ion mass delta", "mass delta (d.)", "frequency", fragMassDeltasDataset,
                PlotOrientation.VERTICAL, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
        fragmentMassDeltasChart.getPlot().setBackgroundPaint(Color.WHITE);
        GuiUtils.setShadowVisible(fragmentMassDeltasChart, Boolean.FALSE);
        
        return fragmentMassDeltasChart;
    }
    
    public static JFreeChart getIonCoverageChart(String title, double[] ion1CoverageValues, double[] ion2CoverageValues) {
        JFreeChart ionCoverageChart = null;

        //create new identification scores dataset
        HistogramDataset ionCoverageDataset = new HistogramDataset();
        ionCoverageDataset.setType(HistogramType.RELATIVE_FREQUENCY);        
        
        ionCoverageDataset.addSeries("1+ ladder", ion1CoverageValues, NUMBER_OF_ION_COVERAGE_BINS, 0.0, 100.0);
        ionCoverageDataset.addSeries("2+ ladder", ion2CoverageValues, NUMBER_OF_ION_COVERAGE_BINS, 0.0, 100.0);
        ionCoverageChart = org.jfree.chart.ChartFactory.createHistogram(
                title, "coverage (%)", "rel. freq.", ionCoverageDataset,
                PlotOrientation.VERTICAL, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        ionCoverageChart.getPlot().setBackgroundPaint(Color.WHITE);
        ionCoverageChart.getPlot().setForegroundAlpha(0.8F);
        GuiUtils.setShadowVisible(ionCoverageChart, Boolean.FALSE);
        
        return ionCoverageChart;
    }
    
    public static JFreeChart getScoresChart(double[] scoresValues) {
        JFreeChart scoresChart = null;

        //create new identification scores dataset
        HistogramDataset scoresDataset = new HistogramDataset();
        scoresDataset.setType(HistogramType.FREQUENCY);
        
        scoresDataset.addSeries("scoreSeries", scoresValues, NUMBER_OF_SCORE_BINS, 0.0, 1.0);
        scoresChart = org.jfree.chart.ChartFactory.createHistogram(
                "Fragment ion score", "score", "frequency", scoresDataset,
                PlotOrientation.VERTICAL, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
        scoresChart.getPlot().setBackgroundPaint(Color.WHITE);
        GuiUtils.setShadowVisible(scoresChart, Boolean.FALSE);
        
        return scoresChart;
    }
    
    public static JFreeChart getIdentificationsChart(SpectrumAnnotatorResult spectrumAnnotatorResult) {
        JFreeChart identificationsChart = null;

        //create new identificiations data set
        DefaultPieDataset identificationsDataset = new DefaultPieDataset();
        identificationsDataset.setValue(UNMODIFIED_LABEL + "(" + spectrumAnnotatorResult.getUnmodifiedPrecursors().size() + ", " + (MathUtils.roundDouble(((double) spectrumAnnotatorResult.getUnmodifiedPrecursors().size() / spectrumAnnotatorResult.getNumberOfIdentifications()) * 100, 2)) + "%)", spectrumAnnotatorResult.getUnmodifiedPrecursors().size());
        identificationsDataset.setValue(MODIFIED_LABEL + "(" + spectrumAnnotatorResult.getModifiedPrecursors().size() + ", " + (MathUtils.roundDouble(((double) spectrumAnnotatorResult.getModifiedPrecursors().size() / spectrumAnnotatorResult.getNumberOfIdentifications()) * 100, 2)) + "%)", spectrumAnnotatorResult.getModifiedPrecursors().size());
        identificationsDataset.setValue(UNEXPLAINED_LABEL + "(" + spectrumAnnotatorResult.getUnexplainedIdentifications().size() + ", " + (MathUtils.roundDouble(((double) spectrumAnnotatorResult.getUnexplainedIdentifications().size() / spectrumAnnotatorResult.getNumberOfIdentifications()) * 100, 2)) + "%)", spectrumAnnotatorResult.getUnexplainedIdentifications().size());
        
        identificationsChart = org.jfree.chart.ChartFactory.createPieChart3D(
                "Identifications(" + spectrumAnnotatorResult.getNumberOfIdentifications() + ")", // chart title
                identificationsDataset, // data
                Boolean.TRUE, // include legend
                Boolean.FALSE,
                Boolean.FALSE);
        
        PiePlot plot = (PiePlot) identificationsChart.getPlot();
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setLabelGenerator(null);
        plot.setNoDataMessage("No data available");
        plot.setCircular(Boolean.TRUE);
        plot.setLabelGap(0.02);
        plot.setBackgroundPaint(Color.WHITE);
        //change pie colors
        for (int i = 0; i < identificationsDataset.getItemCount(); i++) {
            Comparable key = identificationsDataset.getKey(i);
            plot.setSectionPaint(key, PIE_COLORS[i]);
        }
        
        return identificationsChart;
    }
    
    public static JFreeChart getModificationsChart(Map<Modification, Integer> modifications, int numberOfModifications) {
        JFreeChart modificationsChart = null;

        //create new modifications data set
        DefaultCategoryDataset modificationsDataset = new DefaultCategoryDataset();
        for (Modification modification : modifications.keySet()) {
            double relativeCount = (double) (modifications.get(modification)) / (double) (numberOfModifications);
            modificationsDataset.addValue(relativeCount, "relative occurance", modification.getName());
        }
        
        modificationsChart = org.jfree.chart.ChartFactory.createBarChart(
                "Modifications",
                "modification",
                "relative occurance",
                modificationsDataset,
                PlotOrientation.VERTICAL, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
        CategoryPlot countPlot = (CategoryPlot) modificationsChart.getPlot();
        countPlot.setBackgroundPaint(Color.WHITE);
        CategoryAxis xAxis = (CategoryAxis) countPlot.getDomainAxis();
        xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        xAxis.setMaximumCategoryLabelLines(2);
        GuiUtils.setShadowVisible(modificationsChart, Boolean.FALSE);
        
        return modificationsChart;
    }
}
