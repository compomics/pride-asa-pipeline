/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.gui.controller;

import com.compomics.pride_asa_pipeline.gui.view.PipelineProceedDialog;
import com.compomics.pride_asa_pipeline.model.MassRecalibrationResult;
import com.compomics.pride_asa_pipeline.util.GuiUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author niels
 */
public class PipelineProceedController extends WindowAdapter {

    //view
    private PipelineProceedDialog pipelineProceedDialog;
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
        pipelineProceedDialog.setVisible(Boolean.TRUE);
    }

    public void init() {
        pipelineProceedDialog = new PipelineProceedDialog(experimentSelectionController.getMainController().getMainFrame());
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
                experimentSelectionController.onAnnotationProceed();
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
                data[i - 1][1] = GuiUtils.roundDouble(massRecalibrationResult.getError(i));
                data[i - 1][2] = GuiUtils.roundDouble(massRecalibrationResult.getErrorWindow(i));
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
