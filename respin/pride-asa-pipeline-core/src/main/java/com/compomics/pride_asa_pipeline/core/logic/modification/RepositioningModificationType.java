/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.modification;


/**
 *
 * @author Kenneth Verheggen
 */
public enum RepositioningModificationType {

    OXIDATION("oxidation", "oxidation of "),
    ACETYLATION("acetylation", "acetylation of "),
    PYRO("pyro-glu", "pyro-glu from n-term ");
    private final String prefix;
    private final String mark;

    private RepositioningModificationType(String mark, String prefix) {
        this.prefix = prefix;
        this.mark = mark;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getMark() {
        return mark;
    }

}
