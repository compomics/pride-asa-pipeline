/*
 *

 */
package com.compomics.pride_asa_pipeline.spring;

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
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    public void setDefaultApplicationContext(){
        this.applicationContext = new ClassPathXmlApplicationContext("springXMLConfig.xml");
    }
}
