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
package com.compomics.pride_asa_pipeline.core.data.mapper;

import com.compomics.pride_asa_pipeline.core.logic.spectrum.decode.Base64DataDecoder;
import com.compomics.pride_asa_pipeline.core.logic.spectrum.decode.impl.Base64DataDecoderImpl;
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
