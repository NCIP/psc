<%@tag%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<jsgen:insertHtml targetElement="row" position="top">
    <tr>
        <td>
            <input id="addActivityTypeName" type="text" class="addActivityTypeName"/>
        </td>
        <td>
            <input type="submit" id="addActivity" name="addActivity" value="Create" onclick="addNewActivityType()"/>
        </td>
    </tr>

</jsgen:insertHtml>
