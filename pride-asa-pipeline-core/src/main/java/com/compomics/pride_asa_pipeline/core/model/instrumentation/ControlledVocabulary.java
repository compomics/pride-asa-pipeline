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
package com.compomics.pride_asa_pipeline.core.model.instrumentation;

/**
 *
 * @author Kenneth Verheggen
 */
public enum ControlledVocabulary {

    MALDI("MS:1000247"),
    ESI("MS:1000073"),
    CHARGE_STATE("PSI:1000041");

    private String term;

    private ControlledVocabulary(String term) {
        this.term = term;
    }

    public String getTerm() {
        return term.substring(term.indexOf(":")+1);
    }
    

}
