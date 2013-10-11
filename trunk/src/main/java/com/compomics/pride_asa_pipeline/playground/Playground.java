/*
 *

 */
package com.compomics.pride_asa_pipeline.playground;

import com.compomics.pride_asa_pipeline.PrideXmlCommandLineRunner;
import com.compomics.pride_asa_pipeline.logic.PrideXmlSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.service.ExperimentService;
import com.compomics.pride_asa_pipeline.service.ModificationService;
import com.compomics.pride_asa_pipeline.service.PrideXmlExperimentService;
import com.compomics.pride_asa_pipeline.service.PrideXmlModificationService;
import com.compomics.pride_asa_pipeline.spring.ApplicationContextProvider;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.ApplicationContext;

/**
 * @author Niels Hulstaert
 */
public class Playground {

    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(Playground.class);

    public static void main(String[] args) {
        //load application context
        ApplicationContextProvider.getInstance().setDefaultApplicationContext();
        ApplicationContext applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();        

        PrideXmlCommandLineRunner prideXmlCommandLineRunner = applicationContext.getBean("prideXmlCommandLineRunner", PrideXmlCommandLineRunner.class);
        prideXmlCommandLineRunner.runPrideXmlPipeline(new File("C:\\Users\\niels\\Desktop\\ExampleDataSets\\PRIDE_Exp_Complete_Ac_11954.xml"), true);
        
        SpectrumAnnotatorResult spectrumAnnotatorResult = prideXmlCommandLineRunner.getPrideXmlSpectrumAnnotator().getSpectrumAnnotatorResult();
        ModificationService modificationService = prideXmlCommandLineRunner.getPrideXmlSpectrumAnnotator().getModificationService();
        Map<Modification, Integer> usedModifications = modificationService.getUsedModifications(spectrumAnnotatorResult);
        Map<Modification, Double> estimateModificationRate = modificationService.estimateModificationRate(usedModifications, spectrumAnnotatorResult, 0.05);  
        
//        PrideXmlSpectrumAnnotator prideXmlSpectrumAnnotator = (PrideXmlSpectrumAnnotator) applicationContext.getBean("prideXmlSpectrumAnnotator");
//        PrideXmlExperimentService iPrideService = prideXmlSpectrumAnnotator.getExperimentService();
//        iPrideService.init(new File("C:\\Users\\niels\\Desktop\\ExampleDataSets\\PRIDE_Exp_Complete_Ac_15346.xml"));
        
        System.out.println("test");
    }

    public static File filterExperimentAccessions(File experimentAccessionsFile, File resultsDirectory) {
        File filteredExperimentAccessionsFile = null;

        BufferedReader bufferedReader = null;
        PrintWriter printWriter = null;
        try {
            filteredExperimentAccessionsFile = new File("C:\\Users\\niels\\Desktop\\filtered_pride_experiment_accessions.txt");

            //1. read file names in result directory and put them in a set
            Set<String> processedExperimentAccessions = new HashSet<String>();
            if (!resultsDirectory.isDirectory()) {
                return null;
            }
            for (File resultFile : resultsDirectory.listFiles()) {
                processedExperimentAccessions.add(resultFile.getName().substring(0, resultFile.getName().indexOf('.')));
            }

            //2. read experiment accessions file and write the non processed experiment accessions to the new file
            bufferedReader = new BufferedReader(new FileReader(experimentAccessionsFile));
            printWriter = new PrintWriter(new BufferedWriter(new FileWriter(filteredExperimentAccessionsFile)));

            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                if (!processedExperimentAccessions.contains(line)) {
                    printWriter.println(line);
                }
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Playground.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Playground.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException ex) {
                Logger.getLogger(Playground.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return filteredExperimentAccessionsFile;
    }
}
