package com.compomics.pride_asa_pipeline.core.logic.spectrum.decode;

/**
 * Created by IntelliJ IDEA.
 * User: niels
 * Date: 4/01/12
 * Time: 11:38
 * To change this template use File | Settings | File Templates.
 */
public interface Base64DataDecoder {
    
    /**
     * Gets the binary data as a double array
     * 
     * @param dataPrecision precision for encoded doubles
     * @param dataEndian data type, big and little endian
     * @param base64DataString the base 64 encoded string 
     * @return the data array
     */
    double[] getDataAsArray(String dataPrecision, String dataEndian, String base64DataString);

}
