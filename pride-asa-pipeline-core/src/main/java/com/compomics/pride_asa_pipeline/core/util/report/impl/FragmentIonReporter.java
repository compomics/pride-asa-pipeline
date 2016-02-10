package com.compomics.pride_asa_pipeline.core.util.report.impl;

import com.compomics.pride_asa_pipeline.core.logic.inference.ionaccuracy.FragmentIonErrorPredictor;
import com.compomics.pride_asa_pipeline.core.logic.inference.ionaccuracy.massdeficit.logic.AminoAcidMassInference;
import com.compomics.pride_asa_pipeline.core.logic.inference.ionaccuracy.massdeficit.model.MassDeficitResult;
import com.compomics.pride_asa_pipeline.core.util.report.ExtractionReportGenerator;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 *
 *
 * @author Kenneth
 */
public class FragmentIonReporter extends ExtractionReportGenerator {

    private final FragmentIonErrorPredictor predictor;

    public FragmentIonReporter(FragmentIonErrorPredictor predictor) {
        this.predictor = predictor;
    }

    @Override
    protected void writeReport(OutputStreamWriter reportWriter) throws IOException {
        reportWriter.append("DECISION ON Fragment Ion SETTINGS ").append(System.lineSeparator());
        reportWriter.append("Accuracy (da) : " + predictor.getFragmentIonAccuraccy()).append(System.lineSeparator());
        reportWriter.append(System.lineSeparator());
        reportWriter.append("Peptide\t#Mass-errorst").append(System.lineSeparator());
        int i = 1;
        for (AminoAcidMassInference aMassInference : predictor.getAaInferences()) {
            reportWriter.append(i + ". " + aMassInference.getPeptide().getSequenceWithLowerCasePtms()).append(System.lineSeparator());
            reportWriter.append("Mass errors").append(System.lineSeparator());
            for (double anError : aMassInference.getMassErrors()) {
                reportWriter.append(String.valueOf(anError)).append(",");
            }
            reportWriter.append(System.lineSeparator());
            reportWriter.append("Mass deficits").append(System.lineSeparator());
            for (MassDeficitResult anError : aMassInference.getMassDeficits()) {
                reportWriter.append(String.valueOf(anError.getMassDeficit())).append(",");
            }
            reportWriter.append(System.lineSeparator());

            reportWriter.append("Mass deficit gaps").append(System.lineSeparator());
            for (MassDeficitResult anError : aMassInference.getMassDeficits()) {
                reportWriter.append(String.valueOf(anError.getGapToNext())).append(",");
            }
            reportWriter.append(System.lineSeparator());

            reportWriter.append("Deficit to Mass ratio").append(System.lineSeparator());
            for (MassDeficitResult anError : aMassInference.getMassDeficits()) {
                reportWriter.append(String.valueOf(anError.getDeficitToMassRatio())).append(",");
            }
            i++;
        }

    }

    @Override
    public String getReportName() {
        return "fragment_ions.tsv";
    }

}
