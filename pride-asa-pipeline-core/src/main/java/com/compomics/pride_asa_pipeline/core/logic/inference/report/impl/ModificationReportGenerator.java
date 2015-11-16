package com.compomics.pride_asa_pipeline.core.logic.inference.report.impl;

import com.compomics.pride_asa_pipeline.core.logic.inference.modification.ModificationPredictor;
import com.compomics.pride_asa_pipeline.core.logic.inference.report.InferenceReportGenerator;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

/**
 * e
 *
 * @author Kenneth
 */
public class ModificationReportGenerator extends InferenceReportGenerator {

    private final ModificationPredictor predictor;

    public ModificationReportGenerator(ModificationPredictor predictor) {
        this.predictor = predictor;
    }

    @Override
    protected void writeReport(OutputStreamWriter reportWriter) throws IOException {
        reportWriter.append("MODIFICATION INFERENCE REPORT").append(System.lineSeparator());
        reportWriter.append("Consideration threshold\t" + predictor.getConsiderationThreshold()).append(System.lineSeparator());
        reportWriter.append("Threshold for fixed modifications\t" + predictor.getFixedThreshold()).append(System.lineSeparator()).flush();
        reportWriter.append("Accession\tModification\tRate").append(System.lineSeparator());
        for (Map.Entry<Modification, Double> aModEntry : predictor.getModificationRates().entrySet()) {
            Modification aMod = aModEntry.getKey();
            reportWriter.append(aMod.getAccession() + "\t" + aMod.getName() + "\t" + aModEntry.getValue()).append(System.lineSeparator()).flush();
        }

    }

}
