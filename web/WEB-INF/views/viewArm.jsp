<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="display" uri="http://displaytag.sf.net/el"%>
<html>
    <head>
        <title>View Arm</title>
    </head>
    <body>
        <h1>Arm "${arm.name}" for Study "${arm.study.name}"</h1>
        <h2>Periods</h2>
        <display:table id="periods" name="arm.periods" defaultsort="2">
            <display:column property="name"/>
            <display:column property="startDay" title="Start day"/>
            <display:column property="endDay" title="End day"/>
            <display:column property="duration"/>
            <display:column property="repetitions"/>
        </display:table>
        <a href="<c:url value="/pages/newPeriod?id=${arm.id}"/>">Add Period</a><br/>
  </body>
</html>