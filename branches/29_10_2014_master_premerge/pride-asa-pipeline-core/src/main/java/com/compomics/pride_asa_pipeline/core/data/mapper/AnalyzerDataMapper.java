/*
 *

 */
package com.compomics.pride_asa_pipeline.core.data.mapper;

import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author Niels Hulstaert
 */
public class AnalyzerDataMapper implements RowMapper<AnalyzerData> {

    private static final Logger LOGGER = Logger.getLogger(AnalyzerDataMapper.class);

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
