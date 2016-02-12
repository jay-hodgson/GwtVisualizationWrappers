package org.gwtvisualizationwrappers.client.markdown;


import com.google.gwt.core.client.EntryPoint;

/**
 * Provides script injection for the markdown processor
 * 
 */
public class SynapseMarkdownProcessorEntryPoint implements EntryPoint {

    @Override
    public void onModuleLoad() {
    	SynapseMarkdownProcessor processor = SynapseMarkdownProcessor.getInstance();
    }
    
}
