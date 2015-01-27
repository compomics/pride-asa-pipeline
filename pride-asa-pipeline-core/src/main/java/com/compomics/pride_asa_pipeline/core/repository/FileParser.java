package com.compomics.pride_asa_pipeline.core.repository;

import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.Peak;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public interface FileParser {

    void attachSpectra(File peakFile) throws Exception;

    /**
     * Inits the parser with the given Pride XML file
     *
     * @param prideXmlFile the Pride XML file
     * @throws java.lang.ClassNotFoundException
     * @throws uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException
     * @throws uk.ac.ebi.pride.tools.jmzreader.JMzReaderException
     */
    void init(File prideXmlFile) throws ClassNotFoundException, MzXMLParsingException, JMzReaderException;

    /**
     * Clears the parser resources
     *
     */
    void clear();

    /**
     * Gets the experiment identifications
     *
     * @return the list of experiment identifications
     */
    List<Identification> getExperimentIdentifications();

    /**
     * Gets the number of spectra
     *
     * @return the number of spectra
     */
    long getNumberOfSpectra();

    /**
     * Gets the number of peptides
     *
     * @return the number of peptides
     */
    long getNumberOfPeptides();

    /**
     * Gets the number of unique peptides
     *
     * @return the number of unique peptides
     */
    long getNumberUniquePeptides();

    /**
     * Gets the spectrum metadat for a given experiment; experiment ID,
     * precursor mz value and charge state
     *
     * @return the spectra metadata
     */
    List<Map<String, Object>> getSpectraMetadata();

    /**
     * Gets the spectrum IDs
     *
     * @return the spectrum IDs
     */
    List<String> getSpectrumIds();

    /**
     * Gets the modifications found in the file.
     *
     * @return the list of found modifications
     */
    List<Modification> getModifications();

    /**
     * Gets the analyzer sources as a map (key: cv accession, value: cv value)
     *
     * @return the analyzer source map
     */
    Map<String, String> getAnalyzerSources();

    /**
     * Gets the experiment analyzer data
     *
     * @return the experiment analyzer data list
     */
    List<AnalyzerData> getAnalyzerData();

    /**
     * Gets protein accessions for a given experiment
     *
     * @return the protein accession list
     */
    List<String> getProteinAccessions();

    /**
     * Gets the spectrum peak list by spectrum ID.
     *
     * @param spectrumId the spectrum ID
     * @return the spectrum peaks
     */
    List<Peak> getSpectrumPeaksBySpectrumId(String spectrumId);

    /**
     * Gets the spectrum peak map (key: mz value, value: intensity value) by
     * spectrum ID.
     *
     * @param spectrumId the spectrum ID
     * @return the spectrum peaks
     */
    HashMap<Double, Double> getSpectrumPeakMapBySpectrumId(String spectrumId);

}
