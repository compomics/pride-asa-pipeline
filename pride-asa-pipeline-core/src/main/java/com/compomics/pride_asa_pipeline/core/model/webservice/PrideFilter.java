/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.model.webservice;

/**
 *
 * @author Kenneth
 */
public class PrideFilter {

    private final String value;
    private final PrideFilterType type;

    /**
     *
     * @param type The type of the filter
     * @param value The value of the filter
     */
    public PrideFilter(PrideFilterType type, String value) {
        this.value = value;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public PrideFilterType getType() {
        return type;
    }

}
