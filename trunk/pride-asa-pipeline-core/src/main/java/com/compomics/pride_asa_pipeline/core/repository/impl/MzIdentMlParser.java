package com.compomics.pride_asa_pipeline.core.repository.impl;

import com.compomics.pride_asa_pipeline.core.logic.modification.PTMMapper;
import com.compomics.pride_asa_pipeline.core.logic.spectrum.DefaultMGFExtractor;
import com.compomics.pride_asa_pipeline.core.repository.FileParser;
import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import uk.ac.ebi.jmzidml.MzIdentMLElement;
import uk.ac.ebi.jmzidml.model.mzidml.DBSequence;
import uk.ac.ebi.jmzidml.model.mzidml.Modification;
import uk.ac.ebi.jmzidml.model.mzidml.Peptide;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationItem;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationList;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationResult;
import uk.ac.ebi.jmzidml.xml.io.MzIdentMLUnmarshaller;

/**
 *
 * @author Kenneth Verheggen WARNING : THIS CLASS IS A PROTOTYPE AND IS NOT
 * FINISHED !!!!!
 */
public class MzIdentMlParser implements FileParser {

    private MzIdentMLUnmarshaller unmarshaller;
    private final Map<String, Peptide> peptideMap = new HashMap<>();
    private final List<String> proteinAccessions = new ArrayList<>();
    private final List<SpectrumIdentificationList> specEvidenceList = new ArrayList<>();
    private final List<Modification> modEvidenceList = new ArrayList<>();
    private final List<Identification> identificationsList = new ArrayList<>();
    private final List<com.compomics.pride_asa_pipeline.model.Modification> modificationsList = new ArrayList<>();
    private DefaultMGFExtractor extractor;
    private static final Logger LOGGER = Logger.getLogger(MzIdentMlParser.class);

    @Override
    public void init(File mzIdentMlFile) {
        unmarshaller = new MzIdentMLUnmarshaller(mzIdentMlFile);
        // any reference on the fly.
        Iterator<DBSequence> testEvdIter = unmarshaller.unmarshalCollectionFromXpath(MzIdentMLElement.DBSequence);
        // Retrieve information for each PeptideEvidence in the list
        while (testEvdIter.hasNext()) {
            DBSequence element = testEvdIter.next();
            proteinAccessions.add(element.getAccession());
        }
        // any reference on the fly.
        Iterator<Peptide> pepEvdIter = unmarshaller.unmarshalCollectionFromXpath(MzIdentMLElement.Peptide);
        // Retrieve information for each PeptideEvidence in the list
        while (pepEvdIter.hasNext()) {
            Peptide element = pepEvdIter.next();
            peptideMap.put(element.getId(), element);
        }

        // any reference on the fly.
        Iterator<SpectrumIdentificationList> specEvdIter = unmarshaller.unmarshalCollectionFromXpath(MzIdentMLElement.SpectrumIdentificationList);

        // Retrieve information for each spectrum in the list
        while (specEvdIter.hasNext()) {
            SpectrumIdentificationList element = specEvdIter.next();
            specEvidenceList.add(element);
        }

        // any reference on the fly.
        Iterator<Modification> modEvdIter = unmarshaller.unmarshalCollectionFromXpath(MzIdentMLElement.Modification);

        // Retrieve information for each spectrum in the list
        while (modEvdIter.hasNext()) {
            Modification element = modEvdIter.next();
            modEvidenceList.add(element);
        }
    }

    @Override
    public void clear() {
        peptideMap.clear();
    }

