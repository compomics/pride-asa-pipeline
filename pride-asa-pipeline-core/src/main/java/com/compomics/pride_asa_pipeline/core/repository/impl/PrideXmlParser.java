package com.compomics.pride_asa_pipeline.core.repository.impl;

import com.compomics.pride_asa_pipeline.core.exceptions.MGFExtractionException;
import com.compomics.pride_asa_pipeline.core.logic.modification.PTMMapper;
import com.compomics.pride_asa_pipeline.core.logic.spectrum.DefaultMGFExtractor;
import com.compomics.pride_asa_pipeline.core.repository.FileParser;
import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.geneontology.oboedit.dataadapter.GOBOParseException;
import org.xmlpull.v1.XmlPullParserException;
import uk.ac.ebi.pride.jaxb.model.CvParam;
import uk.ac.ebi.pride.jaxb.model.GelFreeIdentification;
import uk.ac.ebi.pride.jaxb.model.Instrument;
import uk.ac.ebi.pride.jaxb.model.ModificationItem;
import uk.ac.ebi.pride.jaxb.model.Param;
import uk.ac.ebi.pride.jaxb.model.PeptideItem;
import uk.ac.ebi.pride.jaxb.model.Precursor;
import uk.ac.ebi.pride.jaxb.model.ProtocolSteps;
import uk.ac.ebi.pride.jaxb.model.UserParam;
import uk.ac.ebi.pride.jaxb.xml.PrideXmlReader;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 *
 * @author Niels Hulstaert
 */
/**
 *
 * @author Kenneth Verheggen
 */
public class PrideXmlParser implements FileParser {

    private static final Logger LOGGER = Logger.getLogger(PrideXmlParser.class);
    private static final String IONIZER_TYPE = "PSI:1000008";
    private static final String MALDI_SOURCE_ACCESSION = "PSI:1000075";
    /**
     * The PrideXmlReader instance
     */
    private PrideXmlReader prideXmlReader;
    private DefaultMGFExtractor mgfExtractor;
    private final Set<Modification> modifications = new HashSet<>();
    private List<Identification> identifications;
    private PTMFactory factory;
    private PTMMapper pridePtmMapper;
    private final long timeout = 30000;
    private int prideXmlIdentifiedSpectraCount;

    /**
     * The list of modification found in PRIDE
     */
    public PrideXmlParser() {
    }

    /**
     * Creates a new PrideXmlReader instance
     *
     * @param prideXmlFile
     */
    @Override
    public void init(File prideXmlFile) throws ClassNotFoundException, MzXMLParsingException, JMzReaderException {
        prideXmlReader = new PrideXmlReader(prideXmlFile);
        //Load standard mods in factory : 
        modifications.clear();
    }

    @Override
    public void clear() {
        prideXmlReader = null;
        modifications.clear();
    }

    @Override
    public List<Identification> getExperimentIdentifications() {
        identifications = new ArrayList<>();
        //Iterate over each protein identification
        for (String proteinIdentificationId : prideXmlReader.getIdentIds()) {
            //uk.ac.ebi.pride.jaxb.model.Identification prideXmlIdentification = prideXmlReader.getIdentById(identificationId);

            //Get the number of peptides within an identification
            int numOfPeptides = prideXmlReader.getNumberOfPeptides(proteinIdentificationId);

            //Iterate over each peptide identification
            for (int i = 0; i < numOfPeptides; i++) {
                PeptideItem peptideItem = prideXmlReader.getPeptide(proteinIdentificationId, i);

                //check for possible null pointers
                if (peptideItem.getSpectrum() != null && peptideItem.getSpectrum().getSpectrumDesc().getPrecursorList() != null && !peptideItem.getSpectrum().getSpectrumDesc().getPrecursorList().getPrecursor().isEmpty()) {
                    //get modifications
                    List<ModificationItem> modificationItems = peptideItem.getModificationItem();
                    for (ModificationItem modificationItem : modificationItems) {
                        Modification modification;
                        modification = PTMMapper.mapModification(modificationItem, peptideItem.getSequence());
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
                        String spectrumId = Long.toString(peptideItem.getSpectrum().getId());
                        if (prideXmlReader.isIdentifiedSpectrum(spectrumId)) {
                            prideXmlIdentifiedSpectraCount++;
                        }
                        Identification identification = new Identification(peptide, proteinIdentificationId, spectrumId, spectrumId);

                        //add to identifications
                        identifications.add(identification);
                    } catch (UnknownAAException ex) {
                        LOGGER.error(ex.getMessage(), ex);
                    }
                } else {
                    LOGGER.warn("Could not find precursor for " + proteinIdentificationId);
                }
            }
        }

        return identifications;
    }

