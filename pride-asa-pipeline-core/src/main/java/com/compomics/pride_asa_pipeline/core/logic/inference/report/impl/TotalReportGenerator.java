package com.compomics.pride_asa_pipeline.core.logic.inference.report.impl;

import com.compomics.pride_asa_pipeline.core.logic.inference.report.InferenceReportGenerator;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 *
 *
 * @author Kenneth
 */
public class TotalReportGenerator extends InferenceReportGenerator {

    private static double precursorAcc;
    private static double fragmentAcc;
    private static String enzyme;

    private static String enzymeMethod;
    private static String precursorAccMethod;
    private static String fragmentAccMethod;

    private static PtmSettings ptmSettings;
    private static String ptmSettingsMethod;
    private static int maxMissedCleavages;
    private static double missedCleavageRatio;

    public static double getPrecursorAcc() {
        return precursorAcc;
    }

    public static void setPrecursorAcc(double precursorAcc) {
        TotalReportGenerator.precursorAcc = precursorAcc;
    }

    public static double getFragmentAcc() {
        return fragmentAcc;
    }

    public static void setFragmentAcc(double fragmentAcc) {
        TotalReportGenerator.fragmentAcc = fragmentAcc;
    }

    public static String getEnzyme() {
        return enzyme;
    }

    public static void setEnzyme(String enzyme) {
        TotalReportGenerator.enzyme = enzyme;
    }

    public static String getEnzymeMethod() {
        return enzymeMethod;
    }

    public static String getPrecursorAccMethod() {
        return precursorAccMethod;
    }

    public static String getFragmentAccMethod() {
        return fragmentAccMethod;
    }

    public static void setEnzymeMethod(String enzymeMethod) {
        TotalReportGenerator.enzymeMethod = enzymeMethod;
    }

    public static void setPrecursorAccMethod(String precursorAccMethod) {
        TotalReportGenerator.precursorAccMethod = precursorAccMethod;
    }

    public static void setFragmentAccMethod(String fragmentAccMethod) {
        TotalReportGenerator.fragmentAccMethod = fragmentAccMethod;
    }

    public static void setPtmSettings(PtmSettings ptmSettings) {
        TotalReportGenerator.ptmSettings = ptmSettings;
    }

    public static PtmSettings getPtmSettings() {
        return ptmSettings;
    }

    public static String getPtmSettingsMethod() {
        return ptmSettingsMethod;
    }

    public static void setPtmSettingsMethod(String ptmSettingsMethod) {
        TotalReportGenerator.ptmSettingsMethod = ptmSettingsMethod;
    }

    public static void setMissedCleavagesRatio(double missedCleavageRatio) {
        TotalReportGenerator.missedCleavageRatio = missedCleavageRatio;
    }

    public static void setMissedCleavages(int maxMissedCleavages) {
        TotalReportGenerator.maxMissedCleavages = maxMissedCleavages;
    }

    public static int getMaxMissedCleavages() {
        return maxMissedCleavages;
    }

    public static double getMissedCleavageRatio() {
        return missedCleavageRatio;
    }

    @Override
    protected void writeReport(OutputStreamWriter reportWriter) throws IOException {
        reportWriter.append("MASS ACCURACIES").append(System.lineSeparator());
        reportWriter.append("Precursor Method\t" + getPrecursorAccMethod()).append(System.lineSeparator());
        reportWriter.append("Precursor acc(da)\t" + getPrecursorAcc()).append(System.lineSeparator());
        reportWriter.append("Fragment Method\t" + getFragmentAccMethod()).append(System.lineSeparator());
        reportWriter.append("Fragment acc(da)\t" + getFragmentAcc()).append(System.lineSeparator());

        reportWriter.append("ENZYME STATISTICS").append(System.lineSeparator());
        reportWriter.append("Enzyme Method\t" + getEnzymeMethod()).append(System.lineSeparator());
        reportWriter.append("Enzyme\t" + getEnzyme()).append(System.lineSeparator());
        reportWriter.append("Missed cleavages\t" + getMaxMissedCleavages()).append(System.lineSeparator());
        reportWriter.append("MC-Ration\t" + getMissedCleavageRatio()).append(System.lineSeparator());

        reportWriter.append("MODIFICATION STATISTICS").append(System.lineSeparator());
        reportWriter.append("Modification Method\t" + getPtmSettingsMethod()).append(System.lineSeparator());
        reportWriter.append("Mods\t");
        for (String mod : getPtmSettings().getAllModifications()) {
            reportWriter.append(mod + ",");
        }
        reportWriter.append(System.lineSeparator());

        reportWriter.append("Missed cleavages : " + getMaxMissedCleavages()).append(System.lineSeparator());
        reportWriter.append("MC-Ration :" + getMissedCleavageRatio()).append(System.lineSeparator());

    }

    @Override
    public String getReportName() {
        return "total_report.tsv";
    }

}
