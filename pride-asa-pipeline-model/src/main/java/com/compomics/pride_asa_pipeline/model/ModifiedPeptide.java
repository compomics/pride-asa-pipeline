package com.compomics.pride_asa_pipeline.model;

import java.util.Arrays;
import org.apache.log4j.Logger;

/**
 * This class represents a special form of the Peptide, namely one with (one or
 * more) AA modification(s). It hold a reference to the base (unmodified)
 * Peptide and adds the space to hold one modification per amino acid plus one
 * space each for a N/C- terminal modification. WARNING: Only a reference to the
 * base peptide is held, so any modification of the unmodified peptide sequence
 * after a ModifiedPeptide has been created may result in inconsistent data!
 * ToDo: maybe add mechanism to prevent or detect such changes to the base
 * peptide. ToDo: maybe add mechanism to Peptide to make it immutable?
 *
 * @author Florian Reisinger Date: 18-Sep-2009
 * @since 0.1
 */
public class ModifiedPeptide extends Peptide {

    private static final Logger LOGGER = Logger.getLogger(ModifiedPeptide.class);
    //the modification array can store one modification for each AA in the unmodPeptide sequence
    private final ModificationFacade[] modifications;
    //the N-terminal modification (if any)
    private ModificationFacade nTermMod;
    //the C-terminal modification (if any)
    private ModificationFacade cTermMod;

    public ModifiedPeptide(Peptide peptide) {
        super(peptide.getCharge(), peptide.getMzRatio(), peptide.getSequence(), peptide.getPeptideId());
        this.modifications = new ModificationFacade[getSequence().length()];
        this.nTermMod = null;
        this.cTermMod = null;
    }

    public ModifiedPeptide(int charge, double mzRatio, AminoAcidSequence aaSequence, Long peptideId) {
        super(charge, mzRatio, aaSequence, peptideId);
        this.modifications = new ModificationFacade[getSequence().length()];
        this.nTermMod = null;
        this.cTermMod = null;
    }

    public Peptide getUnmodifiedPeptide() {
        Peptide p = new Peptide(getCharge(), getMzRatio(), getSequence(), getPeptideId());
        return p;
    }

    public ModificationFacade[] getNTModifications() {
        return modifications;
    }

    public ModificationFacade getNTermMod() {
        return nTermMod;
    }

    public void setNTermMod(ModificationFacade modification) {
        if (modification == null) {
            throw new IllegalArgumentException("Can not set null as modification!");
        }
        this.nTermMod = modification;
    }

    public ModificationFacade getCTermMod() {
        return cTermMod;
    }

    public void setCTermMod(ModificationFacade modification) {
        if (modification == null) {
            throw new IllegalArgumentException("Can not set null as modification!");
        }
        this.cTermMod = modification;
    }

    public ModificationFacade getNTModification(int index) {
        return modifications[index];
    }

    /**
     * This method is used to set a modification for a particular location on
     * the sequence of this peptide. Note: the index location starts on index 0,
     * where 0 is the first amino acid of the peptide and end at 'sequence
     * length -1'. Index '-1' is considered a N-terminal modification and index
     * of 'sequence length' a C-terminal modification.
     *
     * @param index the location on the sequence that carries the modification.
     * @param modification the modification to set.
     * @throws IllegalArgumentException if modification == null.
     * @throws IndexOutOfBoundsException if the specified location is outside
     * the sequence (plus terminal modification cases). E.g. the index is out of
     * range: (index < -1 || index > length())
     */
    public void setNTModification(int index, ModificationFacade modification) {
        if (modification == null) {
            throw new IllegalArgumentException("A modification can not be null!");
        }
        if (index == -1) {
            //consider index -1 as N-terminal modification
            this.nTermMod = modification;
        } else if (index == this.modifications.length) {
            //consider index 'sequence length' as C-terminal modification
            this.cTermMod = modification;
            //regular modification, e.g. within the sequence and not terminal
        } else {
            modifications[index] = modification;
        }
    }

