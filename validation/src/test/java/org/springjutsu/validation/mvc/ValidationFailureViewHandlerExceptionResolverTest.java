package org.springjutsu.validation.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import org.springjutsu.validation.mvc.annotations.ValidationFailureView;
import org.springjutsu.validation.mvc.annotations.ValidationFailureViews;

@RunWith(MockitoJUnitRunner.class)
public class ValidationFailureViewHandlerExceptionResolverTest {
	
	@Mock
	private HttpServletRequest request;
	
	@Mock
	private HttpServletResponse response;
	
	@Mock
	private BindException exception;
	
	@Mock
	private ResourceHttpRequestHandler resourceHandler;
	
	@Mock
	private IllegalArgumentException argException;
	
	ValidationFailureViewHandlerExceptionResolver resolver = new ValidationFailureViewHandlerExceptionResolver();
	
	@Test
	public void testResolveUnsupportedException() {
		ModelAndView modelAndView = resolver.resolveException(request, response, null, argException);
		assertNull(modelAndView);
	}
	
	@Test
	public void testResolveExceptionUnsupportedHandlerClass() {
		ModelAndView modelAndView = resolver.resolveException(request, response, resourceHandler, exception);
		assertNull(modelAndView);
	}
	
	@Test
	public void testNoAnnotations() {
		HandlerMethod handlerMethod = new HandlerMethod(this, 
				ReflectionUtils.findMethod(getClass(), "testNoAnnotations"));
		ModelAndView modelAndView = resolver.resolveException(request, response, handlerMethod, exception);
		assertNull(modelAndView);
	}
	
	@Test
	@ValidationFailureView(targetUrl="/foo/new")
	public void testSingleAnnotation() {
		HandlerMethod handlerMethod = new HandlerMethod(this, 
				ReflectionUtils.findMethod(getClass(), "testSingleAnnotation"));
		ModelAndView modelAndView = resolver.resolveException(request, response, handlerMethod, exception);
		assertEquals("/foo/new", modelAndView.getViewName());
	}
	
	@Test
	@ValidationFailureView(sourceUrl="/foo/new", targetUrl="/foo/new")
	public void testSourceUrlIgnoredForSingleAnnotation() {
		HandlerMethod handlerMethod = new HandlerMethod(this, 
				ReflectionUtils.findMethod(getClass(), "testSourceUrlIgnoredForSingleAnnotation"));
		ModelAndView modelAndView = resolver.resolveException(request, response, handlerMethod, exception);
		assertEquals("/foo/new", modelAndView.getViewName());
	}
	
	@Test(expected=IllegalArgumentException.class)
	@ValidationFailureViews({
		@ValidationFailureView(sourceUrl="/foo/new", targetUrl="/foo/{id}"),
		@ValidationFailureView(targetUrl="/foo/new")
	})
	public void testMultipleAnnotationsWithMissingSourceUrl() {
		HandlerMethod handlerMethod = new HandlerMethod(this, 
				ReflectionUtils.findMethod(getClass(), "testMultipleAnnotationsWithMissingSourceUrl"));
		resolver.resolveException(request, response, handlerMethod, exception);
	}
	
	@Test(expected=IllegalArgumentException.class)
	@ValidationFailureViews({
		@ValidationFailureView(sourceUrl="/foo/new", targetUrl="/foo/{id}"),
		@ValidationFailureView(sourceUrl="/foo/new", targetUrl="/foo/new")
	})
	public void testMultipleAnnotationsWithDuplicateSourceUrl() {
		HandlerMethod handlerMethod = new HandlerMethod(this, 
				ReflectionUtils.findMethod(getClass(), "testMultipleAnnotationsWithDuplicateSourceUrl"));
		resolver.resolveException(request, response, handlerMethod, exception);
	}
	
