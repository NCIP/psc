<%@taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<html>
<title>Activities</title>
<head>
    <tags:includeScriptaculous/>
    <tags:stylesheetLink name="main"/>
    <tags:stylesheetLink name="report"/>
    <tags:javascriptLink name="jquery/jquery.query" />
    <tags:stylesheetLink name="yui-sam/2.7.0/datatable"/>
    <tags:javascriptLink name="psc-tools/misc"/>

    <tags:stylesheetLink name="yui-sam/2.7.0/paginator"/>
    <c:forEach items="${fn:split('yahoo-dom-event element-min datasource-min logger-min json-min connection-min get-min datatable-min button-min paginator-min', ' ')}" var="script">
        <tags:javascriptLink name="yui/2.7.0/${script}"/>
    </c:forEach>
    <tags:javascriptLink name="underscore-min"/>

    <style type="text/css">
        table.query-results th.sortable a { background-image: url(<c:url value="/images/arrow_off.png"/>);
         background-position: left ; display: block}
        table.query-results th.order1 a { background-image: url(<c:url value="/images/arrow_down.png"/>) }
        table.query-results th.order2 a { background-image: url(<c:url value="/images/arrow_up.png"/>) }


        a#newActivityLink {
            padding-left: 2em
        }

        #activities-input label {
            font-weight: bold;
        }

        #activities-input {
            margin: 1em 0;
            background-color: #ddd;
            padding: 0.5em;
        }

        #newActivity label {
            font-weight: bold;
        }

        #newActivity {
            background-color: #ddd;
        }

        #activityName label {
            margin-left:10em;
        }

        #createNewActivity label {
            margin-bottom:1em;
        }

        #exportOptions {
            padding: 0 1em 0 1em;
        }

        .underlined {
            text-decoration:underline;
            display:none
        }

        #bottomsTop input.pageNumberSelected, #bottomsBottom input.pageNumberSelected {
            background-color:lightskyblue;
        }

        #bottomsTop input.pageNumberDefault, #bottomsBottom input.pageNumberDefault {
            background-color:#3876C1;
        }

    </style>

    <script type="text/javascript">

        var activitiesAutocompleter;

        function initMethods() {
            var indicator = $('myIndicator')
            indicator.conceal()
            var val = $('sources').getValue();
            if (val == null || val == "") {
                $('sources').setValue("");
            }
            registerEventHandlers();
            enableExportOptions();
        }


        function registerEventHandlers() {
            var input = $('addSourceButton')
            input.observe('click', function() {
                executeAddSource(input)
            });

            var exportActivitiesLinkXML = $('exportActivitiesLinkXML')
            exportActivitiesLinkXML.observe('click', function(){
                exportActivitiesToXML(".xml")
            });

            var exportActivitiesLinkCSV = $('exportActivitiesLinkCSV')
            exportActivitiesLinkCSV.observe('click', function(){
                exportActivitiesToXML(".csv")
            });

            var exportActivitiesLinkXLS = $('exportActivitiesLinkXLS')
            exportActivitiesLinkXLS.observe('click', function(){
                exportActivitiesToXML(".xls")
            });
        }

        function displayErrorOnFailure(response, indicator){
            indicator.conceal()
            var divError = $('errors')
            var myDIV = document.createElement('div');
            myDIV.innerHTML=response.responseText;
            var errorFromResponse = Builder.node('h4', {id:'error'}, "Error occurs during the request process. Status is "+ response.getStatus() + ". Caused by " + myDIV.getElementsByClassName('message')[0].innerHTML);
            divError.appendChild(errorFromResponse)
        }

        //if response had errors, the error label is cleared when next response comes in.
        function clearErrorMessage(){
            if (jQuery('#error').size() >0) {
                jQuery('#error').remove()
            }
        }

        function displayErrorOnResponse(response) {
            var fullText = response.responseText;
            var statusCode = response.status
            var statusText = response.statusText
            var userFriendlyText = fullText.replace(statusCode,  "");
            userFriendlyText = userFriendlyText.replace(statusText, "");
            var typeLabel = jQuery('<label id="error"/>').text(userFriendlyText.trim());
            jQuery('#errors').append(typeLabel)
        }

        function executeAddSource(input) {
            var newSource = $('addSource').value
            var url = psc.tools.Uris.relative('/api/v1/activities/' + newSource);
            jQuery.ajax({
                url: url,
                type: 'PUT',
                contentType: 'text/xml',
                data: '<source name=\"' + newSource +'\"/>',
                success :function() {
                    $('addSource').value =""
                    var newSourceEtl = jQuery('<option selected="selected" value="'+ newSource +'"/>').text(newSource);
                    jQuery('#sources').append(newSourceEtl);
                    jQuery('#sources').trigger("change");
                },
                failure: function(response) {
                    displayErrorOnResponse(response)
                },
                error: function(response) {
                    displayErrorOnResponse(response)
                }
            });
        }

        (function($){
            var PSC = { activities: {} };
            PSC.activities.XHRDataSource = function(oLiveDataCallback, oConfigs) {
                this.liveDataCallback = oLiveDataCallback || function() {return ""};
                PSC.activities.XHRDataSource.superclass.constructor.call(this, oLiveDataCallback.call(), oConfigs);
            };
            YAHOO.lang.extend(PSC.activities.XHRDataSource, YAHOO.util.XHRDataSource);
            PSC.activities.XHRDataSource.prototype.makeConnection = function(oRequest, oCallback, oCaller) {
                this.liveData = this.liveDataCallback.call();
                PSC.activities.XHRDataSource.superclass.makeConnection.call(this, oRequest, oCallback, oCaller);
            }

            var ACTIVITIES_PER_PAGE = 100;
            var DEFAULT = psc.tools.Uris.relative("/api/v1/activities.json?limit=" + ACTIVITIES_PER_PAGE + "&");
            var dataSource = new PSC.activities.XHRDataSource(function() {
                if ($('#sources').val() != "" && $('#sources').val() != "selectAll") {
                    var source = $('#sources').val();
                    var url = psc.tools.Uris.relative("/api/v1/activities/" + source + ".json?limit=" + ACTIVITIES_PER_PAGE + "&");
                }
                return url || DEFAULT;
            });
            dataSource.responseSchema = {
                resultsList: "activities",
                fields: [
                    {key: 'activity_name', parser: "string"},
                    {key: 'activity_type', parser: "string"},
                    {key: 'activity_code', parser: "string"},
                    {key: 'activity_description', parser: "string"},
                    {key: 'controls'},
                    {key: 'activity_id'},
                    {key: 'deletable'}
                ],
                metaFields: {
                    totalRecords: "total",
                    activityTypes: "activity_types"
                }
            };

            var dataTableAuxConfig = {
                generateRequest: function (oState, oSelf) {
                    oState = oState || { pagination: null, sortedBy: null };
                    var sort = (oState.sortedBy) ? oState.sortedBy.key : "myDefaultColumnKey";
                    var dir = (oState.sortedBy && oState.sortedBy.dir === YAHOO.widget.DataTable.CLASS_DESC) ? "desc" : "asc";
                    var offset = (oState.pagination) ? oState.pagination.recordOffset : 0;
                    var limit = (oState.pagination) ? oState.pagination.rowsPerPage : 100;

                    // Build custom request
                    return  "sort=" + sort +
                            "&order=" + dir +
                            "&limit=" + limit +
                            "&offset=" + offset;
                },
                paginator: new YAHOO.widget.Paginator({
                    rowsPerPage: ACTIVITIES_PER_PAGE,
                    template: "{PreviousPageLink} {PageLinks} {NextPageLink}"
                }),
                dynamicData: true,
                initialLoad: false

            };


            //todo - activityTypes would be smart to withdraw in a separate request, since it's the same for all the activities/and pagination
            var activityTypes;
            var clicked = false;
            function createTable() {
                var dt = new YAHOO.widget.DataTable("results", [
                    { key: "activity_name",     label: "Name", sortable: true,
                        formatter: nameFormatter},
                    { key: "activity_type", label: "Type",     sortable: true,
                        formatter: typeFormatter},
                    { key: "activity_code", label: "Code", sortable: false,
                        formatter: codeFormatter},
                    { key: "activity_description", label: "Description",  sortable: false,
                        formatter: descriptionFormatter},
                    { key: "controls", label: "Controls",  sortable: false,
                        formatter: controlsFormatter
                    }
                ], dataSource, dataTableAuxConfig);
                dt.handleDataReturnPayload = function (oRequest, oResponse, oPayload) {
                    oPayload.totalRecords = oResponse.meta.totalRecords;
                    activityTypes = oResponse.meta.activityTypes;
                    return oPayload;
                };
                dt.subscribe('buttonClickEvent', editSaveDeleteButtonClicked);

                var btnCreateNewActivity = new YAHOO.widget.Button("createNewActivityButton");
                btnCreateNewActivity.subscribe("click", function() {
                    createNewActivity()
                },this,true);

                var btnSaveNewActivity = new YAHOO.widget.Button("saveNewActivityButton");
                btnSaveNewActivity.subscribe("click", function() {
                    createAndSaveNewActivity()
                },this,true);

                var btnClearActivityInfo = new YAHOO.widget.Button("clearActivityInfoButton");
                btnClearActivityInfo.subscribe("click", function() {
                    clearActivity();
                },this,true);

                return dt;
            }

            function saveActivity(activityId) {
                //activityId is null for the new activity
                if (activityId == null) {
                    activityId = ""
                }
                var activityName = $('#inputName'+activityId).val()
                var activityTypeId = $('#sourceTypes'+activityId).val();
                var activityTypeName = $('#sourceTypes'+activityId + ' :selected').text();
                var activityCode = $('#inputCode'+activityId).val()
                var activityDescription = $('#inputDescription' + activityId).val()
                var activitySource= $('#sources').val()

                var element = '<Activity name=\"' + activityName +'\" code=\"' + activityCode +'\" description=\"'
                        + activityName +'\" type=\"'+activityTypeName+'\" type-id=\"' + activityTypeId +'\" source=\"'
                        + activitySource + '\"/>'

                clearErrorMessage();
                $.ajax({
                    url: psc.tools.Uris.relative("/api/v1/activities/"+activitySource+"/"+ activityCode),
                    type: 'PUT',
                    contentType: 'text/xml',
                    data: element,
                    success : function() { sendRequest() },
                    error: function(response) {
                        displayErrorOnResponse(response)
                    }
                });
            }

            function createAndSaveNewActivity() {
                var activityCode = $('#inputCode').val()
                var activitySource= $('#sources').val()

                $.ajax({
                    url: psc.tools.Uris.relative("/api/v1/activities/"+activitySource+"/"+ activityCode),
                    type: 'GET',
                    success : function(response) {
                        var typeLabel = jQuery('<label id="error"/>').text("The activity with this code already exists for this source. Please specify the different code");
                        jQuery('#errors').append(typeLabel)
                    },
                    error: function(response) {
                        if (response.status == 404) {
                            saveActivity(null)
                        }
                    }
                });
            }

            function clearActivity() {
                $('#inputName').val("");
                $('#sourceTypes').val("");
                $('#inputCode').val("")
                $('#inputDescription').val("")
            }

            function deleteActivity(activityId) {
                var activityName = $('#inputName'+activityId).val()
                var activityTypeName = $('#sourceTypes'+activityId + ' :selected').text();
                var activityCode = $('#inputCode'+activityId).val()
                var activitySource= $('#sources').val()

                var confirmMessage = "Are you sure you want to delete activity: [name= " + activityName +", type= " + activityTypeName + ",code= " +
                                    activityCode+ "] ? This will permanently remove it.  " +
                                    "\n\nThis action cannot be undone."

                if (!window.confirm(confirmMessage)) {
                    return false;
                }
                clearErrorMessage();
                $.ajax({
                    url: psc.tools.Uris.relative("/api/v1/activities/"+activitySource+"/"+ activityCode),
                    type: 'DELETE',
                    success : function() { sendRequest () }
                });
                return true;
            }

            function sendRequest (params) {
                clearErrorMessage()
                dataSource.sendRequest(params, {
                    success:
                        dataTable.onDataReturnInitializeTable,
                    failure: function(request, response, payload){
                        displayErrorOnResponse(response)
                    },
                    scope: dataTable,
                    argument: dataTable.getState()
                });
            }

            function editSaveDeleteButtonClicked(e, button) {
                var buttonId = e.target.id;
                var editAction = "edit";
                var saveAction = "save";
                var deleteAction = "delete";
                var advancedEditAction = "advancedEdit";
                var activityId;
                if (buttonId.toLowerCase().startsWith(editAction)){
                    //edit action just displays editable fields for the row
                    activityId = getActivityIdFromTheButton(buttonId, editAction);
                    editActivity(activityId)
                } else if (buttonId.toLowerCase().startsWith(saveAction)) {
                    activityId = getActivityIdFromTheButton(buttonId, saveAction);
                    //when save button from the controls is pressed, the action that is performed is edit
                    saveActivity(activityId);
                } else if (buttonId.toLowerCase().startsWith(deleteAction)) {
                    activityId = getActivityIdFromTheButton(buttonId, deleteAction);
                    deleteActivity(activityId);
                } else if (buttonId.toLowerCase().startsWith(advancedEditAction.toLowerCase())) {
                    activityId = getActivityIdFromTheButton(buttonId, advancedEditAction);
                    advancedEdit(activityId, advancedEditAction);
                }
            }

            function advancedEdit(activityId, action){
                var href = '<c:url value="/pages/activities/edit"/>'
                var data = "?activityId=" + activityId;
                location.href = href+data;
            }

            function getActivityIdFromTheButton (buttonId, action) {
                return buttonId.substr(action.length, buttonId.length)
            }

            function createNewActivity() {
                if (!clicked) {
                    var typeInput = $('<select id="sourceTypes" name="sourceTypes">')
                    for (var i =0; i < activityTypes.length; i++) {
                        var activityTypeId = activityTypes[i].activity_type_id
                        var activityTypeName = activityTypes[i].activity_type_name
                        var row = $('<option id="type" class="type" value="'+ activityTypeId +'"/>').text(activityTypeName);
                        typeInput.append(row);
                    }

                    $('#divType').append(typeInput)
                    $('#newActivityInfo').show();
                    clicked = true;
                }
            }

            function editActivity(activityId) {
                var inputName = '#inputName'+activityId;
                var labelName = '#name'+activityId;
                $(inputName).show();
                $(labelName).hide();


//                var inputCode = '#inputCode'+activityId;
//                var labelCode = '#code'+activityId;
//                $(inputCode).show();
//                $(labelCode).hide();

                var inputDescription = '#inputDescription'+activityId;
                var labelDescription = '#description'+activityId;
                $(inputDescription).show();
                $(labelDescription).hide();

                var divType ='#divType'+activityId;
                var labelType = '#type'+activityId;
                $(labelType).hide();
                $(divType).show();
                var selectType = '#sourceTypes'+activityId;

                var editButton = '#edit'+activityId;
                $(editButton).hide();

                var saveButton = '#save'+activityId;
                $(saveButton).show();
            }

            function typeFormatter (elCell, oRecord, oColumn, oData) {
                var activityType = oRecord.getData('activity_type');
                var activityId = oRecord.getData('activity_id');

                var container = $('<div class="activityType"/>');
                var typeLabel = $('<label id="type' + activityId + '"/>').text(activityType);

                var typeDiv = $('<div id="divType'+activityId+'" style="display:none"/>');
                    var select = $('<select id="sourceTypes'+activityId+'">')

                    for (var i =0; i < activityTypes.length; i++) {
                        var activityTypeId = activityTypes[i].activity_type_id
                        var activityTypeName = activityTypes[i].activity_type_name

                        var row;
                        if (activityType == activityTypeName) {
                            row = $('<option value="'+ activityTypeId +'" selected="selected"/>').text(activityTypeName);
                        } else {
                            row = $('<option value="'+ activityTypeId +'"/>').text(activityTypeName);
                        }
                        select.append(row);
                    }
                typeDiv.append(select);
                container.append(typeLabel);
                container.append(typeDiv);
                $(elCell).append(container);
            }

            function nameFormatter (elCell, oRecord, oColumn, oData) {
                cellEditor(elCell, oRecord, "Name");
            }

            function descriptionFormatter(elCell, oRecord, oColumn, oData) {
                cellEditor(elCell, oRecord, "Description")
            }

            function codeFormatter(elCell, oRecord, oColumn, oData) {
                cellEditor (elCell, oRecord, "Code");
            }


            //common method to add input/label pair for column/row cell.
            function cellEditor (elCell, oRecord, columnName) {
                //columnName = Description, Code, Name
                var record = "activity_"+columnName.toLocaleLowerCase();
                var className = "activity"+columnName;
                var activityEltForColumn= oRecord.getData(record);
                var activityId = oRecord.getData('activity_id');
                var container = $('<div class="'+className +'"/>');
                var label = $('<label id="' + columnName.toLowerCase() + activityId + '"/>').text(activityEltForColumn);
                var input = $('<input id="input' + columnName + activityId + '" type="text" value="' + activityEltForColumn + '" style="display:none"/>');
                container.append(label);
                container.append(input);
                $(elCell).append(container);
            }

            function controlsFormatter (elCell, oRecord, oColumn, oData) {
                var container = $('<div class="controls"/>');
                var activityId = oRecord.getData('activity_id');

                var editButton = $('<input id="edit' + activityId + '" type="button" name="EditButton" value="Edit"/>')
                var saveButton = $('<input id="save' + activityId + '" type="button" name="SaveButton" value="Save" style="display:none"/>')
                var advanceEditButton = $('<input id="advancedEdit' + activityId + '" type="button" name="AdvancedEditButton" value="Advanced edit"/>')

                var isDeletable = oRecord.getData('deletable');

                container.append(editButton)
                container.append(saveButton)
                container.append(advanceEditButton)

                if (isDeletable == "true") {
                    var deleteButton = $('<input id="delete' + activityId + '" type="button" name="DeleteButton" value="Delete"/>')
                    container.append(deleteButton);
                }
                $(elCell).append(container)
            }

            var sourceId = "";
            function search(sourceName) {
                if (sourceName == null) {
                    sourceName = $('#sources').val();
                } else {
                    $('#sources').val(sourceName);
                }
                dataTableAuxConfig.paginator.set('recordOffset', 0);
                clearErrorMessage();
                enableExportOptions();
                if (sourceName != "") {
                    sendRequest();
                }
                return false;
            }

            var dataTable;
            $(function () {
                $('#sources').change(function() {search(null)});

                //this call happens when 'import activities from XML or CSV' is pressed and sourceName and sourceId returned as the URL params
                $(window).load(function() {search("${param.sourceName}")});

                dataTable = createTable() ;
                dataTable.subscribe("initEvent", function(){
                    $('#addActivity').show()
                });
            })
        }(jQuery));

        function enableExportOptions(){
            var val = $('sources').getValue()
            if(val == "" || val == "selectAll") {
                $('exportOptions').style.display = "none"
                $('exportActivitiesLinkXML').style.display = "none"
                $('exportActivitiesLinkCSV').style.display = "none"
                $('exportActivitiesLinkXLS').style.display = "none"
            } else {
                $('exportOptions').style.display = "inline"
                $('exportActivitiesLinkXML').style.display = "inline"
                $('exportActivitiesLinkCSV').style.display = "inline"
                $('exportActivitiesLinkXLS').style.display = "inline"
            }
        }

        function exportActivitiesToXML(extension) {
            var activitySource= $('sources').options[$('sources').selectedIndex].value

            var href = '<c:url value="/pages/activities/display/"/>'
            var data = activitySource+extension;

            location.href = href+data;
        }

        Event.observe(window, "load", initMethods)

    </script>
