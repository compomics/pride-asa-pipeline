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
    private static final ApplicationContextProvider provider = new ApplicationContextProvider();

    private ApplicationContextProvider() throws ExceptionInInitializerError {
        try {
            //load main application context.
            //Gui beans are not loaded and are located in "guiSpringXMLConfig.xml"
            this.applicationContext = new ClassPathXmlApplicationContext("springXMLConfig.xml");
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public synchronized static ApplicationContextProvider getInstance() throws ExceptionInInitializerError {
        return provider;
    }
    
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}