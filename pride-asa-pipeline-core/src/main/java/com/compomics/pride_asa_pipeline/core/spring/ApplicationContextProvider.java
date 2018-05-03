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
package com.compomics.pride_asa_pipeline.core.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author Niels Hulstaert
 */
public class ApplicationContextProvider {

    private ApplicationContext applicationContext;
    private static final ApplicationContextProvider APPLICATION_CONTEXT_PROVIDER = new ApplicationContextProvider();

    //private no-arg constructor
    private ApplicationContextProvider() {
    }

    public synchronized static ApplicationContextProvider getInstance() throws ExceptionInInitializerError {
        return APPLICATION_CONTEXT_PROVIDER;
    }

    public ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            throw new IllegalStateException("The application context is not set yet.");
        }
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setDefaultApplicationContext() {
        this.applicationContext = new ClassPathXmlApplicationContext("springXMLConfig.xml");
    }
    
    public <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }
}
