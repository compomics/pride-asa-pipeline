/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.model.webservice.objects;

import com.compomics.pride_asa_pipeline.core.model.webservice.fields.ModifiedLocationField;

/**
 *
 * @author Kenneth
 */
public class PrideModifiedLocation {

    private int location;
    private String modification;

    public void setField(ModifiedLocationField field, String parameter) {
        if (parameter == null) {
            return;
        }
        switch (field) {
            case location:
                setLocation(Integer.parseInt(parameter));
                break;
            case modification:
                setModification(parameter);
                break;
        }
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public String getModification() {
        return modification;
    }

    public void setModification(String modification) {
        this.modification = modification;
    }

}
