package com.compomics.pride_asa_pipeline.core.gui.controller;


import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.gui.view.IdentificationsFileSelectionPanel;
import com.compomics.pride_asa_pipeline.core.gui.view.PrideSelectionPanel;
import com.compomics.pride_asa_pipeline.core.gui.view.ResultFileSelectionPanel;
import com.compomics.pride_asa_pipeline.core.logic.DbSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.core.logic.FileSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.core.logic.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.core.logic.modification.InputType;
import com.compomics.pride_asa_pipeline.core.logic.recalibration.MassRecalibrationResult;
import com.compomics.pride_asa_pipeline.core.service.ExperimentService;
import com.compomics.pride_asa_pipeline.core.service.ResultHandler;
import com.compomics.pride_asa_pipeline.core.util.ResourceUtils;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.ModificationHolder;
import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * @author Niels Hulstaert
 * @author Harald Barsnes
 */
public class ExperimentSelectionController {

    private static final Logger LOGGER = Logger.getLogger(ExperimentSelectionController.class);
    private static final String EXPERIMENT_ACCESSION_SEPARATOR = ":";
    private static final int NUMBER_OF_PRIDE_PROGRESS_STEPS = 5;
    private static final int NUMBER_OF_FILE_PROGRESS_STEPS = 2;
    //model
    private Integer taxonomyId;
    //hold reference to swingworker for cancelling purposes
    private SwingWorker<?, Void> currentSwingWorker;
    //view
    private PrideSelectionPanel prideSelectionPanel;
    private ResultFileSelectionPanel resultFileSelectionPanel;
    private IdentificationsFileSelectionPanel identificationsFileSelectionPanel;
    //parent controller
    private MainController mainController;
    //child controllers
    private PipelineProgressController pipelineProgressController;
    private SystematicMassErrorsController systematicMassErrorsController;
    private ModificationsMergeController modificationsMergeController;
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

    public IdentificationsFileSelectionPanel getIdentificationsFileSelectionPanel() {
        return identificationsFileSelectionPanel;
    }

    public ResultFileSelectionPanel getResultFileSelectionPanel() {
        return resultFileSelectionPanel;
    }

    public PipelineProgressController getPipelineProgressController() {
        return pipelineProgressController;
    }

    public void setPipelineProgressController(PipelineProgressController pipelineProgressController) {
        this.pipelineProgressController = pipelineProgressController;
    }

    public SystematicMassErrorsController getSystematicMassErrorsController() {
        return systematicMassErrorsController;
    }

    public void setSystematicMassErrorsController(SystematicMassErrorsController systematicMassErrorsController) {
        this.systematicMassErrorsController = systematicMassErrorsController;
    }

    public ModificationsMergeController getModificationsMergeController() {
        return modificationsMergeController;
    }

    public void setModificationsMergeController(ModificationsMergeController modificationsMergeController) {
        this.modificationsMergeController = modificationsMergeController;
    }

    public void init() {
        initPrideSelectionPanel();
        initResultFileSelectionPanel();
        initIdentificationsFileSelectionPanel();

        //init child controllers
        pipelineProgressController.init();
        systematicMassErrorsController.init();
        modificationsMergeController.init();
    }

    public void onIdentificationsLoaded() {
        //execute worker
        InitModificationsWorker initModificationsWorker = new InitModificationsWorker();
        currentSwingWorker = initModificationsWorker;
        initModificationsWorker.execute();
    }

    public void onModificationsLoaded() {
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
        currentSwingWorker.cancel(true);
    }

