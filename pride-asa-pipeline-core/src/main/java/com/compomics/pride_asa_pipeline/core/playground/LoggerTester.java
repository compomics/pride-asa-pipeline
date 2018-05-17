/*
 * Copyright 2018 davy.
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
package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.pride_asa_pipeline.core.gui.PipelineProgressMonitor;

;

/**
 *
 * @author davy
 */
public class LoggerTester {

    public static void main(String[] args) {
        PipelineProgressMonitor.error("TESTER");
        PipelineProgressMonitor.info("TESTER");
        PipelineProgressMonitor.debug("TESTER");
    }
}
