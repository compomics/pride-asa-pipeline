/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.playground.util;

import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class Glyconic {

    private static TreeMap<Double, Spectrum> chronologicalMS1Spectra = new TreeMap<>();
    private static TreeMap<Double, Spectrum> chronologicalMS2Spectra = new TreeMap<>();

    public static void main(String[] args) throws Exception {
        File inputFile = new File("C:\\Users\\compomics\\Documents\\Glyconic_Files\\151029_ly_CD147his_1pmol_complex2.mgf");
        System.out.println("LOADING FILE");
        SpectrumFactory.getInstance().addSpectra(inputFile);
        ArrayList<String> spectrumTitles = SpectrumFactory.getInstance().getSpectrumTitles(inputFile.getName());
        for (String aSpectrumTitle : spectrumTitles) {
          
            MSnSpectrum spectrum = (MSnSpectrum) SpectrumFactory.getInstance().getSpectrum(inputFile.getName(), aSpectrumTitle);
          System.out.println(spectrum.getLevel());
            if (spectrum.getLevel() == 1) {
                chronologicalMS1Spectra.put(spectrum.getPrecursor().getRt(), spectrum);
            } else if (spectrum.getLevel() == 2) {
                chronologicalMS2Spectra.put(spectrum.getPrecursor().getRt(), spectrum);
            }
        }
        print();
    }

    private static void print() {
        System.out.println("MS 1 SPECTRA : ");
        for (Entry<Double, Spectrum> entry : chronologicalMS1Spectra.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue().getSpectrumTitle());
        }
        System.out.println("-----------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------");
        System.out.println("MS 2 SPECTRA : ");
        for (Entry<Double, Spectrum> entry : chronologicalMS2Spectra.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue().getSpectrumTitle());
        }
        System.out.println("-----------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------");
    }

}
