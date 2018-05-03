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
