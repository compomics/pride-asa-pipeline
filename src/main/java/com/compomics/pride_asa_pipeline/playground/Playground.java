/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.playground;

import com.compomics.omssa.xsd.UserMod;
import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.modification.OmssaModiciationMarshaller;
import com.compomics.pride_asa_pipeline.modification.impl.OmssaModificationMarshallerImpl;
import com.compomics.pride_asa_pipeline.service.ExperimentService;
import com.compomics.pride_asa_pipeline.service.ModificationService;
import com.compomics.pride_asa_pipeline.spring.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;

import java.util.Set;

/**
 *
 * @author niels
 */
public class Playground {
    
    public static void main(String[] args) {
        //load application context
        ApplicationContext applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();
        
        ExperimentService experimentService = (ExperimentService) applicationContext.getBean("experimentService");

        ModificationService modificationService = (ModificationService) applicationContext.getBean("modificationService");
        Set<Modification> lModificationSet = modificationService.loadExperimentModifications(17183l);

        System.out.println("printing pride-asap modfications");
        for (Modification lNext : lModificationSet) {
            System.out.println(String.format("%s - %s - %f", lNext.getName(), lNext.getLocation().toString(), lNext.getMassShift()));
        }

        System.out.println("marshalling into OMSSA xsd modfications");
        OmssaModiciationMarshaller marshaller = new OmssaModificationMarshallerImpl();
        UserModCollection lUserModCollection = marshaller.marshallModifications(lModificationSet);

        System.out.println("printing omssa-xsd modfications");
        for (Object lNext : lUserModCollection) {
            UserMod lUserMod = (UserMod) lNext;
            System.out.println(String.format("%s - %s - %f", lUserMod.getModificationName(), lUserMod.getLocation(), lUserMod.getMass()));
        }


//  modificationService.loadExperimentModifications(null);


//        long numberOfPeptides = experimentService.getNumberOfPeptides("7662");
//        File file = experimentService.getSpectraAsMgfFile("7662");
//        Set<String> proteinAccessions = experimentService.getProteinAccessions("7662");
//
//        PrideSpectrumAnnotator prideSpectrumAnnotator = (PrideSpectrumAnnotator) applicationContext.getBean("prideSpectrumAnnotator");
//
//        prideSpectrumAnnotator.annotate("7662");
//        SpectrumAnnotatorResult spectrumAnnotatorResult = prideSpectrumAnnotator.getSpectrumAnnotatorResult();
//
//        UserModCollection userModCollection = modificationService.getModificationsAsUserModCollection(spectrumAnnotatorResult);
    }
    
}
