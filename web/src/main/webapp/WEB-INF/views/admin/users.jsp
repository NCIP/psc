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
    <tags:javascriptLink name="underscore-min"/>
    <tags:javascriptLink name="psc-tools/misc"/>
    <style type="text/css">
        #yui-dt0-paginator1 {
            display: none; /* can't get the expected mechanism to do this */
        }
    </style>
    <script type="text/javascript">
        (function ($) {
            var USERS_PER_PAGE = 10;

            var dataSource = new YAHOO.util.XHRDataSource(
                psc.tools.Uris.relative("/api/v1/users.json?brief=false&limit=" + USERS_PER_PAGE));
            dataSource.responseSchema = {
                resultsList: "users",
                fields: ['username', 'display_name', 'end_date', 'roles' ],
                metaFields: {
                    totalRecords: "total"
                }
            };

            var dataTableAuxConfig = {
                generateRequest: function (oState, oSelf) {
                    var params = "&offset=" + oState.pagination.recordOffset;
                    if (searchText !== "") {
                        params += "&q=" + searchText;
                    }
                    return params;
                },
                paginator: new YAHOO.widget.Paginator({
                    rowsPerPage: USERS_PER_PAGE,
                    template: "{PreviousPageLink} {PageLinks} {NextPageLink}"
                }),
                dynamicData: true
            };

            function userAdminPageUrl(username) {
                return "<c:url value="/pages/admin/users/one?user="/>" + username;
            }

            function createTable() {
                var dt = new YAHOO.widget.DataTable("users", [
                    { key: "username",     label: "Username", sortable: false },
                    { key: "display_name", label: "Name",     sortable: false,
                        formatter: function (elCell, oRecord, oColumn, oData) {
                            elCell.innerHTML =
                                "<a href='" + userAdminPageUrl(oRecord.getData("username")) + "'>" +
                                    oData + "</a>";
                        }
                    },
                    { key: "roles", label: "Roles", sortable: false, formatter: rolesFormatter },
                    { key: "active",       label: "Status",  sortable: false,
                        formatter: function (elCell, oRecord, oColumn, oData) {
                            var endDate = oRecord.getData("end_date") &&
                                psc.tools.Dates.apiDateToUtc(oRecord.getData("end_date"));
                            if (endDate && endDate < new Date()) elCell.innerHTML = "Disabled";
                        }
                    }
                ], dataSource, dataTableAuxConfig);
                dt.handleDataReturnPayload = function (oRequest, oResponse, oPayload) {
                    oPayload.totalRecords = oResponse.meta.totalRecords;
                    return oPayload;
                };
                return dt;
            }

            function rolesFormatter(elCell, oRecord, oColumn, oData) {
                var roles = _(oRecord.getData("roles")).pluck("display_name");
                if (roles.length <= 2) {
                    elCell.innerHTML = roles.join(", ");
                } else {
                    elCell.innerHTML = roles.slice(0, 2).join(", ") +
                        ", <a href='" + userAdminPageUrl(oRecord.getData("username")) + "'>and " +
                        (roles.length - 2) + " more...";
                }
            }

            var searchText = "";
            function search() {
                searchText = $('#q').val();
                dataTableAuxConfig.paginator.set('recordOffset', 0);
                dataSource.sendRequest("&q=" + searchText, {
                    success: dataTable.onDataReturnInitializeTable,
                    failure: dataTable.onDataReturnInitializeTable,
                    scope: dataTable,
                    argument: dataTable.getState()
                });
                return false;
            }

            var dataTable;
            $(function () {
                $('#user-search').submit(search);
                dataTable = createTable();
            })
        }(jQuery));
    </script>
</head>
<body>
<laf:box title="List users" cssClass="yui-skin-sam">
    <laf:division>

        <div class="row">
            <a href="<c:url value="/pages/admin/users/one"/>">Create user</a>
        </div>

        <div class="row">
            <form id="user-search" action="#">
                <label>
                    Search for user:
                    <input id="q" name="q" type="text" value=""/>
                </label>

                <input name="usersSubmitButton" type="submit" value="Search"/>
            </form>
        </div>

        <div id="users" class="row">
            <tags:activityIndicator/> Users loading...
        </div>

        <div class="row instructions">
            This table is sorted by last name (if any), then first name (if any),
            then username.
        </div>
    </laf:division>
</laf:box>
</body>
</html>