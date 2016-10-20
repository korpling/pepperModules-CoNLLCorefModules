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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleDataException;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Amir Zeldes
 */
public class Salt2CoNLLCorefMapper extends PepperMapperImpl {
	
	private static final String ERR_MSG_NO_DOCUMENT = "No document to convert.";
	private static final String ERR_MSG_EMPTY_DOCUMENT = "Document is empty.";
	
	/*properties*/
	private String nodeLayer = "";
	private String edgeTypePattern = "";
	private String edgeAnnoNamePattern = "";
	private String edgeAnnoValuePattern = "";
        private boolean removeSingletons = false;

        /*track nodes identifiers*/
        private HashMap<SNode,String> nodesToGroups = null;  // holds temporary, non-sequential groups numbers
        private HashMap<String,List<SNode>> groupsToNodes = null;
        private HashMap<SToken,List<SNode>> startMap = null;
        private HashMap<SToken,List<SNode>> endMap = null;
        private int groupCounter;
        private int maxSeqID;
        private HashMap<String,String> groupsToSeqIDs = null; // holds sequential group numbers for output
        
        
	private SDocumentGraph docGraph = null;
        private static final Logger logger = LoggerFactory.getLogger(Salt2CoNLLCorefMapper.class);
	
        public StringBuilder stbOutput = new StringBuilder();        
        
