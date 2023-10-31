package com.swisscom.openapi.reverseproxy.util;

import java.util.Optional;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class SpelExpressionEvaluator {

	private final StandardEvaluationContext context = new StandardEvaluationContext();
	private final ExpressionParser parser = new SpelExpressionParser();

	public SpelExpressionEvaluator(BeanFactory beanFactory) {
		context.setBeanResolver(new BeanFactoryResolver(beanFactory));
	}

	public String evaluate(String expression) {
		return Optional.ofNullable(expression).map(expr -> parse("#{".concat(expr).concat("}")))
				.filter(parsed -> !"#{".concat(expression).concat("}").equals(parsed))
				.orElse(Optional.ofNullable(expression).map(expr -> parse(expr)).orElse(null));
	}

	protected String parse(String expression) {
		try {
			return parser.parseExpression(expression).getValue(context, String.class);
		} catch (Exception e) {
			return expression;
		}
	}
}
