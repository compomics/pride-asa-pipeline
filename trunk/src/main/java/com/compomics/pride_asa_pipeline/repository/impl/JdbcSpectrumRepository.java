/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.repository.impl;

import com.compomics.pride_asa_pipeline.data.mapper.Base64DecoderListMapper;
import com.compomics.pride_asa_pipeline.data.mapper.Base64DecoderMapper;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.repository.SpectrumRepository;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author niels
 */
public class JdbcSpectrumRepository extends JdbcDaoSupport implements SpectrumRepository {

    private static final Logger LOGGER = Logger.getLogger(JdbcSpectrumRepository.class);

    private static final String SELECT_SPECTRUM_MZVALUES_BY_SPECTRUM_ID = new StringBuilder().append("select binary_array.data_precision as data_precision, binary_array.data_endian as data_endian, base_64.base_64_data as base_64_data from ").append("mzdata_spectrum spec, mzdata_binary_array binary_array, mzdata_base_64_data base_64 ").append("where spec.mz_array_binary_id = binary_array.binary_array_id ").append("and binary_array.binary_array_id = base_64.binary_array_id ").append("and spec.spectrum_id = ?").toString();
    private static final String SELECT_SPECTRUM_INTENSITIES_BY_SPECTRUM_ID = new StringBuilder().append("select binary_array.data_precision as data_precision, binary_array.data_endian as data_endian, base_64.base_64_data as base_64_data from ").append("mzdata_spectrum spec, mzdata_binary_array binary_array, mzdata_base_64_data base_64 ").append("where spec.inten_array_binary_id = binary_array.binary_array_id ").append("and binary_array.binary_array_id = base_64.binary_array_id ").append("and spec.spectrum_id = ?").toString();

    private static final String SELECT_SPECTRUM_MZVALUES_BY_SPECTRUM_ID_LIST = new StringBuilder().append("select spec.spectrum_id as spectrumid, binary_array.data_precision as data_precision, binary_array.data_endian as data_endian, base_64.base_64_data as base_64_data from ").append("mzdata_spectrum spec, mzdata_binary_array binary_array, mzdata_base_64_data base_64 ").append("where spec.mz_array_binary_id = binary_array.binary_array_id ").append("and binary_array.binary_array_id = base_64.binary_array_id ").append("and spec.spectrum_id IN (%s)").toString();
    private static final String SELECT_SPECTRUM_INTENSITIES_BY_SPECTRUM_ID_LIST = new StringBuilder().append("select spec.spectrum_id as spectrumid, binary_array.data_precision as data_precision, binary_array.data_endian as data_endian, base_64.base_64_data as base_64_data from ").append("mzdata_spectrum spec, mzdata_binary_array binary_array, mzdata_base_64_data base_64 ").append("where spec.inten_array_binary_id = binary_array.binary_array_id ").append("and binary_array.binary_array_id = base_64.binary_array_id ").append("and spec.spectrum_id IN (%s)").toString();

    @Override
    public double[] getMzValuesBySpectrumId(long spectrumId) {
        LOGGER.debug("Loading mz values for spectrum " + spectrumId);
        double[] mzValues = getJdbcTemplate().queryForObject(SELECT_SPECTRUM_MZVALUES_BY_SPECTRUM_ID, new Base64DecoderMapper(), spectrumId);
        return mzValues;
    }

    @Override
    public double[] getIntensitiesBySpectrumId(long spectrumId) {
        LOGGER.debug("Loading intensities for spectrum " + spectrumId);
        double[] intensities = getJdbcTemplate().queryForObject(SELECT_SPECTRUM_INTENSITIES_BY_SPECTRUM_ID, new Base64DecoderMapper(), spectrumId);
        return intensities;
    }

    @Override
    public Map<Long, List<Peak>> getPeakMapsBySpectrumIdList(List<Long> spectrumIds) {
        LOGGER.debug("Loading peaks for spectrum list with size " + spectrumIds.size());

        String spectrumIdString = Joiner.on(",").join(spectrumIds);

        List<Base64DecoderListMapper.PartialSpectrumContent> lIntensitiesRS = getJdbcTemplate().query(String.format(SELECT_SPECTRUM_INTENSITIES_BY_SPECTRUM_ID_LIST, spectrumIdString), new Base64DecoderListMapper());
        Map<Long, double[]> lIntensityMap = Maps.newHashMap();
        for (Base64DecoderListMapper.PartialSpectrumContent lIntensity : lIntensitiesRS) {
            lIntensityMap.put(lIntensity.getSpectrumId(), lIntensity.getDecodedDataArray());
        }

        List<Base64DecoderListMapper.PartialSpectrumContent> lMassesRS = getJdbcTemplate().query(String.format(SELECT_SPECTRUM_MZVALUES_BY_SPECTRUM_ID_LIST, spectrumIdString), new Base64DecoderListMapper());
        Map<Long, double[]> lMZMap = Maps.newHashMap();
        for (Base64DecoderListMapper.PartialSpectrumContent lMass : lMassesRS) {
            lMZMap.put(lMass.getSpectrumId(), lMass.getDecodedDataArray());
        }

        HashMap<Long, List<Peak>> result = Maps.newHashMap();

        for (Long spectrumId : spectrumIds) {
            double[] intensities = lIntensityMap.get(spectrumId);
            double[] mzs = lMZMap.get(spectrumId);

            List<Peak> lPeaks = Lists.newArrayList();
            for (int i = 0; i < intensities.length; i++) {
                double lMz = mzs[i];
                double lIntensity = intensities[i];
                lPeaks.add(new Peak(lMz, lIntensity));
            }

            result.put(spectrumId, lPeaks);
        }

        return result;
    }

}
