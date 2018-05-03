/* 
 * Copyright 2018 compomics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.compomics.pride_asa_pipeline.core.config;

import com.compomics.pride_asa_pipeline.core.util.ResourceUtils;
import java.io.IOException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

/**
 * Created by IntelliJ IDEA. User: niels Date: 9/11/11 Time: 13:41 To change
 * this template use File | Settings | File Templates.
 */
public class PropertiesConfigurationHolder extends PropertiesConfiguration {

    private static final Logger LOGGER = Logger.getLogger(PropertiesConfigurationHolder.class);
    private static PropertiesConfigurationHolder ourInstance;

    static {
        try {
            Resource propertiesResource = ResourceUtils.getResourceByRelativePath("resources/pride-asa-pipeline-core.properties");
            ourInstance = new PropertiesConfigurationHolder(propertiesResource);
            
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (ConfigurationException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Gets the PropertiesConfiguration instance
     *
     * @return the PropertiesConfigurationHolder instance
     */
    public static PropertiesConfigurationHolder getInstance() {
        return ourInstance;
    }

    private PropertiesConfigurationHolder(Resource propertiesResource) throws ConfigurationException, IOException {
        super(propertiesResource.getURL());
    }
}
