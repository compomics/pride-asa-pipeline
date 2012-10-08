/*
 *

 */
package com.compomics.pride_asa_pipeline.gui.controller;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.gui.LoggingBindingListener;
import com.compomics.pride_asa_pipeline.gui.view.ModificationsPanel;
import com.compomics.pride_asa_pipeline.model.AminoAcid;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.service.ModificationService;
import com.compomics.pride_asa_pipeline.util.ResourceUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.apache.log4j.Logger;
import org.jdesktop.beansbinding.*;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.JTableBinding.ColumnBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.jdom2.JDOMException;
import org.jdom2.input.JDOMParseException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 *
 * @author Niels Hulstaert Hulstaert
 */
public class ModificationsController {

    private static final Logger LOGGER = Logger.getLogger(ModificationsController.class);
    //model
    private Resource modificationsResource;
    private BindingGroup bindingGroup;
    private ObservableList<Modification> modificationsBindingList;
    private ObservableList<AminoAcid> aminoAcidsBindingList;
    private ObservableList<AminoAcid> affectedAminoAcidsBindingList;
    //view
    private ModificationsPanel modificationsPanel;
    //parent controller
    private MainController mainController;
    //services
    private ModificationService modificationService;

    public ModificationsController() {
        modificationsResource = ResourceUtils.getResourceByRelativePath(PropertiesConfigurationHolder.getInstance().getString("modification.pipeline_modifications_file"));
    }

    public MainController getMainController() {
        return mainController;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public ModificationsPanel getModificationsPanel() {
        return modificationsPanel;
    }

    public ModificationService getModificationService() {
        return modificationService;
    }

    public void setModificationService(ModificationService modificationService) {
        this.modificationService = modificationService;
    }

    public void init() {
        modificationsPanel = new ModificationsPanel();

        //fill location combobox
        for (Modification.Location location : Modification.Location.values()) {
            modificationsPanel.getModLocationComboBox().addItem(location);
        }

        //init filechooser
        //get file chooser
        JFileChooser fileChooser = modificationsPanel.getFileChooser();
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
                    if (extension.equals("xml")) {
                        return true;
                    } else {
                        return false;
                    }
                }

                return false;
            }

