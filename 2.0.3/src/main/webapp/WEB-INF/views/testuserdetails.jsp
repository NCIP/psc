<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>


<html><head>
<title>Login to Spring Example Account Banking</title></head>

<body>

<h1>User Details</h1>


<table width="95%" bgcolor="f8f8ff" border="0" cellspacing="0" cellpadding="5">
 Listing of the user profile.
 <p>
 User : 
<%=request.getUserPrincipal()%> is logged in.
</table>
<br/>
</body>
</html>
