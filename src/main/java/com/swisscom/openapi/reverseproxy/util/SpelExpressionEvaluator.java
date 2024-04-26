/*
 * Copyright 2024-2099 Swisscom (Schweiz) AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.swisscom.openapi.reverseproxy.util;

import java.util.Optional;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@ConditionalOnMissingBean(value = SpelExpressionEvaluator.class, ignored = SpelExpressionEvaluator.class)
@Component
public class SpelExpressionEvaluator {

    private final StandardEvaluationContext context = new StandardEvaluationContext();

    private final ExpressionParser parser = new SpelExpressionParser();

    public SpelExpressionEvaluator(BeanFactory beanFactory) {
        this.context.setBeanResolver(new BeanFactoryResolver(beanFactory));
    }

    public String evaluate(String expression) {
        return Optional.ofNullable(expression)
            .map((expr) -> parse("#{".concat(expr).concat("}")))
            .filter((parsed) -> !"#{".concat(expression).concat("}").equals(parsed))
            .orElse(Optional.ofNullable(expression).map(this::parse).orElse(null));
    }

    protected String parse(String expression) {
        try {
            return this.parser.parseExpression(expression).getValue(this.context, String.class);
        }
        catch (Exception ex) {
            return expression;
        }
    }

}
