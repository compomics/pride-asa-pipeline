package com.compomics.pride_asa_pipeline.core.model;

import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.*;

/**
 * This class represents a combination of modifications.
 *
 * @author jonarame Date: 23-Feb-2008
 * @since 0.1
 */
public class ModificationCombination {

    private List<Modification> modifications;
    private double mass;

    public ModificationCombination() {
        modifications = new ArrayList<>();
    }

    public ModificationCombination(List<Modification> modifications) {
        this();
        for (Modification modification : modifications) {
            addModification(modification);
        }
    }

    public double getMass() {
        return mass;
    }

    public int getSize() {
        return modifications.size();
    }

    public List<Modification> getModifications() {
        return modifications;
    }

    public Set<Modification> getUniqueModifications() {
        return new HashSet<>(modifications);
    }

    public double[] getModificationMasses() {
        double[] masses = new double[modifications.size()];
        int i = 0;
        for (Modification m : modifications) {
            masses[i] = m.getMassShift();
            ++i;
        }
        return masses;
    }

    public final void addModification(Modification modification) {
        //only allow modifications that actually change the mass difference
        if (modification.getMassShift() == 0.0) {
            throw new IllegalArgumentException("Can not add modification "
                    + "with zero mass shift to ModificationCombination!" + modification.getMassShift());
        }
        modifications.add(modification);
        mass += modification.getMassShift();
    }

    public Set<Modification> getModificationByMass(double mass) {
        Set<Modification> retVal = new HashSet<>();
        for (Modification modification : modifications) {
            if (modification.getMassShift() == mass) {
                retVal.add(modification);
            }
        }
        return retVal;
    }

    @Override
    public String toString() {
        Collections.sort(modifications);
        StringBuilder sb = new StringBuilder();
        for (Modification m : modifications) {
            sb.append(m.getName());
            sb.append("~");
        }
        return sb.toString();
    }

    /**
     * Equals method for the specific purpose of comparing
     * ModificationCombination objects. NOTE: Although the modifications are
     * stored internally as a list (and their order is therefore preserved),
     * this method does not take the order of modifications into account when
     * comparing the modification lists (as they are sorted prior to the
     * equality test).
     *
     * @param o the object to compare to.
     * @return true if the thow objects are deemed equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ModificationCombination that = (ModificationCombination) o;

        if (Double.compare(that.mass, mass) != 0) {
            return false;
        }

        // we don't want to take the order of the modifications into account,
        // so we perform the comparison on sorted lists
        List<Modification> thisMods = new ArrayList<>();
        thisMods.addAll(this.modifications);
        Collections.sort(thisMods);
        List<Modification> thatMods = new ArrayList<>();
        thatMods.addAll(that.modifications);
        Collections.sort(thatMods);
        if (thisMods != null ? !thisMods.equals(thatMods) : thatMods != null) {
            return false;
        }

        // comment to avoid warning (simplify if statement)
        return true;
    }

//    @Override
//    public int hashCode() {
//        int result;
//        long temp;
//        result = modifications != null ? modifications.hashCode() : 0;
//        temp = mass != +0.0d ? Double.doubleToLongBits(mass) : 0L;
//        result = 31 * result + (int) (temp ^ (temp >>> 32));
//        return result;
//    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.mass) ^ (Double.doubleToLongBits(this.mass) >>> 32));
        return hash;
    }       

    public ModificationCombination duplicate() {
        ModificationCombination clone = new ModificationCombination();
        for (Modification modification : modifications) {
            clone.addModification(modification);
        }
        return clone;
    }
}
