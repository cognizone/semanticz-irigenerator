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

package zone.cogni.semanticz.irigenerator;

public class Vocabulary {
  private static final String NS_IRIGENERATOR = "https://data.cogni.zone/voc/irigenerator/";
  public static final String C_IRI_GENERATOR = NS_IRIGENERATOR + "iri-generator";
  public static final String P_IRI_SELECTOR = NS_IRIGENERATOR + "iri-selector";
  public static final String P_VARIABLE_SELECTOR = NS_IRIGENERATOR + "variable-selector";
  public static final String P_IRI_TEMPLATE = NS_IRIGENERATOR + "iri-template";

  public static final String C_IRI_GENERATOR_SPECIFICATION = NS_IRIGENERATOR + "iri-generator-specification";
  public static final String P_PREFIX = NS_IRIGENERATOR + "prefix";
  public static final String P_GENERATOR = NS_IRIGENERATOR + "generator";

  public static final String C_PREFIX = NS_IRIGENERATOR + "prefix-class";
  public static final String P_PREFIX_NAME = NS_IRIGENERATOR + "prefix-name";
  public static final String P_NAMESPACE = NS_IRIGENERATOR + "namespace";
}