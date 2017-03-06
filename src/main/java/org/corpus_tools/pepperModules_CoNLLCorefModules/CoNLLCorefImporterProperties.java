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

import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;


/**
 *
 * @author Amir Zeldes
 */
public class CoNLLCorefImporterProperties extends PepperModuleProperties  {


	public static final String PREFIX = "CoNLLCoref.";


        public final static String NAMESPACE = PREFIX + "namespace";
        public final static String RELTYPE = PREFIX + "relType";
        public final static String ANNONAME = PREFIX + "spanAnnotationName";
        public final static String ANNOVAL = PREFIX + "spanAnnotationValue";

	public CoNLLCorefImporterProperties() {
		this.addProperty(new PepperModuleProperty<String>(NAMESPACE, String.class, "Specifies a layer to assign to all imported spans.", "coref", false));
		this.addProperty(new PepperModuleProperty<String>(RELTYPE, String.class, "Specifies a type to assign to all imported pointing relations.", "coref", false));
		this.addProperty(new PepperModuleProperty<String>(ANNONAME, String.class, "Name of an annotatiton to add to all imported spans.", "entity", false));
		this.addProperty(new PepperModuleProperty<String>(ANNOVAL, String.class, "Value of an annotatiton to add to all imported spans.", "entity", false));
        }

    
}
