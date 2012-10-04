package com.compomics.pride_asa_pipeline.gui;

import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.util.GuiUtils;
import com.compomics.pride_asa_pipeline.util.MathUtils;
import com.google.common.primitives.Doubles;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

/**
 *
 * @author Niels Hulstaert
 * @author Harald Barsnes
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
    private static final Color PIPELINE_MODIFICATION_COLOR = Color.RED;
    private static final Color PRIDE_MODIFICATION_COLOR = Color.BLUE;

    public static JFreeChart getPrecursorMassDeltasChart(double[] precursorMassDeltaValues) {

        //create new precursor mass delta dataset
        HistogramDataset precMassDeltasDataset = new HistogramDataset();
        precMassDeltasDataset.setType(HistogramType.FREQUENCY);

        //sort array in order to find min and max values
        Arrays.sort(precursorMassDeltaValues);
        precMassDeltasDataset.addSeries("precursorMassDeltaSeries", precursorMassDeltaValues, NUMBER_OF_PRECURSOR_MASS_DELTA_BINS, precursorMassDeltaValues[0] - 0.5, precursorMassDeltaValues[precursorMassDeltaValues.length - 1] + 0.5);
        JFreeChart precursorMassDeltasChart = org.jfree.chart.ChartFactory.createHistogram(
                "Precursor Mass Delta", "mass delta (d.)", "frequency", precMassDeltasDataset,
                PlotOrientation.VERTICAL, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
        precursorMassDeltasChart.getPlot().setBackgroundPaint(Color.WHITE);
        precursorMassDeltasChart.getPlot().setOutlineVisible(false);
        GuiUtils.setShadowVisible(precursorMassDeltasChart, Boolean.FALSE);

        return precursorMassDeltasChart;
    }

    public static JFreeChart getFragmentMassDeltasChart(List<Double> fragmentMassDeltaValues) {

        //create new fragment ion mass delta dataset
        HistogramDataset fragMassDeltasDataset = new HistogramDataset();
        fragMassDeltasDataset.setType(HistogramType.FREQUENCY);

        //sort array in order to find min and max values
        Collections.sort(fragmentMassDeltaValues);
        fragMassDeltasDataset.addSeries("precursorMassDeltaSeries", Doubles.toArray(fragmentMassDeltaValues), NUMBER_OF_ION_FRAGMENT_MASS_DELTA_BINS, fragmentMassDeltaValues.get(0) - 0.5, fragmentMassDeltaValues.get(fragmentMassDeltaValues.size() - 1) + 0.5);
        JFreeChart fragmentMassDeltasChart = org.jfree.chart.ChartFactory.createHistogram(
                "Fragment Ion Mass Delta", "mass delta (d.)", "frequency", fragMassDeltasDataset,
                PlotOrientation.VERTICAL, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
        fragmentMassDeltasChart.getPlot().setBackgroundPaint(Color.WHITE);
        fragmentMassDeltasChart.getPlot().setOutlineVisible(false);
        GuiUtils.setShadowVisible(fragmentMassDeltasChart, Boolean.FALSE);

        return fragmentMassDeltasChart;
    }

    public static JFreeChart getIonCoverageChart(String title, double[] ion1CoverageValues, double[] ion2CoverageValues) {

        //create new identification scores dataset
        HistogramDataset ionCoverageDataset = new HistogramDataset();
        ionCoverageDataset.setType(HistogramType.RELATIVE_FREQUENCY);

        ionCoverageDataset.addSeries("1+ ladder", ion1CoverageValues, NUMBER_OF_ION_COVERAGE_BINS, 0.0, 100.0);
        ionCoverageDataset.addSeries("2+ ladder", ion2CoverageValues, NUMBER_OF_ION_COVERAGE_BINS, 0.0, 100.0);
        JFreeChart ionCoverageChart = org.jfree.chart.ChartFactory.createHistogram(
                title, "coverage (%)", "rel. freq.", ionCoverageDataset,
                PlotOrientation.VERTICAL, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        ionCoverageChart.getPlot().setBackgroundPaint(Color.WHITE);
        ionCoverageChart.getPlot().setForegroundAlpha(0.8F);
        ionCoverageChart.getPlot().setOutlineVisible(false);
        GuiUtils.setShadowVisible(ionCoverageChart, Boolean.FALSE);

        return ionCoverageChart;
    }

    public static JFreeChart getScoresChart(double[] scoresValues) {

        //create new identification scores dataset
        HistogramDataset scoresDataset = new HistogramDataset();
        scoresDataset.setType(HistogramType.FREQUENCY);

        scoresDataset.addSeries("scoreSeries", scoresValues, NUMBER_OF_SCORE_BINS, 0.0, 1.0);
        JFreeChart scoresChart = org.jfree.chart.ChartFactory.createHistogram(
                "Fragment Ion Score", "score", "frequency", scoresDataset,
                PlotOrientation.VERTICAL, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
        scoresChart.getPlot().setBackgroundPaint(Color.WHITE);
        scoresChart.getPlot().setOutlineVisible(false);
        GuiUtils.setShadowVisible(scoresChart, Boolean.FALSE);

        return scoresChart;
    }

    public static JFreeChart getIdentificationsChart(SpectrumAnnotatorResult spectrumAnnotatorResult) {

        //create new identificiations data set
        DefaultPieDataset identificationsDataset = new DefaultPieDataset();
        identificationsDataset.setValue(UNMODIFIED_LABEL + "(" + spectrumAnnotatorResult.getUnmodifiedPrecursors().size() + ", " + (MathUtils.roundDouble(((double) spectrumAnnotatorResult.getUnmodifiedPrecursors().size() / spectrumAnnotatorResult.getNumberOfIdentifications()) * 100, 2)) + "%)", spectrumAnnotatorResult.getUnmodifiedPrecursors().size());
        identificationsDataset.setValue(MODIFIED_LABEL + "(" + spectrumAnnotatorResult.getModifiedPrecursors().size() + ", " + (MathUtils.roundDouble(((double) spectrumAnnotatorResult.getModifiedPrecursors().size() / spectrumAnnotatorResult.getNumberOfIdentifications()) * 100, 2)) + "%)", spectrumAnnotatorResult.getModifiedPrecursors().size());
        identificationsDataset.setValue(UNEXPLAINED_LABEL + "(" + spectrumAnnotatorResult.getUnexplainedIdentifications().size() + ", " + (MathUtils.roundDouble(((double) spectrumAnnotatorResult.getUnexplainedIdentifications().size() / spectrumAnnotatorResult.getNumberOfIdentifications()) * 100, 2)) + "%)", spectrumAnnotatorResult.getUnexplainedIdentifications().size());

        JFreeChart identificationsChart = org.jfree.chart.ChartFactory.createPieChart(
                "Identifications (" + spectrumAnnotatorResult.getNumberOfIdentifications() + ")", // chart title
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
        
        // remove the shadow
        plot.setShadowXOffset(0);
        plot.setShadowYOffset(0);
        
        //change pie colors
        for (int i = 0; i < identificationsDataset.getItemCount(); i++) {
            Comparable key = identificationsDataset.getKey(i);
            plot.setSectionPaint(key, PIE_COLORS[i]);
        }

        identificationsChart.getPlot().setOutlineVisible(false);
        return identificationsChart;
    }

    public static JFreeChart getModificationsChart(Map<Modification, Integer> modifications, int numberOfModifications) {

        //keep track of origins for color rendering
        Modification.Origin[] origins = new Modification.Origin[numberOfModifications];
        int index = 0;
        DefaultCategoryDataset modificationsDataset = new DefaultCategoryDataset();
        for (Modification modification : modifications.keySet()) {
            double relativeCount = (double) (modifications.get(modification)) / (double) (numberOfModifications);
            modificationsDataset.addValue(relativeCount, "relative occurance", modification.getName());
            origins[index] = modification.getOrigin();
            index++;
        }

        JFreeChart modificationsChart = org.jfree.chart.ChartFactory.createBarChart(
                "Modifications",
                "modification",
                "relative occurance",
                modificationsDataset,
                PlotOrientation.VERTICAL, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);

        CategoryPlot countPlot = modificationsChart.getCategoryPlot();
        //add custom legend
        addModificationsLegend(modifications.keySet(), countPlot);
        //set custom renderer
        countPlot.setRenderer(new ModificationsRenderer(origins));
        countPlot.getRenderer().setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        countPlot.setBackgroundPaint(Color.WHITE);
        CategoryAxis xAxis = (CategoryAxis) countPlot.getDomainAxis();
        xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        xAxis.setMaximumCategoryLabelLines(2);
        modificationsChart.getPlot().setOutlineVisible(false);
        GuiUtils.setShadowVisible(modificationsChart, Boolean.FALSE);

        return modificationsChart;
    }

    private static void addModificationsLegend(Set<Modification> modifications, CategoryPlot countPlot) {
        LegendItemCollection legendItemCollection = new LegendItemCollection();
        if (!modifications.isEmpty()) {
            if (containsModificationsFromOrigin(modifications, Modification.Origin.PIPELINE)) {
                LegendItem pipelineModLegendItem = new LegendItem("pipeline modification(s)", "-", null, null, Plot.DEFAULT_LEGEND_ITEM_BOX, PIPELINE_MODIFICATION_COLOR);
                legendItemCollection.add(pipelineModLegendItem);
            }
            if (containsModificationsFromOrigin(modifications, Modification.Origin.PRIDE)) {
                LegendItem prideModLegendItem = new LegendItem("pride modification(s)", "-", null, null, Plot.DEFAULT_LEGEND_ITEM_BOX, PRIDE_MODIFICATION_COLOR);
                legendItemCollection.add(prideModLegendItem);
            }
            countPlot.setFixedLegendItems(legendItemCollection);
        }
    }

    private static boolean containsModificationsFromOrigin(Set<Modification> modifications, Modification.Origin origin) {
        boolean containsModificationsFromOrigin = Boolean.FALSE;
        for (Modification modification : modifications) {
            if (modification.getOrigin().equals(origin)) {
                containsModificationsFromOrigin = Boolean.TRUE;
                break;
            }
        }

        return containsModificationsFromOrigin;
    }

    private static class ModificationsRenderer extends BarRenderer {

        private Modification.Origin[] origins;

        public ModificationsRenderer(Modification.Origin[] origins) {
            super();
            this.origins = origins;
        }

        @Override
        public Paint getItemPaint(int row, int column) {
            if (origins[column].equals(Modification.Origin.PIPELINE)) {
                return PIPELINE_MODIFICATION_COLOR;
            } else {
                return PRIDE_MODIFICATION_COLOR;
            }
        }
    }
}
