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

import com.compomics.pride_asa_pipeline.model.util.MathUtils;


/**
 * @author Florian Reisinger
 *         Date: 29-Jun-2010
 * @since $version
 */
public class FragmentIonAnnotation {


    public enum IonType {
        // !! Note: when adding new types the following methods have to be updated as welL!!
        //          setIonType(IonType ionType)
        //          resolveIonType(String type)
        Y_ION,
        Y_ION_H2O_LOSS,
        Y_ION_NH3_LOSS,
        B_ION,
        B_ION_H2O_LOSS,
        B_ION_NH3_LOSS
    }

    // create a fragment ion annotation object that has the relevant information for storing the annotation in the DB

    // The PRIDE database contain a table pride_fragment_ion which has the following columns:
    // Java representation                                          // DB columns
    private long fragment_ion_id = -1;                 // fragment_ion_id              int(20) unsigned
    private boolean automatic_annotation = true;               // automatic_annoatation        tinyint(1)
    private boolean public_flag = false;              // public_flag                  tinyint(1)
    private long peptide_id = -1;                 // peptide_id                   int(20) unsigned
    private double mz = Double.MIN_VALUE;   // mz                           decimal(20,10)
    private String cv_ion_mz = null;               // cv_ion_mz                    varchar(255)
    private String accession_ion_mz = null;               // accession_ion_mz             varchar(255)
    private String name_ion_mz = null;               // name_ion_mz                  text
    private String cv_ion_type = null;               // cv_ion_type                  varchar(255)
    private String accession_ion_type = null;               // accession_ion_type           varchar(255)
    private String ion_type_name = null;               // ion_type_name                text
    private int fragment_ion_number = -1;                 // fragment_ion_number          int(10)
    private String cv_intensity = null;               // cv_intensity                 varchar(255)
    private String accession_intensity = null;               // accession_intensity          varchar(255)
    private double intensity = Double.MIN_VALUE;   // intensity                    decimal(20,10)
    private String name_intensity = null;               // name_intensity               text
    private String cv_mass_error = null;               // cv_mass_error                varchar(255)
    private String accession_mass_error = null;               // accession_mass_error         varchar(255)
    private double mass_error = Double.MIN_VALUE;   // mass_error                   decimal(20,10)
    private String name_mass_error = null;               // name_mass_eror               text
    private String cv_ret_time_error = null;               // cv_ret_time_error            varchar(255)
    private String accession_ret_time_error = null;               // accession_ret_time_error     varchar(255)
    private double retention_time_error = Double.MIN_VALUE;   // retention_time_error         decimal(20,10)
    private String name_ret_time_error = null;               // name_ret_time_error          text
    private String cv_ion_charge = null;               // cv_ion_charge                varchar(255)
    private String accession_ion_charge = null;               // accession_ion_charge         varchar(255)
    private int ion_charge = Integer.MIN_VALUE;  // ion_charge                   decimal(20,10)
    private String name_ion_charge = null;               // name_ion_charge              text

    public FragmentIonAnnotation() {
        // default constructor to build the object manually
    }

    public FragmentIonAnnotation(long peptide_id, IonType ionType, int ionNumber,
                                 double mz, double intensity, int ion_charge) {
        this.automatic_annotation = true;
        this.public_flag = false; // ToDo: check this, as we will also run the pipeline on already public datasets
        this.peptide_id = peptide_id;
        setIonType(ionType);
        this.fragment_ion_number = ionNumber;
        setIonCharge(ion_charge);
        setMZ(mz);
        setIntensity(intensity);
    }

    public FragmentIonAnnotation(long peptide_id, IonType ionType, int ionNumber,
                                 double mz, double intensity, double massError, int ion_charge) {
        this(peptide_id, ionType, ionNumber, mz, intensity, ion_charge);
        setMassError(massError);
    }


    /**
     * Sets the given mass error for the fragment ion plus the
     * PRIDE ontology term values (CV, accession and name).
     *
     * @param massError the mass error of the fragment ion.
     */
    private void setMassError(double massError) {
        this.mass_error = massError;
        this.cv_mass_error = "PRIDE";
        this.accession_mass_error = "PRIDE:0000190";
        this.name_mass_error = "product ion mass error";
    }

    /**
     * Sets the given intensity of the product ion plus the
     * PRIDE ontology term values (CV, accession and name).
     *
     * @param intensity the intensity of the product ion.
     */
    private void setIntensity(double intensity) {
        // set the intensity value, cv, accession, name
        this.intensity = intensity;
        this.cv_intensity = "PRIDE";
        this.accession_intensity = "PRIDE:0000189";
        this.name_intensity = "product ion intensity";
    }

    /**
     * Sets the given m/z value of the product ion plus the
     * PRIDE ontology term values (CV, accession and name).
     *
     * @param mz the m/z value of the product ion.
     */
    private void setMZ(double mz) {
        // set the m/z value, cv, accession, and name
        this.mz = mz;
        this.cv_ion_mz = "PRIDE";
        this.accession_ion_mz = "PRIDE:0000188";
        this.name_ion_mz = "product ion m/z";
    }

