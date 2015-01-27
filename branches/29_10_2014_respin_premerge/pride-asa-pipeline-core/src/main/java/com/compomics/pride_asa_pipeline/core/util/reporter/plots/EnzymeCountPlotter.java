/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.util.reporter.plots;

import com.compomics.pride_asa_pipeline.core.logic.parameters.PrideAsapInterpreter;
import com.compomics.util.experiment.biology.Enzyme;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author Kenneth
 */
public class EnzymeCountPlotter {

    public static void savePlotToFile(PrideAsapInterpreter interpreter, File outputfolder, String chartTitle, String xLab, String yLab) throws IOException {
        //Create the chart

        DefaultCategoryDataset dataset = createDataset(interpreter);

        JFreeChart chart = ChartFactory.createBarChart(
                chartTitle, // chart title
                xLab, // domain axis label
                yLab, // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, // orientation
                false, // include legend
                false, // tooltips?
                false // URLs?
        );
        //Save chart as PNG
        ChartUtilities.saveChartAsPNG(new File(outputfolder, chartTitle + ".png"), chart, 400, 300);
    }

    private static DefaultCategoryDataset createDataset(PrideAsapInterpreter interpreter) {
        // create the dataset...
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<Enzyme, Integer> enzymeCount : interpreter.getEnzymeCounts().entrySet()) {
            dataset.addValue(enzymeCount.getValue(), "", enzymeCount.getKey().getName());
        }
        return dataset;
    }
}
