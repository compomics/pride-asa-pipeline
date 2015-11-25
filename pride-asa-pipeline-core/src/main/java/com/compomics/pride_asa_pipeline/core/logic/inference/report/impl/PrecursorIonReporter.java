package com.compomics.pride_asa_pipeline.core.logic.inference.report.impl;

import com.compomics.pride_asa_pipeline.core.logic.inference.ionaccuracy.PrecursorIonErrorPredictor;
import com.compomics.pride_asa_pipeline.core.logic.inference.report.InferenceReportGenerator;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.Identification;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 * @author Kenneth
 */
public class PrecursorIonReporter extends InferenceReportGenerator {

    private final PrecursorIonErrorPredictor predictor;

    public PrecursorIonReporter(PrecursorIonErrorPredictor predictor) {
        this.predictor = predictor;
    }

    @Override
    protected void writeReport(OutputStreamWriter reportWriter) throws IOException {
        reportWriter.append("FINAL DECISION ON Precursor Accuracy SETTINGS ").append(System.lineSeparator());
        reportWriter.append("Precursor tolerance : " + predictor.getRecalibratedPrecursorAccuraccy()).append(System.lineSeparator());
        reportWriter.append("Max Charge encountered : " + predictor.getRecalibratedMaxCharge() + " +").append(System.lineSeparator());
        reportWriter.append("Min Charge encountered :" + predictor.getRecalibratedMinCharge() + " +").append(System.lineSeparator());
        reportWriter.append(System.lineSeparator());
        reportWriter.append("Peptide\tSpectrumID\tCharge\tMass delta\tMass Error(da)\tConsidered").append(System.lineSeparator());
        for (Identification ident : predictor.getExperimentIdentifications()) {
            reportWriter.append(ident.getPeptide().getSequenceString()).append("\t");
            reportWriter.append(ident.getSpectrumId()).append("\t");
            reportWriter.append(ident.getPeptide().getCharge() + "+").append("\t");
            try {
                double mD = ident.getPeptide().calculateMassDelta();
                double m = mD * ident.getPeptide().getCharge();
                boolean consider = m > predictor.getC13IsotopeMass();
                reportWriter.append(String.valueOf(mD)).append("\t");
                reportWriter.append(String.valueOf(m)).append("\t");
                reportWriter.append(String.valueOf(consider));
            } catch (AASequenceMassUnknownException ex) {
                reportWriter.append("Unknown AA").append("\t").append("\t");
            }
        }
    }

    @Override
    public String getReportName() {
        return "precursor_ions.tsv";
    }

}
