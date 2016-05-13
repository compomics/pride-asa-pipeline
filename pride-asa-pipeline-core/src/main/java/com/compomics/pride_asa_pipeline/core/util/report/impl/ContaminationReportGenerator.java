package com.compomics.pride_asa_pipeline.core.util.report.impl;

import com.compomics.pride_asa_pipeline.core.logic.inference.additional.contaminants.Contamination;
import com.compomics.pride_asa_pipeline.core.logic.inference.additional.contaminants.MassScanResult;
import com.compomics.pride_asa_pipeline.core.util.report.ExtractionReportGenerator;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map.Entry;

/**
 *
 * @author Kenneth Verheggen
 */
public class ContaminationReportGenerator extends ExtractionReportGenerator {

    private final double precursorTolerance;
    private final double fragmentTolerance;

    public ContaminationReportGenerator(double precursorTolerance, double fragmentTolerance) {
        this.precursorTolerance = precursorTolerance;
        this.fragmentTolerance = fragmentTolerance;
    }

    @Override
    protected void writeReport(OutputStreamWriter writer) throws IOException {
        writer.append("Contamination report").append(System.lineSeparator());
        writer.append("Estimated tolerance based on contaminants : " + MassScanResult.getContaminantBasedMassError());
        writer.append("Name\tCount").append(System.lineSeparator());
        for (Entry<String, Integer> countMap : MassScanResult.getMassCounts().entrySet()) {
            writer.append(countMap.getKey() + "\t" + countMap.getValue()).append(System.lineSeparator()).flush();
        }
    }

    @Override
    public String getReportName() {
        return "contaminations.tsv";
    }

}
