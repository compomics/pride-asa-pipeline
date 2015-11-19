package com.compomics.pride_asa_pipeline.core.logic.inference.report;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 *
 * @author Kenneth
 */
public abstract class InferenceReportGenerator {

    public void writeReport(OutputStream out) throws IOException {
        OutputStreamWriter reportWriter = new OutputStreamWriter(out);
        writeReport(reportWriter);
        reportWriter.flush();
    }

    protected abstract void writeReport(OutputStreamWriter reportWriter) throws IOException;
}
