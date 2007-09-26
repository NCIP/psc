<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<html>
  <body>
  <head>
  </head>
  <laf:box title="Take Off Study">
    <laf:division>
        <form:form method="post">
            <form:errors path="*"/>
            <div class="row">
                <div class="label">
                    Participant:
                </div>
                <div class="value">
                    ${assignment.participant.fullName}
                </div>
            </div>
            <div class="row">
                <div class="label">
                    Off Study Date: (mm/dd/yyyy)
                </div>
                <div class="value">
                    <form:input path="expectedEndDate"/>
                </div>
            </div>
            <div class="row">
                <div class="label"></div>
                <div class="value"><input type="submit" value="Submit"/></div>
            </div>
        </form:form>
    </laf:division>
  </laf:box>
  </body>
</html>