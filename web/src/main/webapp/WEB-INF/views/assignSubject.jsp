<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>

<tags:javascriptLink name="psc-tools/misc"/>


<tags:stylesheetLink name="yui-sam/2.7.0/datatable"/>
<%-- TODO: move common YUI parts to a tag if they are re-used --%>
<c:forEach items="${fn:split('yahoo-dom-event element-min datasource-min logger-min json-min connection-min get-min datatable-min', ' ')}" var="script">
   <tags:javascriptLink name="yui/2.7.0/${script}"/>
</c:forEach>

<html>
<head>
    <title>Assign subject</title>
    <tags:includeScriptaculous/>
    <style type="text/css">
        div.submit {
            text-align: center;
        }
        form {
            width: 30em;
        }
        ul#population-checkboxes {
            margin: 0; padding: 0;
        }
        ul#population-checkboxes li {
            list-style-type: none
        }

        td.existingSubjectContent  {
            vertical-align:top;
        }

        .subject-list {
            padding-top:5em;
            padding-left:1em;
            width:50%
        }

        .mainTable{
            width:100%;
            border:1em;
        }
        .mainForm {
            width:100%;
        }

        .newSubjectContent{
            width:50%;
            border-right:1px solid black;
        }

        .existingSubjectContent{
            width:50%;
        }
        .commonForBothColumns {
            border-top: 1px solid black;
        }

        .commonDivToDisableForBoth {
            padding-left:33%;
            padding-right:33%;
            float:left;
        }

        .myLi {
            list-style-type: none;
            padding: 0 0 5px 0;
        }

        .myUl {
            margin: 0 0 0 9em;
            padding: 0;    
        }

    </style>

   <script type="text/javascript">

       var subjectAutocompleter;

       function createSubjectsAutocompleter() {
            subjectAutocompleter = new SC.FunctionalAutocompleter(
                'subjects-autocompleter-input', 'subjects-autocompleter-div', subjectAutocompleterChoices, {
                    select: "subjects-name",
                    afterUpdateElement: function(input, selected) {
                        input.value = ""
                        input.focus()
                    }
                }
            );
        }

        var subjectList;

        function subjectAutocompleterChoices(str, callback) {
            var searchString = $F("subjects-autocompleter-input")
            if (searchString == "Search for subjects") {
                searchString = ""
            }

            var uri = SC.relativeUri("/api/v1/subjects")
            if (searchString.blank()) {
                return;
            }

            var params = { };
            if (!searchString.blank()) params.q = searchString;

            SC.asyncRequest(uri, {
                method: "GET", parameters: params,
                onSuccess: function(response) {

                     var subjectListColumns = [

                       {key: "check", name:"checkbox", label: "",
                            formatter:  checkboxFormatter
                       },
                       { key: "first_name", name:"TableFirstName", value:"First Name", label: "First Name", sortable: true },
                       { key: "last_name", label: "Last Name", sortable: true },
                       { key: "date_of_birth", label: "Date of Birth", formatter: dateOfBirthFormat, sortable: true },
                       { key: "gender", label: "Gender", sortable: true },
                       { key: "person_id", label: "Person ID", sortable: true },
                       { key: "assignments", label: "Other Assignments", formatter:YAHOO.widget.DataTable.formatLink }

                    ];

                    var myDataSource = new YAHOO.util.DataSource(response.responseJSON);
                    myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;

                    myDataSource.responseSchema = {
                        resultsList : "results",
                        fields : [
                            {
                                key: "check", name:"checkbox", formatter: checkboxFormatter
                            },
                            { key: "first_name", name:"TableFirstName", value:"first_name"},
                            { key: "last_name" },
                            { key: "date_of_birth", formatter: dateOfBirthFormat},
                            { key: "gender"},
                            { key: "person_id" },
                            { key: "assignments",  formatter:YAHOO.widget.DataTable.formatLink },
                            { key: "hidden_assignments"},
                            { key: "grid_id"}
                        ]
                    };

                    subjectList = new YAHOO.widget.DataTable("subject-list", subjectListColumns, myDataSource, {scrollable:true});

                    subjectList.subscribe('checkboxClickEvent',function (e) {
                         var select = e.target.checked;
                         this.unselectAllRows();
                         if (select) {
                             this.selectRow(e.target);
                         }
                     });
                     subjectList.subscribe('rowSelectEvent', check);
                     subjectList.subscribe('rowUnselectEvent',uncheck);
                     subjectList.subscribe('unselectAllRowsEvent',uncheckAll);


                }
            })

            // Override the built-in formatter
	        YAHOO.widget.DataTable.formatLink = function(elCell, oRecord, oColumn, oData) {
               var defaultText = "The subject belongs to the "
               elCell.innerHTML = defaultText;

               var showMoreLink = jQuery('<a />').attr('href', '#').text('following studies').click(function () {
                   getCellInfo(elCell, oRecord, oColumn, oData)
                   return false;
               });
               jQuery(elCell).append(showMoreLink);
	        };
        };

        // Create a custom formatter for the checkboxes
        var checkboxFormatter = function (liner,rec,col,data) {
           var name = 'identifier';
           if (rec.getData('grid_id') != null && rec.getData('grid_id')!=""){
               liner.innerHTML = '<input name="'+name+'" type="checkbox" value="'+rec.getData('grid_id')+'">';
           } else {
               liner.innerHTML = '<input name="'+name+'" type="checkbox" value="'+rec.getData('person_id')+'">';
           }
        };

        // Add some convenience methods onto the prototype
        function check(e) {
           var cb = e.el.cells[0].getElementsByTagName('input')[0];
           cb.checked = true;
        };
        function uncheck(e) {
           var cb = e.el.cells[0].getElementsByTagName('input')[0];
           cb.checked = false;
        };
        function uncheckAll() {
           var name = 'identifier';
           var checks = document.getElementsByName(name),i;
           for (i = checks.length - 1; i >= 0; --i) {
               checks[i].checked = false;
           }
        };


       var dateOfBirthFormat = function(elCell, oRecord, oColumn, oData){
           var date_of_birth = oRecord.getData('date_of_birth')
           var date_of_birth_str = date_of_birth.split(' ')
           elCell.innerHTML = elCell.innerHTML + psc.tools.Dates.utcToDisplayDate(psc.tools.Dates.apiDateToUtc(date_of_birth_str[0]));
       };


        // Define a custom format function
        function getCellInfo(elCell, oRecord, oColumn, oData) {
            var container = jQuery('<div class="day"/>');
            var list = container.append('<ul class="myUl"/>')
            var text = "";
            var assignments = oRecord.getData('assignments'),
                assignment = undefined,
                i = 0;

            for(i = 0; i < assignments.length; ++i) {
                var listItem = jQuery('<li class="myLi" />');

                assignment = assignments[i];
                var site = assignment.site;
                var study = assignment.study;
                var start_date_str = assignment.start_date.split(' ');
                var start_date = psc.tools.Dates.utcToDisplayDate(psc.tools.Dates.apiDateToUtc(start_date_str[0]));

                text = "Subject is already enrolled to site '" + site + "' and study '" + study + "' as of start date '" + start_date +"'"
                var end_date_str = assignment.end_date;
                if (end_date_str != null && end_date_str.length> 0) {
                    end_date_str = end_date_str.split(' ');
                    var end_date = psc.tools.Dates.utcToDisplayDate(psc.tools.Dates.apiDateToUtc(end_date_str[0]));
                    text = text + " and till the end date '" + end_date + "'"
                }
                listItem.text(text)
                list.append(listItem);
            }

            jQuery(elCell).append(container);

            var hidden = oRecord.getData('hidden_assignments');
            if (oRecord.getData('hidden_assignments')){

                text = text + '<br>' +"- Note: There are one or more studies the subject belongs to and to which you don't have access."
                elCell.innerHTML = elCell.innerHTML + text + '<br>'
            }

            var showMoreLink = jQuery('<a />').attr('href', '#').text('Hide').click(function () {
                   container.remove();
                   return false;
               });
               container.append(showMoreLink);
        };



        function toggleAlert(buttonName) {
            if(buttonName.className == "newSubjRadioButton"){
                disableEnableElementsOfExistingSubject(true)
                <c:if test="${canCreateNewSubject}">
                    disableEnableElementsOfNewSubject(false)
                </c:if>
                disableEnableElementsForCommonDiv(false)
                $('submitBtn').disabled=false
            } else if (buttonName.className == "existingSubjRadioButton") {
                <c:if test="${canCreateNewSubject}">
                    disableEnableElementsOfNewSubject(true)
                </c:if>
                disableEnableElementsOfExistingSubject(false)
                disableEnableElementsForCommonDiv(false)
                $('submitBtn').disabled=false
            } else {
                $('submitBtn').disabled=true
            }

        }

        function disableEnableElementsOfExistingSubject(flag) {
            $('subjects-autocompleter-input').disabled = flag;
            if (flag) {
                $('subject-list').hide()
            } else {
                $('subject-list').show()
            }
        }

        function disableEnableElementsOfNewSubject(flag) {
            $('firstName').disabled = flag;
            $('lastName').disabled = flag;
            $('dateOfBirth').disabled = flag;
            $('gender').disabled = flag;
            $('personId').disabled = flag;

        }

        function disableEnableElementsForCommonDiv(flag) {
            $('studySubjectId').disabled = flag;
            var studySegmentElement = $('studySegment')
            if (studySegmentElement != null) {
                studySegmentElement.disabled = flag;
            }
            $('startDate').disabled = flag;
            var populationCheckboxes = $('population-checkboxes');
            if (populationCheckboxes != null) {
                populationCheckboxes.disabled = flag;
            }
        }

        function disableFields() {
            createSubjectsAutocompleter()
            <c:choose>
                <c:when test="${canCreateNewSubject}">
                    $('submitBtn').disabled='true'
                    disableEnableElementsForCommonDiv(true)
                    disableEnableElementsOfExistingSubject(true)
                    disableEnableElementsOfNewSubject(true)
                </c:when>
                <c:otherwise>
                    $('submitBtn').disabled= false
                    disableEnableElementsForCommonDiv(false)
                    disableEnableElementsOfExistingSubject(false)
                </c:otherwise>
            </c:choose>
       }

       Event.observe(window, "load", disableFields)

    </script>
