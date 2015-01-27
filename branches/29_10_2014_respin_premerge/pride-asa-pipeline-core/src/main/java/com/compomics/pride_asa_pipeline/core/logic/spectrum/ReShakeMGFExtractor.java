/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.spectrum;

import com.compomics.pride_asa_pipeline.core.exceptions.MGFExtractionException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.jaxb.model.CvParam;
import uk.ac.ebi.pride.jaxb.model.Precursor;
import uk.ac.ebi.pride.jaxb.model.Spectrum;
import uk.ac.ebi.pride.jaxb.xml.PrideXmlReader;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 *
 * @author Kenneth Verheggen
 */
public class ReShakeMGFExtractor extends DefaultMGFExtractor {

    private final static Logger LOGGER = Logger.getLogger(ReShakeMGFExtractor.class);
    private Integer maxPrecursorCharge = null;
    private Integer minPrecursorCharge = null;

    public ReShakeMGFExtractor() {
        super();
    }

    public ReShakeMGFExtractor(File inputFile) throws ClassNotFoundException, MzXMLParsingException, JMzReaderException {
        super(inputFile);
    }

    public File extractMGF() throws MGFExtractionException {
        File mgfFile = new File(inputFile.getAbsolutePath().replace(".xml", ".mgf").replace(".gz",""));
        return extractMGF(mgfFile);
    }

    /**
     * Convert the PRIDE XML file to mgf.
     *
     * @param outputFile
     * @return
     * @throws
     * com.compomics.pride_asa_pipeline.core.exceptions.MGFExtractionException
     */
    @Override
    public File extractMGF(File outputFile) throws MGFExtractionException {
        try {
            PrideXmlReader prideXmlReader = new PrideXmlReader(inputFile);
            try (FileWriter w = new FileWriter(outputFile); BufferedWriter bw = new BufferedWriter(w)) {
                List<String> spectra = prideXmlReader.getSpectrumIds();
                int spectraCount = spectra.size();
                if (spectraCount == 0) {
                    bw.close();
                    w.close();
                    throw new MGFExtractionException("No spectra present");
                }
                int validSpectrumCount = 0;
                for (String spectrumId : spectra) {
                    Spectrum spectrum = (Spectrum) prideXmlReader.getSpectrumById(spectrumId);
                    boolean valid = asMgf(spectrum, bw);
                    if (valid) {
                        validSpectrumCount++;
                    }
                }
                if (validSpectrumCount == 0) {
                    throw new MGFExtractionException("The file (" + inputFile.getName() + ") contains no valid spectra!");
                }
            }
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
        return outputFile;
    }

    /**
     * Writes the given spectrum to the buffered writer.
     *
     * @param spectrum
     * @param bw
     * @return true of the spectrum could be converted to mgf
     * @throws IOException
     */
    public boolean asMgf(Spectrum spectrum, BufferedWriter bw) throws IOException {
        boolean valid = true;
        int msLevel = spectrum.getSpectrumDesc().getSpectrumSettings().getSpectrumInstrument().getMsLevel();
        // ignore ms levels other than 2
        if (msLevel == 2) {
            // add precursor details
            if (spectrum.getSpectrumDesc().getPrecursorList() != null
                    && spectrum.getSpectrumDesc().getPrecursorList().getPrecursor() != null
                    && spectrum.getSpectrumDesc().getPrecursorList().getPrecursor().size() > 0) {
                bw.write("BEGIN IONS" + System.getProperty("line.separator"));
                bw.write("TITLE=" + spectrum.getId() + System.getProperty("line.separator"));

                Precursor precursor = spectrum.getSpectrumDesc().getPrecursorList().getPrecursor().get(0); // get the first precursor
                List<CvParam> precursorCvParams = precursor.getIonSelection().getCvParam();
                Double precursorMz = null, precursorIntensity = null;
                String precursorRt = null;
                Integer precursorCharge = null;

                for (CvParam cvParam : precursorCvParams) {
                    if (cvParam.getAccession().equalsIgnoreCase("MS:1000744") || cvParam.getAccession().equalsIgnoreCase("PSI:1000040")) { // precursor m/z
                        precursorMz = new Double(cvParam.getValue());
                    } else if (cvParam.getAccession().equalsIgnoreCase("MS:1000042") || cvParam.getAccession().equalsIgnoreCase("PSI:1000042")) { // precursor intensity
                        precursorIntensity = new Double(cvParam.getValue());
                    } else if (cvParam.getAccession().equalsIgnoreCase("MS:1000041") || cvParam.getAccession().equalsIgnoreCase("PSI:1000041")) { // precursor charge
                        precursorCharge = new Integer(cvParam.getValue());
                    } else if (cvParam.getAccession().equalsIgnoreCase("PRIDE:0000203") || cvParam.getAccession().equalsIgnoreCase("MS:1000894")) { // precursor retention time
                        precursorRt = cvParam.getValue();
                    }
                }

                if (precursorMz != null) {
                    bw.write("PEPMASS=" + precursorMz);
                } else {
                    valid = false; // @TODO: cancel conversion??
                }

                if (precursorIntensity != null) {
                    bw.write("\t" + precursorIntensity);
                }

                bw.write(System.getProperty("line.separator"));

                if (precursorRt != null) {
                    bw.write("RTINSECONDS=" + precursorRt + System.getProperty("line.separator")); // @TODO: improve the retention time mapping, e.g., support rt windows
                }

                if (precursorCharge != null) {
                    bw.write("CHARGE=" + precursorCharge + System.getProperty("line.separator"));

                    if (maxPrecursorCharge == null || precursorCharge > maxPrecursorCharge) {
                        maxPrecursorCharge = precursorCharge;
                    }
                    if (minPrecursorCharge == null || precursorCharge < minPrecursorCharge) {
                        minPrecursorCharge = precursorCharge;
                    }
                } else {
                    //valid = false; // @TODO: can we use spectra without precursor charge??
                }

                // process all peaks by iterating over the m/z values
                Number[] mzBinaryArray = spectrum.getMzNumberArray();
                Number[] intensityArray = spectrum.getIntentArray();

                for (int i = 0; i < mzBinaryArray.length; i++) {
                    bw.write(mzBinaryArray[i].toString());
                    bw.write(" ");
                    bw.write(intensityArray[i] + System.getProperty("line.separator"));
                }

                bw.write("END IONS" + System.getProperty("line.separator") + System.getProperty("line.separator"));

            } else {
                valid = false;
            }
        } else {
            valid = false;
        }

        return valid;
    }

}
