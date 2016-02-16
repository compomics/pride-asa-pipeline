package com.compomics.pride_asa_pipeline.core.util.report.impl;

import com.compomics.pride_asa_pipeline.core.util.report.ExtractionReportGenerator;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;

/**
 *
 *
 * @author Kenneth
 */
public class TotalReportGenerator extends ExtractionReportGenerator {

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
    private static List<Modification> annotatedMods;
    private static TreeMap<Integer, Integer> MSLevelCount = new TreeMap<>();
    private static TreeMap<Integer, Integer> precChargeCount = new TreeMap<>();
    private static TreeMap<String, String> failedExtractionSpectra = new TreeMap<>();
    private static DescriptiveStatistics overallFragmentMzStat = new DescriptiveStatistics();
    private static DescriptiveStatistics overallPrecursorMzStat = new DescriptiveStatistics();
    private static DescriptiveStatistics overallFragmentIStat = new DescriptiveStatistics();
    private static DescriptiveStatistics overallPrecursorIStat = new DescriptiveStatistics();
    private static DescriptiveStatistics overallPrecursorChargeStat = new DescriptiveStatistics();
    private static String assay;

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

    public static void setExperimentMods(List<Modification> annotatedMods) {
        TotalReportGenerator.annotatedMods = annotatedMods;
    }

    /**
     * Adds an extracted MS2 spectrum (if possible) to the entire pool in the
     * MGF
     *
     * @param aSpectrum
     */
    public static void addSpectrum(Spectrum aSpectrum) {
        MSLevelCount.put(aSpectrum.getMsLevel(), MSLevelCount.getOrDefault(aSpectrum.getMsLevel(), 0) + 1);
        try {
            precChargeCount.put(aSpectrum.getPrecursorCharge(), precChargeCount.getOrDefault(aSpectrum.getPrecursorCharge(), 0) + 1);
        } catch (NullPointerException e) {
            addFailedSpectrum(aSpectrum.getId(), "WARNING : No precursor charge");
        }
        if (aSpectrum.getMsLevel() == 2) {
            try {
                overallPrecursorIStat.addValue(aSpectrum.getPrecursorIntensity());
            } catch (NullPointerException e) {
                addFailedSpectrum(aSpectrum.getId(), "WARNING : No precursor intensity");
            }
            try {
                overallPrecursorMzStat.addValue(aSpectrum.getPrecursorMZ());
            } catch (NullPointerException e) {
                addFailedSpectrum(aSpectrum.getId(), "WARNING : No precursor mz");
            }
            try {
                overallPrecursorChargeStat.addValue(aSpectrum.getPrecursorCharge());
            } catch (NullPointerException e) {
                addFailedSpectrum(aSpectrum.getId(), "WARNING : No precursor charge");
            }
            try {
                Map<Double, Double> peakList = aSpectrum.getPeakList();
                if (peakList.isEmpty()) {
                    addFailedSpectrum(aSpectrum.getId(), "WARNING : Spectrum has empty peaklist");
                }
                for (Map.Entry<Double, Double> aPeak : peakList.entrySet()) {
                    overallFragmentMzStat.addValue(aPeak.getKey());
                    overallFragmentIStat.addValue(aPeak.getValue());
                }
            } catch (NullPointerException e) {
                addFailedSpectrum(aSpectrum.getId(), "WARNING : Spectrum has no peaklist");
            }

        }
    }

    public static void addFailedSpectrum(String spectrumId, String message) {
        failedExtractionSpectra.put(spectrumId, message);
    }

    public static void setAssay(String assay) {
        TotalReportGenerator.assay = assay;

    }

