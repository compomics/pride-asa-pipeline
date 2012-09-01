/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.gui.controller;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.gui.view.FileSelectionPanel;
import com.compomics.pride_asa_pipeline.gui.view.PrideSelectionPanel;
import com.compomics.pride_asa_pipeline.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.service.ExperimentService;
import com.compomics.pride_asa_pipeline.service.ResultHandler;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 *
 * @author Niels Hulstaert Hulstaert
 */
public class ExperimentSelectionController {

    private static final Logger LOGGER = Logger.getLogger(ExperimentSelectionController.class);
    private static final String EXPERIMENT_ACCESSION_SEPARATOR = ":";
    private static final int NUMBER_OF_PRIDE_PROGRESS_STEPS = 5;
    private static final int NUMBER_OF_FILE_PROGRESS_STEPS = 2;
    //model
    private Integer taxonomyId;
    //hold reference to swingworker for cancelling purposes
    private SwingWorker<Void, Void> currentSwingWorker;
    //view
    private PrideSelectionPanel prideSelectionPanel;
    private FileSelectionPanel fileSelectionPanel;
    //parent controller
    private MainController mainController;
    //child controllers
    private PipelineProgressController pipelineProgressController;
    private PipelineProceedController pipelineProceedController;
    //services
    private ExperimentService experimentService;
    private ResultHandler resultHandler;

    public ExperimentService getExperimentService() {
        return experimentService;
    }

    public void setExperimentService(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    public ResultHandler getResultHandler() {
        return resultHandler;
    }

    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }

