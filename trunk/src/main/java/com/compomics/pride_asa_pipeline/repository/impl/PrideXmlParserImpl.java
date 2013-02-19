package com.compomics.pride_asa_pipeline.repository.impl;

import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import com.compomics.pride_asa_pipeline.repository.PrideXmlParser;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.jaxb.model.CvParam;
import uk.ac.ebi.pride.jaxb.model.PeptideItem;
import uk.ac.ebi.pride.jaxb.model.Precursor;
import uk.ac.ebi.pride.jaxb.xml.PrideXmlReader;

/**
 *
 * @author Niels Hulstaert
 */
public class PrideXmlParserImpl implements PrideXmlParser {

    private static final Logger LOGGER = Logger.getLogger(PrideXmlParserImpl.class);
    /**
     * The PrideXmlReader instance
     */
    private PrideXmlReader prideXmlReader;
    private List<String> identificationIds;

    /**
     * Creates a new PrideXmlReader instance
     *
     * @param prideXmlFile
     */
    @Override
    public void init(File prideXmlFile) {
        prideXmlReader = new PrideXmlReader(prideXmlFile);
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Identification> loadExperimentIdentifications() {
        List<Identification> identifications = new ArrayList<Identification>();
        
        //Get a list of Identification ids
        identificationIds = prideXmlReader.getIdentIds();

        //Iterate over each identification
        for (String identificationId : identificationIds) {
            uk.ac.ebi.pride.jaxb.model.Identification prideXmlIdentification = prideXmlReader.getIdentById(identificationId);
            String identificationAccession = prideXmlIdentification.getAccession();

            //Get the number of peptides within an identification
            int numOfPeptides = prideXmlReader.getNumberOfPeptides(identificationId);

            //Iterate over each peptide
            for (int i = 0; i < numOfPeptides; i++) {
                PeptideItem peptideItem = prideXmlReader.getPeptide(identificationId, i);
                //get spectrum precursor for m/z and charge
                Precursor precursor = peptideItem.getSpectrum().getSpectrumDesc().getPrecursorList().getPrecursor().get(0);

                //get precursor CvParams
                List<CvParam> precursorCvParams = precursor.getIonSelection().getCvParam();
                double mzRatio = 0.0;
                int charge = 0;
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
                    Identification identification = new Identification(peptide, identificationId, spectrumId, 0L);
                    
                    //add to identifications
                    identifications.add(identification);
                } catch (UnknownAAException ex) {
                    LOGGER.error(ex.getMessage(), ex);
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
        //prideXmlReader.get
        
        
        return null;
    }
    
    
    
}
