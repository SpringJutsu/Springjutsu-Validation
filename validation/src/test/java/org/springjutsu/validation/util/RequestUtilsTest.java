package org.springjutsu.validation.util;

import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.HandlerMapping;

public class RequestUtilsTest {
	
	protected HttpServletRequest mockRequestWithPattern(String pattern) {
		HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
		Mockito.when(mockRequest.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).thenReturn(pattern);
		return mockRequest;
	}
	
	@Test
	public void testFindFirstMatchingRestPathBasic() {
		String[] candidateViewNames = new String[]{"/success/view"};
		String[] controllerPaths = null;
		HttpServletRequest request = mockRequestWithPattern("/success/view");
		String matchingRestPath = RequestUtils.findFirstMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/success/view", matchingRestPath);
	}
	
	@Test
	public void testFindFirstMatchingRestPathBasicWithSingleControllerPath() {
		String[] candidateViewNames = new String[]{"/success/view"};
		String[] controllerPaths = new String[]{"/account"};
		HttpServletRequest request = mockRequestWithPattern("account/success/view");
		String matchingRestPath = RequestUtils.findFirstMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/success/view", matchingRestPath);
	}
	
	@Test
	public void testFindFirstMatchingRestPathBasicWithMultipleControllerPaths() {
		String[] candidateViewNames = new String[]{"/success/view"};
		String[] controllerPaths = new String[]{"/hamburger", "/account"};
		HttpServletRequest request = mockRequestWithPattern("account/success/view");
		String matchingRestPath = RequestUtils.findFirstMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/success/view", matchingRestPath);
	}
	
	@Test
	public void testFindFirstMatchingRestPathMultiCandidate() {
		String[] candidateViewNames = new String[]{"/account/edit","/account/new"};
		String[] controllerPaths = null;
		HttpServletRequest request = mockRequestWithPattern("/account/new");
		String matchingRestPath = RequestUtils.findFirstMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/account/new", matchingRestPath);
	}
	
	@Test
	public void testFindFirstMatchingRestPathMultiCandidateWithSingleControllerPath() {
		String[] candidateViewNames = new String[]{"/edit","/new"};
		String[] controllerPaths = new String[]{"/account"};
		HttpServletRequest request = mockRequestWithPattern("/account/new");
		String matchingRestPath = RequestUtils.findFirstMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/new", matchingRestPath);
	}
	
	@Test
	public void testFindFirstMatchingRestPathMultiCandidateWithMultipleControllerPaths() {
		String[] candidateViewNames = new String[]{"/edit","/new"};
		String[] controllerPaths = new String[]{"/hamburger", "/account"};
		HttpServletRequest request = mockRequestWithPattern("/account/new");
		String matchingRestPath = RequestUtils.findFirstMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/new", matchingRestPath);
	}
	
