/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.pride_asa_pipeline.core.logic.modification.PTMMapper;
import java.io.IOException;
import java.util.ArrayList;
import org.geneontology.oboedit.dataadapter.GOBOParseException;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Kenneth
 */
public class OxidationProblem {

    public static void main(String[] args) throws XmlPullParserException, IOException, GOBOParseException {
        PTMMapper prideToPtmMapper = PTMMapper.getInstance();
        ArrayList<String> modNames = prideToPtmMapper.lookupRealModNames("Oxidation");
    }
}
