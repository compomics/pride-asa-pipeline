package com.compomics.pride_asa_pipeline.repository.impl;

import com.compomics.pride_asa_pipeline.data.mapper.AnalyzerDataMapper;
import com.compomics.pride_asa_pipeline.model.AminoAcid;
import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import com.compomics.pride_asa_pipeline.repository.PrideXmlParser;
import com.compomics.pridexmltomgfconverter.errors.enums.ConversionError;
import com.compomics.pridexmltomgfconverter.errors.exceptions.XMLConversionException;
import com.compomics.pridexmltomgfconverter.tools.PrideXMLToMGFConverter;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.jaxb.model.AnalyzerList;
import uk.ac.ebi.pride.jaxb.model.CvParam;
import uk.ac.ebi.pride.jaxb.model.Instrument;
import uk.ac.ebi.pride.jaxb.model.ModificationItem;
import uk.ac.ebi.pride.jaxb.model.Param;
import uk.ac.ebi.pride.jaxb.model.PeptideItem;
import uk.ac.ebi.pride.jaxb.model.Precursor;
import uk.ac.ebi.pride.jaxb.model.Spectrum;
import uk.ac.ebi.pride.jaxb.xml.PrideXmlReader;

/**
 *
 * @author Niels Hulstaert
 */
public class PrideXmlParserImpl implements PrideXmlParser {

    private static final Logger LOGGER = Logger.getLogger(PrideXmlParserImpl.class);
    private static final String IONIZER_TYPE = "PSI:1000008";
    private static final String MALDI_SOURCE_ACCESSION = "PSI:1000075";
    /**
     * The PrideXmlReader instance
     */
    private PrideXmlReader prideXmlReader;
    /**
     * The list of modification found in PRIDE
     */
    private List<Modification> modifications = new ArrayList<>();

    public PrideXmlParserImpl() {
    }

    /**
     * Creates a new PrideXmlReader instance
     *
     * @param prideXmlFile
     */
    @Override
    public void init(File prideXmlFile) {
        PrideXMLToMGFConverter.getInstance().init(prideXmlFile);
        prideXmlReader = PrideXMLToMGFConverter.getInstance().getPrideXmlReader();
        modifications.clear();
    }

    @Override
    public void clear() {
        PrideXMLToMGFConverter.getInstance().clearTempFiles();
        prideXmlReader = null;
        modifications.clear();
    }

    @Override
    public List<Identification> getExperimentIdentifications() {
        List<Identification> identifications = new ArrayList<>();

        //Iterate over each protein identification
        for (String proteinIdentificationId : prideXmlReader.getIdentIds()) {
            //uk.ac.ebi.pride.jaxb.model.Identification prideXmlIdentification = prideXmlReader.getIdentById(identificationId);

            //Get the number of peptides within an identification
            int numOfPeptides = prideXmlReader.getNumberOfPeptides(proteinIdentificationId);

            //Iterate over each peptide identification
            for (int i = 0; i < numOfPeptides; i++) {
                PeptideItem peptideItem = prideXmlReader.getPeptide(proteinIdentificationId, i);

                //check for possible null pointers
                if (peptideItem.getSpectrum() != null && peptideItem.getSpectrum().getSpectrumDesc().getPrecursorList() != null) {
                    //get modifications
                    List<ModificationItem> modificationItems = peptideItem.getModificationItem();
                    for (ModificationItem modificationItem : modificationItems) {
                        Modification modification = mapModification(modificationItem, peptideItem.getSequence());
                        if (modification != null) {
                            modifications.add(modification);
                        }
                    }
                    
                    //get spectrum precursor for m/z and charge                    
                    Precursor precursor = peptideItem.getSpectrum().getSpectrumDesc().getPrecursorList().getPrecursor().get(0);
                    
                    //get precursor CvParams
                    List<CvParam> precursorCvParams = precursor.getIonSelection().getCvParam();
                    double mzRatio = 0.0;
                    int charge = -1;
                    for (CvParam cvParam : precursorCvParams) {
                        //precursor m/z
                        if (cvParam.getAccession().equalsIgnoreCase("MS:1000744") || cvParam.getAccession().equalsIgnoreCase("PSI:1000040")) {
                            mzRatio = Double.parseDouble(cvParam.getValue());
                        } //precursor charge 
                        else if (cvParam.getAccession().equalsIgnoreCase("MS:1000041") || cvParam.getAccession().equalsIgnoreCase("PSI:1000041")) {
                            charge = Integer.parseInt(cvParam.getValue());
                        }
                    }
                    try {
                        //new Peptide instance
                        Peptide peptide = new Peptide(charge, mzRatio, new AminoAcidSequence(peptideItem.getSequence()));
                        //new Identification instance
                        long spectrumId = peptideItem.getSpectrum().getId();
                        Identification identification = new Identification(peptide, proteinIdentificationId, spectrumId, spectrumId);

                        //add to identifications
                        identifications.add(identification);
                    } catch (UnknownAAException ex) {
                        LOGGER.error(ex.getMessage(), ex);
                    }
                }
            }
        }

        return identifications;
    }

    @Override
    public long getNumberOfSpectra() {
        return prideXmlReader.getSpectrumIds().size();
    }

    @Override
    public long getNumberOfPeptides() {
        return prideXmlReader.getNumberOfPeptides();
    }

    @Override
    public List<Map<String, Object>> getSpectraMetadata() {
        List<String> testing = prideXmlReader.getSpectrumIds();

        return null;
    }

    @Override
    public List<String> getSpectrumIds() {
        return prideXmlReader.getSpectrumIds();
    }

    @Override
    public List<Modification> getModifications() {
        return modifications;
    }

