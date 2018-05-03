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
package com.compomics.pride_asa_pipeline.core.service.impl;

import com.compomics.pride_asa_pipeline.core.repository.ExperimentRepository;
import com.compomics.pride_asa_pipeline.core.service.ExperimentService;
import com.compomics.pride_asa_pipeline.core.service.ResultHandler;
import java.util.*;
import org.apache.log4j.Logger;

/**
 * @author Niels Hulstaert
 */
public abstract class ExperimentServiceImpl implements ExperimentService {

    private static final Logger LOGGER = Logger.getLogger(ExperimentServiceImpl.class);
    protected static final String MALDI_SOURCE_ACCESSION = "PSI:1000075";
    protected ExperimentRepository experimentRepository;
    protected ResultHandler resultHandler;

    public ExperimentRepository getExperimentRepository() {
        return experimentRepository;
    }

    public void setExperimentRepository(ExperimentRepository experimentRepository) {
        this.experimentRepository = experimentRepository;
    }

    public ResultHandler getResultHandler() {
        return resultHandler;
    }

    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }        

    @Override
    public Map<String, String> findAllExperimentAccessions() {
        return experimentRepository.findAllExperimentAccessions();
    }

    @Override
    public Map<String, String> findExperimentAccessionsByTaxonomy(int taxonomyId) {
        return experimentRepository.findExperimentAccessionsByTaxonomy(taxonomyId);
    }        

}
