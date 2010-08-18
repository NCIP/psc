<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>

<html>
<head>
    <tags:includeScriptaculous/>
   <script type="text/javascript">

       function checkUnCheckBox(box, isAllSites) {
           $('assignSites').disabled=true
           $('errors').innerHTML =""
           var checked = box.checked;
           if (isAllSites == 'false') {
                var inputs = $$('.managedCheckbox');
                var howManySitesSelected = 0;
                inputs.each(function(e) {
                   if (e.checked) {
                       howManySitesSelected =  howManySitesSelected+1;
                   }
                })

                if (howManySitesSelected == 0) {
                    $('errors').innerHTML = "The study has a MANAGED scope and user doesn't have the 'allSites' access to make the study unmanaged. One site have to be checked in per this user.";
                } else {
                    $('assignSites').disabled=false
                }
            }
        }

   </script>

    <style type="text/css">
        div.label {
            width: 35%;
        }
        div.submit {
            text-align: right;
        }
        form {
            width: 20em;
        }
        p {
            margin-left:1%;
        }
    </style>
</head>
<body>
     <laf:box title="${action} Sites">
        <laf:division>
            <p>
                You are making <strong>${study.assignedIdentifier}</strong> available to specific
                sites.
            </p>

            <c:url value="/pages/cal/template/managingSites?id=${study.id}" var="formAction"/>

            <form:form action="${formAction}" method="post">
                <div id="errors" style="margin-right:10px; margin-left:0.5em;">
                    <form:errors path="*"/>
                </div>
                <input type="hidden" name="studyId" value="${study.id}"/>
                <input type="hidden" name="assign" value="true"/>

                <c:forEach items="${command.userSitesToManageGrid}" var="site" varStatus="index">
                    <div class="row">
                        <div class="label">
                            ${site.key.name}
                        </div>
                        <div class="value">
                            <c:choose>
                                <c:when test="${!isManaged || (isManaged && isAllSites)}">
                                    <form:checkbox path="userSitesToManageGrid[${site.key.id}]"/>
                                </c:when>
                                <c:otherwise>
                                    <form:checkbox path="userSitesToManageGrid[${site.key.id}]" cssClass="managedCheckbox" onchange="checkUnCheckBox(this, '${isAllSites}')"/>
                                </c:otherwise>
                            </c:choose>

                        </div>
                    </div>
                </c:forEach>
                <div class="row">
                    <div class="submit">
                        <input type="submit" id="assignSites" value="Assign"/>
                    </div>
                </div>
            </form:form>
        </laf:division>
    </laf:box>
</body>

</html>
