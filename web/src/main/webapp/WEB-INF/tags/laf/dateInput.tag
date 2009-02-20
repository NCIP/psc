<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%--<%@taglib prefix="chrome" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>--%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@attribute name="path"%>
<form:input path="${path}" cssClass="date"/>
<a href="#" id="${path}-calbutton">
    <img src="<laf:imageUrl name="chrome/b-calendar.gif"/>" alt="Calendar" width="17" height="16" border="0" align="absmiddle" />
</a>