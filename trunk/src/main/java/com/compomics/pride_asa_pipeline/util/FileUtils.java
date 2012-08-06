/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.util;

import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 *
 * @author niels
 */
public class FileUtils {

    private static final Logger LOGGER = Logger.getLogger(FileUtils.class);

    /**
     * Gets a file by its relative path. If the file is not found, the classpath
     * is searched. If nothing is found, null is returned.
     *
     * @param relativePath the relative path of the file
     * @return the found file
     */
    public static File getFileByRelativePath(String relativePath) {
        //create file
        File file = new File(relativePath);

        if (!file.exists()) {
            //try to find it on the classpath
            Resource resource = new ClassPathResource(relativePath);
            try {
                file = resource.getFile();
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }

            if (!file.exists()) {
                file = null;
            }
        }

        return file;
    }
    
    /**
     * Checks if a file with the given relative path exists.
     * 
     * @param relativePath the relative path of the file
     * @return the is existing boolean
     */
    public static boolean isExistingFile(String relativePath){
        boolean isExistingFile = Boolean.FALSE;
        
        File file = new File(relativePath);
        if(file.exists()){
            isExistingFile = Boolean.TRUE;
        }
        
        return isExistingFile;
    }
}
