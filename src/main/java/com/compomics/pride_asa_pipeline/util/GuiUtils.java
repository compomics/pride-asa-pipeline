/*
 *

 */
package com.compomics.pride_asa_pipeline.util;

import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.Renderer;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;

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

    /**
     * Enable shadow of renderer? JFreeChart 1.0.11 changed the <b>default</b>
     * look by painting shadows for bars. To revert back to the old look, you
     * can disable the shadows with this method.
     *
     * @param chart JFreeChart.
     * @param state False, to disable shadow-
     * @since 4.1.0
     */
    public static void setShadowVisible(final JFreeChart chart, final boolean state) {
        if (chart != null) {
            final Plot p = chart.getPlot();
            if (p instanceof XYPlot) {
                final XYPlot xyplot = (XYPlot) p;
                final XYItemRenderer xyItemRenderer = xyplot.getRenderer();
                if (xyItemRenderer instanceof XYBarRenderer) {
                    final XYBarRenderer br = (XYBarRenderer) xyItemRenderer;
                    br.setBarPainter(new StandardXYBarPainter());
                    br.setShadowVisible(state);
                }
            } else if (p instanceof CategoryPlot) {
                final CategoryPlot categoryPlot = (CategoryPlot) p;
                final CategoryItemRenderer categoryItemRenderer = categoryPlot.getRenderer();
                if (categoryItemRenderer instanceof BarRenderer) {
                    final BarRenderer br = (BarRenderer) categoryItemRenderer;
                    br.setBarPainter(new StandardBarPainter());
                    br.setShadowVisible(state);
                }
            }
        }
    }
}
