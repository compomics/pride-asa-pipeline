/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.gui.controller;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.gui.view.PipelineParamsPanel;
import com.compomics.pride_asa_pipeline.gui.wrapper.PropertyGuiWrapper;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.util.FileUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
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
 * @author niels
 */
public class PipelineParamsController {

    private static final Logger LOGGER = Logger.getLogger(PipelineParamsController.class);
    //model
    private BindingGroup bindingGroup;
    private ObservableList<PropertyGuiWrapper> propertyGuiWrapperBindingList;
    //view
    private PipelineParamsPanel pipelineParamsPanel;
    //parent controller
    private MainController mainController;
    //services

    public PipelineParamsPanel getPipelineParamsPanel() {
        return pipelineParamsPanel;
    }

    public void setPipelineParamsPanel(PipelineParamsPanel pipelineParamsPanel) {
        this.pipelineParamsPanel = pipelineParamsPanel;
    }

    public MainController getMainController() {
        return mainController;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void init() {
        pipelineParamsPanel = new PipelineParamsPanel();

        //init bindings
        bindingGroup = new BindingGroup();
        //table binding 
        propertyGuiWrapperBindingList = ObservableCollections.observableList(new ArrayList<PropertyGuiWrapper>());
        initPropertyGuiWrappersBindingList();

        JTableBinding pipelineParamsTableBinding = SwingBindings.createJTableBinding(AutoBinding.UpdateStrategy.READ_WRITE, propertyGuiWrapperBindingList, pipelineParamsPanel.getPipelineParamsTable());

        //Add column bindings
        JTableBinding.ColumnBinding columnBinding = pipelineParamsTableBinding.addColumnBinding(ELProperty.create("${key}"));
        columnBinding.setColumnName("Parameter");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(String.class);

        columnBinding = pipelineParamsTableBinding.addColumnBinding(ELProperty.create("${value}"));
        columnBinding.setColumnName("Value");
        columnBinding.setEditable(Boolean.TRUE);
        columnBinding.setColumnClass(Object.class);

        bindingGroup.addBinding(pipelineParamsTableBinding);
        bindingGroup.bind();

        //add listeners
        pipelineParamsPanel.getSaveButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    PropertiesConfigurationHolder.getInstance().save();
                    mainController.showMessageDialog("Save successful", "The pipeline parameters were saved successfully.", JOptionPane.INFORMATION_MESSAGE);
                } catch (ConfigurationException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        });

        pipelineParamsPanel.getResetButton().addActionListener(new ActionListener() {
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
                    LOGGER.error(ex.getMessage(), ex);
                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        });
    }

    private void initPropertyGuiWrappersBindingList() {
        Iterator<String> iterator = PropertiesConfigurationHolder.getInstance().getKeys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            propertyGuiWrapperBindingList.add(new PropertyGuiWrapper(key, PropertiesConfigurationHolder.getInstance().getProperty(key)));
        }
    }
}
