/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.util.pride.PrideWebService;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class PWSTester {
    public static void main(String[]args){
        try {
            PrideWebService.getAssayDetail("test");
        } catch (IOException ex) {
            Logger.getLogger(PWSTester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
