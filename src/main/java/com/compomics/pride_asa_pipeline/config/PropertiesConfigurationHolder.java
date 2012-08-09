package com.compomics.pride_asa_pipeline.config;

import com.compomics.pride_asa_pipeline.util.FileUtils;
import java.io.File;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA. User: niels Date: 9/11/11 Time: 13:41 To change
 * this template use File | Settings | File Templates.
 */
public class PropertiesConfigurationHolder extends PropertiesConfiguration {

    private static final Logger LOGGER = Logger.getLogger(PropertiesConfigurationHolder.class);
    private static PropertiesConfigurationHolder ourInstance;

    static {
        try {
            File propertiesFile = FileUtils.getFileByRelativePath("resources/pride_asa_pipeline.properties");
            ourInstance = new PropertiesConfigurationHolder(propertiesFile);
        } catch (ConfigurationException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Gets the PropertiesConfiguration instance
     *
     * @return
     */
    public static PropertiesConfigurationHolder getInstance() {
        return ourInstance;
    }

    private PropertiesConfigurationHolder(File propertiesFile) throws ConfigurationException {
        super(propertiesFile);
    }
}
