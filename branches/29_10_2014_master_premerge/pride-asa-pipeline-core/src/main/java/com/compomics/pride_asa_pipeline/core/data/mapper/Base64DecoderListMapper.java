/*
 *

 */
package com.compomics.pride_asa_pipeline.core.data.mapper;

import com.compomics.pride_asa_pipeline.core.logic.spectrum.decode.Base64DataDecoder;
import com.compomics.pride_asa_pipeline.core.logic.spectrum.decode.impl.Base64DataDecoderImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Niels Hulstaert
 */
public class Base64DecoderListMapper implements RowMapper<Base64DecoderListMapper.PartialSpectrumContent> {

    private Base64DataDecoder base64DataDecoder;

    public Base64DataDecoder getBase64DataDecoder() {
        return base64DataDecoder;
    }

    public void setBase64DataDecoder(Base64DataDecoder base64DataDecoder) {
        this.base64DataDecoder = base64DataDecoder;
    }

    public Base64DecoderListMapper(){
        super();
        base64DataDecoder = new Base64DataDecoderImpl();
    }

    @Override
    public PartialSpectrumContent mapRow(ResultSet rs, int i) throws SQLException {
        long spectrumId = rs.getLong("spectrumid");
        double[] decodedDataArray = base64DataDecoder.getDataAsArray(rs.getString("data_precision"), rs.getString("data_endian"), rs.getString("base_64_data"));
        return new PartialSpectrumContent(decodedDataArray, spectrumId);
    }

    /**
     * Inner class wraps the spectrumId together with the double[] from either intensities or masses.
     */
    public class PartialSpectrumContent{
        long spectrumId;
        double[] decodedDataArray;

        private PartialSpectrumContent(double[] aDecodedDataArray, long aSpectrumId) {
            decodedDataArray = aDecodedDataArray;
            spectrumId = aSpectrumId;
        }

        public double[] getDecodedDataArray() {
            return decodedDataArray;
        }

        public long getSpectrumId() {
            return spectrumId;
        }
    }
    
}
