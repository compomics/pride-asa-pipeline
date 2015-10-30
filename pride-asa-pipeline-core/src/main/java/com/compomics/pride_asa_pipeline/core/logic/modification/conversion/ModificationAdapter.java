/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.modification.conversion;

/**
 *
 * @author Kenneth
 */
public interface ModificationAdapter<T> {

    public T convertModification(UniModModification mod);

}
