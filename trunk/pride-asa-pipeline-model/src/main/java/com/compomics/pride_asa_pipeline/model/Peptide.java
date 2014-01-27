package com.compomics.pride_asa_pipeline.model;

import org.apache.log4j.Logger;

/**
 * @author Florian Reisinger Date: 20-Aug-2009
 * @since 0.1
 */
public class Peptide {

    private static final Logger LOGGER = Logger.getLogger(Peptide.class);
    private int charge;
    private double mzRatio;
    private AminoAcidSequence sequence;
    //ToDo: can be optional ?    
    private long peptideId;

    public Peptide() {
        mzRatio = 0.0;
        sequence = null;
        peptideId = 0;
    }

    public Peptide(int charge, double mzRatio, AminoAcidSequence sequence) {
        this(charge, mzRatio, sequence, 0L);
    }

    public Peptide(int charge, double mzRatio, AminoAcidSequence sequence, Long peptideId) {
        this.mzRatio = mzRatio;
        this.sequence = sequence;
        this.peptideId = peptideId;

        if (charge == -1) {
            try {
                this.charge = calculateCharge();
            } catch (AASequenceMassUnknownException e) {
                LOGGER.warn("Mass of fragment ion could not be calculated! " + e.getMessage());
            }
            if (this.charge <= 0) {
                this.charge = 1;
            }
        } else {
            this.charge = charge;
        }
    }

    public int getCharge() {
        return charge;
    }

    public void setCharge(int charge) {
        this.charge = charge;
    }

    public double getMzRatio() {
        return mzRatio;
    }

    public void setMzRatio(double mzRatio) {
        this.mzRatio = mzRatio;
    }

    public AminoAcidSequence getSequence() {
        return sequence;
    }

    public void setSequence(AminoAcidSequence sequence) {
        this.sequence = sequence;
    }

    public long getPeptideId() {
        return peptideId;
    }

    public void setPeptideId(long peptideId) {
        this.peptideId = peptideId;
    }

    public String getSequenceString() {
        return sequence.toString();
    }

    /**
     * @param index an int specifying an index on the AA sequence of this
     * precursor.
     * @return the AminoAcid at the specified index of the precursor's AA
     * sequence.
     * @throws IndexOutOfBoundsException if the specified location is outside
     * the sequence. E.g. the index is out of range (index < 0 || index >=
     * length())
     * @see AminoAcidSequence#getAA(int)
     */
    public AminoAcid getAA(int index) {
        return sequence.getAA(index);
    }

    public int length() {
        return sequence.length();
    }

    /**
     * This will calculate the theoretical mass of the amino acid sequence based
     * on the mono-isotopic masses of the amino acids adding the mass of a water
     * molecule. (e.g. mass(AAs in sequence) + mass(H2O) ) (Note that the
     * mass(H2O) origins in the correction for the terminal amino acids:
     * N-terminus + mass(H) and C-terminus + mass(OH) )
     *
     * @return the theoretical mass of the precursor.
     * @throws AASequenceMassUnknownException in case not all AA masses in the
     * sequence are known.
     */
    public double calculateTheoreticalMass() throws AASequenceMassUnknownException {
        return sequence.getSequenceMass() + Constants.MASS_H2O;
    }

    /**
     * This will calculate the experimental mass of the amino acid sequence
     * based on its observed m/z ratio and adjusting for the charge state.
     * (Since the ions are positively charged, we have to substract one proton
     * mass per charge to get to the original mass of the precursor) (e.g. ( m/z
     * * charge ) - ( mass(H) * charge )
     *
     * @return the experimentally observed mass (uncharged) of the precursor.
     */
    public double calculateExperimentalMass() {
        return (getMzRatio() * getCharge()) - (getCharge() * Constants.MASS_H);
    }

    /**
     * Calculates the mass delta between the theoretical mass and the
     * experimental mass. (this delta represents the mass that is to be
     * explained by modifications.)
     *
     * @return the mass delta between theoretical and experimental (from
     * spectrum) mass.
     * @throws AASequenceMassUnknownException in case not all AA masses in the
     * sequence are known.
     */
    public double calculateMassDelta() throws AASequenceMassUnknownException {
        return calculateExperimentalMass() - calculateTheoreticalMass();
    }

    private int calculateCharge() throws AASequenceMassUnknownException {
        return (int) Math.round(calculateTheoreticalMass() / (getMzRatio() - Constants.MASS_H));
    }    

    public double[] getBIonLadderMasses(int charge) {
        //B ion series N -> C
        //b1 = subSequence(0, 1)
        //b2 = subSequence(0, 2)
        // ...
        AminoAcidSequence aaSeq = getSequence();
        int numberOfFragmentIons = length() - 1;
        double[] masses = new double[numberOfFragmentIons];
        for (int i = 0; i < numberOfFragmentIons; i++) {
            try {
                //calculate mass adjusted for the charge state (taking modifications into account)
                AminoAcidSequence fragSeq = aaSeq.subSequence(0, i + 1);
                //calculate the adjustment of the mass due to the charge state
                double adjust = (double) charge * Constants.MASS_H;
                double chargeAdjustedMass = (fragSeq.getSequenceMass() + adjust) / (double) charge;
                //adjust for termini (H and OH)
                //chargeAdjustedMass += Constants.MASS_H2O;
                masses[i] = chargeAdjustedMass;
            } catch (AASequenceMassUnknownException e) {
                LOGGER.warn("Mass of fragment ion could not be calculated! " + e.getMessage());
                masses[i] = 0d;
            }
        }
        return masses;
    }
    
    public double[] getYIonLadderMasses(int charge) {
        //Y ion series N <- C
        //y1 = subSequence(length-1, length)
        //y2 = subSequence(length-2, length)
        //...     
        AminoAcidSequence aaSeq = getSequence();
        int numberOfFragmentIons = length() - 1;
        double[] masses = new double[numberOfFragmentIons];
        for (int i = 0; i < numberOfFragmentIons; i++) {
            try {
                //calculate mass adjusted for the charge state (taking modifications into account)
                AminoAcidSequence fragSeq = aaSeq.subSequence(numberOfFragmentIons - i, numberOfFragmentIons + 1);
                //calculate the adjustment of the mass due to the charge state
                double adjust = (double) charge * Constants.MASS_H;
                //add up fragment mass (uncharged + charge adjustment + termini adjustment) and divide by charge                
                double chargeAdjustedMass = (fragSeq.getSequenceMass() + adjust + Constants.MASS_H2O) / (double) charge;
                masses[i] = chargeAdjustedMass;
            } catch (AASequenceMassUnknownException e) {
                //ToDo: do we want to handle this like that?
                //ToDo: should raise a warning or flag...
                LOGGER.warn("Mass of fragment ion could not be calculated! " + e.getMessage());
                masses[i] = 0d;
            }
        }
        return masses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Peptide peptide = (Peptide) o;

        if (charge != peptide.charge) {
            return false;
        }
        if (Double.compare(peptide.mzRatio, mzRatio) != 0) {
            return false;
        }
        if (peptideId != peptide.peptideId) {
            return false;
        }
        if (sequence != null ? !sequence.equals(peptide.sequence) : peptide.sequence != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = charge;
        temp = mzRatio != +0.0d ? Double.doubleToLongBits(mzRatio) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (sequence != null ? sequence.hashCode() : 0);
        result = 31 * result + (int) (peptideId ^ (peptideId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(sequence.toString());
        sb.append("-");
        sb.append(mzRatio);
        sb.append("-");
        sb.append(charge);
        return sb.toString();
    }
}