    @Override
    public long getNumberOfSpectra() {
        return mgfExtractor.getSpectrumIds().size();
    }

    @Override
    public long getNumberOfPeptides() {
        return prideXmlReader.getNumberOfPeptides();
    }

    @Override
    public long getNumberUniquePeptides() {
        Set<String> uniquePeptides = new HashSet<>();
        for (String identId : prideXmlReader.getIdentIds()) {
            for (PeptideItem aPeptideItem : prideXmlReader.getIdentById(identId).getPeptideItem()) {
                uniquePeptides.add(aPeptideItem.getSequence());
            }
        }
        return uniquePeptides.size();
    }

    @Override
    public List<Map<String, Object>> getSpectraMetadata() {
        return mgfExtractor.getSpectraMetadata();
    }

    @Override
    public List<String> getSpectrumIds() {
        return mgfExtractor.getSpectrumIds();
    }

    @Override
    public List<Modification> getModifications() {
        if (modifications.isEmpty() || identifications == null) {
            //modifications are loaded in the getExperimentIdentifications method...
            identifications = getExperimentIdentifications();
        }
        List<Modification> modList = new ArrayList<>();
        for (Modification aMod : modifications) {
            modList.add(aMod);
        }
        return modList;
    }

    @Override
    public List<Peak> getSpectrumPeaksBySpectrumId(String spectrumId) {
        List<Peak> peaks = new ArrayList<>();
        uk.ac.ebi.pride.tools.jmzreader.model.Spectrum spectrumBySpectrumId = mgfExtractor.getSpectrumBySpectrumId(spectrumId);
        if (spectrumBySpectrumId != null) {
            for (Double anMz : spectrumBySpectrumId.getPeakList().keySet()) {
                Peak peak = new Peak(anMz, spectrumBySpectrumId.getPeakList().get(anMz));
                peaks.add(peak);
            }
        }
        return peaks;
    }

