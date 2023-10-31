package com.swisscom.openapi.reverseproxy.client;

import java.lang.reflect.Method;

public abstract class ProxyMethodInterceptor<T> {

	public abstract T invoke();
	
	public Method getInvocationMethod() {
		try {
			return getClass().getDeclaredMethod("invoke");
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException(e);
		}
	}
}