    private void initPrideSelectionPanel() {
        prideSelectionPanel = new PrideSelectionPanel();

        //fill combo box
        updateComboBox(experimentService.findAllExperimentAccessions());

        //disable taxonomy text field
        prideSelectionPanel.getTaxonomyTextField().setEnabled(Boolean.FALSE);

        //init include pride modifications check box
        prideSelectionPanel.getIncludePrideModificationsCheckBox().setSelected(PropertiesConfigurationHolder.getInstance().getBoolean("spectrumannotator.include_pride_modifications"));

        //add action listeners
        prideSelectionPanel.getTaxonomyFilterCheckBox().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (prideSelectionPanel.getTaxonomyFilterCheckBox().isSelected()) {
                    //enable taxonomy text field
                    prideSelectionPanel.getTaxonomyTextField().setEnabled(true);
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

        prideSelectionPanel.getIncludePrideModificationsCheckBox().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                boolean isSelected = prideSelectionPanel.getIncludePrideModificationsCheckBox().isSelected();
                if (PropertiesConfigurationHolder.getInstance().getBoolean("spectrumannotator.include_pride_modifications") != isSelected) {
                    //update properties file
                    mainController.updatePipelineParam("spectrumannotator.include_pride_modifications", isSelected);
                }
            }
        });

        prideSelectionPanel.getProcessButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainController.setCurrentSpectrumAnnotator(DbSpectrumAnnotator.class);
                //execute worker
                InitIdentificationsWorker initIdentificationsWorker = new InitIdentificationsWorker();
                currentSwingWorker = initIdentificationsWorker;
                initIdentificationsWorker.execute();
            }
        });
    }

    private void initResultFileSelectionPanel() {
        resultFileSelectionPanel = new ResultFileSelectionPanel();

        //init filechooser
        //get file chooser
        JFileChooser fileChooser = resultFileSelectionPanel.getFileChooser();
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
                if (extension.equals("txt")) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public String getDescription() {
                return ("text files only");
            }
        });

        //add listeners
        resultFileSelectionPanel.getSelectFileButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //in response to the button click, show open dialog 
                int returnVal = resultFileSelectionPanel.getFileChooser().showOpenDialog(resultFileSelectionPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    Resource resultFileResource = new FileSystemResource(resultFileSelectionPanel.getFileChooser().getSelectedFile());

                    //show file name in label
                    resultFileSelectionPanel.getFileNameLabel().setText(resultFileResource.getFilename());
                }
            }
        });

        resultFileSelectionPanel.getProcessButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (resultFileSelectionPanel.getFileChooser().getSelectedFile() != null) {
                    mainController.setCurrentSpectrumAnnotator(DbSpectrumAnnotator.class);
                    ImportPipelineResultWorker importPipelineResultWorker = new ImportPipelineResultWorker();
                    importPipelineResultWorker.execute();
                } else {
                    mainController.showMessageDialog("Pride Result Import", "Please select a pipeline result file", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }

    private void initIdentificationsFileSelectionPanel() {
        identificationsFileSelectionPanel = new IdentificationsFileSelectionPanel();

        //init include pride xml modifications check box
        identificationsFileSelectionPanel.getIncludeIdentificationsFileModificationsCheckBox().setSelected(PropertiesConfigurationHolder.getInstance().getBoolean("spectrumannotator.include_pride_xml_modifications"));

        //init filechooser
        //get file chooser
        JFileChooser fileChooser = identificationsFileSelectionPanel.getFileChooser();
        //select only files
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        //select multiple file
        fileChooser.setMultiSelectionEnabled(Boolean.FALSE);
//        //set file filter
//        fileChooser.setFileFilter(new FileFilter() {
//            @Override
//            public boolean accept(File f) {
//                if (f.isDirectory()) {
//                    return true;
//                }
//
//                int index = f.getName().lastIndexOf(".");
//                String extension = f.getName().substring(index + 1);
//                if (extension.equals("xml")) {
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//
//            @Override
//            public String getDescription() {
//                return ("xml files only");
//            }
//        });

        //add listeners
        identificationsFileSelectionPanel.getSelectFileButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //in response to the button click, show open dialog 
                int returnVal = identificationsFileSelectionPanel.getFileChooser().showOpenDialog(identificationsFileSelectionPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    Resource resultFileResource = new FileSystemResource(identificationsFileSelectionPanel.getFileChooser().getSelectedFile());

                    //show file name in label
                    identificationsFileSelectionPanel.getFileNameLabel().setText(resultFileResource.getFilename());
                }
            }
        });

        identificationsFileSelectionPanel.getProcessButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (identificationsFileSelectionPanel.getFileChooser().getSelectedFile() != null) {
                    mainController.setCurrentSpectrumAnnotator(FileSpectrumAnnotator.class);
                    //execute worker
                    InitIdentificationsWorker initIdentificationsWorker = new InitIdentificationsWorker();
                    currentSwingWorker = initIdentificationsWorker;
                    initIdentificationsWorker.execute();
                } else {
                    mainController.showMessageDialog("Pride XML Import", "Please select a pride XML file", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        identificationsFileSelectionPanel.getIncludeIdentificationsFileModificationsCheckBox().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                boolean isSelected = identificationsFileSelectionPanel.getIncludeIdentificationsFileModificationsCheckBox().isSelected();
                if (PropertiesConfigurationHolder.getInstance().getBoolean("spectrumannotator.include_pride_xml_modifications") != isSelected) {
                    //update properties file
                    mainController.updatePipelineParam("spectrumannotator.include_pride_xml_modifications", isSelected);
                }
            }
        });
    }

    private void filterExperimentAccessions() {
        if (!prideSelectionPanel.getTaxonomyTextField().getText().isEmpty()) {
            try {
                Integer newTaxonomyId = Integer.parseInt(prideSelectionPanel.getTaxonomyTextField().getText());
                if (!taxonomyId.equals(newTaxonomyId)) {
                    taxonomyId = newTaxonomyId;
                    updateComboBox(experimentService.findExperimentAccessionsByTaxonomy(taxonomyId));
                }
            } catch (NumberFormatException e) {
                mainController.showMessageDialog("Format Error", "Please insert a correct taxonomy ID (e.g. Homo Sapiens ID: 9606)", JOptionPane.ERROR_MESSAGE);
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
        if (mainController.getCurrentSpectrumAnnotator() instanceof FileSpectrumAnnotator) {
            File identificationsFile = identificationsFileSelectionPanel.getFileChooser().getSelectedFile();
            experimentAccession = identificationsFile.getName().substring(0, identificationsFile.getName().indexOf(".xml"));
        } else {
            if (prideSelectionPanel.getExperimentSelectionComboBox().getSelectedItem() != null) {
                String comboBoxString = prideSelectionPanel.getExperimentSelectionComboBox().getSelectedItem().toString();
                experimentAccession = comboBoxString.substring(0, comboBoxString.indexOf(EXPERIMENT_ACCESSION_SEPARATOR));
            }
        }

        return experimentAccession;
    }

    //swing workers
    private class InitIdentificationsWorker extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            //show progress bar
            pipelineProgressController.showProgressBar(NUMBER_OF_PRIDE_PROGRESS_STEPS, "Processing.");
            if (mainController.getCurrentSpectrumAnnotator() instanceof FileSpectrumAnnotator) {
                ((FileSpectrumAnnotator) mainController.getCurrentSpectrumAnnotator()).initIdentifications(identificationsFileSelectionPanel.getFileChooser().getSelectedFile());
            } else {
                ((DbSpectrumAnnotator) mainController.getCurrentSpectrumAnnotator()).initIdentifications(getExperimentAccesion());
            }

            return null;
        }

        @Override
        protected void done() {
            try {
                get();

                boolean hasCompleteIdentifications = mainController.getCurrentSpectrumAnnotator().getIdentifications().getCompleteIdentifications().isEmpty();
                MassRecalibrationResult massRecalibrationResult = mainController.getCurrentSpectrumAnnotator().getSpectrumAnnotatorResult().getMassRecalibrationResult();

                //check if the experiment has "useful" identifications
                if (hasCompleteIdentifications) {
                    mainController.showMessageDialog("Pipeline Error", "No useful identifications were found for experiment " + getExperimentAccesion()
                            + "." + "\n" + "Please try another experiment.", JOptionPane.ERROR_MESSAGE);
                    onAnnotationCanceled();
                } //check if one of the systematic mass errors per charge state exceeds the threshold value. If so, show a confirmation dialog.
                else if (massRecalibrationResult.exceedsMaximumSystematicMassError()) {
                    systematicMassErrorsController.showDialog("One or more systematic mass errors exceed the threshold value of "
                            + PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.maximum_systematic_mass_error")
                            + ", proceed?", massRecalibrationResult);
                } //else proceed with the annotation
                else {
                    onIdentificationsLoaded();
                }
            } catch (InterruptedException ex) {
                onAnnotationCanceled();
                LOGGER.error(ex.getMessage(), ex);
            } catch (ExecutionException ex) {
                onAnnotationCanceled();
                LOGGER.error(ex.getMessage(), ex);
                mainController.showMessageDialog("Unexpected Error", "An expected error occured: " + ex.getMessage() + ", please try to restart the application.", JOptionPane.ERROR_MESSAGE);
            } catch (CancellationException ex) {
                LOGGER.info("annotation for experiment " + getExperimentAccesion() + " cancelled.");
            }
        }
    }

    private class InitModificationsWorker extends SwingWorker<Set<Modification>, Void> {

        @Override
        protected Set<Modification> doInBackground() throws Exception {
            //init the modfications considered in the pipeline
            Resource modificationsResource = ResourceUtils.getResourceByRelativePath(PropertiesConfigurationHolder.getInstance().getString("modification.pipeline_modifications_file"));
            Set<Modification> prideModifications = mainController.getCurrentSpectrumAnnotator().initModifications(modificationsResource, InputType.PRIDE_ASAP);

            return prideModifications;
        }

        @Override
        protected void done() {
            try {
                Set<Modification> prideModifications = get();
                //check for pride modifications
                if (!prideModifications.isEmpty()) {
                    ModificationHolder modificationHolder = mainController.getCurrentSpectrumAnnotator().getModificationHolder();

                    //filter modifications
                    Set<Modification> conflictingModifications = mainController.getCurrentSpectrumAnnotator().getModificationService().filterModifications(modificationHolder, prideModifications);
                    if (!conflictingModifications.isEmpty()) {
                        modificationsMergeController.showDialog(modificationHolder, prideModifications, conflictingModifications);
                    }//else add all the pride modifications to the pipeline modifications and proceed with the annotation                                      
                    else {
                        modificationHolder.addModifications(prideModifications);
                        onModificationsLoaded();
                    }
                } //else proceed with the annotation
                else {
                    onModificationsLoaded();
                }
            } catch (InterruptedException ex) {
                onAnnotationCanceled();
                LOGGER.error(ex.getMessage(), ex);
            } catch (ExecutionException ex) {
                onAnnotationCanceled();
                LOGGER.error(ex.getMessage(), ex);
                mainController.showMessageDialog("Unexpected Error", "An expected error occured: " + ex.getMessage() + ", please try to restart the application.", JOptionPane.ERROR_MESSAGE);
            } catch (CancellationException ex) {
                LOGGER.info("annotation for experiment " + getExperimentAccesion() + " cancelled.");
            }
        }
    }

    private class AnnotationWorker extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            mainController.getCurrentSpectrumAnnotator().annotate(getExperimentAccesion());
            SpectrumAnnotatorResult spectrumAnnotatorResult = mainController.getCurrentSpectrumAnnotator().getSpectrumAnnotatorResult();
            boolean writeResultToFile;
            if (mainController.getCurrentSpectrumAnnotator() instanceof FileSpectrumAnnotator) {
                writeResultToFile = identificationsFileSelectionPanel.getWriteResultCheckBox().isSelected();
            } else {
                writeResultToFile = prideSelectionPanel.getWriteResultCheckBox().isSelected();
            }

            //write result to file if necessary
            if (writeResultToFile) {
                resultHandler.writeResultToFile(spectrumAnnotatorResult);
                Set<Modification> usedModifications = mainController.getCurrentSpectrumAnnotator().getModificationService().getUsedModifications(spectrumAnnotatorResult).keySet();
                resultHandler.writeUsedModificationsToFile(spectrumAnnotatorResult.getExperimentAccession(),
                        usedModifications);
            }

            return null;
        }

        @Override
        protected void done() {
            try {
                get();
                mainController.onAnnotationFinished();

                //hide progress bar
                pipelineProgressController.hideProgressDialog();
                //enable process buttons
                prideSelectionPanel.getProcessButton().setEnabled(true);
                resultFileSelectionPanel.getProcessButton().setEnabled(true);
            } catch (InterruptedException | ExecutionException ex) {
                onAnnotationCanceled();
                LOGGER.error(ex.getMessage(), ex);
                mainController.showUnexpectedErrorDialog(ex.getMessage());
            } catch (CancellationException ex) {
                LOGGER.info("annotation for experiment " + getExperimentAccesion() + " canceled.");
            }
        }
    }

    private class ImportPipelineResultWorker extends SwingWorker<SpectrumAnnotatorResult, Void> {

        @Override
        protected SpectrumAnnotatorResult doInBackground() throws Exception {

            //show progress bar
            pipelineProgressController.showProgressBar(NUMBER_OF_FILE_PROGRESS_STEPS, "Importing.");

            LOGGER.info("Importing pipeline result file " + resultFileSelectionPanel.getFileChooser().getSelectedFile().getName());
            SpectrumAnnotatorResult spectrumAnnotatorResult = resultHandler.readResultFromFile(resultFileSelectionPanel.getFileChooser().getSelectedFile());
            LOGGER.info("Finished importing pipeline result file " + resultFileSelectionPanel.getFileChooser().getSelectedFile().getName());

            return spectrumAnnotatorResult;
        }

        @Override
        protected void done() {
            try {
                SpectrumAnnotatorResult spectrumAnnotatorResult = get();
                mainController.getCurrentSpectrumAnnotator().setSpectrumAnnotatorResult(spectrumAnnotatorResult);
                mainController.onAnnotationFinished();
            } catch (InterruptedException | ExecutionException ex) {
                LOGGER.error(ex.getMessage(), ex);
                mainController.showUnexpectedErrorDialog(ex.getMessage());
                onAnnotationCanceled();
            } catch (CancellationException ex) {
                LOGGER.info("Importing pipeline results for experiment " + getExperimentAccesion() + " cancelled.");
            } finally {
                //hide progress bar
                pipelineProgressController.hideProgressDialog();
                //enable process buttons
                prideSelectionPanel.getProcessButton().setEnabled(true);
                resultFileSelectionPanel.getProcessButton().setEnabled(true);
            }
        }
    }
}
