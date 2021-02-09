/*
 * Copyright 2016 GU.
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
package org.corpus_tools.pepperModules_CoNLLCorefModules;

import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;

/**
 *
 * @author Amir Zeldes
 */
public class CoNLLCorefExporterProperties extends PepperModuleProperties {
	

        public static final String PREFIX  = "ConllCoref.Exporter.";
    
        // Indicates the Salt Layer containing the nodes that are connected by edges for coref export
        public static final String PROP_NODE_LAYER = PREFIX + "NodeLayer";
        // Indicates the edge type name for coreference edges; may be a regular expression
        public static final String PROP_EDGE_TYPE = PREFIX + "EdgeType";
        // Supplies an annotation key-value pair that an edge must satisfy to be included; both may be a regular expression, but the separator must be '='
        public static final String PROP_EDGE_ANNO = PREFIX + "EdgeAnno";
        // Supplies an annotation key-value pair that an edge must satisfy to be included; both may be a regular expression, but the separator must be '='
        public static final String PROP_REM_SINGLETONS = PREFIX + "RemoveSingletons";
        // Supplies an annotation key that exported nodes have, whose annotation value will be added to the beginning of the bracketed output for each node
        public static final String PROP_NODE_ANNO_OUT = PREFIX + "OutputAnnotation";
        // Supplies an annotation key that exported nodes have, whose annotation value will be added at the end of the bracketed output for each node
        public static final String PROP_NODE_ANNO_OUT_SUFF = PREFIX + "OutputSuffixAnnotation";
	
	
	public CoNLLCorefExporterProperties(){
		this.addProperty(new PepperModuleProperty<String>(PROP_NODE_LAYER, String.class, "Indicates the Salt Layer containing the nodes that are connected by edges for coref export", "", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_EDGE_TYPE, String.class, "Indicates the edge type name for coreference edges; may be a regular expression", "", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_EDGE_ANNO, String.class, "Supplies an annotation key-value pair that an edge must satisfy to be included; both may be a regular expression, but the separator must be '='", "", false));
		this.addProperty(new PepperModuleProperty<Boolean>(PROP_REM_SINGLETONS, Boolean.class, "Whether to remove nodes that have no matching pointing relations, i.e. singletons", false, false));
		this.addProperty(new PepperModuleProperty<String>(PROP_NODE_ANNO_OUT, String.class, "Supplies an annotation key that exported nodes have, whose annotation value will be added to the beginning of the bracketed output for each node", "", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_NODE_ANNO_OUT_SUFF, String.class, "Supplies an annotation key that exported nodes have, whose annotation value will be added at the end of the bracketed output for each node", "", false));
	}
	
        public String getNodeLayer(){
            return getProperty(PROP_NODE_LAYER).getValue().toString();
        }
        public String getEdgeType(){
            return getProperty(PROP_EDGE_TYPE).getValue().toString();
        }
        public String getEdgeAnno(){
            return getProperty(PROP_EDGE_ANNO).getValue().toString();
        }
        public boolean getRemoveSingletons(){
            return ((Boolean) getProperty(PROP_REM_SINGLETONS).getValue());
        }
        public String getOutputAnno(){
            return getProperty(PROP_NODE_ANNO_OUT).getValue().toString();
        }
        public String getOutputSuffAnno(){
            return getProperty(PROP_NODE_ANNO_OUT_SUFF).getValue().toString();
        }
        
}
