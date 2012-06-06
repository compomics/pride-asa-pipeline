/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.model;

/**
 *
 * @author niels
 */
public enum PipelineParams {
    
    DEFAULT(1);
        
    private double value;

    private PipelineParams(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
