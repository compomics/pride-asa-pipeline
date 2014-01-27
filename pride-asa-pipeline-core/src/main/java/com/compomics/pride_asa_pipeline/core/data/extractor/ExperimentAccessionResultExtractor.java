/*
 *

 */
package com.compomics.pride_asa_pipeline.core.data.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 *
 * @author Niels Hulstaert Hulstaert
 */
public class ExperimentAccessionResultExtractor implements ResultSetExtractor<Map<String, String>> {

    @Override
    public Map<String, String> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, String> experimentAccessions = new LinkedHashMap<String, String>();
        while (rs.next()) {
            experimentAccessions.put(rs.getString("accession"), rs.getString("title"));
        }
        return experimentAccessions;
    }
}
