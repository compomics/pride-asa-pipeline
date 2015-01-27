/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.util.reporter.plots;

import com.compomics.pride_asa_pipeline.core.logic.parameters.PrideAsapStats;
import java.io.File;
import java.io.IOException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Kenneth
 */
public class MassErrorPlotter {

    public static void savePlotToFile(PrideAsapStats statistics, File outputfolder, String chartTitle, String xLab, String yLab) throws IOException {
        //Create the chart
        XYDataset dataset = generateXYSeries(statistics);
        JFreeChart chart = ChartFactory.createXYLineChart(chartTitle,
                xLab, yLab, dataset, PlotOrientation.HORIZONTAL, false, false, false);
        //Save chart as PNG
        ChartUtilities.saveChartAsPNG(new File(outputfolder, chartTitle + ".png"), chart, 400, 300);
    }

    private static XYDataset generateXYSeries(PrideAsapStats statistics) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series1 = new XYSeries("");

        double[] values = statistics.getSortedValues();
        for (int i = 0; i < values.length; i++) {
            series1.add(values[i], i + 1);
        }
        dataset.addSeries(series1);
        return dataset;
    }

}
