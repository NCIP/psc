<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="sitecoord" tagdir="/WEB-INF/tags/dashboard/sitecoordinator" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>
                                                    
<html>
  <head>
      <title>Change ownership of subject schedules</title>
      <tags:stylesheetLink name="main"/>
      <script type="text/javascript">
        function registerSelector() {
            var aElement = $('selector')
            if ("${selectedId}" == null || "${selectedId}" == "") {
                location.href = "${action}?selected=" + aElement[0].value;
            }
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
                    if (a.match('li.study')) {

                        var studies = a.up();
                        var site = studies.up();
                        var sites = site.up();

                        // Remove Study
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
                            $('subjects-table').remove()
                            $('subjects-info').show()
                        }
                    }
                })
            }
        }

        function showSubjectsInfo() {
            $('subjects-info').show()
        }

        function selectAllParticipantsCheckbox(studyValues) {
            var checkbox = 'checkbox_' + studyValues;
            var arrayOfSubject = $(studyValues).immediateDescendants();

            var areAllButtonsChecked = true;
            for (var i=0; i< arrayOfSubject.length; i++) {
                if (!arrayOfSubject[i].getElementsByTagName("input")[0].checked) {
                    areAllButtonsChecked = false;
                    break;
                }
            }
            if (!areAllButtonsChecked) {
                for (var i=0; i< arrayOfSubject.length; i++) {
                    arrayOfSubject[i].getElementsByTagName("input")[0].checked = true
                }
            } else {
                for (var i=0; i< arrayOfSubject.length; i++) {
                    arrayOfSubject[i].getElementsByTagName("input")[0].checked = false
                }
            }
        }

        function deselectOrSelectAllCheckbox(idValue, isSelected) {
            var checkbox = 'checkbox_' + idValue;
            if (!isSelected) {
                $(checkbox).checked = isSelected;
            } else {
                var arrayOfSubject = $(idValue).immediateDescendants();
                var areAllButtonsChecked = true;
                for (var i=0; i< arrayOfSubject.length; i++) {
                    if (!arrayOfSubject[i].getElementsByTagName("input")[0].checked) {
                        areAllButtonsChecked = false;
                        break;
                    }
                }
                if (areAllButtonsChecked) {
                    $(checkbox).checked = true;
                } else {
                    $(checkbox).checked = false;
                }
            }
        }

        Event.observe(window, "load", registerSelector);
        <c:if test="${fn:length(displayMap) eq 0}">
            Event.observe(window, "load", showSubjectsInfo);
        </c:if>

      </script>
      <style type="text/css">
          ul {
              padding:0;
          }

          ul.sites {
              width:80%
          }

          ul.subjects {
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

          .site-coord-dash-link {
              color:#0000cc;
              cursor:pointer;
              white-space:nowrap;
          }
      </style>
  </head>
  <body>
  <laf:box title="Change ownership of subject schedules">
      <laf:division>
          <c:url var="action" value="/pages/dashboard/siteCoordinator/assignSubjectToSubjectCoordinatorByUser" />
          <form:form id="PCSelectionForm" action="${action}">
            <form:errors path="*"/>
                <div class="row">
                     <div class="label">
                        Subject Coordinator:
                     </div>
                     <div class="value">
                        <select id="selector">
                            <c:forEach items="${assignableUsers}" var="user">
                                <option value="${user.id}" <c:if test="${user.id == selectedId}">selected</c:if>>${user.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
            </form:form>
            <br/>
            <label id="subjects-info" style="display:none">No subjects assigned to this subject coordinator.</label>
        </laf:division>
        <c:if test="${fn:length(displayMap) > 0}">
            <c:forEach items="${displayMap}" var="site">
                <h3>${site.key.name}</h3>
                <laf:division>
                    <ul class="studies">
                          <c:forEach items="${site.value}" var="study" varStatus="status">
                              <li class="autoclear ${commons:parity(status.count)}">
                                  <form:form action="${action}">
                                      <input type="hidden" name="study" value="${study.key.id}"/>
                                      <input type="hidden" name="site" value="${site.key.id}"/>
                                      <input type="hidden" name="selected" value="${selectedId}"/>

                                      <div class="row">
                                          <div class="study">${study.key.name}</div>
                                          <div class="value">
                                              <c:if test="${fn:length(subjectCoordinatorStudySites[study.key][site.key]) < 1}">
                                                  There are no subject coordinators for this study site.
                                              </c:if>
                                              <c:if test="${fn:length(subjectCoordinatorStudySites[study.key][site.key]) >= 1}">
                                                  <select name="subjectCoordinator">
                                                      <option></option>
                                                      <c:forEach items="${subjectCoordinatorStudySites[study.key][site.key]}" var="user">
                                                          <option value="${user.id}">${user.name}</option>
                                                      </c:forEach>
                                                  </select>
                                                  <input type="button" value="Assign to Subject Coordinator" onclick="assignmentButtonClicked(this.form)"/>
                                                  <tags:activityIndicator id="assign-in-progress-indicator"/>
                                              </c:if>
                                          </div>
                                      </div>

                                      <ul>
                                          <li>
                                              <input class="selectAll" type="checkbox" id="checkbox_${study.key.id}_${site.key.id}" name="doesntmatter" value="all" onclick="selectAllParticipantsCheckbox('${study.key.id}_${site.key.id}')">
                                                   &nbsp;All
                                              </input>
                                          </li>
                                      </ul>

                                      <ul id="${study.key.id}_${site.key.id}" class="subjects">
                                          <sitecoord:displaySubjects study="${study.key}" site="${site.key}" subjects="${study.value}"/>
                                      </ul>

                                  </form:form>
                               </li>
                            </c:forEach>
                      </ul>
                </laf:division>
            </c:forEach>
        </c:if>
  </laf:box>
  </body>
</html>