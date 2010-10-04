<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>

<html>
<head>
    <title>List Users</title>
    <tags:stylesheetLink name="yui-sam/2.7.0/datatable"/>
    <tags:stylesheetLink name="yui-sam/2.7.0/paginator"/>
    <c:forEach items="${fn:split('yahoo-dom-event element-min datasource-min logger-min json-min connection-min get-min datatable-min paginator-min', ' ')}" var="script">
        <tags:javascriptLink name="yui/2.7.0/${script}"/>
    </c:forEach>
    <style type="text/css">

    </style>
    <script type="text/javascript">
        var limit = 10;
        var usersPerPage = 3;

        function searchUser() {
            var searchString = $('usersSearchTextBox').value;
            if (searchString.blank()) {
                return;
            }
            if (!searchString.blank()) {
                processRequest(searchString, 0, true);
            }
        }

        function processRequest(user, offset, isCreateTable) {
            var uri = SC.relativeUri("/api/v1/users");
            var params = {};
            if (!user.blank()) {
                params.q = user;
            }
            params.offset = offset;
            params.limit = limit;

            SC.asyncRequest(uri+".json", {
                method: "GET", parameters: params,
                onSuccess: function(response) {
                    if (isCreateTable) {
                        createTable(response)
                    }
                }
            })
        }

        function createTable(response) {
            var myDataTable = jQuery(function () {
                var usersDataSource = new YAHOO.util.LocalDataSource(response.responseJSON.users);
                usersDataSource.responseSchema = {
                    fields: ['username', 'name', 'active', 'display_name']
                };

                var oConfigs = {
                    paginator: new YAHOO.widget.Paginator({
                        rowsPerPage: usersPerPage,
                        totalRecords: response.responseJSON.total
                    })
                };

                var MyApp = {
                    /* application API */
                    handlePagination : function (newState) {
                        var pageNumber = newState.page;
                        var offset = usersPerPage*(pageNumber-1);

                        processRequest($('usersSearchTextBox').value, offset, false)
                        // Collect page data using the requested page number
                        var pageContent = MyApp.getContent(newState.page);

                        // Update the content area
                        MyApp.replaceContent(pageContent);

                        // Update the Paginator's state
                        oConfigs.paginator.setState(newState);
                    }
                };

                oConfigs.paginator.subscribe('changeRequest',MyApp.handlePagination);

                var usersDataTable = new YAHOO.widget.DataTable("users", [
                    { key: "username",   label: "Username", sortable: true },
                    { key: "display_name", label: "Display Name",     sortable: true,
                        formatter: function (elCell, oRecord, oColumn, oData) {
                            elCell.innerHTML = "<a href='<c:url value="/pages/admin/users/one?user="/>" + oRecord.getData('username') + "'>" + oData + "</a>";
                        }
                    },
                    { key: "active",     label: "Status",  sortable: true,
                        formatter: function (elCell, oRecord, oColumn, oData) {
                            if (!oData) elCell.innerHTML = "Disabled";
                        }
                    },


                ], usersDataSource, oConfigs)
            });
        }

        function createFields() {
            var input = $('usersSubmitButton')
            input.observe('click', function() {
                searchUser()
            });
            processRequest("", 0, true)
        }

       Event.observe(window, "load", createFields)
    </script>
</head>
<body>
<laf:box title="List users" cssClass="yui-skin-sam">
    <laf:division>

     <label for="add-template">Search for user: </label>

     <input id="usersSearchTextBox" type="text" value=""/>
     <input id="usersSubmitButton" name="usersSubmitButton" type="button" value="Search"/>

     <label id="selected-user" style="display:none;"> Selected user: </label>
     <a class="primary" id="selected-user-itself"></a>


        <div class="row">
            <a href="<c:url value="/pages/admin/users/one"/>">Create user</a>
        </div>
        <div id="paginated">
            <div id="users">
                <tags:activityIndicator/> Users loading...
            </div>
        </div>

    </laf:division>
</laf:box>
</body>
</html>