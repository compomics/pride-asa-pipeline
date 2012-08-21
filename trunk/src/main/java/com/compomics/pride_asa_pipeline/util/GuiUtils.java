/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.util;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.math.BigDecimal;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 *
 * @author niels
 */
public class GuiUtils {

    private static final Integer NUMBER_OF_DECIMALS = 4;

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

    public static double roundDouble(double d) {
        BigDecimal bigDecimal = new BigDecimal(Double.toString(d));
        bigDecimal = bigDecimal.setScale(NUMBER_OF_DECIMALS, BigDecimal.ROUND_HALF_UP);

        return bigDecimal.doubleValue();
    }
}