</head>
<body>
    <laf:box title="Activities" cssClass="yui-skin-sam">
        <laf:division>
            <div id="activities-input">
                <img id="myIndicator" src="<c:url value="/images/indicator.white.gif"/>"/>
                <label for="source">Source:</label>
                <select id="sources" name="sources">
                    <option value="">Select... </option>
                    <c:forEach items="${sources}" var="source">
                        <c:if test="${sourceId == source.id}">
                            <option value="${source.name}" selected="true">${source.name}<c:if test="${source.manualFlag}"> (Manual Target) </c:if></option>
                        </c:if>
                        <c:if test="${sourceId != source.id}">
                            <option value="${source.name}">${source.name}<c:if test="${source.manualFlag}"> (Manual Target) </c:if></option>
                        </c:if>
                    </c:forEach>
                    <option value="selectAll">All sources</option>
                </select>

                <label id="add-new-source">Create new source:</label>
                <input id="addSource" type="text" class="addSource" value=""/>
                <input type="button" id="addSourceButton" name="addSourceButton" value="Add" />
                <a id="importActivitiesLink" href="<c:url value="/pages/activities/importActivities"/>" >Import activities from xml or from csv</a>

                <span id="exportOptions" style="display:none"> Export Options:</span>
                <a id="exportActivitiesLinkXML" class="underlined">XML</a>
                <a id="exportActivitiesLinkCSV" class="underlined">CSV</a>
                <a id="exportActivitiesLinkXLS" class="underlined">Excel</a>
            </div>

            <div id="errors" style="margin-right:10px; margin-left:0.5em;">
                <form:errors path="*"/>
            </div>
            <div id="newActivity" style="margin-bottom:10px;"></div>
            <div id="bottomsTop">

            </div>

            <div id="addActivity" style="display:none">
                <input type="button" id="createNewActivityButton" name="createNewActivityButton" value="Create new activity" />
            </div>
            <div id="newActivityInfo" style="display:none;">
                <div class="row">
                    <div class="label">Name</div>
                    <div class="value"><input id="inputName"/></div>
                </div>
                <div class="row">
                    <div class="label">Code</div>
                    <div class="value"><input id="inputCode"/></div>
                </div>
                <div class="row">
                    <div class="label">Type</div>
                    <div class="value" id="divType"></div>
                </div>
                <div class="row">
                    <div class="label">Description</div>
                    <div class="value"><input  id="inputDescription"/></div>
                </div>
                <div class="row">
                    <div class="value">
                        <input type="button" id="saveNewActivityButton" name="saveNewActivityButton" value="Save" />
                        <input type="button" id="clearActivityInfoButton" name="clearActivityInfoButton" value="Clear" />
                    </div>

                </div>
            </div>

            <div id="results" class="results">
            </div>
    </laf:division>
    </laf:box>
</body>
</html>