/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.repository.impl;

import com.compomics.pride_asa_pipeline.data.mapper.Base64DecoderMapper;
import com.compomics.pride_asa_pipeline.repository.SpectrumRepository;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 *
 * @author niels
 */
public class JdbcSpectrumRepository extends JdbcDaoSupport implements SpectrumRepository {
    
    private static final Logger LOGGER = Logger.getLogger(JdbcModificationRepository.class);
    
    private static final String SELECT_SPECTRUM_MZVALUES_BY_SPECTRUM_ID = new StringBuilder().append("select binary_array.data_precision as data_precision, binary_array.data_endian as data_endian, base_64.base_64_data as base_64_data from ").append("mzdata_spectrum spec, mzdata_binary_array binary_array, mzdata_base_64_data base_64 ").append("where spec.mz_array_binary_id = binary_array.binary_array_id ").append("and binary_array.binary_array_id = base_64.binary_array_id ").append("and spec.spectrum_id = ?").toString();
    private static final String SELECT_SPECTRUM_INTENSITIES_BY_SPECTRUM_ID = new StringBuilder().append("select binary_array.data_precision as data_precision, binary_array.data_endian as data_endian, base_64.base_64_data as base_64_data from ").append("mzdata_spectrum spec, mzdata_binary_array binary_array, mzdata_base_64_data base_64 ").append("where spec.inten_array_binary_id = binary_array.binary_array_id ").append("and binary_array.binary_array_id = base_64.binary_array_id ").append("and spec.spectrum_id = ?").toString();    

    @Override
    public double[] getMzValuesBySpectrumId(long spectrumId) {
        //LOGGER.debug("Loading mz values for spectrum " + spectrumId);
        double[] mzValues = getJdbcTemplate().queryForObject(SELECT_SPECTRUM_MZVALUES_BY_SPECTRUM_ID, new Base64DecoderMapper(), spectrumId);         
        return mzValues;
    }

    @Override
    public double[] getIntensitiesBySpectrumId(long spectrumId) {
        //LOGGER.debug("Loading intensities for spectrum " + spectrumId);
        double[] intensities = getJdbcTemplate().queryForObject(SELECT_SPECTRUM_INTENSITIES_BY_SPECTRUM_ID, new Base64DecoderMapper(), spectrumId);        
        return intensities;
    }
}
