package com.compomics.pride_asa_pipeline.core.util.report.impl;

import com.compomics.pride_asa_pipeline.core.logic.inference.enzyme.EnzymePredictor;
import com.compomics.pride_asa_pipeline.core.util.report.ExtractionReportGenerator;
import com.compomics.pride_asa_pipeline.model.AminoAcid;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

/**
 *
 *
 * @author Kenneth
 */
public class EnzymeReportGenerator extends ExtractionReportGenerator {

    private final EnzymePredictor predictor;

    public EnzymeReportGenerator(EnzymePredictor predictor) {
        this.predictor = predictor;
    }

    @Override
    protected void writeReport(OutputStreamWriter reportWriter) throws IOException {
        reportWriter.append("FINAL DECISION ON Enzyme SETTINGS ").append(System.lineSeparator());
        reportWriter.append("Enzyme : " + predictor.getMostLikelyEnzyme().getName()).append(System.lineSeparator());
        reportWriter.append("Missed cleavages : " + predictor.getMissedCleavages()).append(System.lineSeparator());
        reportWriter.append("MC-Ration :" + predictor.getMissedCleavageRatio()).append(System.lineSeparator());
        reportWriter.append(System.lineSeparator());
        reportWriter.append("Amino Acid\t#C-term\t#N-term").append(System.lineSeparator());
        for (AminoAcid anAcid : AminoAcid.values()) {
            reportWriter.append(anAcid.letter()).append("\t");
            reportWriter.append(String.valueOf(predictor.getC_TerminiCount().get(anAcid.letter()))).append("\t");
            reportWriter.append(String.valueOf(predictor.getN_TerminiCount().get(anAcid.letter()))).append(System.lineSeparator());
        }

    }

        @Override
    public String getReportName() {
        return "enzyme.tsv";
    }
    
}
