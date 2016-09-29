package com.compomics.pride_asa_pipeline.core.logic;


import com.compomics.pride_asa_pipeline.core.logic.AbstractSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.core.logic.modification.InputType;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.io.File;
import java.util.Set;
import org.springframework.core.io.Resource;


/**
 *
 * @author Niels Hulstaert
 */
/**
 *
 * @author Kenneth Verheggen
 */
public class FileSpectrumAnnotator extends AbstractSpectrumAnnotator<File> {

    @Override
    public void initIdentifications(File t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Modification> initModifications(Resource modificationsResource, InputType inputType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clearPipeline() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clearTmpResources() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}