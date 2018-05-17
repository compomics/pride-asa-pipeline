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
package com.compomics.pride_asa_pipeline.core.gui.controller;

import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.gui.PropertyGuiWrapper;
import com.compomics.pride_asa_pipeline.core.gui.view.PipelineConfigDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JOptionPane;
import org.apache.commons.configuration.ConfigurationException;
import com.compomics.pride_asa_pipeline.core.gui.PipelineProgressMonitor;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author Niels Hulstaert
 */
public class PipelineParamsController {

    //model
    private BindingGroup bindingGroup;
    private ObservableList<PropertyGuiWrapper> propertyGuiWrapperBindingList;
    //view
    private PipelineConfigDialog pipelineConfigDialog;
    //parent controller
    private MainController mainController;
    //services

    public PipelineConfigDialog getPipelineConfigDialog() {
        return pipelineConfigDialog;
    }

    public void setPipelineConfigDialog(PipelineConfigDialog pipelineConfigDialog) {
        this.pipelineConfigDialog = pipelineConfigDialog;
    }

    public MainController getMainController() {
        return mainController;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void init() {
        pipelineConfigDialog = new PipelineConfigDialog(mainController.getMainFrame(), true);

        //init bindings
        bindingGroup = new BindingGroup();
        //table binding 
        propertyGuiWrapperBindingList = ObservableCollections.observableList(new ArrayList<PropertyGuiWrapper>());
        initPropertyGuiWrappersBindingList();

        JTableBinding pipelineParamsTableBinding = SwingBindings.createJTableBinding(AutoBinding.UpdateStrategy.READ_WRITE, propertyGuiWrapperBindingList, pipelineConfigDialog.getPipelineParamsTable());

        //Add column bindings
        JTableBinding.ColumnBinding columnBinding = pipelineParamsTableBinding.addColumnBinding(ELProperty.create("${key}"));
        columnBinding.setColumnName("Parameter");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(String.class);

        columnBinding = pipelineParamsTableBinding.addColumnBinding(ELProperty.create("${value}"));
        columnBinding.setColumnName("Value");
        columnBinding.setEditable(true);
        columnBinding.setColumnClass(Object.class);

        bindingGroup.addBinding(pipelineParamsTableBinding);
        bindingGroup.bind();

        //add listeners
        pipelineConfigDialog.getSaveButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    PropertiesConfigurationHolder.getInstance().save();
                    mainController.showMessageDialog("Save Successful", "The pipeline parameters were saved successfully.", JOptionPane.INFORMATION_MESSAGE);
                } catch (ConfigurationException ex) {
                    PipelineProgressMonitor.error(ex.getMessage(), ex);
                    mainController.showMessageDialog("Save Unsuccessful", "The pipeline settings could not be saved to file. "
                            + "\n" + "Please check if a \"pride_asa_pipeline.properties\" file exists in the \"resources\" folder. "
                            + "\n" + "The settings will however be used in the pipeline.", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        pipelineConfigDialog.getResetButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    //clear holder and reload default properties
                    PropertiesConfigurationHolder.getInstance().clear();
                    PropertiesConfigurationHolder.getInstance().load(new ClassPathResource("resources/pride_asa_pipeline.properties").getInputStream());
                    //reset binding list
                    propertyGuiWrapperBindingList.clear();
                    initPropertyGuiWrappersBindingList();
                } catch (ConfigurationException ex) {
                    PipelineProgressMonitor.error(ex.getMessage(), ex);
                } catch (IOException ex) {
                    PipelineProgressMonitor.error(ex.getMessage(), ex);
                }
            }
        });
    }

    public void updatePropertyGuiWrapper(String propertyName, Object value) {
        for (PropertyGuiWrapper propertyGuiWrapper : propertyGuiWrapperBindingList) {
            if (propertyGuiWrapper.getKey().equals(propertyName)) {
                propertyGuiWrapper.setValue(value);
            }
        }
    }

    private void initPropertyGuiWrappersBindingList() {
        Iterator<String> iterator = PropertiesConfigurationHolder.getInstance().getKeys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            propertyGuiWrapperBindingList.add(new PropertyGuiWrapper(key, PropertiesConfigurationHolder.getInstance().getProperty(key)));
        }
    }
}
