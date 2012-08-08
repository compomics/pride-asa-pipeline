/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.playground;

import com.compomics.pride_asa_pipeline.service.ExperimentService;
import com.compomics.pride_asa_pipeline.service.SpectrumService;
import com.compomics.pride_asa_pipeline.spring.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;

import java.io.File;

/**
 * @author niels
 */
public class Playground {

    public static void main(String[] args) {
        //load application context
        ApplicationContext applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();
//
//        ModificationService modificationService = (ModificationService) applicationContext.getBean("modificationService");
//
//        Set<Modification> lModificationSet = modificationService.loadExperimentModifications(1748);
//
//        System.out.println("printing pride-asap modfications");
//        for (Modification lNext : lModificationSet) {
//            if(lNext != null){
//                System.out.println(String.format("%s - %s - %f", lNext.getName(), lNext.getLocation().toString(), lNext.getMassShift()));
//            }
//        }
//
//        System.out.println("marshalling into OMSSA xsd modfications");
//        OmssaModiciationMarshaller marshaller = new OmssaModificationMarshallerImpl();
//        UserModCollection lUserModCollection = marshaller.marshallModifications(lModificationSet);
//
//        System.out.println("printing omssa-xsd modfications");
//        for (Object lNext : lUserModCollection) {
//            UserMod lUserMod = (UserMod) lNext;
//            System.out.println(String.format("%s - %s - %f", lUserMod.getModificationName(), lUserMod.getLocation(), lUserMod.getMass()));
//        }
//

//  modificationService.loadExperimentModifications(null);

        ExperimentService experimentService = (ExperimentService) applicationContext.getBean("experimentService");

        SpectrumService spectrumService = (SpectrumService) applicationContext.getBean("spectrumService");

        File file = experimentService.getSpectraAsMgfFile("2");
        System.out.println(file.getAbsolutePath());

//
//        File file = experimentService.getSpectraAsMgfFile("7662");
//
//        Set<String> lAccessions = experimentService.getProteinAccessions("7662");
//        System.out.println(String.format("%d unique protein accessions", lAccessions.size()));

//        ModificationService modificationService = (ModificationService) applicationContext.getBean("modificationService");
//        PrideSpectrumAnnotator annotator = (PrideSpectrumAnnotator) applicationContext.getBean("prideSpectrumAnnotator");
////
//        annotator.annotate("9333");
//        Set<Modification> lUsedModifications = modificationService.getUsedModifications(annotator.getSpectrumAnnotatorResult());
//
//        for(Modification lModification : lUsedModifications){
//            System.out.println(lModification.getName());
//        }


//        List<Long> spectrumIds = Lists.newArrayList();
//        spectrumIds.add(4190355l);
//        spectrumIds.add(4190382l);
//        spectrumIds.add(4190407l);
//
//
//        spectrumService.cacheSpectra(spectrumIds);
//        Map lCachedSpectrum = spectrumService.getCachedSpectrum(4190407l);
//        System.out.println(String.format("spectrum has %d different peaks %s", lCachedSpectrum.keySet().size(), "blabla"));

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