	@Test
	public void testFindFirstMatchingRestPathWithAntPath() {
		String[] candidateViewNames = new String[]{"/account/edit","/*/new","/account/new"};
		String[] controllerPaths = null;
		HttpServletRequest request = mockRequestWithPattern("/account/new");
		String matchingRestPath = RequestUtils.findFirstMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/*/new", matchingRestPath);
	}
	
	@Test
	public void testFindFirstMatchingRestPathWithPartialAntPath() {
		String[] candidateViewNames = new String[]{"/account/edit","/*/n*w","/account/new"};
		String[] controllerPaths = null;
		HttpServletRequest request = mockRequestWithPattern("/account/new");
		String matchingRestPath = RequestUtils.findFirstMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/*/n*w", matchingRestPath);
	}
	
	@Test
	public void testFindFirstMatchingRestPathWithAntPathWithSingleControllerPath() {
		String[] candidateViewNames = new String[]{"/financial/edit","/*/new","/financial/new"};
		String[] controllerPaths = new String[]{"/account"};
		HttpServletRequest request = mockRequestWithPattern("/account/financial/new");
		String matchingRestPath = RequestUtils.findFirstMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/*/new", matchingRestPath);
	}
	
	@Test
	public void testFindFirstMatchingRestPathWithAntPathWithMultipleControllerPaths() {
		String[] candidateViewNames = new String[]{"/financial/edit","/*/new","/financial/new"};
		String[] controllerPaths = new String[]{"/hamburger", "/account"};
		HttpServletRequest request = mockRequestWithPattern("/account/financial/new");
		String matchingRestPath = RequestUtils.findFirstMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/*/new", matchingRestPath);
	}
	
	@Test
	public void testFindFirstMatchingRestPathWithAntPathWithAntControllerPaths() {
		String[] candidateViewNames = new String[]{"/financial/edit","/*/new","/financial/new"};
		String[] controllerPaths = new String[]{"/hamburger", "/*"};
		HttpServletRequest request = mockRequestWithPattern("/account/financial/new");
		String matchingRestPath = RequestUtils.findFirstMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/*/new", matchingRestPath);
	}
	
	@Test
	public void testFindFirstMatchingRestPathWithNestedAntPath() {
		String[] candidateViewNames = new String[]{"/account/financial/edit","/**/new","/account/financial/new"};
		String[] controllerPaths = null;
		HttpServletRequest request = mockRequestWithPattern("/account/financial/new");
		String matchingRestPath = RequestUtils.findFirstMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/**/new", matchingRestPath);
	}
	
	@Test
	public void testFindFirstMatchingRestPathWithNestedAntPathWithSingleControllerPath() {
		String[] candidateViewNames = new String[]{"/financial/holdings/edit","/**/new","/financial/holdings/new"};
		String[] controllerPaths = new String[]{"/account"};
		HttpServletRequest request = mockRequestWithPattern("/account/financial/holdings/new");
		String matchingRestPath = RequestUtils.findFirstMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/**/new", matchingRestPath);
	}
	
	@Test
	public void testFindFirstMatchingRestPathWithNestedAntPathWithMultipleControllerPaths() {
		String[] candidateViewNames = new String[]{"/financial/holdings/edit","/**/new","/financial/holdings/new"};
		String[] controllerPaths = new String[]{"/hamburger", "/account"};
		HttpServletRequest request = mockRequestWithPattern("/account/financial/holdings/new");
		String matchingRestPath = RequestUtils.findFirstMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/**/new", matchingRestPath);
	}
	
	@Test
	public void testFindFirstMatchingRestPathWithAntPathWithNestedAntControllerPaths() {
		String[] candidateViewNames = new String[]{"/financial/holdings/edit","/*","/financial/holdings/new"};
		String[] controllerPaths = new String[]{"/hamburger", "/**"};
		HttpServletRequest request = mockRequestWithPattern("/account/financial/holdings/new");
		String matchingRestPath = RequestUtils.findFirstMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/*", matchingRestPath);
	}
	
	@Test
    public void testPathVariableRequestPath() {
        String[] candidateViewNames = new String[]{"any/path/will/do"};
        String[] controllerPaths = new String[]{"{recordId}"};
        HttpServletRequest request = mockRequestWithPattern("1/any/path/will/do");
        
        String matchingRestPath = RequestUtils.findFirstMatchingRestPath(candidateViewNames, controllerPaths, request);
        assertEquals("any/path/will/do", matchingRestPath);
    }
	
	@Test
    public void testPathVariableControllerPath() {
		String[] candidateViewNames = new String[]{"/financial/edit","/{id}/new","/financial/new"};
		String[] controllerPaths = new String[]{"/hamburger", "/account"};
		HttpServletRequest request = mockRequestWithPattern("/account/financial/new");
		String matchingRestPath = RequestUtils.findFirstMatchingRestPath(candidateViewNames, controllerPaths, request);
		assertEquals("/{id}/new", matchingRestPath);
    }

}
