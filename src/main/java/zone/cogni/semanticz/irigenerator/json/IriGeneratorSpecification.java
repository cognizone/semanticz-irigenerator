/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package zone.cogni.semanticz.irigenerator.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import zone.cogni.semanticz.irigenerator.PrefixDeserializer;
import zone.cogni.semanticz.irigenerator.Vocabulary;

import java.net.URI;
import java.util.List;

@Data
@Accessors(chain = true)
@OWLClass(iri = Vocabulary.C_IRI_GENERATOR_SPECIFICATION)
public class IriGeneratorSpecification {

  @Id
  private URI id;

  @OWLObjectProperty(iri = Vocabulary.P_PREFIX)
  @JsonDeserialize(using = PrefixDeserializer.class)
  private List<Prefix> prefixes;

  @OWLObjectProperty(iri = Vocabulary.P_GENERATOR)
  private List<IriGenerator> generators;
}