    private void setIonCharge(int ionCharge) {
        // set the ion charge, cv, accession, and name
        this.ion_charge = ionCharge;
        this.cv_ion_charge = "PRIDE";
        this.accession_ion_charge = "PRIDE:0000204";
        this.name_ion_charge = "product ion charge";
    }

    /**
     * Set the ion type using PRIDE CV terms according to the
     * provided IonType.
     *
     * @param ionType the type of fragment ion.
     */
    private void setIonType(IonType ionType) {
        // set cv, name, accession for the provided ion type
        switch (ionType) {

            case Y_ION:
                this.cv_ion_type = "PRIDE";
                this.accession_ion_type = "PRIDE:0000193";
                this.ion_type_name = "y ion";
                break;
            case Y_ION_H2O_LOSS:
                this.cv_ion_type = "PRIDE";
                this.accession_ion_type = "PRIDE:0000197";
                this.ion_type_name = "y ion -H2O";
                break;
            case Y_ION_NH3_LOSS:
                this.cv_ion_type = "PRIDE";
                this.accession_ion_type = "PRIDE:0000198";
                this.ion_type_name = "y ion -NH3";
                break;
            case B_ION:
                this.cv_ion_type = "PRIDE";
                this.accession_ion_type = "PRIDE:0000194";
                this.ion_type_name = "b ion";
                break;
            case B_ION_H2O_LOSS:
                this.cv_ion_type = "PRIDE";
                this.accession_ion_type = "PRIDE:0000196";
                this.ion_type_name = "b ion -H2O";
                break;
            case B_ION_NH3_LOSS:
                this.cv_ion_type = "PRIDE";
                this.accession_ion_type = "PRIDE:0000195";
                this.ion_type_name = "b ion -NH3";
                break;
        }

    }


    public static IonType resolveIonType(String type) {
        if (type.equalsIgnoreCase("b ion") || type.equalsIgnoreCase("b_ion") || type.equalsIgnoreCase("b-ion")) {
            return IonType.B_ION;
        }
        if (type.equalsIgnoreCase("b ion -H2O") || type.equalsIgnoreCase("b-ion-H2O") || type.equalsIgnoreCase("b ion-H2O")) {
            return IonType.B_ION_H2O_LOSS;
        }
        if (type.equalsIgnoreCase("b ion -NH3") || type.equalsIgnoreCase("b-ion-NH3") || type.equalsIgnoreCase("b ion-NH3")) {
            return IonType.B_ION_NH3_LOSS;
        }
        if (type.equalsIgnoreCase("y ion") || type.equalsIgnoreCase("y_ion") || type.equalsIgnoreCase("y-ion")) {
            return IonType.Y_ION;
        }
        if (type.equalsIgnoreCase("y ion -H2O") || type.equalsIgnoreCase("y-ion-H2O") || type.equalsIgnoreCase("y ion-H2O")) {
            return IonType.Y_ION_H2O_LOSS;
        }
        if (type.equalsIgnoreCase("y ion -NH3") || type.equalsIgnoreCase("y-ion-NH3") || type.equalsIgnoreCase("y ion-NH3")) {
            return IonType.Y_ION_H2O_LOSS;
        }
        return null;
    }

    /**
     * Method to check if this annotation has been recognised as b ion.
     * To be recognised as b ion, the annotation has to carry the corresponding
     * PRIDE ontoloy accession or the ion type name has to be resolvable to
     * the internal enum type IonType.B_ION.
     *
     * @return true if the annotation was recognised to be for a b ion.
     */
    public boolean isBIon() {
        return this.accession_ion_type.equalsIgnoreCase("PRIDE:0000194")
                || resolveIonType(this.ion_type_name) == IonType.B_ION;
    }

    /**
     * Method to check if this annotation has been recognised as y ion.
     * To be recognised as y ion, the annotation has to carry the corresponding
     * PRIDE ontoloy accession or the ion type name has to be resolvable to
     * the internal enum type IonType.Y_ION.
     *
     * @return true if the annotation was recognised to be for a y ion.
     */
    public boolean isYIon() {
        return this.accession_ion_type.equalsIgnoreCase("PRIDE:0000193")
                || resolveIonType(this.ion_type_name) == IonType.Y_ION;
    }


    /**
     * For this purpose two fragment ion annotations are considered equivalent
     * if they annotate the same peptide, the same m/z value
     * and are of the same ion type + number.
     *
     * @param fragment the FragmentIonAnnotation to check against.
     * @return true if the provided annotation is deemed equivalent.
     */
    public boolean isEquivalent(FragmentIonAnnotation fragment) {
        boolean retVal = false;
        if (this.peptide_id == fragment.peptide_id &&
                this.accession_ion_type.equalsIgnoreCase(fragment.accession_ion_type) &&
                this.fragment_ion_number == fragment.fragment_ion_number &&
                MathUtils.equalValues(this.mz, fragment.mz, 0.0001)) {
            retVal = true;
        }

        return retVal;
    }

    public String toString() {
        return ion_type_name + " " + fragment_ion_number + " for peptide "
                + peptide_id + " at m/z " + mz + " with intensity " + intensity + " with charge " + ion_charge;
    }