    public int getNumberNTModifications() {
        int cnt = 0;
        //count all existing (not null) modifications
        for (ModificationFacade m : modifications) {
            if (m != null) {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * Calculates the total modifications mass
     *
     * @return the total modifications mass
     */
    public double calculateModificationsMass() {
        double modificationsMass = 0.0;
        if (nTermMod != null) {
            modificationsMass += nTermMod.getMassShift();
        }
        for (ModificationFacade modificationFacade : modifications) {
            if (modificationFacade != null) {
                modificationsMass += modificationFacade.getMassShift();
            }
        }
        if (cTermMod != null) {
            modificationsMass += cTermMod.getMassShift();
        }

        return modificationsMass;
    }

    /**
     * Note: this is the mass of the peptide WITHOUT C/N termini adjustment! (to
     * complete a peptide, a H has to added to the N-terminus and a OH to the
     * C-terminus)
     *
     * @param startIndex the start index
     * @param stopIndex the stop index
     * @return
     */
    private double cumulativeModMass(int startIndex, int stopIndex) {
        double modMass = 0;
        for (int i = startIndex; i < stopIndex; i++) {
            if (modifications[i] != null && modifications[i].getType().equals(Modification.Type.MS2)) {
                modMass += modifications[i].getMassShift();
            }
        }
        if (startIndex == 0 && this.getNTermMod() != null && this.getNTermMod().getType().equals(Modification.Type.MS2)) {
            //the current fragment includes the N-terminus and
            //has a N-terminal modification, so we add its mass
            modMass += getNTermMod().getMassShift();
        }
        if (stopIndex == length() && this.getCTermMod() != null && this.getCTermMod().getType().equals(Modification.Type.MS2)) {
            //the current fragment includes the C-terminus and
            //has a C-terminal modification, so we add its mass
            modMass += getCTermMod().getMassShift();
        }

        //both terminal modifications should not appear in a fragment ion, since
        //it would then comprise the whole peptide
        //ToDo: add a check for this (e.g. if both terminal mods, then we are dealing with the whole peptide)

        return modMass;
    }

    @Override
    public double[] getBIonLadderMasses(int charge) {
        //B ion series N -> C
        //b1 = subSequence(0, 1)
        //b2 = subSequence(0, 2)
        //...
        AminoAcidSequence aaSeq = getSequence();
        int numberOfFragmentIons = length() - 1;
        double[] masses = new double[numberOfFragmentIons];
        for (int i = 0; i < numberOfFragmentIons; i++) {
            try {
                //calculate mass adjusted for the charge state (taking modifications into account)
                AminoAcidSequence fragSeq = aaSeq.subSequence(0, i + 1);
                //calculate the cumulative mass of all the modifications within this fragment
                double cumulativeModMass = cumulativeModMass(0, i + 1);
                double modifiedFragmentMass = fragSeq.getSequenceMass() + cumulativeModMass;
                //calculate the adjustment of the mass due to the charge state
                double adjust = (double) charge * Constants.MASS_H;
                double chargeAdjustedMass = (modifiedFragmentMass + adjust) / (double) charge;
                //adjust for termini (H and OH)
                //chargeAdjustedMass += Constants.MASS_H2O;
                masses[i] = chargeAdjustedMass;
            } catch (AASequenceMassUnknownException e) {
                LOGGER.warn("Mass of fragment ion could not be calculated!" + e.getMessage());
                masses[i] = 0d;
            }
        }

        return masses;
    }

    @Override
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
                //calculate the cumulative mass of all the modifications within this fragment
                double cumulativeModMass = cumulativeModMass(numberOfFragmentIons - i, numberOfFragmentIons + 1);
                double modifiedFragmentMass = fragSeq.getSequenceMass() + cumulativeModMass;
                //calculate the adjustment of the mass due to the charge state
                double adjust = (double) charge * Constants.MASS_H;
                //add up fragment mass (uncharged + charge adjustment + termini adjustment) and divide by charge
                double chargeAdjustedMass = (modifiedFragmentMass + adjust + Constants.MASS_H2O) / (double) charge;
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
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final ModifiedPeptide other = (ModifiedPeptide) obj;
        if (!Arrays.deepEquals(this.modifications, other.modifications)) {
            return false;
        }
        if (this.nTermMod != other.nTermMod && (this.nTermMod == null || !this.nTermMod.equals(other.nTermMod))) {
            return false;
        }
        if (this.cTermMod != other.cTermMod && (this.cTermMod == null || !this.cTermMod.equals(other.cTermMod))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 97 * hash + Arrays.deepHashCode(this.modifications);
        hash = 97 * hash + (this.nTermMod != null ? this.nTermMod.hashCode() : 0);
        hash = 97 * hash + (this.cTermMod != null ? this.cTermMod.hashCode() : 0);
        return hash;
    }
}
