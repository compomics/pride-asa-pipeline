/*
 *

 */
package com.compomics.pride_asa_pipeline.data.mapper;

import com.compomics.pride_asa_pipeline.logic.spectrum.decode.Base64DataDecoder;
import com.compomics.pride_asa_pipeline.logic.spectrum.decode.impl.Base64DataDecoderImpl;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author Niels Hulstaert
 */
public class Base64DecoderMapper implements RowMapper<double[]> {
    
    private Base64DataDecoder base64DataDecoder;

    public Base64DataDecoder getBase64DataDecoder() {
        return base64DataDecoder;
    }

    public void setBase64DataDecoder(Base64DataDecoder base64DataDecoder) {
        this.base64DataDecoder = base64DataDecoder;
    }
            
    public Base64DecoderMapper(){
        super();
        base64DataDecoder = new Base64DataDecoderImpl();
    }

    @Override
    public double[] mapRow(ResultSet rs, int i) throws SQLException {
        double[] decodedDataArray = base64DataDecoder.getDataAsArray(rs.getString("data_precision"), rs.getString("data_endian"), rs.getString("base_64_data"));
        return decodedDataArray;
    }
    
}
