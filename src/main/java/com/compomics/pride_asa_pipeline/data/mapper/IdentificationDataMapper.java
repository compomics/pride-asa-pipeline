/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.data.mapper;

import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author Niels Hulstaert
 */
public class IdentificationDataMapper implements RowMapper<Identification> {

    private static final Logger LOGGER = Logger.getLogger(IdentificationDataMapper.class);

    @Override
    public Identification mapRow(ResultSet rs, int i) throws SQLException {
        Identification identificationData = null;

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
        long spectrumId = rs.getLong("spectrum_id");
        //spectrum ref
        long spectrumRef = rs.getLong("spectrum_identifier");
        //peptide ID
        long peptideId = rs.getLong("peptide_id");

        Peptide peptide = null;
        try {
            peptide = new Peptide(chargeState, mz, new AminoAcidSequence(sequence), peptideId);
        } catch (UnknownAAException ex) {
            LOGGER.info("Got peptide with unknown amino acid!");
        }

        return new Identification(peptide, accession, spectrumId, spectrumRef);
    }
}
