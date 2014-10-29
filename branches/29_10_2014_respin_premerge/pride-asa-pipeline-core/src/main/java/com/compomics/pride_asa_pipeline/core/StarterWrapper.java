package com.compomics.pride_asa_pipeline.core;

import com.compomics.software.CompomicsWrapper;
import java.io.File;
import java.net.URISyntaxException;
import org.apache.log4j.Logger;

/**
 *
 * @author Niels Hulstaert
 */
public class StarterWrapper extends CompomicsWrapper {

    private static Logger logger = Logger.getLogger(StarterWrapper.class);

    /**
     * Starts the launcher by calling the launch method. Use this as the main
     * class in the jar file.
     *
     * @param args
     */
    public StarterWrapper(String[] args) {
        try {
            File jarFile = new File(StarterWrapper.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            String mainClass = "com.compomics.pride_asa_pipeline.PrideAsaPipelineStarter";
            launchTool("Pride ASA Pipeline", jarFile, null, mainClass, args);
        } catch (URISyntaxException ex) {
            logger.error(ex);
        }

    }

    public static void main(String[] args) {
        new StarterWrapper(args);
    }
}
