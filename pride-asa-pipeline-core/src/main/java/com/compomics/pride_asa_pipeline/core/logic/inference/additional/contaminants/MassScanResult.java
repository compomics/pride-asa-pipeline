package com.compomics.pride_asa_pipeline.core.logic.inference.additional.contaminants;

import com.compomics.pride_asa_pipeline.core.repository.SpectrumRepository;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileSpectrumRepository;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.AtomImpl;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

import org.springframework.core.io.ClassPathResource;

/**
 * This class represents the result of a scan across all unexplained mass
 * differences (both on precursor and on fragment ion)
 *
 * @author Kenneth Verheggen
 */
public class MassScanResult {

    private static final Logger LOGGER = Logger.getLogger(MassScanResult.class);
    private static final DescriptiveStatistics errorEstimation = new DescriptiveStatistics();
    private static final HashMap<String, Integer> massCount = new HashMap<>();
    private static SpectrumRepository spectrumRepository;
    private static final double C13Mass = new AtomImpl(Atom.C, 1).getMass() - new AtomImpl(Atom.C, 0).getMass();
    /**
     * Loads the known mass shifts with their corresponding name into the
     * scanner
     *
     * @return a hashmap of known mass shifts
     */
    private static HashMap<String, Double> getKnownMassShifts() {
        HashMap<String, Double> shifts = new HashMap<>();
        shifts.put("Methyl (Me)", 14.0269);
        shifts.put("Formyl (CHO)", 28.0104);
        shifts.put("Ehtyl (Et)", 28.0538);
        shifts.put("Acetyl (Ac)", 42.0373);
        shifts.put("t-Butyl (t-Bu)", 56.1075);
        //add all ms contaminants as well?
        Properties contaminationProperties = new Properties();
        try {
            contaminationProperties.load(new ClassPathResource("resources/MSContaminants.properties").getInputStream());
            for (String key : contaminationProperties.stringPropertyNames()) {
                Double value = Double.parseDouble(contaminationProperties.getProperty(key));
                shifts.put(key, value);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return shifts;
    }

    /**
     * Scans the provided identifications and related spectra for mz values that
     * correspond to a known mass contaminant
     *
     * @param identifications the collection of identifications that need to be scanned
     */
    public static void inspectIdentifications(Collection<Identification> identifications) {
        for (Identification ident : identifications) {
            double[] mzValuesBySpectrumId = spectrumRepository.getMzValuesBySpectrumId(ident.getSpectrumId());
            double[] intensityValuesBySpectrumId = spectrumRepository.getMzValuesBySpectrumId(ident.getSpectrumId());
            DescriptiveStatistics intensityFilter = new DescriptiveStatistics(intensityValuesBySpectrumId);
            double intensityThreshold = intensityFilter.getPercentile(90);

            for (int i = 0; i < mzValuesBySpectrumId.length; i++) {
                if (intensityValuesBySpectrumId[i] >= intensityThreshold) {
                    for (Map.Entry<String, Double> massShift : getKnownMassShifts().entrySet()) {
                        double mzError = calculateMassError(massShift.getValue(), ident.getPeptide().getCharge(), mzValuesBySpectrumId[i]);
                        if (mzError != -1) {
                            errorEstimation.addValue(mzError);
                            massCount.put(massShift.getKey(), massCount.getOrDefault(massShift.getKey(), 0) + 1);
                        }
                    }
                }
            }

        }
    }


    /**
     * Calculates the mass error for a peak
     * @param mass the reference mass
     * @param charge the assumed charge for the spectrum
     * @param mz the query mz value
     * @return  the mass error between reference and experiment
     */
    private static double calculateMassError(double mass, int charge, double mz) {
        double experimentalPeakMass = mz * charge;
        double error = Math.abs(experimentalPeakMass - mass);
        //consider isotope
        if (error >= C13Mass) {
            error -= C13Mass;
        }
        if (error < 1) {
            return error;
        }
        return -1;
    }

    /**
     * Gets the median mass error based on the contaminant profile
     * @return 
     */
    public static double getContaminantBasedMassError() {
        return errorEstimation.getPercentile(50);
    }

    /**
     * Sets the spectrum repository for the mass scanner
     * @param fileSpectrumRepository 
     */
    public static void setSpectrumRepository(SpectrumRepository spectrumRepository) {
        MassScanResult.spectrumRepository = spectrumRepository;
    }

    /**
     * Gets the binned counts per discovered mass
     * @return the mass counts
     */
    public static HashMap<String, Integer> getMassCounts() {
        return massCount;
    }

}
