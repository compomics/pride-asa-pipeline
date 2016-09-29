package com.compomics.pride_asa_pipeline.model;

/**
 * @author Kenneth Verheggen
 *         Date: 29-sept-2016
 * @since 0.1
 */
public class ParameterExtractionException extends Exception {

    public ParameterExtractionException() {
    }

    public ParameterExtractionException(String msg) {
        super(msg);
    }

    public ParameterExtractionException(String msg, Throwable t) {
        super(t);
    }

    public ParameterExtractionException(Throwable t) {
        super(t);
    }
}