    public MainController getMainController() {
        return mainController;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public PrideSelectionPanel getPrideSelectionPanel() {
        return prideSelectionPanel;
    }

    public FileSelectionPanel getFileSelectionPanel() {
        return fileSelectionPanel;
    }

    public PipelineProgressController getPipelineProgressController() {
        return pipelineProgressController;
    }

    public void setPipelineProgressController(PipelineProgressController pipelineProgressController) {
        this.pipelineProgressController = pipelineProgressController;
    }

    public PipelineProceedController getPipelineProceedController() {
        return pipelineProceedController;
    }

    public void setPipelineProceedController(PipelineProceedController pipelineProceedController) {
        this.pipelineProceedController = pipelineProceedController;
    }

    public void init() {
        initPrideSelectionPanel();
        initFileSelectionPanel();

        //init child controllers
        pipelineProgressController.init();
        pipelineProceedController.init();
    }

    public void onAnnotationProceed() {
        //execute worker
        AnnotationWorker annotationWorker = new AnnotationWorker();
        currentSwingWorker = annotationWorker;
        annotationWorker.execute();
    }

    public void onAnnotationCanceled() {
        //hide progress bar
        pipelineProgressController.hideProgressDialog();
        mainController.onAnnotationCanceled();

        //cancel swingworker
        currentSwingWorker.cancel(Boolean.TRUE);

        //enable process buttons
        prideSelectionPanel.getProcessButton().setEnabled(Boolean.TRUE);
        fileSelectionPanel.getProcessButton().setEnabled(Boolean.TRUE);
    }

    private void initPrideSelectionPanel() {
        prideSelectionPanel = new PrideSelectionPanel();

        //fill combo box
        updateComboBox(experimentService.findAllExperimentAccessions());

        //disable taxonomy text field
        prideSelectionPanel.getTaxonomyTextField().setEnabled(Boolean.FALSE);

        //add action listeners
        prideSelectionPanel.getTaxonomyFilterCheckBox().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (prideSelectionPanel.getTaxonomyFilterCheckBox().isSelected()) {
                    //enable taxonomy text field
                    prideSelectionPanel.getTaxonomyTextField().setEnabled(Boolean.TRUE);
                    filterExperimentAccessions();
                } else {
                    //disable taxonomy text field
                    prideSelectionPanel.getTaxonomyTextField().setEnabled(Boolean.FALSE);
                    //reset combo box                    
                    updateComboBox(experimentService.findAllExperimentAccessions());
                    taxonomyId = null;
                }
            }
        });

        prideSelectionPanel.getTaxonomyTextField().addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fe) {
            }

            @Override
            public void focusLost(FocusEvent fe) {
                filterExperimentAccessions();
            }
        });

        prideSelectionPanel.getProcessButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //execute worker
                InitAnnotationWorker initAnnotationWorker = new InitAnnotationWorker();
                currentSwingWorker = initAnnotationWorker;
                initAnnotationWorker.execute();

                //disable process buttons
                prideSelectionPanel.getProcessButton().setEnabled(Boolean.FALSE);
                fileSelectionPanel.getProcessButton().setEnabled(Boolean.FALSE);
            }
        });
    }

    private void initFileSelectionPanel() {
        fileSelectionPanel = new FileSelectionPanel();

        //init filechooser
        //get file chooser
        JFileChooser fileChooser = fileSelectionPanel.getFileChooser();
        //select only files
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        //select multiple file
        fileChooser.setMultiSelectionEnabled(Boolean.FALSE);
        //set MGF file filter
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }

                int index = f.getName().lastIndexOf(".");
                String extension = f.getName().substring(index + 1);
                if (extension != null) {
                    if (extension.equals("txt")) {
                        return true;
                    } else {
                        return false;
                    }
                }

                return false;
            }

            @Override
            public String getDescription() {
                return ("text files only");
            }
        });

        //add listeners
        fileSelectionPanel.getSelectFileButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //in response to the button click, show open dialog 
                int returnVal = fileSelectionPanel.getFileChooser().showOpenDialog(fileSelectionPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    Resource resultFileResource = new FileSystemResource(fileSelectionPanel.getFileChooser().getSelectedFile());

                    //show file name in label
                    fileSelectionPanel.getFileNameLabel().setText(resultFileResource.getFilename());
                }
            }
        });

        fileSelectionPanel.getProcessButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fileSelectionPanel.getFileChooser().getSelectedFile() != null) {
                    ImportPipelineResultWorker importPipelineResultWorker = new ImportPipelineResultWorker();
                    importPipelineResultWorker.execute();

                    //disable process buttons
                    prideSelectionPanel.getProcessButton().setEnabled(Boolean.FALSE);
                    fileSelectionPanel.getProcessButton().setEnabled(Boolean.FALSE);
                } else {
                    mainController.showMessageDialog("Pipeline result import", "Please select an pipeline result file", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }

    private void filterExperimentAccessions() {
        if (!prideSelectionPanel.getTaxonomyTextField().getText().isEmpty()) {
            try {
                Integer newTaxonomyId = Integer.parseInt(prideSelectionPanel.getTaxonomyTextField().getText());
                if (taxonomyId != newTaxonomyId) {
                    taxonomyId = newTaxonomyId;
                    updateComboBox(experimentService.findExperimentAccessionsByTaxonomy(taxonomyId));
                }
            } catch (NumberFormatException e) {
                mainController.showMessageDialog("Format error", "Please insert a correct taxonomy ID (e.g. Homo Sapiens ID: 9606)", JOptionPane.ERROR_MESSAGE);
                prideSelectionPanel.getTaxonomyTextField().setText("");
            }
        }
    }

    private void updateComboBox(Map<String, String> experimentAccessions) {
        //empty combo box
        prideSelectionPanel.getExperimentSelectionComboBox().removeAllItems();
        //load experiment accessions and fill combo box        
        for (String experimentAccession : experimentAccessions.keySet()) {
            prideSelectionPanel.getExperimentSelectionComboBox().addItem(experimentAccession + EXPERIMENT_ACCESSION_SEPARATOR + " " + experimentAccessions.get(experimentAccession));
        }
    }

    private String getExperimentAccesion() {
        String experimentAccession = null;
        if (prideSelectionPanel.getExperimentSelectionComboBox().getSelectedItem() != null) {
            String comboBoxString = prideSelectionPanel.getExperimentSelectionComboBox().getSelectedItem().toString();
            experimentAccession = comboBoxString.substring(0, comboBoxString.indexOf(EXPERIMENT_ACCESSION_SEPARATOR));
        }

        return experimentAccession;
    }

    //swing workers
    private class InitAnnotationWorker extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            //show progress bar
            pipelineProgressController.showProgressBar(NUMBER_OF_PRIDE_PROGRESS_STEPS, "Processing");

            mainController.getPrideSpectrumAnnotator().initAnnotation(getExperimentAccesion());

            return null;
        }

        @Override
        protected void done() {
            try {
                get();
                //check if the experiment has "useful" identifications
                if (mainController.getPrideSpectrumAnnotator().getIdentifications().getCompleteIdentifications().isEmpty()) {
                    mainController.showMessageDialog("Pipeline error", "No useful identifications were found for experiment " + getExperimentAccesion()
                            + "." + "\n" + "Please try another experiment.", JOptionPane.ERROR_MESSAGE);
                    onAnnotationCanceled();
                } //check if one of the systematic mass errors per charge state exceeds the threshold value. If so, show a confirmation dialog.
                else if (mainController.getPrideSpectrumAnnotator().getSpectrumAnnotatorResult().getMassRecalibrationResult().exceedsMaximumSystematicMassError()) {
                    pipelineProceedController.showDialog("One or more systematic mass errors exceed the threshold value of "
                            + PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.maximum_systematic_mass_error")
                            + ", proceed?", mainController.getPrideSpectrumAnnotator().getSpectrumAnnotatorResult().getMassRecalibrationResult());
                } //else proceed with the annotation
                else {
                    onAnnotationProceed();
                }
            } catch (InterruptedException ex) {
                //hide progress bar
                pipelineProgressController.hideProgressDialog();
                LOGGER.error(ex.getMessage(), ex);
            } catch (ExecutionException ex) {
                //hide progress bar
                pipelineProgressController.hideProgressDialog();
                mainController.showMessageDialog("Unexpected error", "Un expected error occured: " + ex.getMessage() + ", please try to restart the application.", JOptionPane.ERROR_MESSAGE);
            } catch (CancellationException ex) {
                LOGGER.debug("annotation canceled.");
            }
        }
    }

    private class AnnotationWorker extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            mainController.getPrideSpectrumAnnotator().annotate(getExperimentAccesion());

            //write result to file if necessary
            if (prideSelectionPanel.getWriteResultCheckBox().isSelected()) {
                resultHandler.writeResultToFile(mainController.getPrideSpectrumAnnotator().getSpectrumAnnotatorResult());
            }

            return null;
        }

        @Override
        protected void done() {
            try {
                get();
                mainController.onAnnotationFinished(mainController.getPrideSpectrumAnnotator().getSpectrumAnnotatorResult());
            } catch (InterruptedException ex) {
                LOGGER.error(ex.getMessage(), ex);
                mainController.showUnexpectedErrorDialog(ex.getMessage());
            } catch (ExecutionException ex) {
                LOGGER.error(ex.getMessage(), ex);
                mainController.showUnexpectedErrorDialog(ex.getMessage());
            } catch (CancellationException ex) {
                LOGGER.info("annotation for experiment " + getExperimentAccesion() + " canceled.");
            } finally {
                //hide progress bar
                pipelineProgressController.hideProgressDialog();
                //enable process buttons
                prideSelectionPanel.getProcessButton().setEnabled(Boolean.TRUE);
                fileSelectionPanel.getProcessButton().setEnabled(Boolean.TRUE);
            }
        }
    }

    private class ImportPipelineResultWorker extends SwingWorker<SpectrumAnnotatorResult, Void> {

        @Override
        protected SpectrumAnnotatorResult doInBackground() throws Exception {
            SpectrumAnnotatorResult spectrumAnnotatorResult = null;

            //show progress bar
            pipelineProgressController.showProgressBar(NUMBER_OF_FILE_PROGRESS_STEPS, "Importing");

            LOGGER.info("Importing pipeline result file " + fileSelectionPanel.getFileChooser().getSelectedFile().getName());
            spectrumAnnotatorResult = resultHandler.readResultFromFile(fileSelectionPanel.getFileChooser().getSelectedFile());
            LOGGER.info("Finished importing pipeline result file " + fileSelectionPanel.getFileChooser().getSelectedFile().getName());

            return spectrumAnnotatorResult;
        }

        @Override
        protected void done() {
            try {
                mainController.onAnnotationFinished(get());
            } catch (InterruptedException ex) {
                LOGGER.error(ex.getMessage(), ex);
                mainController.showUnexpectedErrorDialog(ex.getMessage());
            } catch (ExecutionException ex) {
                LOGGER.error(ex.getMessage(), ex);
                mainController.showUnexpectedErrorDialog(ex.getMessage());
            } catch (CancellationException ex) {
                LOGGER.info("annotation for experiment " + getExperimentAccesion() + " canceled.");
            } finally {
                //hide progress bar
                pipelineProgressController.hideProgressDialog();
                //enable process buttons
                prideSelectionPanel.getProcessButton().setEnabled(Boolean.TRUE);
                fileSelectionPanel.getProcessButton().setEnabled(Boolean.TRUE);
            }
        }
    }
}
