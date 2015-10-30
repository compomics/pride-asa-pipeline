package com.compomics.pride_asa_pipeline.core.logic.modification.conversion;

import java.util.Objects;
import uk.ac.ebi.pridemod.model.PTM;

/**
 * Date: 11-Jan-2008
 *
 * @since 0.1
 */
public class UniModModification implements Comparable<UniModModification> {

    private final int frequency;
    private final PTM ptm;

    public UniModModification(uk.ac.ebi.pridemod.model.PTM ptm, int frequency) {
        this.ptm = ptm;
        this.frequency = frequency;
    }

    public uk.ac.ebi.pridemod.model.PTM getPtm() {
        return ptm;
    }

    public int getFrequency() {
        return frequency;
    }

    @Override
    public int compareTo(UniModModification o) {
        if (o.getFrequency() > this.getFrequency()) {
            return 1;
        } else if (o.getFrequency() < this.getFrequency()) {
            return -1;
        } else {
            return -1;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + this.frequency;
        hash = 47 * hash + Objects.hashCode(this.ptm);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UniModModification) {
            UniModModification other = (UniModModification) obj;
            PTM myPTM = getPtm();
            PTM otherPTM = other.getPtm();
            if (!myPTM.getAccession().equalsIgnoreCase(otherPTM.getAccession())) {
                return false;
            }
            if (!Objects.equals(myPTM.getAveDeltaMass(), otherPTM.getAveDeltaMass())) {
                return false;
            }
            if (!Objects.equals(myPTM.getMonoDeltaMass(), otherPTM.getMonoDeltaMass())) {
                return false;
            }
            if (!myPTM.getFormula().equalsIgnoreCase(otherPTM.getFormula())) {
                return false;
            }
            if (myPTM.getName().equalsIgnoreCase(otherPTM.getName())) {
                return true;
            }
        }
        return false;
    }
}
