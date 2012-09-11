/*
 */
package com.compomics.pride_asa_pipeline.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JToolBar;

/**
 *
 * @author niels
 */
public class GradientToolbar extends JToolBar {

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        //to get height and width of the component
        int w = getWidth();
        int h = getHeight();

        //generating two colors for gradient pattern
        /*parameters are consentration of Red, Blue and Green color in HEX  format*/
        Color color1 = new Color(2, 102, 204);
        Color color2 = new Color(255, 255, 255);

        /*generating gradient pattern from two colors*/
        GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
        g2d.setPaint(gp); //set gradient color to graphics2D object
        g2d.fillRect(0, 0, w, h); //filling color
        setOpaque(false);

        super.paintComponent(g);

        setOpaque(true);
    }
}
