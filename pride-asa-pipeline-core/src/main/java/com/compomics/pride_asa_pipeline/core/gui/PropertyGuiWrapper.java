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
package com.compomics.pride_asa_pipeline.core.gui;

import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import java.beans.PropertyChangeListener;
import javax.swing.event.SwingPropertyChangeSupport;

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
    private final SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);

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
        Object oldValue = this.value;
        this.value = value;
        propertyChangeSupport.firePropertyChange("value", oldValue, value);
        PropertiesConfigurationHolder.getInstance().setProperty(key, value);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
}
