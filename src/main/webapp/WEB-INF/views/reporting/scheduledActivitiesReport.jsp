<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<html>
<head>
    <style type="text/css">
        .search_box {
            float:right;
            border: 1px dotted #ccc;
            width:8em;
            height:8em;
            padding:1em
        }
    </style>
</head>
<body>
<laf:box title="Administration">
    <laf:division>
        <form:form method="post">
            <tags:errors path="*"/>
             <div class="search_box">
                    <input type="submit"
                           value="Search"/>
            </div>
            <div class="row">
                <div class="label" >
                    <%--<form:label path="command.filter.activityMode" >--%>
                    Activity Mode:
                    <%--</form:label>--%>
                </div>
                <div class="value">
                    <select name="filter_modes">
                        <option value=""></option>
                        <c:forEach items="${modes}" var="mode">
                            <option value="${mode.id}">${mode.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div>
                <c:forEach items="${results}" var="row">
                    ${row.id} <br/>
                </c:forEach>
            </div>

        </form:form>
    </laf:division>
</laf:box>
</body>
</html>