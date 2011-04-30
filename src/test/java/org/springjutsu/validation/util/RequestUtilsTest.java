package org.springjutsu.validation.util;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.mockito.Mockito;

public class RequestUtilsTest {
	
	public static final String BEST_MATCH_KEY = 
		"org.springframework.web.servlet.HandlerMapping.bestMatchingPattern";
	
	protected HttpServletRequest mockRequestWithPattern(String pattern) {
		HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
		Mockito.when(mockRequest.getAttribute(BEST_MATCH_KEY)).thenReturn(pattern);
		return mockRequest;
	}
	
	@Test
	public void testFindMatchingRestPathBasic() {
		String[] candidateViewNames = new String[]{"/success/view"};
		String[] controllerPaths = null;
		HttpServletRequest request = mockRequestWithPattern("/account/new");
		String matchingRestPath = RequestUtils.findMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/success/view", matchingRestPath);
	}
	
	@Test
	public void testFindMatchingRestPathBasicWithSingleControllerPath() {
		String[] candidateViewNames = new String[]{"/success/view"};
		String[] controllerPaths = new String[]{"/account"};
		HttpServletRequest request = mockRequestWithPattern("account/new");
		String matchingRestPath = RequestUtils.findMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/success/view", matchingRestPath);
	}
	
	@Test
	public void testFindMatchingRestPathBasicWithMultipleControllerPaths() {
		String[] candidateViewNames = new String[]{"/success/view"};
		String[] controllerPaths = new String[]{"/hamburger", "/account"};
		HttpServletRequest request = mockRequestWithPattern("account/new");
		String matchingRestPath = RequestUtils.findMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/success/view", matchingRestPath);
	}
	
	@Test
	public void testFindMatchingRestPathMultiCandidate() {
		String[] candidateViewNames = new String[]{"/account/edit=/fail/view","/account/new=/success/view"};
		String[] controllerPaths = null;
		HttpServletRequest request = mockRequestWithPattern("/account/new");
		String matchingRestPath = RequestUtils.findMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/success/view", matchingRestPath);
	}
	
	@Test
	public void testFindMatchingRestPathMultiCandidateWithSingleControllerPath() {
		String[] candidateViewNames = new String[]{"/edit=/fail/view","/new=/success/view"};
		String[] controllerPaths = new String[]{"/account"};
		HttpServletRequest request = mockRequestWithPattern("/account/new");
		String matchingRestPath = RequestUtils.findMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/success/view", matchingRestPath);
	}
	
	@Test
	public void testFindMatchingRestPathMultiCandidateWithMultipleControllerPaths() {
		String[] candidateViewNames = new String[]{"/edit=/fail/view","/new=/success/view"};
		String[] controllerPaths = new String[]{"/hamburger", "/account"};
		HttpServletRequest request = mockRequestWithPattern("/account/new");
		String matchingRestPath = RequestUtils.findMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/success/view", matchingRestPath);
	}
	
	@Test
	public void testFindMatchingRestPathWithAntPath() {
		String[] candidateViewNames = new String[]{"/account/edit=/fail/view","/*/new=/success/view","/account/new=/fail/view"};
		String[] controllerPaths = null;
		HttpServletRequest request = mockRequestWithPattern("/account/new");
		String matchingRestPath = RequestUtils.findMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/success/view", matchingRestPath);
	}
	
	@Test
	public void testFindMatchingRestPathWithPartialAntPath() {
		String[] candidateViewNames = new String[]{"/account/edit=/fail/view","/*/n*w=/success/view","/account/new=/fail/view"};
		String[] controllerPaths = null;
		HttpServletRequest request = mockRequestWithPattern("/account/new");
		String matchingRestPath = RequestUtils.findMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/success/view", matchingRestPath);
	}
	
	@Test
	public void testFindMatchingRestPathWithAntPathWithSingleControllerPath() {
		String[] candidateViewNames = new String[]{"/financial/edit=/fail/view","/*/new=/success/view","/financial/new=/fail/view"};
		String[] controllerPaths = new String[]{"/account"};
		HttpServletRequest request = mockRequestWithPattern("/account/financial/new");
		String matchingRestPath = RequestUtils.findMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/success/view", matchingRestPath);
	}
	
	@Test
	public void testFindMatchingRestPathWithAntPathWithMultipleControllerPaths() {
		String[] candidateViewNames = new String[]{"/financial/edit=/fail/view","/*/new=/success/view","/financial/new=/fail/view"};
		String[] controllerPaths = new String[]{"/hamburger", "/account"};
		HttpServletRequest request = mockRequestWithPattern("/account/financial/new");
		String matchingRestPath = RequestUtils.findMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/success/view", matchingRestPath);
	}
	
	@Test
	public void testFindMatchingRestPathWithAntPathWithAntControllerPaths() {
		String[] candidateViewNames = new String[]{"/financial/edit=/fail/view","/*/new=/success/view","/financial/new=/fail/view"};
		String[] controllerPaths = new String[]{"/hamburger", "/*"};
		HttpServletRequest request = mockRequestWithPattern("/account/financial/new");
		String matchingRestPath = RequestUtils.findMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/success/view", matchingRestPath);
	}
	
	@Test
	public void testFindMatchingRestPathWithNestedAntPath() {
		String[] candidateViewNames = new String[]{"/account/financial/edit=/fail/view","/**/new=/success/view","/account/financial/new=/fail/view"};
		String[] controllerPaths = null;
		HttpServletRequest request = mockRequestWithPattern("/account/financial/new");
		String matchingRestPath = RequestUtils.findMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/success/view", matchingRestPath);
	}
	
	@Test
	public void testFindMatchingRestPathWithNestedAntPathWithSingleControllerPath() {
		String[] candidateViewNames = new String[]{"/financial/holdings/edit=/fail/view","/**/new=/success/view","/financial/holdings/new=/fail/view"};
		String[] controllerPaths = new String[]{"/account"};
		HttpServletRequest request = mockRequestWithPattern("/account/financial/holdings/new");
		String matchingRestPath = RequestUtils.findMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/success/view", matchingRestPath);
	}
	
	@Test
	public void testFindMatchingRestPathWithNestedAntPathWithMultipleControllerPaths() {
		String[] candidateViewNames = new String[]{"/financial/holdings/edit=/fail/view","/**/new=/success/view","/financial/holdings/new=/fail/view"};
		String[] controllerPaths = new String[]{"/hamburger", "/account"};
		HttpServletRequest request = mockRequestWithPattern("/account/financial/holdings/new");
		String matchingRestPath = RequestUtils.findMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/success/view", matchingRestPath);
	}
	
	@Test
	public void testFindMatchingRestPathWithAntPathWithNestedAntControllerPaths() {
		String[] candidateViewNames = new String[]{"/financial/holdings/edit=/fail/view","/*=/success/view","/financial/holdings/new=/fail/view"};
		String[] controllerPaths = new String[]{"/hamburger", "/**"};
		HttpServletRequest request = mockRequestWithPattern("/account/financial/holdings/new");
		String matchingRestPath = RequestUtils.findMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/success/view", matchingRestPath);
	}

}
