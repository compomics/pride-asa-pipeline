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

import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.compomics.pride_asa_pipeline.core.gui.PipelineProgressMonitor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 *
 * @author Niels Hulstaert
 */
public class IdentificationsExtractor implements ResultSetExtractor<List<Identification>> {

    @Override
    public List<Identification> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<Identification> identifications = new ArrayList<>();
        while (rs.next()) {
            try {
                //sequence
                String sequence = rs.getString("sequence");
                //charge state
                int chargeState = -1;
                if (rs.getString("charge_state") != null && !"".equals(rs.getString("charge_state"))) {
                    chargeState = rs.getInt("charge_state");
                }
                //mz ratio
                double mz = rs.getDouble("mz");
                //accession
                String accession = rs.getString("accession");
                //spectrum ID
                String spectrumId = rs.getString("spectrum_id");
                //spectrum ref
                String spectrumRef = rs.getString("spectrum_identifier");
                //peptide ID
                long peptideId = rs.getLong("peptide_id");

                Peptide peptide = new Peptide(chargeState, mz, new AminoAcidSequence(sequence), peptideId);
                
                identifications.add(new Identification(peptide, accession, spectrumId, spectrumRef));
            } catch (UnknownAAException ex) {
                PipelineProgressMonitor.info("Got peptide with unknown amino acid!");
            }
        }

        return identifications;
    }
}
