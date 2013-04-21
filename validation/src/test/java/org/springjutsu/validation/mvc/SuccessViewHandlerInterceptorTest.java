package org.springjutsu.validation.mvc;

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
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import org.springjutsu.validation.mvc.annotations.SuccessView;
import org.springjutsu.validation.mvc.annotations.SuccessViews;

@RunWith(MockitoJUnitRunner.class)
public class SuccessViewHandlerInterceptorTest {
	
	@Mock
	private HttpServletRequest request;
	
	@Mock
	private HttpServletResponse response;
	
	@Mock
	private ResourceHttpRequestHandler resourceHandler;
	
	@Mock
	private ModelAndView modelAndView;
	
	private SuccessViewHandlerInterceptor interceptor = new SuccessViewHandlerInterceptor();
	
	@Test
	public void testUnsupportedHandlerClass() {
		interceptor.postHandle(request, response, resourceHandler, modelAndView);
		Mockito.verify(modelAndView, Mockito.never()).setViewName(Mockito.anyString());
	}
	
	@Test
	public void testNoAnnotations() {
		HandlerMethod handlerMethod = new HandlerMethod(this, 
				ReflectionUtils.findMethod(getClass(), "testNoAnnotations"));
		interceptor.postHandle(request, response, handlerMethod, modelAndView);
		Mockito.verify(modelAndView, Mockito.never()).setViewName(Mockito.anyString());
	}
	
	@Test
	@SuccessView(targetUrl="/foo/new")
	public void testSingleAnnotation() {
		HandlerMethod handlerMethod = new HandlerMethod(this, 
				ReflectionUtils.findMethod(getClass(), "testSingleAnnotation"));
		interceptor.postHandle(request, response, handlerMethod, modelAndView);
		Mockito.verify(modelAndView).setViewName("/foo/new");
	}
	
	@Test
	@SuccessView(sourceUrl="/foo/new", targetUrl="/foo/new")
	public void testSourceUrlIgnoredForSingleAnnotation() {
		HandlerMethod handlerMethod = new HandlerMethod(this, 
				ReflectionUtils.findMethod(getClass(), "testSourceUrlIgnoredForSingleAnnotation"));
		interceptor.postHandle(request, response, handlerMethod, modelAndView);
		Mockito.verify(modelAndView).setViewName("/foo/new");
	}
	
	@Test(expected=IllegalArgumentException.class)
	@SuccessViews({
		@SuccessView(sourceUrl="/foo/new", targetUrl="/foo/{id}"),
		@SuccessView(targetUrl="/foo/new")
	})
	public void testMultipleAnnotationsWithMissingSourceUrl() {
		HandlerMethod handlerMethod = new HandlerMethod(this, 
				ReflectionUtils.findMethod(getClass(), "testMultipleAnnotationsWithMissingSourceUrl"));
		interceptor.postHandle(request, response, handlerMethod, modelAndView);
	}
	
	@Test(expected=IllegalArgumentException.class)
	@SuccessViews({
		@SuccessView(sourceUrl="/foo/new", targetUrl="/foo/{id}"),
		@SuccessView(sourceUrl="/foo/new", targetUrl="/foo/new")
	})
	public void testMultipleAnnotationsWithDuplicateSourceUrl() {
		HandlerMethod handlerMethod = new HandlerMethod(this, 
				ReflectionUtils.findMethod(getClass(), "testMultipleAnnotationsWithDuplicateSourceUrl"));
		interceptor.postHandle(request, response, handlerMethod, modelAndView);
	}
	
	@Test
	@SuccessViews({
		@SuccessView(sourceUrl="/foo/new", targetUrl="/foo/{id}"),
		@SuccessView(sourceUrl="/foo/{id}/edit", targetUrl="/foo/{id}")
	})
	public void testMultipleAnnotationsNoMatch() {
		Mockito.when(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).thenReturn("/foo/list");
		HandlerMethod handlerMethod = new HandlerMethod(this, 
				ReflectionUtils.findMethod(getClass(), "testMultipleAnnotationsNoMatch"));
		interceptor.postHandle(request, response, handlerMethod, modelAndView);
		Mockito.verify(modelAndView, Mockito.never()).setViewName(Mockito.anyString());
	}
	