    @Override
    public HashMap<Double, Double> getSpectrumPeakMapBySpectrumId(String spectrumId) {
        return mgfExtractor.getSpectrumPeakMapBySpectrumId(spectrumId);
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
        List<AnalyzerData> defaultMassAnalyzerList = new ArrayList<>();
        String instrument = "unknown";
        try {
            instrument = prideXmlReader.getInstrument().getInstrumentName();
            //TODO REVIEW THIS SECTION !!!
        } catch (Exception e) {
            LOGGER.error("Could not extract instrument");
            throw (e);
        }
        defaultMassAnalyzerList.add(AnalyzerData.getAnalyzerDataByAnalyzerType(instrument));
        return defaultMassAnalyzerList;
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

    public PrideXmlReader getPrideXmlReader() {
        return prideXmlReader;
    }

    public void setPrideXmlReader(PrideXmlReader reader) {
        this.prideXmlReader = reader;
    }

    public HashMap<PTM, Boolean> getProtocolPTMs() {
        HashMap<PTM, Boolean> protocolPtmMap = new HashMap<>();
        // FIND PROTOCOL MODIFICATIONS
        //check if protocolsteps have some mods...
        try {
            ProtocolSteps protocolSteps = prideXmlReader.getProtocol().getProtocolSteps();
            for (Param aParam : protocolSteps.getStepDescription()) {
                for (CvParam aCvParam : aParam.getCvParam()) {
//MASCOT PROTOCOL SEARCH PARAMETERS
                    if (aCvParam.getName().toLowerCase().contains("modification")) {
                        String[] modsInProtocol = aCvParam.getValue().toLowerCase().replace(" ,", ",").split(",");
                        for (String aModName : modsInProtocol) {
                            addFilePtm(aModName, aCvParam.getName().toLowerCase().contains("fixed"), protocolPtmMap);
                        }
//OTHER PROTOCOL SEARCH PARAMETERS                        
                    } else if (aCvParam.getCvLabel().toLowerCase().contains("mod")) {
                        addFilePtm(aCvParam.getName(), false, protocolPtmMap);
                    } else if (aCvParam.getName().toLowerCase().contains("alkylation")) {
                        addFilePtm(aCvParam.getValue(), false, protocolPtmMap);
                    } else if (aCvParam.getAccession().equalsIgnoreCase("PRIDE:0000072")) {
                        //fixed mods
                        String[] fixedMods = aCvParam.getValue().split(",");
                        for (String aMod : fixedMods) {
                            addFilePtm(aMod.trim(), true, protocolPtmMap);
                        }
                    } else if (aCvParam.getAccession().equalsIgnoreCase("PRIDE:0000073")) {
                        //var mods
                        String[] varMods = aCvParam.getValue().split(",");
                        for (String aMod : varMods) {
                            addFilePtm(aMod.trim(), true, protocolPtmMap);
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
            LOGGER.error("Could not retrieve protocolsteps : " + e);
        }
        return protocolPtmMap;
    }

    public HashMap<PTM, Boolean> getGellFreePTMs() {
        HashMap<PTM, Boolean> protocolPtmMap = new HashMap<>();
        // FIND PROTOCOL MODIFICATIONS
        //check if protocolsteps have some mods...
        LOGGER.info("Searching for annotated peptideitems...");
        for (String aGelFreeIdent : prideXmlReader.getGelFreeIdentIds()) {
            if (aGelFreeIdent != null) {
                GelFreeIdentification gfi = prideXmlReader.getGelFreeIdentById(aGelFreeIdent);
                if (gfi == null) {
                    break;
                }
                for (PeptideItem peptideItem : gfi.getPeptideItem()) {
                    if (peptideItem == null) {
                        break;
                    }
                    for (ModificationItem modItem : peptideItem.getModificationItem()) {
                        if (modItem == null) {
                            break;
                        }
                        for (CvParam aCvParam : modItem.getAdditional().getCvParam()) {
                            if (aCvParam == null) {
                                break;
                            } else if (aCvParam.getCvLabel().toLowerCase().contains("mod")) {
                                String[] modsInProtocol = aCvParam.getName().toLowerCase().replace(" ,", ",").split(",");
                                for (String aModName : modsInProtocol) {
                                    addFilePtm(aModName, aCvParam.getName().toLowerCase().contains("fixed"), protocolPtmMap);
                                }
                            } else if (aCvParam.getCvLabel().toLowerCase().contains("mod")) {
                                addFilePtm(aCvParam.getName(), false, protocolPtmMap);
                            }
                        }
                        LOGGER.info("Searching for user annotated modifications...");
                        for (UserParam aUserParam : modItem.getAdditional().getUserParam()) {
                            if (aUserParam == null) {
                                break;
                            } else if (aUserParam.getName().toLowerCase().contains("mod")) {
                                String[] modsInProtocol = aUserParam.getValue().toLowerCase().replace(" ,", ",").split(",");
                                for (String aModName : modsInProtocol) {
                                    addFilePtm(aModName, aUserParam.getValue().toLowerCase().contains("fixed"), protocolPtmMap);
                                }
                            }
                        }
                    }
                }
            }
        }
        return protocolPtmMap;
    }

    private void addFilePtm(String aModName, boolean fixed, HashMap<PTM, Boolean> protocolPtmMap) {
        if (pridePtmMapper == null) {
            try {
                pridePtmMapper = PTMMapper.getInstance();
            } catch (XmlPullParserException | IOException | GOBOParseException ex) {
                LOGGER.error(ex);
            }
        }
        ArrayList<String> unifiedModNames = pridePtmMapper.lookupRealModNames(aModName.toLowerCase());
        for (String aUnifiedModName : unifiedModNames) {
            if (aUnifiedModName == null) {
                aUnifiedModName = aModName.toLowerCase();
            }
            if (factory == null) {
                factory = PTMFactory.getInstance();
            }
            PTM ptm = factory.getPTM(aUnifiedModName);
            if (ptm != null && !protocolPtmMap.containsKey(ptm)) {
                protocolPtmMap.put(ptm, fixed);
            }
        }
    }

    public int getIdentifiedSpectraInPrideXml() {
        return prideXmlIdentifiedSpectraCount;
    }

    @Override
    public void attachSpectra(File peakFile) throws ClassNotFoundException, MzXMLParsingException, JMzReaderException {
        mgfExtractor = new DefaultMGFExtractor(peakFile);
    }

    @Override
    public void saveMGF(File outputFile) throws JMzReaderException, IOException, MGFExtractionException {
        mgfExtractor.extractMGF(outputFile, timeout);
    }

    @Override
    public void saveMGF(File outputFile, File spectrumLogFile) throws JMzReaderException, IOException, MGFExtractionException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(spectrumLogFile)) {
            mgfExtractor.extractMGF(outputFile, fileOutputStream, timeout);
        }
    }

}
