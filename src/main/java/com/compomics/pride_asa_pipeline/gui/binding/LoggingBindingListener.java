/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.gui.binding;

import javax.swing.JLabel;
import org.jdesktop.beansbinding.AbstractBindingListener;
import org.jdesktop.beansbinding.Binding;

/**
 *
 * @author Niels Hulstaert Hulstaert
 */
public class LoggingBindingListener extends AbstractBindingListener {

    /**
     * Label used to display warnings.
     */
    private JLabel outputLabel;

    public LoggingBindingListener(JLabel outputLabel) {
        if (outputLabel == null) {
            throw new IllegalArgumentException();
        }
        this.outputLabel = outputLabel;
    }

    @Override
    public void syncFailed(Binding binding, Binding.SyncFailure fail) {
        String description;
        if ((fail != null) && (fail.getType() == Binding.SyncFailureType.VALIDATION_FAILED)) {
            description = fail.getValidationResult().getDescription();
            String msg = "[" + binding.getName() + "] " + description;
            outputLabel.setText(msg);
        }                         
    }

    @Override
    public void synced(Binding binding) {
        String bindName = binding.getName();
        String msg = "[" + bindName + "] Synced";
        //System.out.println(msg);
        outputLabel.setText("");
    }
}