            @Override
            public String getDescription() {
                return ("XML files only");
            }
        });

        //init bindings
        bindingGroup = new BindingGroup();
        //table binding
        try {
            modificationsBindingList = ObservableCollections.observableList(getModificationsAsList(
                    modificationService.loadPipelineModifications(modificationsResource)));
        } catch (JDOMParseException ex) {
            LOGGER.error(ex.getMessage(), ex);
            mainController.showMessageDialog("Import unsuccessful", "The modifications file could not be imported. Please check the validity of the file. "
                    + "\n" + "Error on line " + ex.getLineNumber() + ", message: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        } catch (JDOMException ex) {
            LOGGER.error(ex.getMessage(), ex);
            mainController.showMessageDialog("Import unsuccessful", "The modifications file could not be imported. Please check the validity of the file. "
                    + "\n" + "Error message: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }

        JTableBinding modificationsTableBinding = SwingBindings.createJTableBinding(AutoBinding.UpdateStrategy.READ_WRITE, modificationsBindingList, modificationsPanel.getModifcationsTable());

        //Add column bindings
        ColumnBinding columnBinding = modificationsTableBinding.addColumnBinding(ELProperty.create("${name}"));
        columnBinding.setColumnName("Name");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(String.class);

        columnBinding = modificationsTableBinding.addColumnBinding(ELProperty.create("${accession}"));
        columnBinding.setColumnName("Accession");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(String.class);

        columnBinding = modificationsTableBinding.addColumnBinding(ELProperty.create("${accessionValue}"));
        columnBinding.setColumnName("Accession value");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(String.class);

        columnBinding = modificationsTableBinding.addColumnBinding(ELProperty.create("${monoIsotopicMassShift}"));
        columnBinding.setColumnName("Monoisotopic mass shift");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(String.class);

        columnBinding = modificationsTableBinding.addColumnBinding(ELProperty.create("${averageMassShift}"));
        columnBinding.setColumnName("Average mass shift");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(String.class);

        columnBinding = modificationsTableBinding.addColumnBinding(ELProperty.create("${location}"));
        columnBinding.setColumnName("Location");
        columnBinding.setEditable(Boolean.FALSE);
        columnBinding.setColumnClass(Modification.Location.class);

        bindingGroup.addBinding(modificationsTableBinding);

        //selected modication in table bindings
        Binding nameBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, modificationsPanel.getModifcationsTable(), ELProperty.create("${selectedElement.name}"), modificationsPanel.getModNameTextField(), BeanProperty.create("text"), "name");
        nameBinding.setValidator(new RequiredStringValidator());
        bindingGroup.addBinding(nameBinding);

        Binding accessionBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, modificationsPanel.getModifcationsTable(), ELProperty.create("${selectedElement.accession}"), modificationsPanel.getModAccessionTextField(), BeanProperty.create("text"), "accession");
        accessionBinding.setValidator(new RequiredStringValidator());
        bindingGroup.addBinding(accessionBinding);

        Binding accessionValueBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, modificationsPanel.getModifcationsTable(), ELProperty.create("${selectedElement.accessionValue}"), modificationsPanel.getModAccessionValueTextField(), BeanProperty.create("text"), "accessionValue");
        accessionValueBinding.setValidator(new RequiredStringValidator());
        bindingGroup.addBinding(accessionValueBinding);

        Binding monoIsotopicMassShiftBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, modificationsPanel.getModifcationsTable(), ELProperty.create("${selectedElement.monoIsotopicMassShift}"), modificationsPanel.getModMonoIsotopicMassShiftTextField(), BeanProperty.create("text"), "monoIsotopicMassShift");
        monoIsotopicMassShiftBinding.setConverter(new DoubleConverter());
        bindingGroup.addBinding(monoIsotopicMassShiftBinding);

        Binding averageMassShiftBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, modificationsPanel.getModifcationsTable(), ELProperty.create("${selectedElement.averageMassShift}"), modificationsPanel.getModAverageMassShiftTextField(), BeanProperty.create("text"), "averageMassShift");
        averageMassShiftBinding.setConverter(new DoubleConverter());
        bindingGroup.addBinding(averageMassShiftBinding);

        Binding locationBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, modificationsPanel.getModifcationsTable(), ELProperty.create("${selectedElement.location}"), modificationsPanel.getModLocationComboBox(), BeanProperty.create("selectedItem"), "location");
        bindingGroup.addBinding(locationBinding);

        //amino acid list bindings
        aminoAcidsBindingList = ObservableCollections.observableList(getAminoAcidsAsList());
        JListBinding aminoAcidsBinding = SwingBindings.createJListBinding(AutoBinding.UpdateStrategy.READ_WRITE, aminoAcidsBindingList, modificationsPanel.getAminoAcidsList());
        bindingGroup.addBinding(aminoAcidsBinding);

        affectedAminoAcidsBindingList = ObservableCollections.observableList(new ArrayList<AminoAcid>());
        JListBinding affectedAminoAcidsBinding = SwingBindings.createJListBinding(AutoBinding.UpdateStrategy.READ_WRITE, affectedAminoAcidsBindingList, modificationsPanel.getAffectedAminoAcidsList());
        bindingGroup.addBinding(affectedAminoAcidsBinding);

        bindingGroup.addBindingListener(new LoggingBindingListener(modificationsPanel.getBindingLoggingLabel()));

        bindingGroup.bind();

        //add listeners
        modificationsPanel.getModifcationsTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                if (!lse.getValueIsAdjusting()) {
                    if (modificationsPanel.getModifcationsTable().getSelectedRow() != -1) {
                        Modification selectedModification = modificationsBindingList.get(modificationsPanel.getModifcationsTable().getSelectedRow());
                        affectedAminoAcidsBindingList.clear();
                        affectedAminoAcidsBindingList.addAll(selectedModification.getAffectedAminoAcids());

                        aminoAcidsBindingList.clear();
                        for (AminoAcid aminoAcid : AminoAcid.values()) {
                            if (!selectedModification.getAffectedAminoAcids().contains(aminoAcid)) {
                                aminoAcidsBindingList.add(aminoAcid);
                            }
                        }

                        //disable remove button if there's only one affected amino acid
                        changeRemoveAminoAcidButtonState(selectedModification);
                    }
                }
            }
        });

        modificationsPanel.getAddAminoAcidButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (modificationsPanel.getModifcationsTable().getSelectedRow() != -1) {
                    Object[] selectedValues = modificationsPanel.getAminoAcidsList().getSelectedValues();

                    Modification selectedModification = modificationsBindingList.get(modificationsPanel.getModifcationsTable().getSelectedRow());

                    for (Object o : selectedValues) {
                        AminoAcid aminoAcid = (AminoAcid) o;
                        affectedAminoAcidsBindingList.add(aminoAcid);
                        Collections.sort(affectedAminoAcidsBindingList);
                        aminoAcidsBindingList.remove(aminoAcid);
                        selectedModification.getAffectedAminoAcids().add(aminoAcid);

                        //enable remove button if there's more then one affected amino acid
                        changeRemoveAminoAcidButtonState(selectedModification);

                        modificationsPanel.getModifcationsTable().updateUI();
                    }
                }
            }
        });

        modificationsPanel.getRemoveAminoAcidButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (modificationsPanel.getModifcationsTable().getSelectedRow() != -1) {
                    Object[] selectedValues = modificationsPanel.getAffectedAminoAcidsList().getSelectedValues();

                    Modification selectedModification = modificationsBindingList.get(modificationsPanel.getModifcationsTable().getSelectedRow());

                    for (Object o : selectedValues) {
                        AminoAcid aminoAcid = (AminoAcid) o;
                        aminoAcidsBindingList.add(aminoAcid);
                        affectedAminoAcidsBindingList.remove(aminoAcid);
                        Collections.sort(aminoAcidsBindingList);
                        selectedModification.getAffectedAminoAcids().remove(aminoAcid);

                        //disable remove button if there's only one affected amino acid
                        changeRemoveAminoAcidButtonState(selectedModification);

                        modificationsPanel.getModifcationsTable().updateUI();
                    }
                }
            }
        });

        modificationsPanel.getAddModificationButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                Set<AminoAcid> affectedAminoAcids = new HashSet<AminoAcid>();
                affectedAminoAcids.add(AminoAcid.Ala);
                modificationsBindingList.add(new Modification("mod" + modificationsBindingList.size(), 0.0, 0.0, Modification.Location.NON_TERMINAL, affectedAminoAcids, "accession", "accessionValue"));
                modificationsPanel.getModifcationsTable().getSelectionModel().setSelectionInterval(modificationsBindingList.size() - 1, modificationsBindingList.size() - 1);

                //disable remove button if there's only one modification
                changeRemoveModificationButtonState();
            }
        });

        modificationsPanel.getRemoveModificationButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (modificationsPanel.getModifcationsTable().getSelectedRow() != -1) {
                    modificationsBindingList.remove(modificationsPanel.getModifcationsTable().getSelectedRow());
                    modificationsPanel.getModifcationsTable().getSelectionModel().setSelectionInterval(0, 0);

                    //disable remove button if there's only one modification
                    changeRemoveModificationButtonState();
                }
            }
        });

        modificationsPanel.getImportButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //in response to the button click, show open dialog 
                int returnVal = modificationsPanel.getFileChooser().showOpenDialog(modificationsPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    Resource modificationsImportResource = new FileSystemResource(modificationsPanel.getFileChooser().getSelectedFile());

                    //import modifications and refill the binding list
                    Set<Modification> importedModifications = null;
                    try {
                        importedModifications = modificationService.importPipelineModifications(modificationsImportResource);

                        modificationsBindingList.clear();
                        modificationsBindingList.addAll(importedModifications);

                        //select first modification
                        modificationsPanel.getModifcationsTable().getSelectionModel().setSelectionInterval(0, 0);

                        //disable remove button if there's only one modification
                        changeRemoveModificationButtonState();
                    } catch (JDOMParseException ex) {
                        LOGGER.error(ex.getMessage(), ex);
                        mainController.showMessageDialog("Import unsuccessful", "The modifications file could not be imported. Please check the validity of the file. "
                                + "\n" + "Error on line " + ex.getLineNumber() + ", message: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (JDOMException ex) {
                        LOGGER.error(ex.getMessage(), ex);
                        mainController.showMessageDialog("Import unsuccessful", "The modifications file could not be imported. Please check the validity of the file. "
                                + "\n" + "Error message: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        modificationsPanel.getSaveButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //check if modifications file in resources folder can be found
                if (ResourceUtils.isExistingFile(PropertiesConfigurationHolder.getInstance().getString("modification.pipeline_modifications_file"))) {
                    modificationService.savePipelineModifications(modificationsResource, modificationsBindingList);
                } else {
                    mainController.showMessageDialog("Save unsuccessful", "The modifications could not be saved to file. "
                            + "\n" + "Please check if a \"modifications.xml\" file exists in the \"resources\" folder. "
                            + "\n" + "The modifications will however be used in the pipeline.", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }

    /**
     * Gets the pipeline modifications as a list.
     *
     * @param modificationSet the set of modifications
     * @return the list of modifications
     */
    private List<Modification> getModificationsAsList(Set<Modification> modificationSet) {
        List<Modification> modifications = new ArrayList<Modification>();
        modifications.addAll(modificationSet);

        return modifications;
    }

    /**
     * Gets the amino acids as list. Arrays.asList(AminoAcid.values()) doesn't
     * work with the binding.
     *
     * @return the list of amino acids
     */
    private List<AminoAcid> getAminoAcidsAsList() {
        List<AminoAcid> aminoAcids = new ArrayList<AminoAcid>();
        aminoAcids.addAll(Arrays.asList(AminoAcid.values()));

        return aminoAcids;
    }

    /**
     * Changes the state of the removeAminoAcidButton (enable or disabled)
     * depending on the size of the affected amino acids of the selected
     * modification
     *
     * @param selectedModification the selected modification
     */
    private void changeRemoveAminoAcidButtonState(Modification selectedModification) {
        if (selectedModification.getAffectedAminoAcids().size() == 1) {
            modificationsPanel.getRemoveAminoAcidButton().setEnabled(Boolean.FALSE);
        } else {
            modificationsPanel.getRemoveAminoAcidButton().setEnabled(Boolean.TRUE);
        }
    }

    /**
     * Changes the state of the removeModificationButton (enable or disabled)
     * depending on the number of pipeline modifications
     *
     */
    private void changeRemoveModificationButtonState() {
        if (modificationsBindingList.size() == 1) {
            modificationsPanel.getRemoveModificationButton().setEnabled(Boolean.FALSE);
        } else {
            modificationsPanel.getRemoveModificationButton().setEnabled(Boolean.TRUE);
        }
    }

    /**
     * Converter class used for binding. In case of a NumberFormatException, the
     * value of the bound field is set 0.0
     */
    private class DoubleConverter extends Converter<Double, String> {

        @Override
        public String convertForward(Double s) {
            return String.valueOf(s);
        }

        @Override
        public Double convertReverse(String t) {
            double value;
            try {
                value = (t == null) ? 0 : Double.parseDouble(t);
            } catch (NumberFormatException e) {
                value = 0.0;
            }

            return value;
        }
    }

    /**
     * Validator class used for binding. Checks if the bound field is not empty
     */
    private class RequiredStringValidator extends Validator<String> {

        @Override
        public Result validate(String arg) {
            if ((arg == null) || (arg.length() == 0)) {
                return new Validator.Result(null, "Empty value: previous value will be restored.");
            }
            return null;
        }
    }
}
