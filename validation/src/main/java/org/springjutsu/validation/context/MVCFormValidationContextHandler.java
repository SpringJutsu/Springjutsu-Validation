package org.springjutsu.validation.context;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springjutsu.validation.mvc.HttpRequestAttributesNamedAttributeAccessor;
import org.springjutsu.validation.mvc.HttpRequestParametersNamedAttributeAccessor;
import org.springjutsu.validation.mvc.HttpSessionAttributesNamedAttributeAccessor;
import org.springjutsu.validation.spel.NamedAttributeAccessorPropertyAccessor;
import org.springjutsu.validation.spel.SPELResolver;
import org.springjutsu.validation.util.RequestUtils;

/**
 * Handles validation contexts of type "form".
 * Determines whether or not the given form path 
 * matches the current form being submitted
 * via spring MVC.
 * @author Clark Duplichien
 */
public class MVCFormValidationContextHandler implements ValidationContextHandler {

	/**
	 * Will return true if the current request is
	 * a spring MVC request, and the given qualifier 
	 * stating an MVC form path matches the current
	 * MVC request path.
	 */
	@Override
	public boolean isActive(Set<String> qualifiers) {
		if (!isMVCRequest()) {
			return false;
		} else {
			return appliesToForm(getMVCFormName(), qualifiers);
		}
	}
	
	@Override
	public void initializeSPELResolver(SPELResolver spelResolver) {
		
		// initialize property accessors
		spelResolver.getScopedContext().addPropertyAccessor(new NamedAttributeAccessorPropertyAccessor());
		
		// access scoped request
		RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
		
		// initialize named contexts
		spelResolver.getScopedContext().addContext("requestParameters", 
				new HttpRequestParametersNamedAttributeAccessor(request));
		spelResolver.getScopedContext().addContext("requestAttributes", 
				new HttpRequestAttributesNamedAttributeAccessor(request));
		spelResolver.getScopedContext().addContext("session", 
				new HttpSessionAttributesNamedAttributeAccessor(request));
	}
	
	/**
	 * @return true if the current web request is associated
	 * with Spring MVC.
	 */
	public static boolean isMVCRequest() {
		return RequestContextHolder.getRequestAttributes() != null;
	}
	
	/**
	 * Just cleans up a Servlet path URL for rule resolving by
	 * the rules container.
	 * Restful URL paths may be used, with \{variable} path support.
	 * As of 0.6.1, ant paths like * and ** may also be used.
	 */
	protected String getMVCFormName() {
		RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
		return RequestUtils.removeLeadingAndTrailingSlashes(request.getServletPath());
	}
	
	/** Returns true if the rule applies to the current form.
	 * Replace any REST variable wildcards with wildcard regex.
	 * Replace ant path wildcards with wildcard regexes as well.
	 * Iterate through possible form names to find the first match.
	 */
	public boolean appliesToForm(String form, Set<String> candidateForms) {
		if (form == null || form.isEmpty()) {
			return true;
		}
		boolean appliesToForm = candidateForms.isEmpty();
		for (String formName : candidateForms) {
			String formPattern = 
				formName.replaceAll("\\{[^\\}]*}", "[^/]+")
				.replaceAll("\\*\\*/?", "(*/?)+")
				.replace("*", "[^/]+");
			if (form.matches(formPattern)) {
				appliesToForm = true;
				break;
			}			
		}
		return appliesToForm;
	}

}
