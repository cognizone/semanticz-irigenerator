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


import lombok.Getter;
import lombok.Setter;
import zone.cogni.semanticz.irigenerator.json.IriGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IriGeneratorResult {

  @Setter
  @Getter
  private IriGenerator generator;
  @Setter
  @Getter
  private Set<String> uris;
  private final Map<String, String> replacements = new HashMap<>();

  public IriGeneratorResult() {
  }

  public boolean alreadyReplaced(String oldUri) {
    return replacements.containsKey(oldUri);
  }

  public void addReplacement(String oldUri, String newUri) {
    replacements.put(oldUri, newUri);
  }
}
