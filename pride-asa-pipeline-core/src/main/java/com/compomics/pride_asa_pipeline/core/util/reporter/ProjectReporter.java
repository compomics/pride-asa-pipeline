/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.util.reporter;

import com.compomics.pride_asa_pipeline.core.logic.parameters.PrideAsapExtractor;

/**
 *
 * @author Kenneth
 */
public abstract class ProjectReporter {

    protected final PrideAsapExtractor extractor;

    public ProjectReporter(PrideAsapExtractor extractor) {
        this.extractor = extractor;
    }

    public abstract void generateReport();
    
    public abstract void clear();
    


}
