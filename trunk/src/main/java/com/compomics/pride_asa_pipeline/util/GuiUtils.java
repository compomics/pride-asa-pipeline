/*
 *

 */
package com.compomics.pride_asa_pipeline.util;

import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 *
 * @author Niels Hulstaert
 */
public class GuiUtils {

    /**
     * Centers the dialog on the parent frame.
     *
     * @param parentFrame the parent frame
     * @param dialog the dialog
     */
    public static void centerDialogOnFrame(JFrame parentFrame, JDialog dialog) {
        Point topLeft = parentFrame.getLocationOnScreen();
        Dimension parentSize = parentFrame.getSize();

        Dimension dialogSize = dialog.getSize();

        int x = 0;
        int y = 0;

        if (parentSize.width > dialogSize.width) {
            x = ((parentSize.width - dialogSize.width) / 2) + topLeft.x;
        } else {
            x = topLeft.x;
        }

        if (parentSize.height > dialogSize.height) {
            y = ((parentSize.height - dialogSize.height) / 2) + topLeft.y;
        } else {
            y = topLeft.y;
        }

        dialog.setLocation(x, y);
    }
}