    @Override
    public List<Peak> getSpectrumPeaksBySpectrumId(String spectrumId) {
        List<Peak> peaks = new ArrayList<Peak>();

        Spectrum spectrum = prideXmlReader.getSpectrumById(spectrumId);
        Number[] mzRatios = spectrum.getMzNumberArray();
        Number[] intensities = spectrum.getIntentArray();

        for (int i = 0; i < mzRatios.length; i++) {
            Peak peak = new Peak((Double) mzRatios[i], (Double) intensities[i]);
            peaks.add(peak);
        }

        return peaks;
    }

    @Override
    public HashMap<Double, Double> getSpectrumPeakMapBySpectrumId(String spectrumId) {
        HashMap<Double, Double> peaks = new HashMap<Double, Double>();

        Spectrum spectrum = prideXmlReader.getSpectrumById(spectrumId);
        Number[] mzRatios = spectrum.getMzNumberArray();
        Number[] intensities = spectrum.getIntentArray();

        for (int i = 0; i < mzRatios.length; i++) {
            peaks.put(mzRatios[i].doubleValue(), intensities[i].doubleValue());
        }

        return peaks;
    }

    @Override
    public Map<String, String> getAnalyzerSources() {
        Map<String, String> analyzerSources = new HashMap<String, String>();

        //get instrument
        Instrument instrument = prideXmlReader.getDescription().getInstrument();
        //get ionizer type cv param and maldi source cv param
        CvParam ionizerTypeCvParam = instrument.getSource().getCvParamByAcc(IONIZER_TYPE);
        CvParam maldiSourceCvParam = instrument.getSource().getCvParamByAcc(MALDI_SOURCE_ACCESSION);

        if (ionizerTypeCvParam != null) {
            analyzerSources.put(ionizerTypeCvParam.getAccession(), ionizerTypeCvParam.getName());
        }
        if (maldiSourceCvParam != null) {
            analyzerSources.put(maldiSourceCvParam.getAccession(), maldiSourceCvParam.getName());
        }

        return analyzerSources;
    }

    @Override
    public List<AnalyzerData> getAnalyzerData() {
        List<AnalyzerData> analyzerDataList = new ArrayList<AnalyzerData>();

        //get instrument
        Instrument instrument = prideXmlReader.getDescription().getInstrument();
        //get analyzer list
        AnalyzerList analyzerList = instrument.getAnalyzerList();
        //get analyzer params
        List<Param> analyzerParams = analyzerList.getAnalyzer();
        for (Param analyzerParam : analyzerParams) {
            for (CvParam cvParam : analyzerParam.getCvParam()) {
                AnalyzerData analyzerData = AnalyzerDataMapper.getAnalyzerDataByAnalyzerType(cvParam.getName());
                analyzerDataList.add(analyzerData);
            }
        }

        //if list is empty, return default analyzer
        if (analyzerDataList.isEmpty()) {
            analyzerDataList.add(AnalyzerDataMapper.getAnalyzerDataByAnalyzerType(null));
        }

        return analyzerDataList;
    }

    @Override
    public List<String> getProteinAccessions() {
        List<String> proteinAccessions = new ArrayList<String>();

        //Iterate over each protein identification
        for (String proteinIdentificationId : prideXmlReader.getIdentIds()) {
            uk.ac.ebi.pride.jaxb.model.Identification prideXmlIdentification = prideXmlReader.getIdentById(proteinIdentificationId);

            proteinAccessions.add(prideXmlIdentification.getAccession());
        }
        return proteinAccessions;
    }

    @Override
    public List<ConversionError> getSpectraAsMgf(File experimentPrideXmlFile, File mgfFile) throws XMLConversionException {
        return PrideXMLToMGFConverter.getInstance().extractMGFFromPrideXML(experimentPrideXmlFile, mgfFile);
    }

    /**
     * Map a ModificationItem onto the pipeline Modification
     *
     * @param modificationItem the ModificationItem
     * @param peptideSequence the peptide sequence
     * @return the mapped modification
     */
    private Modification mapModification(ModificationItem modificationItem, String peptideSequence) {
        Integer modificationLocation = modificationItem.getModLocation().intValue();

        Modification.Location location = null;
        int sequenceIndex;

        if (modificationLocation == 0) {
            location = Modification.Location.N_TERMINAL;
            sequenceIndex = 0;
        } else if (0 < modificationLocation && modificationLocation < (peptideSequence.length() + 1)) {
            location = Modification.Location.NON_TERMINAL;
            sequenceIndex = modificationLocation - 1;
        } else if (modificationLocation == (peptideSequence.length() + 1)) {
            location = Modification.Location.C_TERMINAL;
            sequenceIndex = peptideSequence.length() - 1;
        } else {
            //in this case, return null for the modification
            return null;
        }

        double monoIsotopicMassShift = (modificationItem.getModMonoDelta().isEmpty()) ? 0.0 : Double.parseDouble(modificationItem.getModMonoDelta().get(0));
        //if average mass shift is empty, use the monoisotopic mass.
        double averageMassShift = (modificationItem.getModAvgDelta().isEmpty()) ? monoIsotopicMassShift : Double.parseDouble(modificationItem.getModAvgDelta().get(0));
        String accessionValue = (modificationItem.getAdditional().getCvParamByAcc(modificationItem.getModAccession()) == null) ? modificationItem.getModAccession() : modificationItem.getAdditional().getCvParamByAcc(modificationItem.getModAccession()).getName();

        Modification modification = new Modification(accessionValue, monoIsotopicMassShift, averageMassShift, location, EnumSet.noneOf(AminoAcid.class), modificationItem.getModAccession(), accessionValue);
        modification.getAffectedAminoAcids().add(AminoAcid.getAA(peptideSequence.substring(sequenceIndex, sequenceIndex + 1)));
        modification.setOrigin(Modification.Origin.PRIDE);

        return modification;
    }
}
