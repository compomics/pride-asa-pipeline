package com.compomics.pride_asa_pipeline.core.model.modification;

import java.util.List;
import java.util.Objects;
import uk.ac.ebi.pridemod.model.PTM;
import uk.ac.ebi.pridemod.model.Specificity;

/**
 * Date: 11-Jan-2008
 *
 * @since 0.1
 */
public class PRIDEModification implements PTM, Comparable<PRIDEModification> {

    private int frequency;
    private String accession;
    private String name;
    private Double aveDeltaMass;
    private String description;
    private Double monoMassDelta;
    private String formula;
    private List<Specificity> specificityCollection;

    public PRIDEModification(uk.ac.ebi.pridemod.model.PTM ptm, int frequency) {
        this.accession = ptm.getAccession();
        this.name = ptm.getName();
        this.aveDeltaMass = ptm.getAveDeltaMass();
        this.description = ptm.getDescription();
        this.monoMassDelta = ptm.getMonoDeltaMass();
        this.formula = ptm.getFormula();
        this.frequency = frequency;
        this.specificityCollection = ptm.getSpecificityCollection();
    }

    public PRIDEModification() {
    }
   
    
    @Override
    public int compareTo(PRIDEModification o) {
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
        hash = 47 * hash + Objects.hashCode(this.formula);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PRIDEModification) {
            PRIDEModification other = (PRIDEModification) obj;
            if (!getAccession().equalsIgnoreCase(other.getAccession())) {
                return false;
            }
            if (!Objects.equals(getAveDeltaMass(), other.getAveDeltaMass())) {
                return false;
            }
            if (!Objects.equals(getMonoDeltaMass(), other.getMonoDeltaMass())) {
                return false;
            }
            if (!getFormula().equalsIgnoreCase(other.getFormula())) {
                return false;
            }
            if (getName().equalsIgnoreCase(other.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getAccession() {
        return accession;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Double getMonoDeltaMass() {
        return monoMassDelta;
    }

    @Override
    public Double getAveDeltaMass() {
        return aveDeltaMass;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<Specificity> getSpecificityCollection() {
        return specificityCollection;
    }

    @Override
    public String getFormula() {
        return formula;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAverageMassDelta(Double averageMassDelta) {
        this.aveDeltaMass = averageMassDelta;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMonoMassDelta(Double monoMassDelta) {
        this.monoMassDelta = monoMassDelta;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public void setSpecificityCollection(List<Specificity> specificityCollection) {
        this.specificityCollection = specificityCollection;
    }
    
    
    
}