	@Override
	public DOCUMENT_STATUS mapSDocument(){		
		if (getDocument()==null){
			throw new PepperModuleDataException(this, ERR_MSG_NO_DOCUMENT);
		}
		docGraph = getDocument().getDocumentGraph();
		if (docGraph==null){
			throw new PepperModuleDataException(this, ERR_MSG_EMPTY_DOCUMENT);
		}
		
		readProperties();

                this.nodesToGroups = new HashMap();
                this.groupsToSeqIDs = new HashMap();
                this.groupsToNodes = new HashMap();
                this.startMap = new HashMap();
                this.endMap = new HashMap();
                
                // Collect all pointing relations in targeted layer
                List<SPointingRelation> pointingRels;
                pointingRels = docGraph.getPointingRelations();

                for (SPointingRelation rel : pointingRels){
                    SNode src = rel.getSource();
                    SNode trg = rel.getTarget();
                    Iterator<SLayer> srcLayers = src.getLayers().iterator();
                    Iterator<SLayer> trgLayers = src.getLayers().iterator();
                    boolean srcOK = false;
                    boolean trgOK = false;
                    if (src.getLayers().isEmpty() && this.nodeLayer=="") {
                        srcOK = true;
                    } 
                    else {
                        while (srcLayers.hasNext()){
                            SLayer layer = srcLayers.next();
                            if (layer.getName().matches(this.nodeLayer)){
                                srcOK = true;
                            }
                        }                     
                    }
                    if (trg.getLayers().isEmpty() && this.nodeLayer=="") {
                        trgOK = true;
                    } 
                    else {
                        while (trgLayers.hasNext()){
                            SLayer layer = trgLayers.next();
                            if (layer.getName().matches(this.nodeLayer)){
                                trgOK = true;
                            }
                        }                     
                    }
                    if (srcOK && trgOK){ // this relation connects two nodes from valid layers
                        // check the edge itself
                        String edgeType = rel.getType();

                        if (edgeType.matches(this.edgeTypePattern)){
                            if (this.edgeAnnoNamePattern == ""){
                                addNodePair(src,trg);
                            }
                            else{
                                for (SAnnotation anno : rel.getAnnotations()){
                                    if (anno.getName().matches(this.edgeAnnoNamePattern) && anno.getValue_STEXT().matches(this.edgeAnnoValuePattern)){
                                        addNodePair(src,trg);                                        
                                    }
                                }
                            }
                            
                        }
                    }

                }      
                
                // Collect all relevant span annotations
                List<SSpan> spans;
                spans = docGraph.getSpans();

                for (SNode span : spans){
                    // Check layers
                    Iterator<SLayer> layers = span.getLayers().iterator();
                    boolean spanOK = false;
                    if (span.getLayers().isEmpty() && this.nodeLayer=="") {
                        spanOK = true;
                    } 
                    else {
                        while (layers.hasNext()){
                            SLayer layer = layers.next();
                            if (layer.getName().matches(this.nodeLayer)){
                                spanOK = true;
                            }
                        }                     
                    }
                    if (spanOK && !this.removeSingletons){
                        addSingleNode(span);
                    }
                }
                
                
                
                String docName = getDocument().getName();
                stbOutput.append("# begin document " + docName + "\n");
        	int i = -1;
                
                for (SToken out_tok : getDocument().getDocumentGraph().getSortedTokenByText()){
                    
                    i++;
                    String coref_col = "";
                    String line = Integer.toString(i) + "\t" + docGraph.getText(out_tok) + "\t";
                    if (this.startMap.containsKey(out_tok)){
                        for (SNode out_mark : this.startMap.get(out_tok)){
                            coref_col += "(" + getSeqID(out_mark);
                            if (this.endMap.containsKey(out_tok)){
                                if (this.endMap.get(out_tok).contains(out_mark)){
                                    coref_col += ")";
                                    this.endMap.get(out_tok).remove(out_mark);
                                }
                            }
                        }
                    }
                    if (this.endMap.containsKey(out_tok)){
                        for (SNode out_mark : this.endMap.get(out_tok)){
                                if (this.startMap.containsKey(out_tok)){
                                    if (this.startMap.get(out_tok).contains(out_mark)){
                                        coref_col += ")"; // Single token markable
                                    }
                                    else{
                                        if (coref_col.length() > 0){
                                            if (Character.isDigit(coref_col.charAt(coref_col.length()-1))){
                                                coref_col += "|";  // Use pipe to separate group 1 opening and 2 closing leading to (12) -> (1|2)
                                            }
                                        }
                                        coref_col += getSeqID(out_mark) + ")";
                                        
                                    }
                                }
                                else{
                                    if (coref_col.length() > 0){
                                        if (Character.isDigit(coref_col.charAt(coref_col.length()-1))){
                                            coref_col += "|";  // Use pipe to separate group 1 opening and 2 closing leading to (12) -> (1|2)
                                        }
                                    }
                                    coref_col += getSeqID(out_mark) + ")";
                                }
                        }
                    }
                    if (! this.startMap.containsKey(out_tok) && ! this.endMap.containsKey(out_tok)){
                        coref_col = "_";
                    }

                    line += coref_col + "\n";
                    stbOutput.append(line);
                
                }
    
                stbOutput.append("# end document\n\n");
                
                File outputFile;
                if (getResourceURI().toFileString() != null) {
                        outputFile = new File(getResourceURI().toFileString());
                } else {
                        outputFile = new File(getResourceURI().toString());
                }

                if ((!outputFile.isDirectory()) && (!outputFile.getParentFile().exists())) {
                        outputFile.getParentFile().mkdirs();
                }
                FileWriter flwTemp = null;
                try {
                        flwTemp = new FileWriter(outputFile);
                        flwTemp.write(stbOutput.toString());
                        flwTemp.flush();
                } catch (IOException e) {
                        throw new PepperModuleException(this, "Unable to write output file for ConllCoref export '" + getResourceURI() + "'.", e);
                } finally {
                        try {
                                if (flwTemp != null) {
                                        flwTemp.close();
                                }
                        } catch (IOException e) {
                                throw new PepperModuleException(this, "Unable to close output file writer for ConllCoref export '" + getResourceURI() + "'.", e);
                        }
                }        

		return DOCUMENT_STATUS.COMPLETED;                
	}

	private void readProperties(){
		CoNLLCorefExporterProperties properties = (CoNLLCorefExporterProperties) this.getProperties();

		this.nodeLayer = properties.getNodeLayer();
		this.edgeTypePattern = properties.getEdgeType();
		String annoKeyVal = properties.getEdgeAnno();
                if (annoKeyVal.contains("=")){
                    this.edgeAnnoNamePattern = annoKeyVal.split("=")[0];
                    this.edgeAnnoValuePattern = annoKeyVal.split("=")[1];
                }                
                this.removeSingletons = properties.getRemoveSingletons();
                
	}
        
