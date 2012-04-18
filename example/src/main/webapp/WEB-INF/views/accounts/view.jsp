<%@ include file="/WEB-INF/views/templates/headers.jsp" %>
<%@ include file="/WEB-INF/views/templates/includes.jsp" %>
<table>
	<tr>
		<td colspan="2">
			<h1>Details for ${account.username}</h1>
		</td>
	</tr>
	<tr>
		<td>
			First Name:
		</td>
		<td>
			${account.firstName}
		</td>
	</tr>
	<tr>
		<td>
			Last Name:
		</td>
		<td>
			${account.lastName}
		</td>
	</tr>
	<tr>
		<td>
			Phone Number:
		</td>
		<td>
			${account.phoneNumber}
		</td>
	</tr>
	<tr>
		<td>
			Email Address:
		</td>
		<td>
			${account.email}
		</td>
	</tr>
	<tr>
		<td>
			User Name:
		</td>
		<td>
			${account.username}
		</td>
	</tr>
	<tr>
		<td>
			Wants Email:
		</td>
		<td>
			${account.wantsEmail ? 'Totally' : 'Naw Dawg.'}
		</td>
	</tr>
	<tr>
		<td colspan="2" align="center">
			<c:choose>
				<c:when test="${not empty flowExecutionUrl}">
					<form method="post" action="${flowExecutionUrl}">
						<input type="submit" name="_eventId_edit" value="Edit" />
					</form>
				</c:when>
				<c:otherwise>
					<form method="get" action="${account.username}/edit">
						<input type="submit" value="Edit" />
					</form>
				</c:otherwise>
			</c:choose>
		</td>
	</tr>
</table>