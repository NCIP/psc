<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="sitecoord" tagdir="/WEB-INF/tags/dashboard/sitecoordinator" %>
                                                    
<html>
  <head>
      <title>Site Coordinator Dashboard</title>
      <script type="text/javascript">
        function registerSelector() {
            var aElement = $('selector')
            Event.observe(aElement, "change", function(e) {
                Event.stop(e)
                location.href = "${action}?selected=" + aElement.value;
            })
        }

        function assignmentButtonClicked(form) {
            $('assign-in-progress-indicator').reveal();

            SC.asyncSubmit(form, {
                onComplete: function() {
                    $('assign-in-progress-indicator').conceal();
                }
            })
        }

        Event.observe(window, "load", registerSelector);
      </script>
      <style type="text/css">
          .levelOne, .levelTwo, .levelThree {border:1px solid #ccc; padding:3px}
          label {font-weight:bold;}
      </style>
  </head>
  <body>
  <laf:box title="Site Coordinator Dashboard">
      <laf:division>
          <c:url var="action" value="/pages/dashboard/siteCoordinator/assignParticipantToParticipantCoordinatorByUser" />
          <form:form id="PCSelectionForm" action="${action}">
              <form:errors path="*"/>
              <%--<form:hidden path="selected"/>--%>
              <select id="selector">
                  <c:forEach items="${assignableUsers}" var="user">
                      <option value="${user.id}" <c:if test="${user.id == selectedId}">selected</c:if>>${user.name}</option>
                  </c:forEach>
              </select>
              <input type="button" value="Studies" onclick="location.href='<c:url value="/pages/dashboard/siteCoordinator/assignParticipantCoordinatorByUser"/>?selected=${selectedId}'"/>
              <input type="button" value="Participants" onclick="location.href='<c:url value="/pages/dashboard/siteCoordinator/assignParticipantToParticipantCoordinatorByUser"/>?selected=${selectedId}'"/>
          </form:form>
          <br/>
          <c:choose>
              <c:when test="${fn:length(displayMap) < 1}">
                  No participants assigned to this participant coordinator.
              </c:when>
              <c:otherwise>
                  <ul>
                      <c:forEach items="${displayMap}" var="site">
                          <li class="levelOne"><label>Site:</label> ${site.key.name}</li>
                          <ul>
                              <c:forEach items="${site.value}" var="study">
                                  <form:form action="${action}">
                                      <input type="hidden" name="study" value="${study.key.id}"/>
                                      <input type="hidden" name="site" value="${site.key.id}"/>

                                      <li class="levelTwo">
                                              <label>Study:</label> ${study.key.name} -
                                          <c:if test="${fn:length(participantCoordinatorStudySites[study.key][site.key]) < 1}">
                                              There are no participant coordinators for this study site.
                                          </c:if>
                                          <c:if test="${fn:length(participantCoordinatorStudySites[study.key][site.key]) >= 1}">
                                              <select name="participantCoordinator">
                                                  <option></option>
                                                  <c:forEach items="${participantCoordinatorStudySites[study.key][site.key]}" var="user">
                                                      <option value="${user.id}">${user.name}</option>
                                                  </c:forEach>
                                              </select>
                                              <input type="button" value="Assign" onclick="assignmentButtonClicked(this.form)"/>
                                              <tags:activityIndicator id="assign-in-progress-indicator"/>
                                          </c:if>
                                      </li>
                                      <ul id="${study.key.id}_${site.key.id}">
                                          <sitecoord:displayParticipants study="${study.key}" site="${site.key}" participants="${study.value}"/>
                                      </ul>
                                  </form:form>
                              </c:forEach>
                          </ul>
                      </c:forEach>
                  </ul>
              </c:otherwise>
          </c:choose>

      </laf:division>
  </laf:box>
  </body>
</html>