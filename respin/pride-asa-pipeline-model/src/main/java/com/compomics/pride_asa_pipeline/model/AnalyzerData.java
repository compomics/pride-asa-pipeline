package com.compomics.pride_asa_pipeline.model;

import org.apache.log4j.Logger;

/**
 * @author Florian Reisinger Date: 08-Aug-2009
 * @since 0.1
 */
public class AnalyzerData {

    public static enum ANALYZER_FAMILY {

        IONTRAP,
        TOF,
        FT,
        ORBITRAP,
        UNKNOWN
    }
    
    private static final Logger LOGGER = Logger.getLogger(AnalyzerData.class);

    //instrument mass error on precursor ions
    private Double precursorMassError;
    //instrument mass error on fragment ions
    private Double fragmentMassError;
    //instrument identifier/name
    private ANALYZER_FAMILY analyzerFamily;

    public AnalyzerData(Double precursorMassError, Double fragmentMassError, ANALYZER_FAMILY analyzerFamily) {
        this.precursorMassError = precursorMassError;
        this.fragmentMassError = fragmentMassError;
        this.analyzerFamily = analyzerFamily;
    }

    public Double getPrecursorMassError() {
        return precursorMassError;
    }

    public Double getFragmentMassError() {
        return fragmentMassError;
    }

    public ANALYZER_FAMILY getAnalyzerFamily() {
        return analyzerFamily;
    }

    @Override
    public String toString() {
        return "AnalyzerData{"
                + "precursorMassError=" + precursorMassError
                + ", fragmentMassError=" + fragmentMassError
                + ", analyzer family='" + analyzerFamily.toString() + '\''
                + '}';
    }

