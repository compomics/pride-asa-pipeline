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
package com.compomics.pride_asa_pipeline.core.data.user;

import com.compomics.pride_asa_pipeline.core.model.modification.impl.AsapModificationAdapter;
import com.compomics.pride_asa_pipeline.core.model.modification.source.PRIDEModificationFactory;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.HashSet;
import java.util.Set;
import com.compomics.pride_asa_pipeline.core.gui.PipelineProgressMonitor;

/**
 *
 * @author Kenneth Verheggen
 */
public class UserSuggestedModifications {

    private static UserSuggestedModifications instance;
    /**
     * The modification adapter to return pride asap modificaitons
     */
    private final AsapModificationAdapter adapter = new AsapModificationAdapter();

    private Set<Modification> modifications = new HashSet<>();

    private UserSuggestedModifications() {

    }

    public static UserSuggestedModifications getInstance() {
        if (instance == null) {
            instance = new UserSuggestedModifications();
        }
        return instance;
    }

    public void addModification(String modificationName) {
        String capitalizedModificationName = modificationName.toUpperCase().charAt(0)+modificationName.substring(1,modificationName.length());
        Modification mod = (Modification) PRIDEModificationFactory.getInstance().getModification(adapter, capitalizedModificationName);
        if (mod == null) {
            PipelineProgressMonitor.error(modificationName + " was not found in the PRIDEModification Factory...");
        } else {
            modifications.add(mod);
        }
    }

    public Set<Modification> getAdditionalModifications() {
        return modifications;
    }

}
