/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.util.reporter.impl;

import com.compomics.pride_asa_pipeline.core.logic.parameters.PrideAsapSearchParamExtractor;
import com.compomics.pride_asa_pipeline.core.util.reporter.ProjectReporter;
import com.compomics.pride_asa_pipeline.core.util.reporter.plots.EnzymeCountPlotter;
import com.compomics.pride_asa_pipeline.core.util.reporter.plots.MassErrorPlotter;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.util.experiment.biology.Enzyme;
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

    public DefaultProjectReporter(PrideAsapSearchParamExtractor extractor, File outputFolder) {
        super(extractor);
        this.outputFolder = outputFolder;
    }

    private void generateModReport() {
        File modReport = new File(outputFolder, "modification_report.tsv");
        try (FileWriter modWriter = new FileWriter(modReport)) {
            modWriter.append("Modification\tRate\tConsidered").flush();
            for (Map.Entry<Modification, Double> entry : extractor.getModificationRates().entrySet()) {
                modWriter.append(entry.getKey().getName() + "\t"
                        + 100 * entry.getValue() + "\t"
                        + (entry.getValue() >= extractor.getConsiderationThreshold())).flush();
            }
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }

    private void generateEnzReport() {
        File enzymeReport = new File(outputFolder, "enzyme_report.tsv");
        try (FileWriter enzWriter = new FileWriter(enzymeReport)) {
            enzWriter.append("Enzyme N-Terminus Count").flush();
            for (Map.Entry<Enzyme, Integer> enzymeCount : extractor.getEnzymeCounts().entrySet()) {
                enzWriter.append(("\t\t" + enzymeCount.getKey().getName() + "\t" + enzymeCount.getValue())).flush();
            }
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }

    private void generatePrecursorAccReport() {
        File precAccReport = new File(outputFolder, "prec_acc_report.tsv");
        try (FileWriter precAccWriter = new FileWriter(precAccReport)) {
            precAccWriter.append("Mass Delta (da)\tValue").flush();
            precAccWriter.append("Mean\t" + extractor.getPrecursorStats().getMean()).flush();
            precAccWriter.append("Variance\t" + extractor.getPrecursorStats().getVariance()).flush();
            precAccWriter.append("").flush();
            precAccWriter.append("Minimum\t" + extractor.getPrecursorStats().getMin()).flush();
            precAccWriter.append("1st Quartile\t" + extractor.getPrecursorStats().getPercentile(25)).flush();
            precAccWriter.append("Median delta\t" + extractor.getPrecursorStats().getPercentile(50)).flush();
            precAccWriter.append("3rd Quartile\t" + extractor.getPrecursorStats().getPercentile(75)).flush();
            precAccWriter.append("Maximum\t" + extractor.getPrecursorStats().getMax()).flush();
            precAccWriter.append("").flush();
            precAccWriter.append("Post Drop-Off Cut").flush();
            precAccWriter.append("Mean\t" + extractor.getPrecursorStats().getPostDropStatistic(1)).flush();
            precAccWriter.append("Variance\t" + extractor.getPrecursorStats().getPostDropStatistic(2)).flush();
            precAccWriter.append("Max\t" + extractor.getPrecursorStats().getPostDropStatistic(3)).flush();
            precAccWriter.append("").flush();
            precAccWriter.append("Consensus\t" + extractor.getPrecursorAccuraccy()).flush();
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }

    private void generateFragmentIonReport() {
        File fragAccReport = new File(outputFolder, "frag_acc_report.tsv");
        try (FileWriter fragAccWriter = new FileWriter(fragAccReport)) {
            fragAccWriter.append("Mass Delta (da)\tValue").flush();
            fragAccWriter.append("Mean\t" + extractor.getFragmentIonStats().getMean()).flush();
            fragAccWriter.append("Variance\t" + extractor.getFragmentIonStats().getVariance()).flush();
            fragAccWriter.append("").flush();
            fragAccWriter.append("Minimum\t" + extractor.getFragmentIonStats().getMin()).flush();
            fragAccWriter.append("1st Quartile\t" + extractor.getFragmentIonStats().getPercentile(25)).flush();
            fragAccWriter.append("Median delta\t" + extractor.getFragmentIonStats().getPercentile(50)).flush();
            fragAccWriter.append("3rd Quartile\t" + extractor.getFragmentIonStats().getPercentile(75)).flush();
            fragAccWriter.append("Maximum\t" + extractor.getFragmentIonStats().getMax()).flush();
            fragAccWriter.append("").flush();
            fragAccWriter.append("Consensus\t" + extractor.getFragmentIonAccuraccy()).flush();
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

    private void generateEnzymeCountGraph() {
        String yLab = "# matching N-termini";
        String xLab = "Enzyme";
        try {
            EnzymeCountPlotter.savePlotToFile(extractor, outputFolder, "Enzyme N-Terminus Count", xLab, yLab);
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }

    @Override
    public void generateReport() {
        LOGGER.info("Pride asap extraction report");
        LOGGER.info("General parameters");
        LOGGER.info("___________________________");
        LOGGER.info("File : " + extractor.getInputFile().getName());
        LOGGER.info("Generating modification report");
        generateModReport();
        LOGGER.info("Generating enzyme report");
        generateEnzReport();
        LOGGER.info("Generating enzyme count distribution graph");
        generateEnzymeCountGraph();
        LOGGER.info("Generating precursor accuraccy report");
        generatePrecursorAccReport();
        LOGGER.info("Generating fragment ion accuraccy report");
        generateFragmentIonReport();
        LOGGER.info("Generating mass error graphs");
        generateMassErrorGraphs();
    }

}
