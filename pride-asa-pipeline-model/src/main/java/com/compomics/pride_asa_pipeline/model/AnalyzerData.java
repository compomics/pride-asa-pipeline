package com.compomics.pride_asa_pipeline.model;

import org.apache.log4j.Logger;

/**
 * @author Florian Reisinger Date: 08-Aug-2009
 * @since 0.1
 */
public class AnalyzerData {

    private double prec_acc;
    private double frag_acc;

    private void setPrecursorAccuraccy(double prec_acc) {
        this.prec_acc = prec_acc;
    }

    private void setFragmentIonAccuraccy(double frag_acc) {
        this.frag_acc = frag_acc;
    }

    public double getPrecursorAccuraccy() {
        return prec_acc;
    }

    public double getFragmentAccuraccy() {
        return frag_acc;
    }

    public static enum ANALYZER_FAMILY {
        IONTRAP(1.0, 1.0),
        TOF(0.3, 0.3),
        FT(0.1, 0.6),
        ORBITRAP(0.1, 0.6),
        QEXACTIVE(0.02, 0.04),
        UNKNOWN(1.0, 1.0);
        private final double defaultPrecursorMassError;
        private final double defaultFragmentMassError;

        private ANALYZER_FAMILY(double precursorMassError, double fragmentMassError) {
            this.defaultPrecursorMassError = precursorMassError;
            this.defaultFragmentMassError = fragmentMassError;
        }

        public double getPrecursorMassError() {
            return defaultPrecursorMassError;
        }

        public double getFragmentMassError() {
            return defaultFragmentMassError;
        }

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

    public AnalyzerData(ANALYZER_FAMILY analyzerFamily) {
        this.precursorMassError = analyzerFamily.getPrecursorMassError();
        this.fragmentMassError = analyzerFamily.getFragmentMassError();
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

    public static AnalyzerData getDataDerivedAnalyzerData(double prec_acc, double frag_acc) {
        AnalyzerData dataDerivedAnalyzer = new AnalyzerData(prec_acc, frag_acc, ANALYZER_FAMILY.UNKNOWN);
        return dataDerivedAnalyzer;
    }

    public static AnalyzerData getDataDerivedAnalyzerDataByAnalyzerType(double prec_acc, double frag_acc, String analyzerType) {
        AnalyzerData analyzerDataByAnalyzerType = getAnalyzerDataByAnalyzerType(analyzerType);
        analyzerDataByAnalyzerType.setPrecursorAccuraccy(prec_acc);
        analyzerDataByAnalyzerType.setFragmentIonAccuraccy(frag_acc);
        return analyzerDataByAnalyzerType;
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
            LOGGER.debug("Analyzer not annotated! " + analyzerType);
            return new AnalyzerData(ANALYZER_FAMILY.UNKNOWN);
        }

        String lcAnalyzerType = analyzerType.trim().toLowerCase().replaceAll(" +", "").replaceAll("_|-", "");
        String[] possibleMachineTerms;

//check for orbitrap
        possibleMachineTerms = new String[]{"exactive"};
        for (String anOrbi : possibleMachineTerms) {
            if (lcAnalyzerType.contains(anOrbi)) {
                LOGGER.debug(analyzerType + " recognized as Q-exactive");
                return new AnalyzerData(ANALYZER_FAMILY.QEXACTIVE);
            }
        }
//check for iontraps
        possibleMachineTerms = new String[]{"iontrap", "qit", "qtrap", "lineartrap", "lcq", "ltq"};
        for (String anIonTrap : possibleMachineTerms) {
            if (lcAnalyzerType.contains(anIonTrap)) {
                LOGGER.debug(analyzerType + " recognized as iontrap");
                return new AnalyzerData(ANALYZER_FAMILY.IONTRAP);
            }
        }
        //check for TOF
        possibleMachineTerms = new String[]{"tof", "timeofflight", "qstar", "quadrupole"};
        for (String aTof : possibleMachineTerms) {
            if (lcAnalyzerType.contains(aTof)) {
                LOGGER.debug(analyzerType + " recognized as Time Of Flight");
                return new AnalyzerData(ANALYZER_FAMILY.TOF);
            }
        }
        //check for orbitrap
        possibleMachineTerms = new String[]{"orbitrap", "orbi"};
        for (String anOrbi : possibleMachineTerms) {
            if (lcAnalyzerType.contains(anOrbi)) {
                LOGGER.debug(analyzerType + " recognized as Orbitrap");
                return new AnalyzerData(ANALYZER_FAMILY.ORBITRAP);
            }
        }
        //check for FT
        possibleMachineTerms = new String[]{"fourier", "ft", "transform"};
        for (String anFT : possibleMachineTerms) {
            if (lcAnalyzerType.contains(anFT)) {
                LOGGER.debug(analyzerType + " recognized as Fourrier Transform");
                return new AnalyzerData(ANALYZER_FAMILY.FT);
            }
        }
        //unknown !
        LOGGER.warn(analyzerType + " UNRECOGNIZED");
        return new AnalyzerData(ANALYZER_FAMILY.UNKNOWN);
    }
}
