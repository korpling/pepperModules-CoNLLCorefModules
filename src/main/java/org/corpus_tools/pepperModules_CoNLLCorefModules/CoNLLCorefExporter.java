package org.corpus_tools.pepperModules_CoNLLCorefModules;

import java.util.List;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.impl.PepperExporterImpl;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.PepperExporter;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.PepperModule;
import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleNotReadyException;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.graph.Identifier;
import org.corpus_tools.salt.util.SaltUtil;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

/**
 * 
 * @author Amir Zeldes
 */
//@formatter:off
@Component(name = "CoNLLCorefExporterComponent", factory = "PepperExporterComponentFactory")
//@formatter:on
public class CoNLLCorefExporter extends PepperExporterImpl implements PepperExporter {

    public CoNLLCorefExporter() {
		super();
		setName("CoNLLCorefExporter");
		setSupplierContact(URI.createURI(PepperConfiguration.EMAIL));
		setSupplierHomepage(URI.createURI(PepperConfiguration.HOMEPAGE));

                setDesc("Exports to the CoNLL coreference format, with 1 token per line and opening and closing brackets with entity numbers marking coreferent mention borders. ");
		addSupportedFormat("ConllCoref", "1.0", null);
		setDocumentEnding("conll");
		setExportMode(EXPORT_MODE.DOCUMENTS_IN_FILES);
		setProperties(new CoNLLCorefExporterProperties());
	}


        @Override
	public PepperMapper createPepperMapper(Identifier Identifier) {
		PepperMapper mapper = new Salt2CoNLLCorefMapper();
		mapper.setResourceURI(getIdentifier2ResourceTable().get(Identifier));
		return (mapper);
	}


	@Override
	public boolean isReadyToStart() throws PepperModuleNotReadyException {
		// TODO make some initializations if necessary
		return true;
	}
}
