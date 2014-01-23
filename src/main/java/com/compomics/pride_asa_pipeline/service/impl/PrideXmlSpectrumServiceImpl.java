/*
 *

 */
package com.compomics.pride_asa_pipeline.service.impl;

import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.repository.PrideXmlParser;
import com.compomics.pride_asa_pipeline.service.PrideXmlSpectrumService;
import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Niels Hulstaert
 */
public class PrideXmlSpectrumServiceImpl implements PrideXmlSpectrumService {

    private static final Logger LOGGER = Logger.getLogger(PrideXmlSpectrumServiceImpl.class);
    private PrideXmlParser prideXmlParser;        

    public PrideXmlParser getPrideXmlParser() {
        return prideXmlParser;
    }

    @Override
    public void setPrideXmlParser(PrideXmlParser prideXmlParser) {
        this.prideXmlParser = prideXmlParser;
    }        

    @Override
    public List<Peak> getSpectrumPeaksBySpectrumId(String spectrumId) {
        return prideXmlParser.getSpectrumPeaksBySpectrumId(spectrumId);
    }

    @Override
    public HashMap<Double, Double> getSpectrumPeakMapBySpectrumId(String spectrumId) {
        return prideXmlParser.getSpectrumPeakMapBySpectrumId(spectrumId);
    }
}
