package com.compomics.pride_asa_pipeline.core.logic.inference.report.impl;

import com.compomics.pride_asa_pipeline.core.logic.inference.additional.contaminants.Contamination;
import com.compomics.pride_asa_pipeline.core.logic.inference.additional.contaminants.MassScanResult;
import com.compomics.pride_asa_pipeline.core.logic.inference.report.InferenceReportGenerator;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 *
 * @author compomics
 */
public class ContaminationReportGenerator extends InferenceReportGenerator {

    private final double precursorTolerance;
    private final double fragmentTolerance;

    public ContaminationReportGenerator(double precursorTolerance, double fragmentTolerance) {
        this.precursorTolerance = precursorTolerance;
        this.fragmentTolerance = fragmentTolerance;
    }

    @Override
    protected void writeReport(OutputStreamWriter writer) throws IOException {
        writer.append("Contamination report").append(System.lineSeparator());
        writer.append("Precursor accuracy based on contaminants\t: "+MassScanResult.estimatePrecursorIonToleranceBasedOnContaminants()).append(System.lineSeparator());
        writer.append("Fragment accuracy based on contaminants\t: "+MassScanResult.estimateFragmentIonToleranceBasedOnContaminants()).append(System.lineSeparator());
        writer.append(Contamination.getHeader()).append(System.lineSeparator()).flush();
        writer.append("Validated against " + precursorTolerance + " da precursor tolerance and " + fragmentTolerance + " da fragment ion tolerance").append(System.lineSeparator()).flush();
        writer.append(Contamination.getHeader()).append(System.lineSeparator());
        for (Contamination aContaminant : MassScanResult.getPrecursorContamination()) {
            writer.append(aContaminant.toString(precursorTolerance)).append(System.lineSeparator()).flush();
        }
        for (Contamination aContaminant : MassScanResult.getFragmentContamination()) {
            writer.append(aContaminant.toString(fragmentTolerance)).append(System.lineSeparator()).flush();
        }
        writer.flush();
    }

    @Override
    public String getReportName() {
        return "contaminations.tsv";
    }

}
