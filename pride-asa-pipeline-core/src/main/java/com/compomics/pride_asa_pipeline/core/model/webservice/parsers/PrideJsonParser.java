/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.model.webservice.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Kenneth
 */
public abstract class PrideJsonParser {

    protected JSONObject getObjectFromURL(URL url) throws IOException, ParseException {
        JSONObject parsedObject;
        try ( //make sure the stream gets closed !!!!
                InputStream in = url.openStream(); InputStreamReader inReader = new InputStreamReader(in)) {
            JSONParser jsonParser = new JSONParser();
            parsedObject = (JSONObject) jsonParser.parse(inReader);
        }
        return parsedObject;
    }

}
