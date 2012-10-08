/*
 *

 */
package com.compomics.pride_asa_pipeline.model;

/**
 *
 * @author Niels Hulstaert
 */
public class PipelineException extends Exception {

    public PipelineException() {
    }

    public PipelineException(String message) {
        super(message);
    }

    public PipelineException(String message, Throwable cause) {
        super(message, cause);
    }

    public PipelineException(Throwable cause) {
        super(cause);
    }
}
