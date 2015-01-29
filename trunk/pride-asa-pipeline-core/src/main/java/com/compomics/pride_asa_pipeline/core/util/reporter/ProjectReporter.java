/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.util.reporter;

import com.compomics.pride_asa_pipeline.core.logic.parameters.PrideAsapSearchParamExtractor;

/**
 *
 * @author Kenneth
 */
public abstract class ProjectReporter {

    protected final PrideAsapSearchParamExtractor extractor;

    public ProjectReporter(PrideAsapSearchParamExtractor extractor) {
        this.extractor = extractor;
    }

    public abstract void generateReport();


}