	@Test
	@ValidationFailureViews({
		@ValidationFailureView(sourceUrl="/foo/new", targetUrl="/foo/{id}"),
		@ValidationFailureView(sourceUrl="/foo/{id}/edit", targetUrl="/foo/{id}")
	})
	public void testMultipleAnnotationsNoMatch() {
		Mockito.when(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).thenReturn("/foo/list");
		HandlerMethod handlerMethod = new HandlerMethod(this, 
				ReflectionUtils.findMethod(getClass(), "testMultipleAnnotationsNoMatch"));
		ModelAndView modelAndView = resolver.resolveException(request, response, handlerMethod, exception);
		assertNull(modelAndView);
	}
	
	@Test
	@ValidationFailureViews({
		@ValidationFailureView(sourceUrl="/foo/new", targetUrl="/foo/win"),
		@ValidationFailureView(sourceUrl="/foo/{id}/edit", targetUrl="/foo/lose")
	})
	public void testMultipleAnnotations() {
		Mockito.when(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).thenReturn("/foo/new");
		HandlerMethod handlerMethod = new HandlerMethod(this, 
				ReflectionUtils.findMethod(getClass(), "testMultipleAnnotations"));
		ModelAndView modelAndView = resolver.resolveException(request, response, handlerMethod, exception);
		assertEquals("/foo/win", modelAndView.getViewName());
	}
	
	@Test
	@ValidationFailureViews({
		@ValidationFailureView(sourceUrl="/foo/new", targetUrl="/foo/{id}"),
		@ValidationFailureView(sourceUrl="/foo/{id}/edit", targetUrl="/foo/{id}")
	})
	public void testRestPathVariableSubstitutionFromUriTemplateVariables() {
		Map<String, Object> model = new HashMap<String, Object>();
		Map<String, Object> uriVariables = new HashMap<String, Object>();
		Mockito.when(exception.getModel()).thenReturn(model);
		Mockito.when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(uriVariables);
		uriVariables.put("id", 5);
		
		Mockito.when(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).thenReturn("/foo/new");
		HandlerMethod handlerMethod = new HandlerMethod(this, 
				ReflectionUtils.findMethod(getClass(), "testRestPathVariableSubstitutionFromUriTemplateVariables"));
		ModelAndView modelAndView = resolver.resolveException(request, response, handlerMethod, exception);
		assertEquals("/foo/5", modelAndView.getViewName());
	}
	
	@Test
	@ValidationFailureViews({
		@ValidationFailureView(sourceUrl="/foo/new", targetUrl="/foo/{id}"),
		@ValidationFailureView(sourceUrl="/foo/{id}/edit", targetUrl="/foo/{id}")
	})
	public void testRestPathVariableSubstitutionFromModel() {
		Map<String, Object> model = new HashMap<String, Object>();
		Map<String, Object> uriVariables = new HashMap<String, Object>();
		Mockito.when(exception.getModel()).thenReturn(model);
		Mockito.when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(uriVariables);
		model.put("id", 5);
		
		Mockito.when(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).thenReturn("/foo/new");
		HandlerMethod handlerMethod = new HandlerMethod(this, 
				ReflectionUtils.findMethod(getClass(), "testRestPathVariableSubstitutionFromModel"));
		ModelAndView modelAndView = resolver.resolveException(request, response, handlerMethod, exception);
		assertEquals("/foo/5", modelAndView.getViewName());
	}
	
	@Test(expected=IllegalArgumentException.class)
	@ValidationFailureViews({
		@ValidationFailureView(sourceUrl="/foo/new", targetUrl="/foo/{id}"),
		@ValidationFailureView(sourceUrl="/foo/{id}/edit", targetUrl="/foo/{id}")
	})
	public void testRestPathVariableSubstitutionMissingVariable() {
		Map<String, Object> model = new HashMap<String, Object>();
		Map<String, Object> uriVariables = new HashMap<String, Object>();
		Mockito.when(exception.getModel()).thenReturn(model);
		Mockito.when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(uriVariables);
		
		Mockito.when(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).thenReturn("/foo/new");
		HandlerMethod handlerMethod = new HandlerMethod(this, 
				ReflectionUtils.findMethod(getClass(), "testRestPathVariableSubstitutionFromUriTemplateVariables"));
		resolver.resolveException(request, response, handlerMethod, exception);
	}

}
