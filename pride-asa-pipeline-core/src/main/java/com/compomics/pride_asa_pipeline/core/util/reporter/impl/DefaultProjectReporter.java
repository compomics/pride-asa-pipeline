/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.util.reporter.impl;

import com.compomics.pride_asa_pipeline.core.logic.inference.parameters.PrideAsapExtractor;
import com.compomics.pride_asa_pipeline.core.util.reporter.ProjectReporter;
import com.compomics.pride_asa_pipeline.core.util.reporter.plots.MassErrorPlotter;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class DefaultProjectReporter extends ProjectReporter {

    private final File outputFolder;
    private final static Logger LOGGER = Logger.getLogger(DefaultProjectReporter.class);

    public DefaultProjectReporter(PrideAsapExtractor extractor, File outputFolder) {
        super(extractor);
        this.outputFolder = outputFolder;
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
    }

    private void generateModReport() {
        File modReport = new File(outputFolder, "modification_report.tsv");
        try (FileWriter modWriter = new FileWriter(modReport)) {
            modWriter.append("Modification\tRate\tConsidered" + System.lineSeparator()).flush();
            for (Map.Entry<Modification, Double> entry : extractor.getModificationRates().entrySet()) {
                modWriter.append(entry.getKey().getName() + "\t"
                        + 100 * entry.getValue() + "\t"
                        + (entry.getValue() >= extractor.getConsiderationThreshold()) + System.lineSeparator()).flush();
            }
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }


    private void generatePrecursorAccReport() {
        File precAccReport = new File(outputFolder, "prec_acc_report.tsv");
        try (FileWriter precAccWriter = new FileWriter(precAccReport)) {
            precAccWriter.append("Mass Delta (da)\tValue" + System.lineSeparator()).flush();
            precAccWriter.append("Mean\t" + extractor.getPrecursorStats().getMean() + System.lineSeparator()).flush();
            precAccWriter.append("Variance\t" + extractor.getPrecursorStats().getVariance() + System.lineSeparator()).flush();
            precAccWriter.append("" + System.lineSeparator()).flush();
            precAccWriter.append("Minimum\t" + extractor.getPrecursorStats().getMin() + System.lineSeparator()).flush();
            precAccWriter.append("1st Quartile\t" + extractor.getPrecursorStats().getPercentile(25) + System.lineSeparator()).flush();
            precAccWriter.append("Median delta\t" + extractor.getPrecursorStats().getPercentile(50) + System.lineSeparator()).flush();
            precAccWriter.append("3rd Quartile\t" + extractor.getPrecursorStats().getPercentile(75) + System.lineSeparator()).flush();
            precAccWriter.append("Maximum\t" + extractor.getPrecursorStats().getMax() + System.lineSeparator()).flush();
            precAccWriter.append(System.lineSeparator()).flush();
            precAccWriter.append("Post Drop-Off Cut" + System.lineSeparator()).flush();
            precAccWriter.append("Consensus\t" + extractor.getPrecursorAccuraccy() + System.lineSeparator()).flush();
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }

    private void generateFragmentIonReport() {
        File fragAccReport = new File(outputFolder, "frag_acc_report.tsv");
        try (FileWriter fragAccWriter = new FileWriter(fragAccReport)) {
            fragAccWriter.append("Mass Delta (da)\tValue" + System.lineSeparator()).flush();
            fragAccWriter.append("Mean\t" + extractor.getFragmentIonStats().getMean() + System.lineSeparator()).flush();
            fragAccWriter.append("Variance\t" + extractor.getFragmentIonStats().getVariance() + System.lineSeparator()).flush();
            fragAccWriter.append("" + System.lineSeparator()).flush();
            fragAccWriter.append("Minimum\t" + extractor.getFragmentIonStats().getMin() + System.lineSeparator()).flush();
            fragAccWriter.append("1st Quartile\t" + extractor.getFragmentIonStats().getPercentile(25) + System.lineSeparator()).flush();
            fragAccWriter.append("Median delta\t" + extractor.getFragmentIonStats().getPercentile(50) + System.lineSeparator()).flush();
            fragAccWriter.append("3rd Quartile\t" + extractor.getFragmentIonStats().getPercentile(75) + System.lineSeparator()).flush();
            fragAccWriter.append("Maximum\t" + extractor.getFragmentIonStats().getMax() + System.lineSeparator()).flush();
            fragAccWriter.append(System.lineSeparator()).flush();
            fragAccWriter.append("Consensus\t" + extractor.getFragmentIonAccuraccy() + System.lineSeparator()).flush();
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }

    private void generateMassErrorGraphs() {
        String yLab = "";
        String xLab = "Mass Delta (da)";
        try {
            MassErrorPlotter.savePlotToFile(extractor.getPrecursorStats(), outputFolder, "Precursor Error", xLab, yLab);
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
        try {
            MassErrorPlotter.savePlotToFile(extractor.getFragmentIonStats(), outputFolder, "Fragment Ion Error", xLab, yLab);
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }



    @Override
    public void generateReport() {
        LOGGER.info("Pride asap extraction report");
        LOGGER.info("General parameters");
        LOGGER.info("___________________________");
        LOGGER.info("Generating modification report");
        generateModReport();
        LOGGER.info("Generating precursor accuraccy report");
        generatePrecursorAccReport();
        LOGGER.info("Generating fragment ion accuraccy report");
        generateFragmentIonReport();
        LOGGER.info("Generating mass error graphs");
        generateMassErrorGraphs();
    }

    @Override
    public void clear() {
       extractor.clear();
    }

}
