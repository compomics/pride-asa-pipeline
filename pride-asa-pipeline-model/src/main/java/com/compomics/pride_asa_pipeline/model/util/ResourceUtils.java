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
package com.compomics.pride_asa_pipeline.model.util;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 *
 * @author Niels Hulstaert
 */
public class ResourceUtils {

    private static final Logger LOGGER = Logger.getLogger(ResourceUtils.class);

    /**
     * Gets a resource by its relative path. If the resource is not found on the
     * file system, the classpath is searched. If nothing is found, null is
     * returned.
     *
     * @param relativePath the relative path of the resource
     * @return the found resource
     */
    public static Resource getResourceByRelativePath(String relativePath) {       
        Resource resource = new FileSystemResource(relativePath);

        if (!resource.exists()) {
            //try to find it on the classpath
            resource = new ClassPathResource(relativePath);

            if (!resource.exists()) {
                resource = null;
            }
        }

        return resource;
    }

    /**
     * Checks if a resource with the given relative path exists on the file
     * system.
     *
     * @param relativePath the relative path of the resource
     * @return the is existing boolean
     */
    public static boolean isExistingFile(String relativePath) {
        boolean isExistingResource = Boolean.FALSE;

        Resource resource = new FileSystemResource(relativePath);
        if (resource.exists()) {
            isExistingResource = true;
        }

        return isExistingResource;
    }
}
