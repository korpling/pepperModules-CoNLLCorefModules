/*
 * Copyright 2017 GU.
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleDataException;
import org.corpus_tools.peppermodules.conll.tupleconnector.TupleConnectorFactory;
import org.corpus_tools.peppermodules.conll.tupleconnector.TupleReader;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SLayer;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 *
 * @author Amir Zeldes
 */public class CoNLLCoref2SaltMapper extends PepperMapperImpl{
    
    private String namespace;
    private String relType;
    private String annoName;
    private String annoVal;
    private SLayer layer;

    /**
     * Mapper for CoNLLCoref format to Salt
     * 
     */

    public CoNLLCoref2SaltMapper()
    {
        setProperties(new CoNLLCorefImporterProperties());

    }
    
        private static final Logger logger = LoggerFactory.getLogger(CoNLLCoref2SaltMapper.class);

        @Override
        public DOCUMENT_STATUS mapSCorpus() {

                return (DOCUMENT_STATUS.COMPLETED);
        }

        /**
         * {@inheritDoc PepperMapper#setDocument(SDocument)}
         * 
         */
        @Override
        public DOCUMENT_STATUS mapSDocument() {

                getDocument().setDocumentGraph(SaltFactory.createSDocumentGraph());

                // assign customizationn values from importer properties
                this.namespace = (String) getProperties().getProperties().getProperty(CoNLLCorefImporterProperties.NAMESPACE, "coref");
                this.relType = (String) getProperties().getProperties().getProperty(CoNLLCorefImporterProperties.RELTYPE, "coref");
                this.annoName = (String) getProperties().getProperties().getProperty(CoNLLCorefImporterProperties.ANNONAME, "entity");
                this.annoVal = (String) getProperties().getProperties().getProperty(CoNLLCorefImporterProperties.ANNOVAL, "entity");
                
                if (this.namespace != null){
                    this.layer = SaltFactory.createSLayer();
                    this.layer.setName(this.namespace);
                    this.layer.setGraph(getDocument().getDocumentGraph());
                }

                // to get the exact resource which is processed now, call
                // getResources(), make sure, it was set in createMapper()
                URI resource = getResourceURI();

                // we record, which file currently is imported to the debug stream
                logger.debug("Importing the file {}.", resource.toFileString());


        TupleReader tupleReader = TupleConnectorFactory.fINSTANCE.createTupleReader();
        
        // try reading the input file
        try {
                tupleReader.setSeperator("\t");
                tupleReader.setFile(new File(this.getResourceURI().toFileString()));
                tupleReader.readFile();
        } catch (IOException e) {
                String errorMessage = "Input file could not be read. Aborting conversion of file " + this.getResourceURI() + ".";
                logger.error(errorMessage);
                throw new PepperModuleDataException(this, errorMessage);
        }

        STextualDS sTextualDS = SaltFactory.createSTextualDS();
        sTextualDS.setGraph(getDocument().getDocumentGraph());

	int tok_counter = 0;
	ArrayList<SToken> tokens = new ArrayList<>();
	ArrayList<CoNLLCorefMarkable> markables = new ArrayList<>();
        DefaultDict<Integer,List<CoNLLCorefMarkable>> markstart_dict = new DefaultDict<>(ArrayList.class);
        DefaultDict<Integer,List<CoNLLCorefMarkable>> markend_dict = new DefaultDict<>(ArrayList.class);
        LinkedHashMap<String,CoNLLCorefMarkable> last_mark_by_group = new LinkedHashMap<>();        
        LinkedHashMap<String,CoNLLCorefMarkable> open_marks_by_group = new LinkedHashMap<>();        
        DefaultDict<String,String> mark_text_by_group = new DefaultDict<>(String.class);        
        
        Collection<String> tuple = null;
        int numOfTuples = tupleReader.getNumOfTuples();
        int tupleSize;

                // variables to keep track of tokens and text                
        // using a StringBuilder for the iteratively updated raw text
        int stringBuilderCharBufferSize = tupleReader.characterSize(2) + numOfTuples;
        StringBuilder primaryText = new StringBuilder(stringBuilderCharBufferSize);
        String corefInfo = ""; // TODO: properly decode text offsets to reconstruct whitespace preserving STextualDS
        String tokText;
        String group;
        
        // regex patterns to match coref Info with opening brackets, closing, or both
        Pattern patOpen = Pattern.compile("\\(([0-9]+)");
        Pattern patClose = Pattern.compile("([0-9]+)\\)");
        Pattern patDouble = Pattern.compile("\\(([0-9]+)\\)");

        boolean nestedClosed = true;


        // iteration over all data rows (the complete input file)
        for (int rowIndex = 0; rowIndex < numOfTuples; rowIndex++) {
                try {
                        tuple = tupleReader.getTuple();
                } catch (IOException e) {
                        String errorMessage = String.format("line %d of input file could not be read. Abort conversion of file " + this.getResourceURI() + ".", rowIndex + 1);
                        throw new PepperModuleDataException(this, errorMessage);
                }

                tupleSize = tuple.size();

                if (tupleSize < 2) { // A minimal CoNLL coref line must have at least 3 fields: tok_id, token, coref info
                    // skip this line
                }
                else{  // Token row
                    // Create the token and index it in the token list
                    Iterator<String> iter = tuple.iterator();
                    iter.next(); // skip tokID
                    tokText = iter.next();
                    // TODO: allow more columnn annotations
                    while (iter.hasNext()){ // Assume last column has coref info
                        corefInfo = iter.next();
                    }

                    // create token and add to token list
                    SToken sToken = SaltFactory.createSToken();
                    sToken.setGraph(getDocument().getDocumentGraph());

                    // update primary text string builder (sTextualDS.sText will be set after
                    // completely reading the input file)
                    int tokenTextStartOffset = primaryText.length();
                    primaryText.append(tokText).append(" ");
                    int tokenTextEndOffset = primaryText.length() - 1;
                    
                    // create textual relation
                    STextualRelation sTextualRelation = SaltFactory.createSTextualRelation();
                    sTextualRelation.setSource(sToken);
                    sTextualRelation.setTarget(sTextualDS);
                    sTextualRelation.setStart(tokenTextStartOffset);
                    sTextualRelation.setEnd(tokenTextEndOffset);
                    sTextualRelation.setGraph(getDocument().getDocumentGraph());
                    
                    tokens.add(sToken);

			// Find single token markables;
                        Matcher m = patDouble.matcher(corefInfo);                        
                        while (m.find()) {
                            group = m.group(1);
                            CoNLLCorefMarkable new_mark = new CoNLLCorefMarkable(tok_counter);
                            new_mark.setEnd(tok_counter);
                            if (last_mark_by_group.containsKey(group)){
                                new_mark.antecedent = last_mark_by_group.get(group);
                            }
                            if (open_marks_by_group.containsKey(group)){
                                // This is a nested markable
                            }
                            last_mark_by_group.put(group, new_mark);
                            markables.add(new_mark);
                            markstart_dict.get(tok_counter).add(new_mark);
                            markend_dict.get(tok_counter).add(new_mark);
                        }
			corefInfo = corefInfo.replaceAll("\\(([0-9]+)\\)","");
			// Find opening markables;
                        m = patOpen.matcher(corefInfo);                        
                        while (m.find()) {
                            group = m.group(1);
                            CoNLLCorefMarkable new_mark = new CoNLLCorefMarkable(tok_counter);
                            markables.add(new_mark);
                            if (last_mark_by_group.containsKey(group)){
                                new_mark.antecedent = last_mark_by_group.get(group);
                            }
                            if (open_marks_by_group.containsKey(group)){
                                // This is a nested markable
                                nestedClosed = false;
                            }
                            else{
                                open_marks_by_group.put(group, new_mark);
                            }
                            last_mark_by_group.put(group,new_mark);
                            mark_text_by_group.put(group,mark_text_by_group.get(group) + tokText + " ");
                            markstart_dict.get(tok_counter).add(new_mark);
                        }
			corefInfo = corefInfo.replaceAll("\\(([0-9]+)","");
			// Find closing markables;
                        m = patClose.matcher(corefInfo);       
                        CoNLLCorefMarkable mark;
                        while (m.find()) {
                            group = m.group(1);
                            if (open_marks_by_group.containsKey(group)){
                                if (!nestedClosed){
                                    // A nested markable will now be closed
                                    mark = last_mark_by_group.get(group);                                                                        
                                }
                                else{
                                    if (open_marks_by_group.get(group) != last_mark_by_group.get(group) && nestedClosed){
                                        // This is a nesting markable - there is an open markable, but it is not the last one seen in this group

                                        mark = open_marks_by_group.get(group);
                                    }
                                    else{
                                        mark = last_mark_by_group.get(group);                                    
                                    }
                                }
                            }
                            else{
                                mark = last_mark_by_group.get(group);
                            }
                            mark.setText(mark_text_by_group.get(group).trim());
                            mark.setEnd(tok_counter);
                            markend_dict.get(tok_counter).add(mark);
                            if (!nestedClosed && open_marks_by_group.containsKey(group)){
                                nestedClosed = true;
                            }
                            else{
                                // Empty markable text buffer
                                mark_text_by_group.put(group,"");
                                open_marks_by_group.remove(group);
                            }
                        }
			for (String g : mark_text_by_group.keySet()){
                            if (mark_text_by_group.get(g) != "") {
                                mark_text_by_group.put(g,mark_text_by_group.get(g) + tokText + " ");
                            }
                        }
               tok_counter++;
            }                                    
        }

            // ### file is completely read now ###

        // delete last char of primary text (a space character) and set it as
        // text for STextualDS
        primaryText.deleteCharAt(primaryText.length() - 1);
        sTextualDS.setText(primaryText.toString());

        // add all covered tokens to markables
        for (CoNLLCorefMarkable mark : markables){
            for (int i = mark.getStart(); i <= mark.getEnd(); i++){
                mark.addToken(tokens.get(i));
            }
        }
 
        // keep a mapping of Markables to SSpans to link edges later
        LinkedHashMap<CoNLLCorefMarkable,SSpan> marks2spans = new LinkedHashMap<>();
        
        // create sSpans for all markables and link to antecedents if necessary
        for (CoNLLCorefMarkable mark : markables){
            SSpan sSpan = getDocument().getDocumentGraph().createSpan(mark.getTokens());
            if (this.namespace != null){
                if (sSpan == null){
                    throw new PepperModuleDataException(this, "Null span detected, created from markable object: " + mark.toString());
                }
                sSpan.addLayer(this.layer);
            }
            if (this.annoName != "" && this.annoVal != ""){
                SAnnotation annotation = SaltFactory.createSAnnotation();
                annotation.setName(this.annoName);
                annotation.setValue(this.annoVal);
                if (this.namespace != null){
                    annotation.setNamespace(this.namespace);
                }                
                sSpan.addAnnotation(annotation);
            }
            if (mark.getNodeName()!=null){
                sSpan.setName(mark.getNodeName());
            }
            
            // remember SSpan object belonging to this markID
            marks2spans.put(mark, sSpan);
        }
        
        // add edges
        for (CoNLLCorefMarkable mark : markables){
             if (mark.antecedent != null){
                SPointingRelation sRel = SaltFactory.createSPointingRelation();  
                sRel.setSource(marks2spans.get(mark));
                sRel.setTarget(marks2spans.get(mark.antecedent));
                sRel.setType(this.relType);
                if (this.namespace != null){
                    sRel.addLayer(this.layer);
                }
                getDocument().getDocumentGraph().addRelation(sRel);
             }
        }

        return (DOCUMENT_STATUS.COMPLETED);
    }

 }

        

        