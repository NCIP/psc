<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<html>
<head>
    <title>Plugins list</title>
    <tags:stylesheetLink name="admin"/>
    <tags:stylesheetLink name="yui-sam/2.7.0/datatable"/>
    <%-- TODO: move common YUI parts to a tag if they are re-used --%>
    <c:forEach items="${fn:split('yahoo-dom-event element-min datasource-min logger-min json-min connection-min get-min datatable-min', ' ')}" var="script">
        <tags:javascriptLink name="yui/2.7.0/${script}"/>
    </c:forEach>
    <script type="text/javascript">
        YAHOO.widget.Logger.enableBrowserConsole();

        function startBundle(evt) { changeBundleState(evt, "STARTING"); }
        function stopBundle(evt)  { changeBundleState(evt, "STOPPING"); }

        function changeBundleState(clickEvt, newState) {
            var elt = Event.element(clickEvt);
            var bundleId = elt.id.split('-')[1];
            var rowId = elt.up("tr").id;
            var indicator = $("bundle-" + bundleId + "-indicator");
            indicator.reveal()
            YAHOO.util.Connect.initHeader("Content-Type", "application/json")
            YAHOO.util.Connect.asyncRequest('PUT',
                INTERNAL_URI_BASE_PATH + "api/v1/osgi/bundles/" + bundleId + "/state.json",
                {
                    success: function(o) {
                        updateBundleRowState(rowId, bundleId, indicator);
                    },
                    failure: function() {
                        indicator.conceal();
                    }
                },
                YAHOO.lang.JSON.stringify({ state: newState })
            );
        }

        function updateBundleRowState(rowId, bundleId, indicator) {
            indicator.reveal();
            YAHOO.util.Connect.asyncRequest("GET",
                INTERNAL_URI_BASE_PATH + "api/v1/osgi/bundles/" + bundleId + ".json",
                {
                    success: function(o) {
                        var data = YAHOO.lang.JSON.parse(o.responseText);
                        bundleList.updateRow(rowId, data);
                        indicator.conceal();
                    },
                    failure: function(o) {
                        indicator.conceal();
                    }
                });

        }

        var bundleList;
        function setupBundleList() {
            var bundleListColumns = [
                { key: "id", label: "ID", sortable: true },
                { key: "name", label: "Name", sortable: true },
                {
                    key: "state", label: "State", sortable: true,
                    formatter: function(elCell, oRecord, oColumn, oData) {
                        elCell.innerHTML = oData.toLowerCase(); 
                    }
                },
                {
                    key: "state_switch", label: "",
                    formatter: function(elCell, oRecord, oColumn, oData) {
                        var id = oRecord.getData()['id']
                        Element.addClassName(elCell, "bundle-" + id);
                        var started = $w("STARTING ACTIVE").include(oRecord.getData()['state'])
                        var start_disabled = started ? "disabled='disabled'" : "";
                        var stop_disabled = started ? "" : "disabled='disabled'";
                        elCell.innerHTML =
                            "<input id='bundle-" + id + "-start' class='start' type='button' value='Start' " + start_disabled + ">&nbsp;" +
                            "<input id='bundle-" + id + "-stop' class='stop' type='button' value='Stop' " + stop_disabled + ">" +
                            '<tags:activityIndicator id="bundle-' + id + '-indicator"/>';
                    }
                }
            ];

            var dataSource = new YAHOO.util.XHRDataSource(INTERNAL_URI_BASE_PATH + "api/v1/osgi/bundles.json");
            dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSARRAY;

            bundleList = new YAHOO.widget.DataTable("bundle-list", bundleListColumns, dataSource, {
                sortedBy: { key: 'id' }
            });

            bundleList.subscribe("postRenderEvent", function() {
                $$('input.start').each(function(startButton) {
                    startButton.observe("click", startBundle);
                });
                $$('input.stop').each(function(stopButton) {
                    stopButton.observe("click", stopBundle);
                });
            });
        }
        $(document).observe("dom:loaded", setupBundleList);
    </script>
</head>
<body>
<laf:box title="Plugins" cssClass="yui-skin-sam" autopad="true">
    <p>
        PSC's plugin layer is based on OSGi bundles.  This is a list of all the currently
        loaded bundles (including bundles which are only used by other bundles).
    </p>
    <p>
        Changes to the run status of bundles in this page will be persisted across PSC restarts.
        However, whenever you redeploy PSC (e.g., when upgrading) the active/inactive states of
        all bundles will be reset to their defaults.
    </p>
    <p>The <em>State</em> column indicates the status of each bundle.  Rough meanings of the states:</p>
    <ul>
        <li><strong>installed</strong>: the bundle is available for other bundles to use, but is not currently being used.</li>
        <li><strong>resolved</strong>: the bundle is being used by other bundles, but is not active itself.</li>
        <li>
            <strong>active</strong>: the bundle is running.
            Any functionality it provides is available for PSC to use.  E.g., authentication plugins
            may be selected only when they are in this state.  Similarly, data providers will only
            be used when they are in this state.
        </li>
    </ul>
    <p>
        <strong>Be very careful</strong>: if you deactivate the wrong bundle, it is possible to render
        PSC unstartable.  If this happens, you'll need to cleanly redeploy the PSC WAR.
    </p>

    <div id="bundle-list">
    </div>
</laf:box>
</body>
</html>