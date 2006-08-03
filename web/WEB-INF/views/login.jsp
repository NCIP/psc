<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>


<html><head>
<title>Account Login</title></head>

<body>

<h1>Please enter your userid and password.</h1>

<p><form method="post">

<table width="95%" bgcolor="f8f8ff" border="0" cellspacing="0" cellpadding="5">

<tr>
<td alignment="right" width="20%">User id:</td>
<spring:bind path="loginCredentials.userId">
<td width="20%">
<input type="text" name="userId" value="<c:out value=""/>">
</td>
</spring:bind>
<td width="60%">
</tr>

<tr>
<td alignment="right" width="20%">Password:</td>
<spring:bind path="loginCredentials.password">
<td width="20%">
<input type="password" name="password" value="<c:out value=""/>">
</spring:bind>
</td>
<td width="60%">
</tr>

</table>
<br>
<br>
<input type="submit" alignment="center" value="login">
</form>
</p>


</body>
</html>
