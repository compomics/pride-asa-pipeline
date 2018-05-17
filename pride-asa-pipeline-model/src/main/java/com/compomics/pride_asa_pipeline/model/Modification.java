/* 
 * Copyright 2018 compomics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.compomics.pride_asa_pipeline.model;

import com.compomics.pride_asa_pipeline.model.util.ResourceUtils;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.SwingPropertyChangeSupport;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.core.io.Resource;

/**
 * Date: 11-Jan-2008
 *
 * @since 0.1
 */
public class Modification implements Comparable<Modification>, ModificationFacade {

    public static enum Location {

        N_TERMINAL("N-terminus"),
        C_TERMINAL("C-terminus"),
        NON_TERMINAL("any");

        private final String userFriendlyValue;

        private Location(String userFriendlyValue) {
            this.userFriendlyValue = userFriendlyValue;
        }

        public String getUserFriendlyValue() {
            return userFriendlyValue;
        }
    }

    public static enum Origin {

        PIPELINE,
        PRIDE
    }

    public static enum Type {

        MS1,
        MS2
    }

    static {
        try {
            Resource propertiesResource = ResourceUtils.getResourceByRelativePath("resources/pride-asa-pipeline-model.properties");
            PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration(propertiesResource.getURL());

            useMonoIsotopicMass = propertiesConfiguration.getBoolean("modification.use_monoisotopic_mass");
        } catch (IOException | ConfigurationException e) {
            //do nothing
        }
    }

    private static boolean useMonoIsotopicMass;
    private String name;
    private double monoIsotopicMassShift;
    private double averageMassShift;
    private Location location;
    private Set<AminoAcid> affectedAminoAcids;
    private String accession;
    private String accessionValue;
    private Origin origin = Origin.PIPELINE;
    private Type type = Type.MS2;
    private final SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);

    public Modification(double massShift, Location location, String accession, String accessionValue) {
        this.name = accessionValue;
        this.location = location;
        this.monoIsotopicMassShift = massShift;
        this.averageMassShift = massShift;
        this.accession = accession;
        this.accessionValue = accessionValue;
        affectedAminoAcids = new HashSet<>();
    }

    public Modification(String name, double monoIsotopicMassShift, double averageMassShift, Location location, Set<AminoAcid> affectedAminoAcids, String accession, String accessionValue) {
        this.name = name;
        this.monoIsotopicMassShift = monoIsotopicMassShift;
        this.averageMassShift = averageMassShift;
        this.location = location;
        this.affectedAminoAcids = affectedAminoAcids;
        this.accession = accession;
        this.accessionValue = accessionValue;
    }

    public double getAverageMassShift() {
        return averageMassShift;
    }

    public void setAverageMassShift(double averageMassShift) {
        double oldAverageMassShift = this.averageMassShift;
        this.averageMassShift = averageMassShift;
        propertyChangeSupport.firePropertyChange("averageMassShift", oldAverageMassShift, averageMassShift);
    }

    public double getMonoIsotopicMassShift() {
        return monoIsotopicMassShift;
    }

    public void setMonoIsotopicMassShift(double monoIsotopicMassShift) {
        double oldMonoIsotopicMassShift = this.monoIsotopicMassShift;
        this.monoIsotopicMassShift = monoIsotopicMassShift;
        propertyChangeSupport.firePropertyChange("monoIsotopicMassShift", oldMonoIsotopicMassShift, monoIsotopicMassShift);
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        String oldAccession = this.accession;
        this.accession = accession;
        propertyChangeSupport.firePropertyChange("accession", oldAccession, accession);
    }

    public String getAccessionValue() {
        return accessionValue;
    }

    public void setAccessionValue(String accessionValue) {
        String oldAccessionValue = this.accessionValue;
        this.accessionValue = accessionValue;
        propertyChangeSupport.firePropertyChange("accessionValue", oldAccessionValue, accessionValue);
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
        if (useMonoIsotopicMass) {
            return monoIsotopicMassShift;
        } else {
            return averageMassShift;
        }
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

    public Origin getOrigin() {
        return origin;
    }

    public void setOrigin(Origin origin) {
        Origin oldOrigin = this.origin;
        this.origin = origin;
        propertyChangeSupport.firePropertyChange("type", oldOrigin, origin);
    }

    @Override
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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

        if (Double.compare(that.monoIsotopicMassShift, monoIsotopicMassShift) != 0) {
            return false;
        }
        if (Double.compare(that.averageMassShift, averageMassShift) != 0) {
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
        int hash = 7;
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.monoIsotopicMassShift) ^ (Double.doubleToLongBits(this.monoIsotopicMassShift) >>> 32));
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.averageMassShift) ^ (Double.doubleToLongBits(this.averageMassShift) >>> 32));
        hash = 79 * hash + (this.location != null ? this.location.hashCode() : 0);
        hash = 79 * hash + (this.affectedAminoAcids != null ? this.affectedAminoAcids.hashCode() : 0);
        return hash;
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
