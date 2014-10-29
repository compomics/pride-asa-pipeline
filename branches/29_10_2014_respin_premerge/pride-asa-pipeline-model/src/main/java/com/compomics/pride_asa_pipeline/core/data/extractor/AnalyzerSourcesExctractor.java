/*
 */
package com.compomics.pride_asa_pipeline.core.data.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 *
 * @author Niels Hulstaert
 */
public class AnalyzerSourcesExctractor implements ResultSetExtractor<Map<String, String>> {

    @Override
    public Map<String, String> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, String> analyzerSourceMap = new HashMap<String, String>();
        while (rs.next()) {
            analyzerSourceMap.put(rs.getString("accession"), rs.getString("value"));
        }
        return analyzerSourceMap;
    }
}
