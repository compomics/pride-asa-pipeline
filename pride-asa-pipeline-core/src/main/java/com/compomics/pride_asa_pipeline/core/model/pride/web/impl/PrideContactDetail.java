/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.model.pride.web.impl;

import com.compomics.pride_asa_pipeline.core.model.pride.web.json.ContactField;

/**
 *
 * @author Kenneth
 */
public class PrideContactDetail {

    private String title,
            firstName,
            lastName,
            affiliation,
            email;

    public void setField(ContactField field, String parameter) {
        if (parameter == null) {
            return;
        }
        switch (field) {
            case title:
                setTitle(parameter);
                break;
            case firstName:
                setFirstName(parameter);
                break;
            case lastName:
                setLastName(parameter);
                break;
            case affiliation:
                setAffiliation(parameter);
                break;
            case email:
                setEmail(parameter);
                break;
        }
    }

    public String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public String getFirstName() {
        return firstName;
    }

    private void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    private void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAffiliation() {
        return affiliation;
    }

    private void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getEmail() {
        return email;
    }

    private void setEmail(String email) {
        this.email = email;
    }

}