	@Test
	@SuccessViews({
		@SuccessView(sourceUrl="/foo/new", targetUrl="/foo/win"),
		@SuccessView(sourceUrl="/foo/{id}/edit", targetUrl="/foo/lose")
	})
	public void testMultipleAnnotations() {
		Mockito.when(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).thenReturn("/foo/new");
		HandlerMethod handlerMethod = new HandlerMethod(this, 
				ReflectionUtils.findMethod(getClass(), "testMultipleAnnotations"));
		interceptor.postHandle(request, response, handlerMethod, modelAndView);
		Mockito.verify(modelAndView).setViewName("/foo/win");
	}
	
	@Test
	@SuccessViews({
		@SuccessView(sourceUrl="/foo/new", targetUrl="/foo/{id}"),
		@SuccessView(sourceUrl="/foo/{id}/edit", targetUrl="/foo/{id}")
	})
	public void testRestPathVariableSubstitutionFromUriTemplateVariables() {
		Map<String, Object> model = new HashMap<String, Object>();
		Map<String, Object> uriVariables = new HashMap<String, Object>();
		Mockito.when(modelAndView.getModel()).thenReturn(model);
		Mockito.when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(uriVariables);
		uriVariables.put("id", 5);
		
		Mockito.when(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).thenReturn("/foo/new");
		HandlerMethod handlerMethod = new HandlerMethod(this, 
				ReflectionUtils.findMethod(getClass(), "testRestPathVariableSubstitutionFromUriTemplateVariables"));
		interceptor.postHandle(request, response, handlerMethod, modelAndView);
		Mockito.verify(modelAndView).setViewName("/foo/5");
	}
	
	@Test
	@SuccessViews({
		@SuccessView(sourceUrl="/foo/new", targetUrl="/foo/{id}"),
		@SuccessView(sourceUrl="/foo/{id}/edit", targetUrl="/foo/{id}")
	})
	public void testRestPathVariableSubstitutionFromModel() {
		Map<String, Object> model = new HashMap<String, Object>();
		Map<String, Object> uriVariables = new HashMap<String, Object>();
		Mockito.when(modelAndView.getModel()).thenReturn(model);
		Mockito.when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(uriVariables);
		model.put("id", 5);
		
		Mockito.when(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).thenReturn("/foo/new");
		HandlerMethod handlerMethod = new HandlerMethod(this, 
				ReflectionUtils.findMethod(getClass(), "testRestPathVariableSubstitutionFromModel"));
		interceptor.postHandle(request, response, handlerMethod, modelAndView);
		Mockito.verify(modelAndView).setViewName("/foo/5");
	}
	
	@Test(expected=IllegalArgumentException.class)
	@SuccessViews({
		@SuccessView(sourceUrl="/foo/new", targetUrl="/foo/{id}"),
		@SuccessView(sourceUrl="/foo/{id}/edit", targetUrl="/foo/{id}")
	})
	public void testRestPathVariableSubstitutionMissingVariable() {
		Map<String, Object> model = new HashMap<String, Object>();
		Map<String, Object> uriVariables = new HashMap<String, Object>();
		Mockito.when(modelAndView.getModel()).thenReturn(model);
		Mockito.when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(uriVariables);
		
		Mockito.when(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).thenReturn("/foo/new");
		HandlerMethod handlerMethod = new HandlerMethod(this, 
				ReflectionUtils.findMethod(getClass(), "testRestPathVariableSubstitutionFromUriTemplateVariables"));
		interceptor.postHandle(request, response, handlerMethod, modelAndView);
	}

}
