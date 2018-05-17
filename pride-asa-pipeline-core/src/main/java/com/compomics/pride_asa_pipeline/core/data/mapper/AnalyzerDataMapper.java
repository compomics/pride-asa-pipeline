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

import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author Niels Hulstaert
 */
public class AnalyzerDataMapper implements RowMapper<AnalyzerData> {

  
    @Override
    public AnalyzerData mapRow(ResultSet rs, int i) throws SQLException {
        AnalyzerData analyzerData = null;
        String analyzerType = "";
                       
        //check to see if annotated with PSI:1000010. If so, use value
        if ("AnalyzerType".equals(rs.getString("name")) || "Analyzer Type".equals(rs.getString("name"))) {
            analyzerType = rs.getString("value");
        } else {
            //otherwise use name of param
            analyzerType = rs.getString("name");
        }
        //get analyzer data
        analyzerData = AnalyzerData.getAnalyzerDataByAnalyzerType(analyzerType);

        return analyzerData;
    }    
}
