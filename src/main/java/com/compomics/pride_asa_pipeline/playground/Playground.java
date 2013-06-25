/*
 *

 */
package com.compomics.pride_asa_pipeline.playground;

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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.xml.xml_soap.Map;
import org.apache.xml.xml_soap.MapItem;
import org.springframework.context.ApplicationContext;
import uk.ac.ebi.ontology_lookup.ontologyquery.Query;

/**
 * @author Niels Hulstaert
 */
public class Playground {

    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(Playground.class);
    
    public static void main(String[] args) {
//        //load application context
//        ApplicationContext applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();
//
//        PrideXmlSpectrumAnnotator prideSpectrumAnnotator = (PrideXmlSpectrumAnnotator) applicationContext.getBean("prideXmlSpectrumAnnotator");
//        ResultHandler resultHandler = (ResultHandler) applicationContext.getBean("prideXmlResultHandler");
//        Resource prideXmlResource = new FileSystemResource("C:\\Users\\niels\\Desktop\\PRIDE_Experiment_11954.xml");
//        
//        try {
//            prideSpectrumAnnotator.initPrideXmlFile(prideXmlResource.getFile());
//            
//            //init the annotiation
//            prideSpectrumAnnotator.initIdentifications(prideXmlResource.getFile());
//
//            //check if the experiment has "useful" identifications
//            if (prideSpectrumAnnotator.getIdentifications().getCompleteIdentifications().isEmpty()) {
//                //LOGGER.warn("No useful identifications were found for experiment " + experimentAccession + ". This experiment will be skipped.");
//                prideSpectrumAnnotator.clearPipeline();
//            } else {
//                //check if the maximum systematic mass error is exceeded
//                if (prideSpectrumAnnotator.getSpectrumAnnotatorResult().getMassRecalibrationResult().exceedsMaximumSystematicMassError()) {
//                    LOGGER.warn("One or more systematic mass error exceed the maximum value of " + PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.maximum_systematic_mass_error") + ", experiment " + prideXmlResource.getFilename() + " will be skipped.");
//                    prideSpectrumAnnotator.clearPipeline();
//                } else {
//                    //continue with the annotiation
//                    prideSpectrumAnnotator.annotate(prideXmlResource.getFile());
//                    //write result to file
//                    resultHandler.writeResultToFile(prideSpectrumAnnotator.getSpectrumAnnotatorResult());
//                    resultHandler.writeUsedModificationsToFile(prideSpectrumAnnotator.getSpectrumAnnotatorResult());
//                }
//            }
//        } catch (Exception e) {
//            LOGGER.error(e.getMessage(), e);
//        }
        
//        BigDecimal mzDelta = new BigDecimal(0.123456789).setScale(4, BigDecimal.ROUND_HALF_UP);
//        System.out.println("--" +  mzDelta.toPlainString());
       
//        ApplicationContext applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();
//        Query olsClient = (Query) applicationContext.getBean("olsClient");        
//        
//        Map ontologyNames = olsClient.getOntologyNames();
//        for (MapItem mapItem : ontologyNames.getItem()) {
//            if (mapItem.getKey().equals("MS")) {
//                System.out.println(mapItem.getKey());
//                System.out.println(mapItem.getValue());
//            }
//        }
//        
//        String test = olsClient.getTermById("MOD:00408", "MOD"); 
//        System.out.println(test);
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
