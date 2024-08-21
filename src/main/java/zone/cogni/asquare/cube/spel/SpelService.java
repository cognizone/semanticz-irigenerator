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

package zone.cogni.asquare.cube.spel;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.HashMap;
import java.util.Map;

public class SpelService implements TemplateService {

  private final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
  private final Map<String, Expression> expressionCache = new HashMap<>();

  public String processTemplate(String template, Object root) {
    Expression expression = getExpression(template);
    EvaluationContext context = getContext(root);
    return expression.getValue(context, String.class);
  }

  /**
   * According to stackoverflow https://stackoverflow.com/a/16775689/328808
   * Expression instances are threadsafe, so we can cache them.
   */
  private Expression getExpression(String template) {
    try {
      String md5Hex = DigestUtils.md5Hex(template);

      boolean isCached = expressionCache.containsKey(md5Hex);
      Expression expression = isCached
                              ? expressionCache.get(md5Hex)
                              : spelExpressionParser.parseExpression(template, new TemplateParserContext());

      if (!isCached) expressionCache.put(md5Hex, expression);
      return expression;
    }
    catch (RuntimeException e) {
      throw new RuntimeException("SpEL expression template parsing failed for: \n" + template, e);
    }
  }

  private EvaluationContext getContext(Object root) {
    return root instanceof EvaluationContext ? (EvaluationContext) root
                                             : new StandardEvaluationContext(root);
  }
}
