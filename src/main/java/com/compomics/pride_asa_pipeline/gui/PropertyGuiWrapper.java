/*
 *

 */
package com.compomics.pride_asa_pipeline.gui;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;

/**
 *
 * @author Niels Hulstaert
 */
public class PropertyGuiWrapper {

    /**
     * The property key
     */
    private String key;
    /**
     * The property value
     */
    private Object value;

    public PropertyGuiWrapper(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    /**
     * Sets the value of the property. Sets the new value in the properties
     * configuration holder.
     *
     * @param value the new property value
     */
    public void setValue(Object value) {
        this.value = value;
        PropertiesConfigurationHolder.getInstance().setProperty(key, value);
    }
}
