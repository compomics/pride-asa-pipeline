/*
 *

 */
package com.compomics.pride_asa_pipeline.core.gui.controller;

import com.compomics.pride_asa_pipeline.core.gui.view.SystematicMassErrorsDialog;
import com.compomics.pride_asa_pipeline.core.model.MassRecalibrationResult;
import com.compomics.pride_asa_pipeline.core.util.GuiUtils;
import com.compomics.pride_asa_pipeline.core.util.MathUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Niels Hulstaert
 */
public class SystematicMassErrorsController extends WindowAdapter {

    //view
    private SystematicMassErrorsDialog pipelineProceedDialog;
    //parent controller
    private ExperimentSelectionController experimentSelectionController;

    public ExperimentSelectionController getExperimentSelectionController() {
        return experimentSelectionController;
    }

    public void setExperimentSelectionController(ExperimentSelectionController experimentSelectionController) {
        this.experimentSelectionController = experimentSelectionController;
    }

    public void showDialog(String infoMessage, MassRecalibrationResult massRecalibrationResult) {
        pipelineProceedDialog.getInfoMessageLabel().setText(infoMessage);
        pipelineProceedDialog.getMassRecalibrationResultTable().setModel(new MassRecalibrationResultTableModel(massRecalibrationResult));
        GuiUtils.centerDialogOnFrame(experimentSelectionController.getMainController().getMainFrame(), pipelineProceedDialog);
        pipelineProceedDialog.setVisible(true);
    }

    public void init() {
        pipelineProceedDialog = new SystematicMassErrorsDialog(experimentSelectionController.getMainController().getMainFrame());
        pipelineProceedDialog.addWindowListener(this);

        //add action listeners
        pipelineProceedDialog.getCancelButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pipelineProceedDialog.setVisible(Boolean.FALSE);
                experimentSelectionController.onAnnotationCanceled();
            }
        });

        pipelineProceedDialog.getProceedButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pipelineProceedDialog.setVisible(Boolean.FALSE);
                experimentSelectionController.onIdentificationsLoaded();
            }
        });

    }

    @Override
    public void windowClosed(WindowEvent e) {
        super.windowClosed(e);
        experimentSelectionController.onAnnotationCanceled();
    }

    private class MassRecalibrationResultTableModel extends AbstractTableModel {

        private String[] columnNames = {"Charge", "Systemic mass error", "Mass error window"};
        private Object[][] data;

        public MassRecalibrationResultTableModel(MassRecalibrationResult massRecalibrationResult) {
            data = new Object[massRecalibrationResult.getCharges().size()][3];
            for (Integer i : massRecalibrationResult.getCharges()) {
                data[i - 1][0] = i;
                data[i - 1][1] = MathUtils.roundDouble(massRecalibrationResult.getError(i));
                data[i - 1][2] = MathUtils.roundDouble(massRecalibrationResult.getErrorWindow(i));
            }
        }

        @Override
        public int getRowCount() {
            return data.length;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data[rowIndex][columnIndex];
        }
    }
}
