<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="sitecoord" tagdir="/WEB-INF/tags/dashboard/sitecoordinator" %>
                                                    
<html>
  <head>
      <title>Site Coordinator Dashboard</title>
      <tags:stylesheetLink name="main"/>
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
                    if($('assign-in-progress-indicator')) {
                        $('assign-in-progress-indicator').conceal();
                    }
                }
            })
        }

        function hideEmptyLists(studyId, siteId) {
            var studySiteId = studyId + '_' + siteId;
            if($(studySiteId).descendants().size() < 1) {
                $(studySiteId).ancestors().each(function(a) {
                    // Remove Study
                    if (a.match('li.study')) {

                        var studies = a.up();
                        var site = studies.up();
                        var sites = site.up();


                        a.remove();
                        // Remove Studies
                        if (studies.descendants().size() == 0) {
                            studies.remove();
                        }

                        // Remove Site, site will always have 1 descendant for the site label
                        if(site.descendants().size() <= 1) {
                            site.remove();
                        }

                        // Remove Sites
                        if(sites.descendants().size() == 0) {
                            $('participants-table').remove()
                            $('participants-info').show()
                        }
                    }
                })
            }
        }

        function showParticipantsInfo() {
            $('participants-info').show()
        }

        Event.observe(window, "load", registerSelector);
        <c:if test="${fn:length(displayMap) < 1}">
            Event.observe(window, "load", showParticipantsInfo);
        </c:if>

      </script>
      <style type="text/css">
          ul {
              padding:0;
          }

          ul.sites {
              width:80%
          }

          ul.participants {
              padding-bottom:1em
          }

          li {
              margin: 0;
              padding-left:1em;
              list-style:none;
          }

          label.site {
              padding:.2em;
              font-weight:bold;
          }

          div.row div.study {
              float:left;
          }

          label.participant {
              font-weight:normal;
          }

          .site-coord-dash-link {
              color:#0000cc;
              cursor:pointer;
              white-space:nowrap;
          }
      </style>
  </head>
  <body>
  <laf:box title="Site Coordinator Dashboard">
      <laf:division>
          <c:url var="action" value="/pages/dashboard/siteCoordinator/assignParticipantToParticipantCoordinatorByUser" />
          <form:form id="PCSelectionForm" action="${action}">
              <form:errors path="*"/>
              <div class="links-row">
                  Assign By:
                      <span id="study-view" class="site-coord-dash-link" onclick="location.href='<c:url value="/pages/dashboard/siteCoordinator/assignParticipantCoordinatorByStudy"/>'">Study</span>,
                      Participant Coordinator
              </div>
              <br/>
              <div class="row">
                  <div class="label">
                      Participant Coordinator:
                  </div>
                  <div class="value">
                      <select id="selector">
                          <c:forEach items="${assignableUsers}" var="user">
                              <option value="${user.id}" <c:if test="${user.id == selectedId}">selected</c:if>>${user.name}</option>
                          </c:forEach>
                      </select>
                      <input type="button" value="Studies" onclick="location.href='<c:url value="/pages/dashboard/siteCoordinator/assignParticipantCoordinatorByUser"/>?selected=${selectedId}'"/>
                      <input type="button" value="Participants" onclick="location.href='<c:url value="/pages/dashboard/siteCoordinator/assignParticipantToParticipantCoordinatorByUser"/>?selected=${selectedId}'"/>
                  </div>
              </div>
          </form:form>
          <br/>
              <label id="participants-info" style="display:none">No participants assigned to this participant coordinator.</label>

              <c:if test="${fn:length(displayMap) > 0}">
                  <div class="row" id="participants-table">
                      <div class="label">
                          Re-assign Participant:
                      </div>
                      <div class="value">
                          <ul class="sites">
                              <c:forEach items="${displayMap}" var="site">
                                  <li><label class="site">${site.key.name}</label>

                                      <ul class="studies">
                                          <c:forEach items="${site.value}" var="study">
                                          <li class="study">
                                              <form:form action="${action}">
                                                  <input type="hidden" name="study" value="${study.key.id}"/>
                                                  <input type="hidden" name="site" value="${site.key.id}"/>
                                                  <input type="hidden" name="selected" value="${selectedId}"/>

                                                  <div class="row">
                                                      <div class="study">${study.key.name}</div>
                                                      <div class="value">
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
                                                              <input type="button" value="Assign Participant Coordinator" onclick="assignmentButtonClicked(this.form)"/>
                                                              <tags:activityIndicator id="assign-in-progress-indicator"/>
                                                          </c:if>
                                                      </div>
                                                  </div>
                                                  <ul id="${study.key.id}_${site.key.id}" class="participants">
                                                      <sitecoord:displayParticipants study="${study.key}" site="${site.key}" participants="${study.value}"/>
                                                  </ul>

                                              </form:form>
                                              </c:forEach>
                                          </li>
                                      </ul>
                                  </li>
                              </c:forEach>
                          </ul>
                      </div>
                  </div>
              </c:if>
      </laf:division>
  </laf:box>
  </body>
</html>