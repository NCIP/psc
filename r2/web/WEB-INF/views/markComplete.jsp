<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Mark template for ${study.name} complete.</title>
</head>
<body>
<h1>Mark template for ${study.name} complete.</h1>
<p>You are about to mark the ${study.name} template complete. Are you sure you want to do this?</p>
<c:url value="/pages/markComplete?id=${study.id}" var="formAction"/>
<form action="${formAction}" method="post">
   <input type="hidden" name="studyId" value="${study.id}"/>
   <div class="row">
        <div class="label">
            Mark complete?
        </div>
        <div class="value">
            <label><input type="radio" id="complete-yes" name="completed" value="true"/> Yes</label>
            <label><input type="radio" id="complete-no"  name="completed" value="false" checked="checked"/> No</label>
        </div>
    </div>
	<div class="row">
        <div class="value submit">
            <input type="submit" value="Submit"/>
        </div>		
	</div>
</form>
</body>
</html>