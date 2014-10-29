package com.compomics.pride_asa_pipeline.core.gui;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.ModificationFacade;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.core.util.MathUtils;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Niels Hulstaert
 * @author Harald Barsnes
 */
public class IdentificationsTableFormat implements AdvancedTableFormat<Object> {

    private static final Logger LOGGER = Logger.getLogger(IdentificationsTableFormat.class);
    public static final String N_TERM_PREFIX = "NT_";
    public static final String C_TERM_PREFIX = "CT_";
    public static final String MODS_OPEN = "(";
    public static final String MODS_CLOSE = ")";
    public static final String MODS_DELIMITER = ", ";
    private static final String UNMOD_MASS_DELTA_OPEN = "[";
    private static final String UNMOD_MASS_DELTA_CLOSE = "]";
    private static final String[] columnNames = {"ID", "Peptide", "Modifications", "Charge", "Mass Delta", "M/Z Delta", "Precursor m/z", "Noise Threshold", "Score"};    
    public static final int SPECTRUM_REF = 0;
    public static final int PEPTIDE = 1;
    public static final int MODIFICATIONS = 2;
    public static final int CHARGE = 3;
    public static final int MASS_DELTA = 4;
    public static final int MZ_DELTA = 5;
    public static final int PRECURSOR_MZ = 6;
    public static final int NOISE_THRESHOLD = 7;
    public static final int SCORE = 8;
    

    @Override
    public Class getColumnClass(int column) {
        switch (column) {
            case SPECTRUM_REF:
                return Long.class;
            case PEPTIDE:
                return String.class;
            case MODIFICATIONS:
                return String.class;
            case CHARGE:
                return Integer.class;
            case MASS_DELTA:
                return String.class;
            case MZ_DELTA:
                return String.class;
            case PRECURSOR_MZ:
                return Double.class;
            case NOISE_THRESHOLD:
                return Double.class;
            case SCORE:
                return Double.class;
            default:
                throw new IllegalArgumentException("Unexpected column number " + column);
        }
    }

    @Override
    public Comparator getColumnComparator(int column) {
        return GlazedLists.comparableComparator();
    }

    @Override
    public int getColumnCount() {
        return 9;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getColumnValue(Object baseObject, int column) {
        Identification identification = (Identification) baseObject;
        switch (column) {
            case SPECTRUM_REF:
                return identification.getSpectrumRef();
            case PEPTIDE:
                return identification.getPeptide().getSequenceString();
            case MODIFICATIONS:
                return constructModificationsString(identification.getPeptide());
            case CHARGE:
                return identification.getPeptide().getCharge();
            case MASS_DELTA:
                return constructMassDeltaString(identification.getPeptide(), Boolean.FALSE);
            case MZ_DELTA:
                return constructMassDeltaString(identification.getPeptide(), true);
            case PRECURSOR_MZ:
                return MathUtils.roundDouble(identification.getPeptide().getMzRatio());
            case NOISE_THRESHOLD:
                return MathUtils.roundDouble(identification.getAnnotationData().getNoiseThreshold());
            case SCORE:
                return MathUtils.roundDouble(identification.getAnnotationData().getIdentificationScore().getAverageFragmentIonScore());
            default:
                throw new IllegalArgumentException("Unexpected column number " + column);
        }
    }

    private String constructModificationsString(Peptide peptide) {
        String modificationsInfoString = "0";
        if (peptide instanceof ModifiedPeptide) {
            List<String> modifications = new ArrayList<>();

            ModifiedPeptide modifiedPeptide = (ModifiedPeptide) peptide;
            if (modifiedPeptide.getNTermMod() != null) {
                modifications.add(N_TERM_PREFIX + modifiedPeptide.getNTermMod().getName());
            }
            if (modifiedPeptide.getNTModifications() != null) {
                for (int i = 0; i < modifiedPeptide.getNTModifications().length; i++) {
                    ModificationFacade modificationFacade = modifiedPeptide.getNTModifications()[i];
                    if (modificationFacade != null) {
                        modifications.add(modificationFacade.getName());
                    }
                }
            }
            if (modifiedPeptide.getCTermMod() != null) {
                modifications.add(C_TERM_PREFIX + modifiedPeptide.getCTermMod().getName());
            }

            Joiner joiner = Joiner.on(MODS_DELIMITER);
            modificationsInfoString = modifications.size() + MODS_OPEN + joiner.join(modifications) + MODS_CLOSE;
        }

        return modificationsInfoString;
    }

    private String constructMassDeltaString(Peptide peptide, boolean doChargeAdjustment) {
        String massDelta = "N/A";
        try {
            double massDeltaValue = peptide.calculateMassDelta();
            if (doChargeAdjustment) {
                massDeltaValue = massDeltaValue / peptide.getCharge();
            }
            massDelta = MathUtils.roundDoubleAsBigDecimal(massDeltaValue, MathUtils.NUMBER_OF_DECIMALS).toPlainString();
            //check if the peptide is a modified peptide,
            //if so, show the corrected mass delta as well.
            if (peptide instanceof ModifiedPeptide) {
                double massDeltaValueWithMods = peptide.calculateMassDelta() - ((ModifiedPeptide) peptide).calculateModificationsMass();
                if (doChargeAdjustment) {
                    massDeltaValueWithMods = massDeltaValueWithMods / peptide.getCharge();
                }
                massDelta = MathUtils.roundDoubleAsBigDecimal(massDeltaValueWithMods, MathUtils.NUMBER_OF_DECIMALS).toPlainString() + " " + UNMOD_MASS_DELTA_OPEN + massDelta + UNMOD_MASS_DELTA_CLOSE;
            }
        } catch (AASequenceMassUnknownException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        return massDelta;
    }
}