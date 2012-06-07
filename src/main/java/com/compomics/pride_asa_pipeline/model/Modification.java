package com.compomics.pride_asa_pipeline.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

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

    public void setModificationAccession(String modificationAccession) {
        String oldModificationAccession = this.modificationAccession;
        this.modificationAccession = modificationAccession;
        propertyChangeSupport.firePropertyChange("modificationAccession", oldModificationAccession, modificationAccession);
    }

    public String getModificationName() {
        return modificationName;
    }

    public void setModificationName(String modificationName) {
        String oldModificationName = modificationName;
        this.modificationName = modificationName;
        propertyChangeSupport.firePropertyChange("modificationName", oldModificationName, modificationName);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        propertyChangeSupport.firePropertyChange("name", oldName, name);
    }

    @Override
    public double getMassShift() {
        return massShift;
    }

    public void setMassShift(double massShift) {
        double oldMassShift = this.massShift;
        this.massShift = massShift;
        propertyChangeSupport.firePropertyChange("massShift", oldMassShift, massShift);
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        Location oldLocation = this.location;
        this.location = location;
        propertyChangeSupport.firePropertyChange("location", oldLocation, location);
    }

    public Set<AminoAcid> getAffectedAminoAcids() {
        return affectedAminoAcids;
    }

    public void setAffectedAminoAcids(Set<AminoAcid> affectedAminoAcids) {
        Set<AminoAcid> oldAffectedAminoAcids = this.affectedAminoAcids;
        this.affectedAminoAcids = affectedAminoAcids;
        propertyChangeSupport.firePropertyChange("affectedAminoAcids", oldAffectedAminoAcids, affectedAminoAcids);
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

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
}
