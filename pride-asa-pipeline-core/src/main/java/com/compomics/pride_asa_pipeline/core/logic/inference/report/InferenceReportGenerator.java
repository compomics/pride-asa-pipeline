package com.compomics.pride_asa_pipeline.core.logic.inference.report;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 *
 * @author Kenneth
 */
public abstract class InferenceReportGenerator {

    public String getReportName(){
        return this.getClass().getName().replace("com.compomics.pride_asa_pipeline.core.logic.inference.report.impl.","")+".report.tsv";
    }
    
    public void writeReport(OutputStream out) throws IOException {
        OutputStreamWriter reportWriter = new OutputStreamWriter(out);
        writeReport(reportWriter);
        reportWriter.flush();
    }

    protected abstract void writeReport(OutputStreamWriter reportWriter) throws IOException;
}
