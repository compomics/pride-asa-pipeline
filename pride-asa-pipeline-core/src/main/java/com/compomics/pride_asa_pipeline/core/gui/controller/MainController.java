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
import com.compomics.pride_asa_pipeline.core.gui.view.MainFrame;
import com.compomics.pride_asa_pipeline.core.logic.spectrum.annotation.impl.SpectrumAnnotatorImpl;
import com.compomics.pride_asa_pipeline.core.logic.spectrum.annotation.AbstractSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.core.model.SpectrumAnnotatorResult;
import com.compomics.util.examples.BareBonesBrowserLaunch;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Level;
import javax.swing.*;
import org.apache.log4j.Logger;
import org.jdesktop.beansbinding.ELProperty;

/**
 *
 * @author Niels Hulstaert
 * @author Harald Barsnes
 */
public class MainController implements ActionListener {

    private static final Logger LOGGER = Logger.getLogger(MainController.class);
    //model
    /**
     * Keep track of the SpectrumAnnotator (db or identifications file)
     */
    private ControllerMode currentMode;

    private AbstractSpectrumAnnotator currentSpectrumAnnotator;
    private SpectrumAnnotatorImpl spectrumAnnotator;
    //view
    private MainFrame mainFrame;
    //child controllers
    private ExperimentSelectionController experimentSelectionController;
    private ModificationsController modificationsController;
    private PipelineResultController pipelineResultController;
    private PipelineParamsController pipelineParamsController;
    //services

    public ExperimentSelectionController getExperimentSelectionController() {
        return experimentSelectionController;
    }

    public void setExperimentSelectionController(ExperimentSelectionController experimentSelectionController) {
        this.experimentSelectionController = experimentSelectionController;
    }

    public ControllerMode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(ControllerMode currentMode) {
        this.currentMode = currentMode;
    }

    public SpectrumAnnotatorImpl getSpectrumAnnotator() {
        return spectrumAnnotator;
    }

    public void setSpectrumAnnotator(SpectrumAnnotatorImpl spectrumAnnotator) {
        this.spectrumAnnotator = spectrumAnnotator;
    }

    public ModificationsController getModificationsController() {
        return modificationsController;
    }

    public void setModificationsController(ModificationsController modificationsController) {
        this.modificationsController = modificationsController;
    }

    public PipelineResultController getPipelineResultController() {
        return pipelineResultController;
    }

    public void setPipelineResultController(PipelineResultController pipelineResultController) {
        this.pipelineResultController = pipelineResultController;
    }

    public PipelineParamsController getPipelineParamsController() {
        return pipelineParamsController;
    }

    public void setPipelineParamsController(PipelineParamsController pipelineParamsController) {
        this.pipelineParamsController = pipelineParamsController;
    }

