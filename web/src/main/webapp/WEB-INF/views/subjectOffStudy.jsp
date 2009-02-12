<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="laf" uri="/WEB-INF/tags/laf.tld"%>
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
                    Subject:
                </div>
                <div class="value">
                    ${assignment.subject.fullName}
                </div>
            </div>
            <div class="row">
                <div class="label">
                    Off Study Date: (mm/dd/yyyy)
                </div>
                <div class="value">
                    <laf:dateInput path="expectedEndDate"/>
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