        private void addNodePair(SNode src, SNode trg){

            String group;
            String oldGroup;
            HashMap<String,SNode> NodesToAdd = new HashMap();
            HashMap<String,SNode> NodesToRemove = new HashMap();
            if (this.nodesToGroups.containsKey(src)){
                // Source already has group ID, apply to target
                group = this.nodesToGroups.get(src);
                // Check whether target also has a group
                if (this.nodesToGroups.containsKey(trg)){
                    oldGroup = this.nodesToGroups.get(trg);
                    // Update old group members to new group ID
                    for (SNode transitiveNode : this.groupsToNodes.get(oldGroup)){
                        nodesToGroups.put(transitiveNode, group);
                        NodesToAdd.put(group,transitiveNode);
                        NodesToRemove.put(oldGroup,transitiveNode);
                    }                    
                    for (Map.Entry<String, SNode> entry : NodesToAdd.entrySet()) {
                        String grp = entry.getKey();
                        SNode nd = entry.getValue();
                        groupsToNodes.get(grp).add(nd);
                    }                
                    for (Map.Entry<String, SNode> entry : NodesToRemove.entrySet()) {
                        String grp = entry.getKey();
                        SNode nd = entry.getValue();
                        groupsToNodes.get(grp).remove(nd);
                    }                
                }
            }
            else if (this.nodesToGroups.containsKey(trg)){
                group = this.nodesToGroups.get(trg);  
            } 
            else{
                this.groupCounter++;
                group = Integer.toString(this.groupCounter);
            }
            this.nodesToGroups.put(src,group);
            this.nodesToGroups.put(trg,group);
            if (!this.groupsToNodes.containsKey(group)){
                this.groupsToNodes.put(group,new ArrayList());
            }
            this.groupsToNodes.get(group).add(src);
            this.groupsToNodes.get(group).add(trg);
            
            if (this.removeSingletons){
                // spans will not be added to start/end maps later, do it here
                addNodeStartEnd(src);
                addNodeStartEnd(trg);
            }

        }
        
        private void addSingleNode(SNode span){
            String group;
            // Add node if it is not already included
            if (!this.nodesToGroups.containsKey(span)){
                this.groupCounter++;
                group = Integer.toString(this.groupCounter);
                this.nodesToGroups.put(span,group);
            }
            addNodeStartEnd(span);
        }
         
        private void addNodeStartEnd(SNode span){
            List<SToken> borders = getBorders(span);
           
            SToken firstTok;
            SToken lastTok;
            
            if (borders!=null){
                 firstTok = borders.get(0);
                 lastTok = borders.get(1);
            
                // Add to start map
                if (!(this.startMap.containsKey(firstTok))){
                    List<SNode> nodeList = new ArrayList();
                    nodeList.add(span);
                    this.startMap.put(firstTok,nodeList);
                }
                else{
                    if (!this.startMap.get(firstTok).contains(span)){
                        this.startMap.get(firstTok).add(span);           
                    }
                }
                
                // Add to end map
                if (!this.endMap.containsKey(lastTok)){
                    List<SNode> nodeList = new ArrayList();
                    nodeList.add(span);
                    this.endMap.put(lastTok, nodeList);
                }
                else{
                    if (!this.endMap.get(lastTok).contains(span)){
                        this.endMap.get(lastTok).add(span);              
                    }
                }
                                   
            }
            else{
                throw new PepperModuleException("Found node without start and end tokens: " + span);
            }
            
            
        }
        
        private List<SToken> getBorders(SNode span){
            // Assume we are dealing with a span covering some tokens
            // TODO: deal with hierarchical SStruct case
            
            List<SToken> borderTokens = new ArrayList();
                     
            List<SToken> tokens = getDocument().getDocumentGraph().getSortedTokenByText(getDocument().getDocumentGraph().getOverlappedTokens(span));
            if (!tokens.isEmpty()) {
                borderTokens.add(tokens.get(0)); // First token
                borderTokens.add(tokens.get(tokens.size()-1)); // Last token
            }
        
            return borderTokens;
            
        }
        
        private String getSeqID(SNode markable){
            
            String temp_group = this.nodesToGroups.get(markable);
            if (!this.groupsToSeqIDs.containsKey(temp_group)){
                // this temp_groups needs to be assigned a seqID
                this.maxSeqID++;
                this.groupsToSeqIDs.put(temp_group, Integer.toString(this.maxSeqID));                
            }
            return this.groupsToSeqIDs.get(temp_group);
        }
        
        
}