    public AbstractSpectrumAnnotator getCurrentSpectrumAnnotator() {
        return currentSpectrumAnnotator;
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {

        if (((JMenuItem) actionEvent.getSource()).getText().equalsIgnoreCase("Configuration")) {
            pipelineParamsController.getPipelineConfigDialog().setVisible(true);
        } else { // modification details
            modificationsController.getModificationsConfigDialog().setVisible(true);
        }
    }

    public void init() {
        // check for new version
        checkForNewVersion(getVersion());

        mainFrame = new MainFrame();

        // set the title of the frame and add the icon
        mainFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/pride-asap.png")));
        mainFrame.setTitle("pride-asap " + getVersion());

        //workaround for better beansbinding logging issue
        org.jdesktop.beansbinding.util.logging.Logger.getLogger(ELProperty.class.getName()).setLevel(Level.SEVERE);

        //set default currentSpectrumAnnotator
        currentSpectrumAnnotator = spectrumAnnotator;

        //init child controllers
        experimentSelectionController.init();
        modificationsController.init();
        pipelineResultController.init();
        pipelineParamsController.init();

        //set uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOGGER.error(e.getMessage(), e);
                showUnexpectedErrorDialog(e.getMessage());
                onAnnotationCanceled();
            }
        });

        //add panel components                        
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;

        mainFrame.getPrideSelectionParentPanel().add(experimentSelectionController.getPrideSelectionPanel(), gridBagConstraints);
        mainFrame.getFileSelectionParentPanel().add(experimentSelectionController.getResultFileSelectionPanel(), gridBagConstraints);
        mainFrame.getIdentificationsFileSelectionParentPanel().add(experimentSelectionController.getIdentificationsFileSelectionPanel(), gridBagConstraints);
        mainFrame.getIdentificationsParentPanel().add(pipelineResultController.getIdentificationsPanel(), gridBagConstraints);
        mainFrame.getSummaryParentPanel().add(pipelineResultController.getSummaryPanel(), gridBagConstraints);

        //add action listeners
        mainFrame.getModificationsMenuItem().addActionListener(this);
        mainFrame.getPipelineConfigurationMenuItem().addActionListener(this);

        //fit to screen
        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        //set main frame visible
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    /**
     * Set the current SpectrumAnnotator.
     *
     * @param mode The mode for the current spectrum annotator
     */
    public void setCurrentSpectrumAnnotator(ControllerMode mode) {
        currentMode = mode;
    }

    public void showMessageDialog(String title, String message, int messageType) {
        if (messageType == JOptionPane.ERROR_MESSAGE) {
            //add message to JTextArea
            JTextArea textArea = new JTextArea(message);
            //put JTextArea in JScrollPane
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 200));
            textArea.setEditable(Boolean.FALSE);

            JOptionPane.showMessageDialog(mainFrame.getContentPane(), scrollPane, title, messageType);
        } else {
            JOptionPane.showMessageDialog(mainFrame.getContentPane(), message, title, messageType);
        }
    }

    public void showUnexpectedErrorDialog(String message) {
        showMessageDialog("Unexpected Error", "An unexpected error occured: "
                + "\n" + message
                + "\n" + "Please try to rerun the application.", JOptionPane.ERROR_MESSAGE);
    }

    public void updatePipelineParam(String propertyName, Object value) {
        pipelineParamsController.updatePropertyGuiWrapper(propertyName, value);
    }

    public void onAnnotationFinished() {
        SpectrumAnnotatorResult spectrumAnnotatorResult = currentSpectrumAnnotator.getSpectrumAnnotatorResult();
        pipelineResultController.update(spectrumAnnotatorResult);
    }

    public void onAnnotationCanceled() {
        LOGGER.info("Annotation canceled.");
        currentSpectrumAnnotator.clearPipeline();
        currentSpectrumAnnotator.clearTmpResources();
    }

    /**
     * Retrieves the version number set in the pom file.
     *
     * @return the version number of PeptideShaker
     */
    public String getVersion() {
        return PropertiesConfigurationHolder.getInstance().getString("pride-asap.version", "UNKNOWN");
    }

    /**
     * Check if a newer version of pride-asap is available.
     *
     * @param currentVersion the version number of the currently running
     * reporter
     */
    private static void checkForNewVersion(String currentVersion) {

        try {
            boolean deprecatedOrDeleted = false;
            URL downloadPage = new URL(
                    "http://code.google.com/p/pride-asa-pipeline/downloads/detail?name=pride-asa-pipeline-"
                    + currentVersion + ".zip");

            if ((java.net.HttpURLConnection) downloadPage.openConnection() != null) {

                int respons = ((java.net.HttpURLConnection) downloadPage.openConnection()).getResponseCode();

                // 404 means that the file no longer exists, which means that
                // the running version is no longer available for download,
                // which again means that a never version is available.
                if (respons == 404) {
                    deprecatedOrDeleted = true;
                } else {

                    // also need to check if the available running version has been
                    // deprecated (but not deleted)
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(downloadPage.openStream()));

                    String inputLine;

                    while ((inputLine = in.readLine()) != null && !deprecatedOrDeleted) {
                        if (inputLine.lastIndexOf("Deprecated") != -1
                                && inputLine.lastIndexOf("Deprecated Downloads") == -1
                                && inputLine.lastIndexOf("Deprecated downloads") == -1) {
                            deprecatedOrDeleted = true;
                        }
                    }

                    in.close();
                }

                // informs the user about an updated version of the tool, unless the user
                // is running a beta version
                if (deprecatedOrDeleted && currentVersion.lastIndexOf("beta") == -1) {
                    int option = JOptionPane.showConfirmDialog(null,
                            "A newer version of pride-asap is available.\n"
                            + "Do you want to upgrade?",
                            "Upgrade Available",
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        BareBonesBrowserLaunch.openURL("http://pride-asa-pipeline.googlecode.com/");
                        System.exit(0);
                    } else if (option == JOptionPane.CANCEL_OPTION) {
                        System.exit(0);
                    }
                }
            }
        } catch (UnknownHostException e) {
            // ignore exception
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
