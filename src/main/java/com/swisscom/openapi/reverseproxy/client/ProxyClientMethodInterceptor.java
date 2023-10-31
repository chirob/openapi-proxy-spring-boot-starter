package com.swisscom.openapi.reverseproxy.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.FatalBeanException;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import com.swisscom.openapi.reverseproxy.annotation.ProxyInterceptor;

public class ProxyClientMethodInterceptor extends ProxyMethodInterceptor<Object> {

	private final AntPathMatcher antPathMatcher = new AntPathMatcher();
	private final Object proxyOptionsBean;
	private final ProxyClient proxyClient;
	private Method interceptorMethod;

	public ProxyClientMethodInterceptor(Object proxyOptionsBean, String path, List<RequestMethod> requestMethods,
			ProxyClient proxyClient) {
		this.proxyOptionsBean = proxyOptionsBean;
		this.proxyClient = proxyClient;
		
		var interceptorMethods = Arrays.stream(ClassUtils.getUserClass(proxyOptionsBean).getMethods())
				.filter(method -> Optional.ofNullable(method.getAnnotation(ProxyInterceptor.class))
						.map(annot -> matchAnnotationPaths(annot.path(), path)
								&& matchAnnotationMethods(annot.method(), requestMethods))
						.orElse(false))
				.toArray(Method[]::new);
		if (interceptorMethods.length > 1) {
			throw new FatalBeanException(
					"Too many interceptors for the same [path, methods] pair: [" + path + ", " + requestMethods + "]");
		}
		if (interceptorMethods.length == 1) {
			interceptorMethod = interceptorMethods[0];
			interceptorMethod.setAccessible(true);
		}
	}

	@Override
	public Object invoke() {
		try {
			return Optional.ofNullable(interceptorMethod).map(method -> {
				try {
					return proxyClient.updateResponse(method.invoke(proxyOptionsBean, proxyClient));
				} catch (InvocationTargetException e) {
					throw new IllegalStateException(e.getTargetException());
				} catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
			}).orElseGet(() -> proxyClient.exchange());
		} finally {
			proxyClient.cleanRequestAttributes();
		}
	}

	protected boolean matchAnnotationMethods(RequestMethod[] annotationMethods, List<RequestMethod> requestMethods) {
		return annotationMethods.length == 0 || requestMethods.containsAll(List.of(annotationMethods))
				|| List.of(annotationMethods).containsAll(requestMethods);
	}

	protected boolean matchAnnotationPaths(String[] annotationPaths, String path) {
		return annotationPaths.length == 0 || Arrays.stream(annotationPaths)
				.anyMatch(ap -> antPathMatcher.match(ap, path) || antPathMatcher.match(path, ap));
	}

}