    public static AnalyzerData getAnalyzerDataByAnalyzerType(String analyzerType) {

        /*
         * iontrap PSI:1000010	Analyzer Type	3D-Ion Trap iontrap PSI:1000010
         * analyzer type	ion trap iontrap PSI:1000010	Analyzer Type	Ion_trap
         * iontrap PSI:1000010	Analyzer Type	Quadrupole Ion Trap iontrap
         * PSI:1000010	AnalyzerType	AxialEjectionLinearIonTrap iontrap
         * PSI:1000010	AnalyzerType	Ion Trap iontrap PSI:1000010	AnalyzerType
         * IonTrap iontrap PSI:1000010	AnalyzerType	Quadrupole Ion Trap iontrap
         * PSI:1000078	Axial Ejection Linear Ion Trap iontrap PSI:1000078
         * AxialEjectionLinearIonTrap iontrap PSI:1000081	Quadrupole ion trap
         * iontrap PSI:1000082	AnalyzerType	QIT iontrap PSI:1000082	Quadrupole
         * Ion Trap iontrap PSI:1000083	RadialEjectionLinearIonTrap iontrap
         * PSI:1000264	Ion Trap iontrap PSI:1000291	Linear Ion Trap	2D linear
         * quadrupole ion trap iontrap PSI:1000291	Linear Ion Trap iontrap
         * PSI:1000139	4000 Q TRAP iontrap PSI:1000010	AnalyzerType	LinearTrap
         * iontrap PSI:1000169	LCQ Deca XP Plus iontrap PSI:1000447	LTQ
         *
         * tof PSI:1000010	Analyzer Type	Tof tof PSI:1000010	Analyzer Type
         * TOF_TOF tof PSI:1000010	AnalyzerType	Q-TOF tof PSI:1000010
         * AnalyzerType	Quadrupole-TOF tof PSI:1000010	AnalyzerType	TOF tof
         * PSI:1000084	Time-of-flight tof PSI:1000084	Time-of-flight tof
         * PSI:1000084	TOF tof PSI:1000149	autoFlex TOF/TOF tof PSI:1000188
         * Q-Tof micro tof PSI:1000202	Bruker Daltonics ultraFlex TOF/TOF MS tof
         * PSI:1000189	Q-Tof Ultima tof PSI:1000140	4700 Proteomic Analyzer tof
         * PSI:1000190	QSTAR tof PSI:1000081	Quadrupole
         *
         * ft PSI:1000010	AnalyzerType	LinearTrap/FTMS ft PSI:1000079	Fourier
         * Transform Ion Cyclotron Resonance Mass Spectrometer ft PSI:1000079
         * FT-ICR ft PSI:1000448	LTQ FT ft PSI:1000010	analyzer type	fourier
         * transform ion cyclotron resonance mass spectrometer ft PSI:1000010
         * Analyzer Type	FT ft PSI:1000010	AnalyzerType	FT
         *
         * orbitrap PSI:1000010	AnalyzerType	LinearTrap/Orbitrap orbitrap
         * PSI:1000010	AnalyzerType	Orbitrap orbitrap PSI:1000449	LTQ Orbitrap
         *
         *
         */
        if (analyzerType == null || "".equals(analyzerType.trim())) {
            LOGGER.debug("Analyzer not annontated! " + analyzerType);
            return new AnalyzerData(1.0, 1.0, ANALYZER_FAMILY.UNKNOWN);
        }

        String lcAnalyzerType = analyzerType.trim().toLowerCase().replaceAll(" +", "").replaceAll("_|-", "");

        //iontrap - 1.0 1.0
        if (lcAnalyzerType.indexOf("iontrap") > -1) {
            LOGGER.debug(analyzerType + " recignized as iontrap");
            return new AnalyzerData(1.0, 1.0, ANALYZER_FAMILY.IONTRAP);
        } else if (lcAnalyzerType.indexOf("qit") > -1) {
            LOGGER.debug(analyzerType + " recignized as iontrap");
            return new AnalyzerData(1.0, 1.0, ANALYZER_FAMILY.IONTRAP);
        } else if (lcAnalyzerType.indexOf("qtrap") > -1) {
            LOGGER.debug(analyzerType + " recignized as iontrap");
            return new AnalyzerData(1.0, 1.0, ANALYZER_FAMILY.IONTRAP);
        } else if (lcAnalyzerType.indexOf("lineartrap") > -1) {
            LOGGER.debug(analyzerType + " recignized as iontrap");
            return new AnalyzerData(1.0, 1.0, ANALYZER_FAMILY.IONTRAP);
        } else if (lcAnalyzerType.indexOf("lcq") > -1) {
            LOGGER.debug(analyzerType + " recignized as iontrap");
            return new AnalyzerData(1.0, 1.0, ANALYZER_FAMILY.IONTRAP);
        } else if (lcAnalyzerType.equals("ltq")) {
            LOGGER.debug(analyzerType + " recignized as iontrap");
            return new AnalyzerData(1.0, 1.0, ANALYZER_FAMILY.IONTRAP);
        }

        //tof/qtof - 0.3 0.3
        if (lcAnalyzerType.indexOf("tof") > -1) {
            LOGGER.debug(analyzerType + " recignized as TOF");
            return new AnalyzerData(0.3, 0.3, ANALYZER_FAMILY.TOF);
        } else if (lcAnalyzerType.indexOf("timeofflight") > -1) {
            LOGGER.debug(analyzerType + " recignized as TOF");
            return new AnalyzerData(0.3, 0.3, ANALYZER_FAMILY.TOF);
        } else if (lcAnalyzerType.indexOf("4700") > -1) {
            LOGGER.debug(analyzerType + " recignized as TOF");
            return new AnalyzerData(0.3, 0.3, ANALYZER_FAMILY.TOF);
        } else if (lcAnalyzerType.indexOf("qstar") > -1) {
            LOGGER.debug(analyzerType + " recignized as TOF");
            return new AnalyzerData(0.3, 0.3, ANALYZER_FAMILY.TOF);
        } else if (lcAnalyzerType.equals("quadrupole")) {
            LOGGER.debug(analyzerType + " recignized as TOF");
            return new AnalyzerData(0.3, 0.3, ANALYZER_FAMILY.TOF);
        }

        //orbitrap - 0.1 0.6
        if (lcAnalyzerType.indexOf("orbitrap") > -1) {
            LOGGER.debug(analyzerType + " recignized as orbitrap");
            return new AnalyzerData(0.1, 0.6, ANALYZER_FAMILY.ORBITRAP);
        }

        //ft - 0.1 0.6
        if (lcAnalyzerType.indexOf("fourier") > -1) {
            LOGGER.debug(analyzerType + " recignized as FT");
            return new AnalyzerData(0.1, 0.6, ANALYZER_FAMILY.FT);
        } else if (lcAnalyzerType.indexOf("ft") > -1) { // important to check for toftof before otherwise it'll be mislabeled
            LOGGER.debug(analyzerType + " recignized as FT");
            return new AnalyzerData(0.1, 0.6, ANALYZER_FAMILY.FT);
        }

        LOGGER.warn(analyzerType + " UNRECOGNIZED");
        return new AnalyzerData(1.0, 1.0, ANALYZER_FAMILY.UNKNOWN);
    }
}
