/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.modification;

import com.compomics.pride_asa_pipeline.model.Modification;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;
import org.jdom2.JDOMException;

/**
 *
 * @author niels
 */
public interface ModificationMarshaller {

    /**
     * Unmarchalls the given modifications XML file and returns a set of
     * modifications
     *
     * @param modificationsFile the modifications XML file to be parsed
     * @return the set of modifications
     * @exception JDOMException
     */
    Set<Modification> unmarshall(File modificationsFile) throws JDOMException;

    /**
     * Marshalls the collection of modifications to the modifications.xml file.
     *
     * @param modificationsFile the modifications file
     * @param modifications the modifications collection
     * @return the modifications output stream
     */
    void marshall(File modificationsFile, Collection<Modification> modifications);
}