    @Override
    public List<Identification> getExperimentIdentifications() {

        for (SpectrumIdentificationList anIdentification : specEvidenceList) {
            for (SpectrumIdentificationResult anIdentificationResult : anIdentification.getSpectrumIdentificationResult()) {
                for (SpectrumIdentificationItem anIdentificationItem : anIdentificationResult.getSpectrumIdentificationItem()) {
                    //only allow rank 1 peptides
                    if (anIdentificationItem.getRank() == 1) {
                        try {
                            //make a pride asap peptide
                            com.compomics.pride_asa_pipeline.model.Peptide peptide = new com.compomics.pride_asa_pipeline.model.Peptide();
                            //set charge state 
                            peptide.setCharge(anIdentificationItem.getChargeState());
                            //set the experimental measured MZ
                            peptide.setMzRatio(anIdentificationItem.getExperimentalMassToCharge());
                            //set the peptide sequence
                            String sequence = peptideMap.get(anIdentificationItem.getPeptideRef()).getPeptideSequence();
                            peptide.setSequence(new AminoAcidSequence(AminoAcidSequence.toAASequence(sequence)));
                            //Verify the spectrum information in the identification part
                            Identification ident = new Identification(peptide, anIdentificationResult.getSpectraDataRef(), anIdentificationResult.getSpectrumID(), anIdentificationResult.getSpectrumID());
                            ident.setPeptide(peptide);
                            identificationsList.add(ident);
                            List<Modification> modificationsInFile = peptideMap.get(anIdentificationItem.getPeptideRef()).getModification();
                            if (!modificationsInFile.isEmpty()) {
                                for (Modification aMod : modificationsInFile) {
                                    modificationsList.add(PTMMapper.mapModificationWithParameters(aMod, sequence));
                                }
                            }
                        } catch (UnknownAAException | NullPointerException ex) {
                            ex.printStackTrace();
                            LOGGER.error(ex);
                        }
                    }
                }
            }

        }
        return identificationsList;
    }

    @Override
    public long getNumberOfSpectra() {
        return extractor.getSpectrumIds().size();
    }

    @Override
    public long getNumberOfPeptides() {
        return peptideMap.size();
    }

    @Override
    public long getNumberUniquePeptides() {
        Set<String> uniquePeptides = new HashSet<>();
        for (SpectrumIdentificationList identId : specEvidenceList) {
            for (SpectrumIdentificationResult aPeptideItem : identId.getSpectrumIdentificationResult()) {
                for (SpectrumIdentificationItem anItem : aPeptideItem.getSpectrumIdentificationItem()) {
                    uniquePeptides.add(anItem.getPeptide().getPeptideSequence());
                }
            }
        }
        return uniquePeptides.size();
    }

    @Override
    public List<Map<String, Object>> getSpectraMetadata() {
        return extractor.getSpectraMetadata();
    }

    @Override
    public List<String> getSpectrumIds() {
        return extractor.getSpectrumIds();
    }

    @Override
    public List<com.compomics.pride_asa_pipeline.model.Modification> getModifications() {
        return modificationsList;
    }

    @Override
    public Map<String, String> getAnalyzerSources() {
        //TODO CHECK IF THIS IS STILL NEEDED WHEN USING RECALIBRATION
        return new HashMap<>();
    }

    @Override
    public List<AnalyzerData> getAnalyzerData() {
        //TODO FIX THIS SO IT ACTUALLY DOES IT RIGHT!
        List<AnalyzerData> defaultMassAnalyzerList = new ArrayList<>();
        String defaultString = "lqtof";
        defaultMassAnalyzerList.add(AnalyzerData.getAnalyzerDataByAnalyzerType(defaultString));
        return defaultMassAnalyzerList;
    }

    @Override
    public List<String> getProteinAccessions() {
        return proteinAccessions;
    }

    @Override
    public void attachSpectra(File peakFile) throws Exception {
        extractor = new DefaultMGFExtractor(peakFile);
    }

    @Override
    public List<Peak> getSpectrumPeaksBySpectrumId(String spectrumId) {
        List<Peak> peaks = new ArrayList<>();
        uk.ac.ebi.pride.tools.jmzreader.model.Spectrum aSpectrum = extractor.getSpectrumBySpectrumId(spectrumId);
        Map<Double, Double> peakValues = aSpectrum.getPeakList();
        for (Map.Entry<Double, Double> peakValue : peakValues.entrySet()) {
            Peak peak = new Peak(peakValue.getKey(), peakValue.getValue());
            peaks.add(peak);
        }

        return peaks;
    }

    @Override
    public HashMap<Double, Double> getSpectrumPeakMapBySpectrumId(String spectrumId
    ) {
        return extractor.getSpectrumPeakMapBySpectrumId(spectrumId);
    }

}
