package com.compomics.pride_asa_pipeline.core.util.report.impl;

import com.compomics.pride_asa_pipeline.core.logic.inference.ionaccuracy.PrecursorIonErrorPredictor;
import com.compomics.pride_asa_pipeline.core.util.report.ExtractionReportGenerator;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.AtomImpl;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 *
 *
 * @author Kenneth
 */
public class PrecursorIonReporter extends ExtractionReportGenerator {

    private final PrecursorIonErrorPredictor predictor;

    public PrecursorIonReporter(PrecursorIonErrorPredictor predictor) {
        this.predictor = predictor;
    }

    @Override
    protected void writeReport(OutputStreamWriter reportWriter) throws IOException {
        reportWriter.append("DECISION ON Precursor Accuracy SETTINGS ").append(System.lineSeparator());
        reportWriter.append("Precursor tolerance : " + predictor.getRecalibratedPrecursorAccuraccy()).append(System.lineSeparator());
        reportWriter.append("Max Charge encountered : " + predictor.getRecalibratedMaxCharge() + " +").append(System.lineSeparator());
        reportWriter.append("Min Charge encountered :" + predictor.getRecalibratedMinCharge() + " +").append(System.lineSeparator());
        reportWriter.append(System.lineSeparator());
        reportWriter.append("Peptide\tSpectrumID\tCharge\tMass delta\t\tConsidered").append(System.lineSeparator());
        for (Identification ident : predictor.getExperimentIdentifications()) {
            reportWriter.append(ident.getPeptide().getSequenceString()).append("\t");
            reportWriter.append(ident.getSpectrumId()).append("\t");
            reportWriter.append(ident.getPeptide().getCharge() + "+").append("\t");
            try {
                double mD = ident.getPeptide().calculateMassDelta();
                boolean consider = mD > new AtomImpl(Atom.C, 1).getMass() - new AtomImpl(Atom.C, 0).getMass();
                reportWriter.append(String.valueOf(mD)).append("\t");
                reportWriter.append(String.valueOf(consider));
            } catch (AASequenceMassUnknownException ex) {
                reportWriter.append("Unknown AA").append("\t").append("\t");
            }
            reportWriter.append(System.lineSeparator());
        }
    }

    @Override
    public String getReportName() {
        return "precursor_ions.tsv";
    }

}
