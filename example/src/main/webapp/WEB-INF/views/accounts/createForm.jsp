<%@ include file="/WEB-INF/views/templates/headers.jsp" %>
<%@ include file="/WEB-INF/views/templates/includes.jsp" %>
<form:form modelAttribute="account" action="${not empty flowExecutionUrl ? flowExecutionUrl : null}">
<table>
	<tr>
		<td colspan="2">
			<h1>Account Creation</h1>
		</td>
	</tr>
	<tr>
		<td width="200">
			First Name:
			<form:errors path="firstName" cssClass="error"/>
		</td>
		<td>
			<form:input path="firstName"/>
		</td>
	</tr>
	<tr>
		<td>
			Last Name:
			<form:errors path="lastName" cssClass="error"/>
		</td>
		<td>
			<form:input path="lastName"/>
		</td>
	</tr>
	<tr>
		<td>
			Phone Number:
			<form:errors path="phoneNumber" cssClass="error"/>
		</td>
		<td>
			<form:input path="phoneNumber"/>
		</td>
	</tr>
	<tr>
		<td>
			Email Address:
			<form:errors path="email" cssClass="error"/>
		</td>
		<td>
			<form:input path="email"/>
		</td>
	</tr>
	<tr>
		<td>
			User Name:
			<form:errors path="username" cssClass="error"/>
		</td>
		<td>
			<form:input path="username"/>
		</td>
	</tr>
	<tr>
		<td>
			Password:
			<form:errors path="password" cssClass="error"/>
		</td>
		<td>
			<form:password path="password"/>
		</td>
	</tr>
	<tr>
		<td>
			Confirm Password:
		</td>
		<td>
			<input type="password" name="confirmPassword"/>
		</td>
	</tr>
	<tr>
		<td colspan="2">
			<input type="checkbox" name="acceptTOS" /> I accept the invisible Terms and Conditions! 
			<form:errors path="wantsEmail" cssClass="error"/>
		</td>
	</tr>
	<tr>
		<td colspan="2">
			<form:checkbox path="wantsEmail"/> Send me email!
		</td>
	</tr>
	<tr>
		<td colspan="2" align="center">
			<input name="_eventId_save" type="submit" value="Save!"/>
		</td>
	</tr>
</table>
</form:form>