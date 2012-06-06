/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.playground;

import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.model.*;
import com.compomics.pride_asa_pipeline.pipeline.PrideSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.pipeline.PrideSpectrumAnnotatorTemplate;
import com.compomics.pride_asa_pipeline.repository.ExperimentRepository;
import com.compomics.pride_asa_pipeline.service.ExperimentService;
import com.compomics.pride_asa_pipeline.service.ModificationService;
import com.compomics.pride_asa_pipeline.service.SpectrumService;
import com.compomics.pride_asa_pipeline.spring.ApplicationContextProvider;
import com.google.common.base.Objects;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author niels
 */
public class Playground {
    
    public static void main(String[] args) {
        //load application context
        ApplicationContext applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();
        
        ExperimentService experimentService = (ExperimentService) applicationContext.getBean("experimentService");        
        File file = experimentService.getSpectraAsMgfFile("7662");
        long numberOfPeptides = experimentService.getNumberOfPeptides("7662");                
        Set<String> proteinAccessions = experimentService.getProteinAccessions("7662");
       
        PrideSpectrumAnnotator prideSpectrumAnnotator = (PrideSpectrumAnnotator) applicationContext.getBean("prideSpectrumAnnotator");
        
        prideSpectrumAnnotator.annotate("7662");
        SpectrumAnnotatorResult spectrumAnnotatorResult = prideSpectrumAnnotator.getSpectrumAnnotatorResult();
        
        ModificationService modificationService = (ModificationService) applicationContext.getBean("modificationService");
        UserModCollection userModCollection = modificationService.getModificationsAsUserModCollection(spectrumAnnotatorResult);                               
    }
    
}