    ///// ///// ///// ///// ///// ///// ///// ///// ///// /////
    // public Getter & Setter

    public double getIntensity() {
        return this.intensity;
    }

    public long getFragment_ion_id() {
        return fragment_ion_id;
    }

    public void setFragment_ion_id(long fragment_ion_id) {
        this.fragment_ion_id = fragment_ion_id;
    }

    public boolean isAutomatic_annotation() {
        return automatic_annotation;
    }

    public void setAutomatic_annotation(boolean automatic_annotation) {
        this.automatic_annotation = automatic_annotation;
    }

    public boolean isPublic_flag() {
        return public_flag;
    }

    public void setPublic_flag(boolean public_flag) {
        this.public_flag = public_flag;
    }

    public long getPeptide_id() {
        return peptide_id;
    }

    public void setPeptide_id(long peptide_id) {
        this.peptide_id = peptide_id;
    }

    public double getMz() {
        return mz;
    }

    public void setMz(double mz) {
        this.mz = mz;
    }

    public String getCv_ion_mz() {
        return cv_ion_mz;
    }

    public void setCv_ion_mz(String cv_ion_mz) {
        this.cv_ion_mz = cv_ion_mz;
    }

    public String getAccession_ion_mz() {
        return accession_ion_mz;
    }

    public void setAccession_ion_mz(String accession_ion_mz) {
        this.accession_ion_mz = accession_ion_mz;
    }

    public String getName_ion_mz() {
        return name_ion_mz;
    }

    public void setName_ion_mz(String name_ion_mz) {
        this.name_ion_mz = name_ion_mz;
    }

    public String getCv_ion_type() {
        return cv_ion_type;
    }

    public void setCv_ion_type(String cv_ion_type) {
        this.cv_ion_type = cv_ion_type;
    }

    public String getAccession_ion_type() {
        return accession_ion_type;
    }

    public void setAccession_ion_type(String accession_ion_type) {
        this.accession_ion_type = accession_ion_type;
    }

    public String getIon_type_name() {
        return ion_type_name;
    }   

    public void setIon_type_name(String ion_type_name) {
        this.ion_type_name = ion_type_name;
    }

    public int getFragment_ion_number() {
        return fragment_ion_number;
    }

    public void setFragment_ion_number(int fragment_ion_number) {
        this.fragment_ion_number = fragment_ion_number;
    }

    public String getCv_intensity() {
        return cv_intensity;
    }

    public void setCv_intensity(String cv_intensity) {
        this.cv_intensity = cv_intensity;
    }

    public String getAccession_intensity() {
        return accession_intensity;
    }

    public void setAccession_intensity(String accession_intensity) {
        this.accession_intensity = accession_intensity;
    }

    public String getName_intensity() {
        return name_intensity;
    }

    public void setName_intensity(String name_intensity) {
        this.name_intensity = name_intensity;
    }

    public String getCv_mass_error() {
        return cv_mass_error;
    }

    public void setCv_mass_error(String cv_mass_error) {
        this.cv_mass_error = cv_mass_error;
    }

    public String getAccession_mass_error() {
        return accession_mass_error;
    }

    public void setAccession_mass_error(String accession_mass_error) {
        this.accession_mass_error = accession_mass_error;
    }

    public double getMass_error() {
        return mass_error;
    }

    public void setMass_error(double mass_error) {
        this.mass_error = mass_error;
    }

    public String getName_mass_error() {
        return name_mass_error;
    }

    public void setName_mass_error(String name_mass_error) {
        this.name_mass_error = name_mass_error;
    }

    public String getCv_ret_time_error() {
        return cv_ret_time_error;
    }

    public void setCv_ret_time_error(String cv_ret_time_error) {
        this.cv_ret_time_error = cv_ret_time_error;
    }

    public String getAccession_ret_time_error() {
        return accession_ret_time_error;
    }

    public void setAccession_ret_time_error(String accession_ret_time_error) {
        this.accession_ret_time_error = accession_ret_time_error;
    }

    public double getRetention_time_error() {
        return retention_time_error;
    }

    public void setRetention_time_error(double retention_time_error) {
        this.retention_time_error = retention_time_error;
    }

    public String getName_ret_time_error() {
        return name_ret_time_error;
    }

    public void setName_ret_time_error(String name_ret_time_error) {
        this.name_ret_time_error = name_ret_time_error;
    }

    public int getIon_charge() {
        return ion_charge;
    }

    public void setIon_charge(int ion_charge) {
        this.ion_charge = ion_charge;
    }

    public String getCv_ion_charge() {
        return cv_ion_charge;
    }

    public void setCv_ion_charge(String aCv_ion_charge) {
        cv_ion_charge = aCv_ion_charge;
    }

    public String getAccession_ion_charge() {
        return accession_ion_charge;
    }

    public void setAccession_ion_charge(String aAccession_ion_charge) {
        accession_ion_charge = aAccession_ion_charge;
    }

    public String getName_ion_charge() {
        return name_ion_charge;
    }

    public void setName_ion_charge(String aName_ion_charge) {
        name_ion_charge = aName_ion_charge;
    }
}