    @Override
    protected void writeReport(OutputStreamWriter reportWriter) throws IOException {
        reportWriter.append("---------------------").append(System.lineSeparator());
        reportWriter.append("SEARCH INPUT INFERENCE REPORT").append(System.lineSeparator());
        reportWriter.append("Date\t").append(new Date().toString()).append(System.lineSeparator());
        reportWriter.append("Assay\t" + assay).append(System.lineSeparator());
        reportWriter.append("---------------------").append(System.lineSeparator());
        reportWriter.append("1.\tMASS ACCURACIES").append(System.lineSeparator());
        reportWriter.append("---------------------").append(System.lineSeparator());
        reportWriter.append("\t1.1.\tPrecursor Method\t" + getPrecursorAccMethod()).append(System.lineSeparator());
        reportWriter.append("\t1.2.\tPrecursor acc(da)\t" + getPrecursorAcc()).append(System.lineSeparator());
        reportWriter.append("\t1.3.\tFragment Method\t" + getFragmentAccMethod()).append(System.lineSeparator());
        reportWriter.append("\t1.4.\tFragment acc(da)\t" + getFragmentAcc()).append(System.lineSeparator());
        reportWriter.append("---------------------").append(System.lineSeparator());
        reportWriter.append("2.\tENZYME STATISTICS").append(System.lineSeparator());
        reportWriter.append("---------------------").append(System.lineSeparator());
        reportWriter.append("\t2.1.\tEnzyme Method\t" + getEnzymeMethod()).append(System.lineSeparator());
        reportWriter.append("\t2.2.\tEnzyme\t" + getEnzyme()).append(System.lineSeparator());
        reportWriter.append("\t2.3.\tMissed cleavages\t" + getMaxMissedCleavages()).append(System.lineSeparator());
        reportWriter.append("\t2.4.\tMC-Ration\t" + getMissedCleavageRatio()).append(System.lineSeparator());
        reportWriter.append("---------------------").append(System.lineSeparator());
        reportWriter.append("3.\tMODIFICATION STATISTICS").append(System.lineSeparator());
        reportWriter.append("---------------------").append(System.lineSeparator());
        reportWriter.append("\t3.1.\tModification Method\t" + getPtmSettingsMethod()).append(System.lineSeparator());
        reportWriter.append("\t3.2.\tAnnotated Mods\t");
        HashSet<String> exportedMods = new HashSet<>();
        int parameterCounter = 1;
        if (annotatedMods.isEmpty()) {
            reportWriter.append("\t\tnone").append(System.lineSeparator());
        } else {
            for (Modification mod : annotatedMods) {
                if (!exportedMods.contains(mod.getName())) {
                    exportedMods.add(mod.getName());
                    reportWriter.append("\t\t3.2." + parameterCounter + "\t" + mod.getName()).append(System.lineSeparator());
                    parameterCounter++;
                }
            }
        }
        reportWriter.append(System.lineSeparator());
        reportWriter.append("\t3.3.\tUsed Mods (utilities)\t").append(System.lineSeparator());
        parameterCounter = 1;
        if (getPtmSettings().getAllModifications().isEmpty()) {
            reportWriter.append("\t\tnone").append(System.lineSeparator());
        } else {
            for (String mod : getPtmSettings().getAllModifications()) {
                reportWriter.append("\t\t3.3." + parameterCounter + "\t" + mod).append(System.lineSeparator());
                parameterCounter++;
            }
        }
        reportWriter.append(System.lineSeparator());
        reportWriter.append("---------------------").append(System.lineSeparator());
        reportWriter.append("4.\tMGF PROPERTIES").append(System.lineSeparator());
        reportWriter.append("---------------------").append(System.lineSeparator());

        long totalSpectra = 0;
        for (Map.Entry<Integer, Integer> msLevelCount : MSLevelCount.entrySet()) {
            totalSpectra = +msLevelCount.getValue();
        }

        reportWriter.append("\t4.1.\tGeneral Spectra Properties").append(System.lineSeparator());
        reportWriter.append("\t\t4.1.1\t#Spectra\t" + totalSpectra).append(System.lineSeparator());
        reportWriter.append("\t\t4.1.2.\tMS-levels\tCount").append(System.lineSeparator());

        reportWriter.append("\t\t\tLevel\tCount").append(System.lineSeparator());
        for (int msLevel : MSLevelCount.keySet()) {
            reportWriter.append("\t\t\t" + msLevel + "\t" + MSLevelCount.get(msLevel)).append(System.lineSeparator());
        }
        reportWriter.append(System.lineSeparator());

        reportWriter.append("\t\t4.1.3.\tPrecursor charges\tCount").append(System.lineSeparator());
        reportWriter.append("\t\t\tCharge\tCount").append(System.lineSeparator());
        for (int precursorCharge : precChargeCount.keySet()) {
            reportWriter.append("\t\t\t" + precursorCharge + "\t" + precChargeCount.get(precursorCharge)).append(System.lineSeparator());
        }
        reportWriter.append(System.lineSeparator());

        reportWriter.append("\t4.2.\tMS2 Properties").append(System.lineSeparator());
        reportWriter.append("\t\t4.2.1.\tMax MS2 Precursor Intensity\t" + overallPrecursorIStat.getMax()).append(System.lineSeparator());
        reportWriter.append("\t\t4.2.2.\tMedian MS2 Precursor Intensity\t" + overallPrecursorIStat.getPercentile(50)).append(System.lineSeparator());
        reportWriter.append("\t\t4.2.3.\tMin MS2 Precursor Intensity\t" + overallPrecursorIStat.getMin()).append(System.lineSeparator());

        reportWriter.append("\t\t4.2.4.\tMax MS2 Precursor MZ\t" + overallPrecursorMzStat.getMax()).append(System.lineSeparator());
        reportWriter.append("\t\t4.2.5.\tMedian MS2 Precursor MZ\t" + overallPrecursorMzStat.getPercentile(50)).append(System.lineSeparator());
        reportWriter.append("\t\t4.2.6.\tMin MS2 Precursor MZ\t" + overallPrecursorMzStat.getMin()).append(System.lineSeparator());

        reportWriter.append("\t\t4.2.7.\tMax MS2 Precursor Charge\t" + overallPrecursorChargeStat.getMax()).append(System.lineSeparator());
        reportWriter.append("\t\t4.2.8.\tMedian MS2 Precursor Charge\t" + overallPrecursorChargeStat.getPercentile(50)).append(System.lineSeparator());
        reportWriter.append("\t\t4.2.9.\tMin MS2 Precursor Charge\t" + overallPrecursorChargeStat.getMin()).append(System.lineSeparator());

        reportWriter.append("\t\t4.2.10.\tMax MS2 Fragment Intensity\t" + overallFragmentIStat.getMax()).append(System.lineSeparator());
        reportWriter.append("\t\t4.2.11.\tMedian MS2 Fragment Intensity\t" + overallFragmentIStat.getPercentile(50)).append(System.lineSeparator());
        reportWriter.append("\t\t4.2.12.\tMin MS2 Fragment Intensity\t" + overallFragmentIStat.getMin()).append(System.lineSeparator());

        reportWriter.append("\t\t4.2.13.\tMax MS2 Fragment Intensity\t" + overallFragmentMzStat.getMax()).append(System.lineSeparator());
        reportWriter.append("\t\t4.2.14.\tMedian MS2 Fragment Intensity\t" + overallFragmentMzStat.getPercentile(50)).append(System.lineSeparator());
        reportWriter.append("\t\t4.2.15.\tMin MS2 Fragment Intensity\t" + overallFragmentMzStat.getMin()).append(System.lineSeparator());
        reportWriter.append(System.lineSeparator());
        reportWriter.append("---------------------").append(System.lineSeparator());
        if (!failedExtractionSpectra.isEmpty()) {
            reportWriter.append("\t\t5.\tMGF INVALID SPECTRA").append(System.lineSeparator());
            reportWriter.append("\t\t5.1.\t#Failed spectra\t" + failedExtractionSpectra.size()).append(System.lineSeparator());
            reportWriter.append("\t\t5.2.\tSpectrumID\tError").append(System.lineSeparator());
            for (Map.Entry<String, String> aSpectrumID : failedExtractionSpectra.entrySet()) {
                reportWriter.append("\t\t\t" + aSpectrumID.getKey() + "\t" + aSpectrumID.getValue()).append(System.lineSeparator());
            }
        }
    }

    @Override
    public String getReportName() {
        return "Search_Input_Inference_Report.tsv";
    }

}
