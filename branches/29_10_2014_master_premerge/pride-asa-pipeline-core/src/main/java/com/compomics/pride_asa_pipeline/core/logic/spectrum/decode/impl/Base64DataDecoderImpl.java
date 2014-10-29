package com.compomics.pride_asa_pipeline.core.logic.spectrum.decode.impl;

import com.compomics.pride_asa_pipeline.core.logic.spectrum.decode.Base64DataDecoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.apache.commons.codec.binary.Base64;

/**
 * Created by IntelliJ IDEA. User: niels Date: 4/01/12 Time: 11:39 To change
 * this template use File | Settings | File Templates.
 */
public class Base64DataDecoderImpl implements Base64DataDecoder {

    /**
     * Defines the valid String indicating big endian byte order.
     */
    public static final String BIG_ENDIAN_LABEL = "big";
    /**
     * Defines the valid String indicating little endian byte order.
     */
    public static final String LITTLE_ENDIAN_LABEL = "little";
    /**
     * Defines the valid String indicating the correct precision for encoded
     * floats.
     */
    public static final String FLOAT_PRECISION = "32";
    /**
     * Defines the valid String indicating the correct precision for encoded
     * doubles.
     */
    public static final String DOUBLE_PRECISION = "64";
    /**
     * Defines the number of bytes required in an UNENCODED byte array to hold a
     * single float value.
     */
    public static final int BYTES_TO_HOLD_FLOAT = 4;
    /**
     * Defines the number of bytes required in an UNENCODED byte array to hold a
     * dingle double value.
     */
    public static final int BYTES_TO_HOLD_DOUBLE = 8;

    /**
     * Checks if all the necessary information is provided and then converts the
     * decoded binary array into an array of double values (that for example
     * could be used to draw a spectra).
     *
     * @return the decoded binary array converted into an array of double
     * values.
     */
    @Override
    public double[] getDataAsArray(String dataPrecision, String dataEndian, String base64DataString) {
        //Note the 'precision' value is constrained to be only "32" or "64".
        int step = getStep(dataPrecision);

        byte[] fullArray = this.getDecodedByteArray(base64DataString);
        if (fullArray == null || fullArray.length == 0) {
            //No data array set - return null.
            return null;
        }
        if (fullArray.length % step != 0) {
            throw new IllegalStateException("Error caused by attempting to split a byte array of length " + fullArray.length + " into pieces of length " + step);
        }

        double[] doubleArray = new double[fullArray.length / step];
        ByteBuffer bb = ByteBuffer.wrap(fullArray);
        // Set the order to BIG or LITTLE ENDIAN
        bb.order(getByteOrder(dataEndian));

        for (int indexOut = 0; indexOut < fullArray.length; indexOut += step) {
            /*
             * Note that the 'getFloat(index)' method gets the next 4 bytes and
             * the 'getDouble(index)' method gets the next 8 bytes.
             */
            doubleArray[indexOut / step] = (step == BYTES_TO_HOLD_FLOAT) ? (double) bb.getFloat(indexOut)
                    : bb.getDouble(indexOut);
        }

        return doubleArray;
    }

    /**
     * Returns the number of bytes used for each element in the (NON-encoded)
     * byte array depending on the precision of the data.
     *
     * @return an int value being either 4 (for storage of floats) or 8 (for
     * storage of doubles).
     */
    private int getStep(String dataPrecision) {
        if (FLOAT_PRECISION.equals(dataPrecision)) {
            return BYTES_TO_HOLD_FLOAT;
        } else if (DOUBLE_PRECISION.equals(dataPrecision)) {
            return BYTES_TO_HOLD_DOUBLE;
        } else {
            throw new IllegalStateException("The value for data precision for this binary array must be either 32 or 64.  In this case it is: " + dataPrecision);
        }
    }

    /**
     * <p>Returns the contents of the binary array <i>decoded</i> using the
     * Base64 algorithm.</p>
     *
     * @param base64DataString the base64 data string
     * @return the contents of the binary array <i>decoded</i> using the Base64
     * algorithm.
     */
    private byte[] getDecodedByteArray(String base64DataString) {
        if (base64DataString == null) {
            return null;
        } else {
            return Base64.decodeBase64(base64DataString);
        }
    }

    /**
     * Returns the appropriate ByteOrder object depending on whether big endian
     * or little endian byte order is being used.
     *
     * @return the appropriate ByteOrder object depending on whether big endian
     * or little endian byte order is being used.
     */
    private ByteOrder getByteOrder(String dataEndian) {
        if (BIG_ENDIAN_LABEL.equals(dataEndian)) {
            return ByteOrder.BIG_ENDIAN;
        } else if (LITTLE_ENDIAN_LABEL.equals(dataEndian)) {
            return ByteOrder.LITTLE_ENDIAN;
        } else {
            throw new IllegalStateException("The value for data endian for this binary array must be either 'big' or 'little'.  In this case it is: " + dataEndian);
        }
    }
}
