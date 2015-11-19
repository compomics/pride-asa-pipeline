package com.compomics.pride_asa_pipeline.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author Niels Hulstaert
 */
public class IOUtils {

    /**
     * Unzip and write to file.
     *
     * @param inputFile the input file
     * @param outputFile the output file
     *
     * @throws java.io.IOException
     */
    public static void unzip(File inputFile, File outputFile) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                GZIPInputStream gZIPInputStream = new GZIPInputStream(new FileInputStream(inputFile))) {
            //unzip
            //this method uses a buffer internally
            org.apache.commons.io.IOUtils.copy(gZIPInputStream, fileOutputStream);
        }
    }

}
