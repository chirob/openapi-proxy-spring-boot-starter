package com.swisscom.openapi.reverseproxy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Proxy {

	@interface Options {
		String prefix() default "";

		String[] ignoredRequestHeaders() default {};

		String[] ignoredResponseHeaders() default {};
	}

	Options options() default @Options();

	String specification() default "";

	String target() default "";

}
