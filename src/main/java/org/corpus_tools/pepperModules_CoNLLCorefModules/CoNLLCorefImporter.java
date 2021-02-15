/**
 * Copyright 2016 GU.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.corpus_tools.pepperModules_CoNLLCorefModules;

import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.impl.PepperImporterImpl;
import org.corpus_tools.pepper.modules.PepperImporter;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.PepperModule;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleNotReadyException;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CoNLLCoref format implementation of a {@link PepperImporter}.
 * 
 * @author Amir Zeldes
 */
@Component(name = "CoNNLCorefImporterComponent", factory = "PepperImporterComponentFactory")
public class CoNLLCorefImporter extends PepperImporterImpl implements PepperImporter{

        private static final Logger logger = LoggerFactory.getLogger(CoNLLCorefImporter.class);

	public CoNLLCorefImporter() {
		super();
		this.setName("CoNLLCorefImporter");
		setSupplierContact(URI.createURI(PepperConfiguration.EMAIL));
		setSupplierHomepage(URI.createURI(PepperConfiguration.HOMEPAGE));
		setDesc("This is an importer module for the CoNLL Coref format. It imports tokens, entity spans and relations from the CoNLL Coref tab delimited format with numbered bracketed coreferent spans.");

                this.addSupportedFormat("CoNLLCoref", "1.0", null);

                getDocumentEndings().add(PepperImporter.ENDING_ALL_FILES);

	}
	

	/**
	 * @param corpusGraph
	 *            the CorpusGraph object, which has to be filled.
	 */
	@Override
	public void importCorpusStructure(SCorpusGraph sCorpusGraph) throws PepperModuleException {
		/**
		 * TODO this implementation is just a showcase, in production you might
		 * want to use the default. If yes, uncomment the following line and
		 * delete the rest of the implementation, or delete the entire method to
		 * trigger the default method.
		 */
		super.importCorpusStructure(sCorpusGraph);

	}

	/**
	 * This method creates a customized {@link PepperMapper} object for the 
         * CoNLLCoref format and returns it. 
	 * @param Identifier
	 *            {@link Identifier} of the {@link SCorpus} or {@link SDocument}
	 *            to be processed.
	 * @return {@link PepperMapper} object to do the mapping task for object
	 *         connected to given {@link Identifier}
	 */
	public PepperMapper createPepperMapper(Identifier Identifier) {
		CoNLLCoref2SaltMapper mapper = new CoNLLCoref2SaltMapper();
                return (mapper);
	}



	/**
	 * This method is called by the pepper framework and returns if a corpus
	 * located at the given {@link URI} is importable by this importer. If yes,
	 * 1 must be returned, if no 0 must be returned. If it is not quite sure, if
	 * the given corpus is importable by this importer any value between 0 and 1
	 * can be returned. If this method is not overridden, null is returned.
	 * 
	 * @return 1 if corpus is importable, 0 if corpus is not importable, 0 < X <
	 *         1, if no definitive answer is possible, null if method is not
	 *         overridden
	 */
	public Double isImportable(URI corpusPath) {
		// TODO some code to analyze the given corpus-structure
		return (null);
	}

	// =================================================== optional
	// ===================================================
	/**
	 * 
	 * @return false, {@link PepperModule} instance is not ready for any reason,
	 *         true, else.
	 */
	@Override
	public boolean isReadyToStart() throws PepperModuleNotReadyException {

            //return (super.isReadyToStart());
            return true;
	}
}
