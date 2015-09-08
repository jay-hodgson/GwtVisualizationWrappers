package org.gwtvisualizationwrappers.client.cytoscape;


import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

/**
 * @author Jay Hodgson
 */
public interface CytoscapeJsClientBundle extends ClientBundle {

    static final CytoscapeJsClientBundle INSTANCE = GWT.create(CytoscapeJsClientBundle.class);

    @Source("resource/js/2.4.2-cytoscape.closurecompiled.js")
    TextResource cytoscape2_4_2();

    @Source("resource/js/jquery-1.11.2.min.cache.js")
    TextResource jQuery();
    
    @Source("resource/js/sage-cytoscape-utils.js")
    TextResource sageCytoscapeUtils();
}
