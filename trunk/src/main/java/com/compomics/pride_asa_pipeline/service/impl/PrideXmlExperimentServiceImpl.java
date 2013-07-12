package com.compomics.pride_asa_pipeline.service.impl;

import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Identifications;
import com.compomics.pride_asa_pipeline.repository.PrideXmlParser;
import com.compomics.pride_asa_pipeline.service.PrideXmlExperimentService;
import com.compomics.pridexmltomgfconverter.errors.enums.ConversionError;
import com.compomics.pridexmltomgfconverter.errors.exceptions.XMLConversionException;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author Niels Hulstaert
 */
public class PrideXmlExperimentServiceImpl extends ExperimentServiceImpl implements PrideXmlExperimentService {

    private static final Logger LOGGER = Logger.getLogger(PrideXmlExperimentServiceImpl.class);
    private PrideXmlParser prideXmlParser;

    public PrideXmlExperimentServiceImpl() {
        System.out.println("----------------------- new PrideXmlExperimentServiceImpl instance created by thread " + Thread.currentThread().getName());
    }   

    public PrideXmlParser getPrideXmlParser() {
        return prideXmlParser;
    }

    public void setPrideXmlParser(PrideXmlParser prideXmlParser) {
        this.prideXmlParser = prideXmlParser;
    }        

    @Override
    public Map<String, String> findAllExperimentAccessions() {
        return experimentRepository.findAllExperimentAccessions();
    }

    @Override
    public Map<String, String> findExperimentAccessionsByTaxonomy(int taxonomyId) {
        return experimentRepository.findExperimentAccessionsByTaxonomy(taxonomyId);
    }

    @Override
    public Identifications loadExperimentIdentifications(File experimentPrideXmlFile) {
        Identifications identifications = new Identifications();
        //@todo check if the parser is initialized
        List<Identification> identificationList = prideXmlParser.getExperimentIdentifications();
        for (Identification identification : identificationList) {
            identifications.addIdentification(identification);
        }
        return identifications;
    }

    @Override
    public void updateChargeStates(Set<Integer> chargeStates) {
        Map<String, String> analyzerSources = prideXmlParser.getAnalyzerSources();
        if (analyzerSources.containsKey(MALDI_SOURCE_ACCESSION)) {
            LOGGER.debug("Found MALDI source, setting charge state to 1.");
            chargeStates.clear();
            chargeStates.add(1);
        } else {
            //iterate over values to check if a maldi source is found
            for (String value : analyzerSources.values()) {
                if (value.indexOf("maldi") > -1 || value.indexOf("matrix") > -1) {
                    LOGGER.debug("Found MALDI source, setting charge state to 1.");
                    chargeStates.clear();
                    chargeStates.add(1);
                }
            }
        }
    }

    @Override
    public AnalyzerData getAnalyzerData() {
        //@ToDo: for the moment, only take the first result into account, check the other results
        List<AnalyzerData> analyzerDataList = prideXmlParser.getAnalyzerData();
        AnalyzerData analyzerData = null;
        if (!analyzerDataList.isEmpty()) {
            analyzerData = analyzerDataList.get(0);
            if (analyzerDataList.size() != 1) {
                for (AnalyzerData ad : analyzerDataList) {
                    if (!analyzerData.getAnalyzerFamily().equals(AnalyzerData.ANALYZER_FAMILY.UNKNOWN)) {
                        analyzerData = ad;
                        break;
                    }
                }
            }
        }
        return analyzerData;
    }

    @Override
    public long getNumberOfSpectra() {
        return prideXmlParser.getNumberOfSpectra();
    }

    @Override
    public Set<String> getProteinAccessions() {
        Set<String> proteinAccessions = new HashSet<String>();
        List<String> proteinAccessionList = prideXmlParser.getProteinAccessions();
        for (String proteinAccession : proteinAccessionList) {
            proteinAccessions.add(proteinAccession);
        }
        return proteinAccessions;
    }

    @Override
    public long getNumberOfPeptides() {
        return prideXmlParser.getNumberOfPeptides();
    }    

    @Override
    public void init(File experimentPrideXmlFile) {        
        prideXmlParser.init(experimentPrideXmlFile);
    }

    @Override
    public void clear() {
        prideXmlParser.clear();
    }        

    @Override
    public List<ConversionError> getSpectraAsMgf(File experimentPrideXmlFile, File mgfFile) throws XMLConversionException {
        return prideXmlParser.getSpectraAsMgf(experimentPrideXmlFile, mgfFile);
    }        
}
