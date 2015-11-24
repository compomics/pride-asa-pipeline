package com.compomics.pride_asa_pipeline.core.logic.inference.contaminants;

import com.compomics.pride_asa_pipeline.model.FragmentIonAnnotation;
import com.compomics.pride_asa_pipeline.model.Identification;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
    /**
     * The allowed range surrounding a "hit". This defaults to 1.0, but can also
     * be set to the actual accuracies in the report.
     */
    private static final double tolerance = 1.0;
    /**
     * The percentile of peaks to use for this analysis. This is to eliminate
     * contaminants being found in the noise.
     */
    private static final double peakFiltrationPercentile = 70;
    /**
     * A list of contaminations on the precursor level
     */
    private static final List<Contamination> precursorContamination = new ArrayList<>();
    /**
     * A list of contaminations on the fragment ion level
     */
    private static final List<Contamination> fragmentContamination = new ArrayList<>();
    /**
     * A hashmap of known mass shifts caused by technical contaminations. More
     * can be added to the property file
     */
    private static final HashMap<String, Double> knownMassShifts = getKnownMassShifts();

    /**
     * Scans the given hashmap of identifications with their corresponding
     * unexplained delta mass for contaminants on the precursor level
     *
     * @param unexplainedPrecursorMasses a hashmap of identifications and the
     * corresponding unexplained precursor mass difference
     */
    public static void scanForPrecursorIonContamination(HashMap<Identification, Double> unexplainedPrecursorMasses) {
        for (Map.Entry<Identification, Double> identification : unexplainedPrecursorMasses.entrySet()) {
            String sequence = identification.getKey().getPeptide().getSequenceString();
            double observedMassDifference = identification.getValue();
            for (Map.Entry<String, Double> shift : knownMassShifts.entrySet()) {
                double aQueryMass = shift.getValue();
                if (aQueryMass - tolerance <= observedMassDifference && observedMassDifference <= aQueryMass + tolerance) {
                    precursorContamination.add(new Contamination(shift.getKey(), sequence, observedMassDifference, aQueryMass, false));
                }
            }
        }
    }

    /**
     * Scans the given collection of identifications for unexplained masses in
     * between peaks on the fragment ion level
     *
     * @param identifications a collection of identifications
     */
    public static void reportFragmentIonContamination(Collection<Identification> identifications) {
        for (Identification identification : identifications) {
            String sequence = identification.getPeptide().getSequenceString();
            try {
                List<FragmentIonAnnotation> fragmentIonAnnotations = (identification.getAnnotationData().getFragmentIonAnnotations());
                double[] masses = new double[fragmentIonAnnotations.size()];
                int i = 0;
                for (FragmentIonAnnotation anIon : fragmentIonAnnotations) {
                    masses[i] = anIon.getMz() * anIon.getIon_charge();
                    i++;
                }
                for (int j = 0; j < masses.length; j++) {
                    double[] temp = new double[masses.length];
                    System.arraycopy(masses, i, temp, 0, temp.length - i);
                    for (int k = 0; k < temp.length; k++) {
                        for (Map.Entry<String, Double> shift : knownMassShifts.entrySet()) {
                            double observedMassDifference = Math.abs(masses[j] - temp[k]);
                            double aQueryMass = shift.getValue();
                            if (aQueryMass - tolerance <= observedMassDifference && observedMassDifference <= aQueryMass + tolerance) {
                                precursorContamination.add(new Contamination(shift.getKey(), sequence, observedMassDifference, aQueryMass, true));
                            }
                        }
                    }
                }
            } catch (NullPointerException e) {
                //no annotations?
            }
        }
    }

    /**
     * Prints the report of the scan(s) to a file
     *
     * @param outputFile the output file
     * @param precursorTolerance the actual precursor tolerance
     * @param fragmentIonTolerance the actual fragment ion tolerance
     */
    public static void printToFile(File outputFile, double precursorTolerance, double fragmentIonTolerance) {

        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.append("Validated against " + precursorTolerance + " da precursor tolerance and " + fragmentIonTolerance + " da fragment ion tolerance").append(System.lineSeparator()).flush();
            writer.append(Contamination.getHeader()).append(System.lineSeparator());
            for (Contamination aContaminant : precursorContamination) {
                writer.append(aContaminant.toString(precursorTolerance)).append(System.lineSeparator()).flush();
            }
            for (Contamination aContaminant : fragmentContamination) {
                writer.append(aContaminant.toString(fragmentIonTolerance)).append(System.lineSeparator()).flush();
            }
            writer.flush();
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }

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

}
