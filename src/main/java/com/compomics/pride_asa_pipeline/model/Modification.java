package com.compomics.pride_asa_pipeline.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Date: 11-Jan-2008
 *
 * @since 0.1
 */
public class Modification implements Comparable<Modification>, ModificationFacade {

    private String name;
    private double massShift;
    private Location location;
    private Set<AminoAcid> affectedAminoAcids;
    private String modificationAccession;
    private String modificationName;

    public static enum Location {

        N_TERMINAL,
        C_TERMINAL,
        NON_TERMINAL
    }

    public Modification(double massShift, Location location, String modificationAccession, String modifiationName) {
        this.name = modifiationName;
        this.massShift = massShift;
        this.modificationAccession = modificationAccession;
        this.modificationName = modifiationName;        
        this.location = location;
        affectedAminoAcids = new HashSet<AminoAcid>();
        //affectedAminoAcids.addAll(Arrays.asList(AminoAcid.values()));
    }

    public Modification(String name, double massShift, Location location, Set<AminoAcid> affectedAminoAcids) {
        this.name = name;
        this.massShift = massShift;
        this.location = location;
        this.affectedAminoAcids = affectedAminoAcids;
    }

    public Modification(String name, double massShift, Location location, Set<AminoAcid> affectedAminoAcids, String modificationAccession, String modificationName) {
        this(name, massShift, location, affectedAminoAcids);
        this.modificationAccession = modificationAccession;
        this.modificationName = modificationName;
    }

    public String getModificationAccession() {
        return modificationAccession;
    }

    public String getModificationName() {
        return modificationName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getMassShift() {
        return massShift;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }    

    public Set<AminoAcid> getAffectedAminoAcids() {
        return affectedAminoAcids;
    }

    @Override
    public int compareTo(Modification m) {
        return this.getName().compareTo(m.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Modification that = (Modification) o;

        if (Double.compare(that.massShift, massShift) != 0) {
            return false;
        }
        if (affectedAminoAcids != null ? !affectedAminoAcids.equals(that.affectedAminoAcids) : that.affectedAminoAcids != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (location != that.location) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name != null ? name.hashCode() : 0;
        temp = massShift != +0.0d ? Double.doubleToLongBits(massShift) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (affectedAminoAcids != null ? affectedAminoAcids.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return getName();
    }
}
