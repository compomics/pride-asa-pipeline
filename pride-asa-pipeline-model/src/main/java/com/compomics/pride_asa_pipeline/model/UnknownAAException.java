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
package com.compomics.pride_asa_pipeline.model;

/**
 * @author Florian Reisinger
 *         Date: 20-Aug-2009
 * @since 0.1
 */
public class UnknownAAException extends Exception {

    public UnknownAAException() {
    }

    public UnknownAAException(String msg) {
        super(msg);
    }

    public UnknownAAException(String msg, Throwable t) {
        super(t);
    }

    public UnknownAAException(Throwable t) {
        super(t);
    }
}