</head>
<body>
<laf:box title="Assign Subject to Study ${study.assignedIdentifier}" cssClass="yui-skin-sam" autopad="true">

<c:url value="/pages/cal/assignSubject" var="action"/>
<form:form method="post" action="${action}" cssClass="mainForm">
    <table class="mainTable">
        <tr><form:errors path="*"/></tr>
        <tr class="subjectContent">
            <c:if test="${canCreateNewSubject}">
            <td class="newSubjectContent">
                <Input type = radio class="newSubjRadioButton" Name = radioButton Value = "new" onclick="toggleAlert(this)" > New Subject
                <c:if test="${not empty sites}">

                    <laf:division cssClass="divisionClass">
                            <div class="row">
                                <div class="label">
                                    <form:label path="firstName">First name *</form:label>
                                </div>
                                <div class="value">
                                    <form:input path="firstName"/>
                                </div>
                            </div>
                            <div class="row">
                                <div class="label">
                                    <form:label path="lastName">Last name *</form:label>
                                </div>
                                <div class="value">
                                    <form:input path="lastName"/>
                                </div>
                            </div>
                            <div class="row">
                                <div class="label">
                                    <c:set var="origPattern" value="${configuration.map.displayDateFormat}"/>
                                    <form:label path="dateOfBirth">Date of birth* (${fn:toLowerCase(origPattern)})</form:label>
                                </div>
                                <div class="value">
                                    <form:input path="dateOfBirth"/>
                                </div>
                                <!--for IE7 -> need to set the break, to get a new row be aligned along with others,
                                since the date is split into two rows and cause the wrong format for the next row-->
                                <br>
                            </div>
                            <div class="row">
                                <div class="label">
                                    <form:label path="gender">Gender</form:label>
                                </div>
                                <div class="value">
                                    <form:select path="gender">
                                        <form:options items="${genders}"/>
                                    </form:select>
                                </div>
                            </div>
                            <div class="row">
                                <div class="label">
                                    <form:label path="personId">Person ID</form:label>
                                </div>
                                <div class="value">
                                    <form:input path="personId"/>
                                </div>
                            </div>
                            <br />

                    </laf:division>
                </c:if>
            </td>
            </c:if>
            <td class="existingSubjectContent">
                <Input type = radio <c:if test="${!canCreateNewSubject}"> checked="true"</c:if>
                       class="existingSubjRadioButton" Name = radioButton Value = "existing" onclick="toggleAlert(this)" > Existing Subject
                <div class="commonDivToDisable2">
                    <div class="row">
                        <div class="label"> Name: </div>
                        <div class="value">
                            <input id="subjects-autocompleter-input" class="autocomplete input-hint" type="text" autocomplete="off" hint="Search for subjects" value=""/>
                            <div id="subjects-autocompleter-div" class="autocomplete" style="display: none;"/>
                        </div>
                    </div>

                    <div id="subject-list" class="subject-list">
                    </div>
                </div>
            </td>
        </tr>
        <tr>
            <td class="commonForBothColumns" colspan="2">
                <div class="commonDivToDisableForBoth">
                    <input type="hidden" name="study" value="${study.id}"/>
                    <div class="row">
                        <div class="label">
                            <form:label path="site">Site</form:label>
                        </div>
                        <div class="value">
                            <c:if test="${fn:length(sites) gt 1}">
                                <form:select path="site">
                                   <c:choose>
                                    <c:when test="${defaultSite != null}">
                                        <c:forEach items="${sites}" var="pair">
                                           <c:if test="${defaultSite eq pair.key.id}">
                                              <form:option value="${pair.key.id}" label="${pair.value}"/>
                                           </c:if>
                                        </c:forEach>
                                        <c:forEach items="${sites}" var="pair">
                                           <c:if test="${defaultSite != pair.key.id}">
                                              <form:option value="${pair.key.id}" label="${pair.value}"/>
                                           </c:if>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach items="${sites}" var="pair">
                                            <form:option value="${pair.key.id}" label="${pair.value}"/>
                                        </c:forEach>
                                    </c:otherwise>
                                    </c:choose>
                                </form:select>
                            </c:if>
                            <c:if test="${fn:length(sites) eq 1}">
                                <c:forEach items="${sites}" var="pair">
                                    ${pair.value}
                                    <input type="hidden" name="site" value="${pair.key.id}"/>
                                </c:forEach>
                            </c:if>
                        </div>
                    </div>
                    <div class="row">
                        <div class="label">
                            <form:label path="studySubjectId">Study subject ID</form:label>
                        </div>
                        <div class="value">
                            <form:input path="studySubjectId"/>
                        </div>
                    </div>

                    <c:if test="${not empty studySegments}">
                        <div class="row">
                            <div class="label">
                                <form:label path="studySegment">First study segment</form:label>
                            </div>
                            <div class="value">
                                <form:select path="studySegment">
                                    <c:forEach items="${studySegments}" var="sSegment">
                                            <form:option value="${sSegment.id}" label="${sSegment.qualifiedName}"/>
                                    </c:forEach>
                                </form:select>
                            </div>
                        </div>
                    </c:if>

                    <div class="row">
                        <div class="label">
                            <form:label path="startDate">Start date *</form:label>
                        </div>
                        <div class="value">
                            <laf:dateInput path="startDate"/>
                        </div>
                    </div>

                    <c:if test="${not empty populations}">
                        <div class="row">
                            <div class="label">
                                Populations
                            </div>
                            <div class="value">
                                <ul id="population-checkboxes">
                                <c:forEach items="${populations}" var="pop">
                                    <li><label>
                                        <form:checkbox path="populations" value="${pop.id}"/>
                                        ${pop.name}
                                    </label></li>
                                </c:forEach>
                                </ul>
                            </div>
                        </div>
                    </c:if>

                    <div class="row">
                        <div class="submit">
                            <input id="submitBtn" type="submit" value="Assign"/>
                        </div>
                    </div>
                </div>
            </td>
        </tr>

    </table>
 </form:form>
    <c:if test="${not empty unapprovedSites}">
        <h3>Unapproved sites</h3>
        <laf:division>
            <p>
                Please note:  the following sites are participating in the study, but the template
                has not yet been approved for them.  No subjects may be assigned until this happens.
                Please speak to a site coordinator if you have any questions.
            </p>
            <ul>
                <c:forEach items="${unapprovedSites}" var="site">
                    <li>${site.name}</li>
                </c:forEach>
            </ul>
        </laf:division>
    </c:if>
</laf:box>
</body>
</html>