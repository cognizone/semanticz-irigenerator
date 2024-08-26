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

package zone.cogni.sem.jena.template;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;

import java.util.*;


public class JenaQueryUtils {

  public static List<Map<String, RDFNode>> convertToListOfMaps(ResultSet resultSet) {
    final List<Map<String, RDFNode>> result = new ArrayList<>();

    while (resultSet.hasNext()) {
      final QuerySolution querySolution = resultSet.next();
      final List<String> resultVars = resultSet.getResultVars();
      final Map<String, RDFNode> result1 = new HashMap<>();
      resultVars.forEach(var -> result1.put(var, querySolution.get(var)));
      result.add(result1);
    }

    return result;
  }
}
