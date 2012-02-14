<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<html>
<head>
    <title>Plugins list</title>
    <tags:stylesheetLink name="admin"/>
    <tags:stylesheetLink name="yui-sam/2.7.0/datatable"/>
    <style type="text/css">
        .yui-dt-col-state_switch {
            white-space: nowrap;
        }
    </style>

    <%-- TODO: move common YUI parts to a tag if they are re-used --%>
    <c:forEach items="${fn:split('yahoo-dom-event element-min datasource-min logger-min json-min connection-min get-min datatable-min', ' ')}" var="script">
        <tags:javascriptLink name="yui/2.7.0/${script}"/>
    </c:forEach>
    <tags:javascriptLink name="psc-tools/misc"/>
    <tags:javascriptLink name="jquery/jquery.enumerable"/>
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
                INTERNAL_URI_BASE_PATH + "api/v1/osgi/bundles/" + bundleId + "/state",
                {
                    success: function(o) {
                        updateBundleRow(rowId, bundleId, indicator);
                    },
                    failure: function(o) {
                        alert("Changing bundle " + bundleId + " state to " + newState + 
                          " failed: " + o.status + " " + o.statusText + 
                          "\n\n" + o.responseText);
                        indicator.conceal();
                    }
                },
                YAHOO.lang.JSON.stringify({ state: newState })
            );
        }

        function updateBundleRow(rowId, bundleId, indicator) {
            indicator.reveal();
            getBundleInfo(bundleId, {
                success: function(o) {
                    var data = YAHOO.lang.JSON.parse(o.responseText);
                    bundleList.updateRow(rowId, data);
                    indicator.conceal();
                },
                failure: function(o) {
                    alert("Updating row display for " + bundleId + 
                      " failed: " + o.status + " " + o.statusText + 
                      "\n\n" + o.responseText);
                    indicator.conceal();
                }
            });
        }

        function getBundleInfo(bundleId, callbacks) {
            YAHOO.util.Connect.asyncRequest("GET",
                INTERNAL_URI_BASE_PATH + "api/v1/osgi/bundles/" + bundleId + ".json", callbacks);
        }

        function configureService(clickEvt) {
            var button = Event.element(clickEvt);
            var serviceId = parseInt(button.id.split('-')[1]);
            (function ($) {
                $('#configure-service-loading-indicator').css('visibility', 'hidden')
                $('#configure-service-loading').show();
                $('#configure-service-fields').hide();
                $('#configure-service-controls').data('service-id', serviceId);
                LB.Lightbox.activate();
                getBundleInfo(bundleList.getRecord(button.up('tr')).getData()['id'], {
                    success: function (o) {
                        var bundle = YAHOO.lang.JSON.parse(o.responseText);
                        var service = $(bundle.services).select(function () {
                            return this.properties && this.properties.service.id == serviceId;
                        })[0];
                        if (!service) {
                            LB.Lightbox.deactivate();
                            alert("Target service is no longer live for configuring");
                            return;
                        }
                        $('#configure-service-name').text(service.properties.service.pid);
                        $('#configure-service-controls').data('bundle-id', bundle.id);
                        $('#configure-service-fields').empty();
                        $(service.metatype.attributes).each(function (i) {
                            var currentValue = extractDotSeparatedProperty(service.properties, this.id);
                            if (currentValue === null || currentValue === undefined) {
                                currentValue = "";
                            }
                            $('#configure-service-fields').append(
                                "<div class='row'><div class='label'>" + this.name +
                                "</div><div class='value'><input type='text' name='" + this.id +
                                "' value='" + currentValue + "'></div>")
                        });
                        $('#configure-service-loading').hide();
                        $('#configure-service-fields').show();
                    },
                    failure: function (o) {
                        alert("Could not load data for service");
                        LB.Lightbox.deactivate();
                    }
                });
            }(jQuery));
        }

        function extractCurrentPropertyValue(serviceInfo, metatypeId) {
            return extractDotSeparatedProperty(serviceInfo.properties, metatypeId);
        }

        function extractDotSeparatedProperty(object, dotSeparatedProperty) {
            var firstDot = dotSeparatedProperty.indexOf('.');
            if (!object) {
                return undefined;
            } else if (firstDot < 0) {
                return object[dotSeparatedProperty];
            } else {
                var head = dotSeparatedProperty.substring(0, firstDot);
                var rest = dotSeparatedProperty.substring(firstDot + 1);
                return extractDotSeparatedProperty(object[head], rest);
            }
        }

        function commitNewConfiguration() {
            (function ($) {
                $('#configure-service-commit-indicator').css('visibility', 'visible');
                var newProperties = $('#configure-service-fields input').inject({}, function (h) {
                    h[this.name] = this.value;
                    return h;
                });
                var bundleId = $('#configure-service-controls').data('bundle-id');
                var serviceId = $('#configure-service-controls').data('service-id');
                YAHOO.util.Connect.initHeader("Content-Type", "application/json")
                YAHOO.util.Connect.asyncRequest('PUT',
                    psc.tools.Uris.relative("api/v1/osgi/bundles/" + bundleId + "/services/" + serviceId + "/properties"),
                    {
                        success: function(o) {
                            LB.Lightbox.deactivate();
                            $('#configure-service-commit-indicator').css('visibility', 'hidden');
                        },
                        failure: function() {
                            LB.Lightbox.deactivate();
                            $('#configure-service-commit-indicator').css('visibility', 'hidden');
                            alert("Updating properties failed");
                        }
                    },
                    YAHOO.lang.JSON.stringify(newProperties)
                );
            }(jQuery));
        }

        var bundleList;
        function setupBundleList() {
            var bundleListColumns = [
                { key: "id", label: "ID", sortable: true },
                {
                    key: "name", label: "Name", sortable: true,
                    formatter: function (elCell, oRecord, oColumn, oData) {
                        if (oData) {
                            elCell.innerHTML = oData.replace(/\./g, ".&#8203;");
                        } else {
                            elCell.innerHTML = oRecord.getData()['symbolicname'];
                        }
                    }
                },
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
                },
                {
                    key: "services", label: "Configure Services",
                    formatter: function (elCell, oRecord, oColumn, oData) {
                        var bundleId = oRecord.getData()['id'];
                        var services = oRecord.getData()['services'];
                        elCell.innerHTML = "";
                        if (services) {
                            for (var i = 0 ; i < services.length ; i++) {
                                var service = services[i];
                                var configurable = service.metatype &&
                                        service.interfaces.include("org.osgi.service.cm.ManagedService");
                                if (configurable) {
                                    elCell.innerHTML += " <input type='button' value='Configure " +
                                                        service.properties.service.pid +
                                                        "' class='service-configure' id='service-" +
                                                        service.properties.service.id + "-configure' />";
                                }
                            }
                        }
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
                $$('input.service-configure').each(function(configureButton) {
                    configureButton.observe("click", configureService);
                });
                $('configure-service-cancel').observe("click", function () { LB.Lightbox.deactivate() });
                $('configure-service-commit').observe("click", commitNewConfiguration);
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
<div id="lightbox">
    <h1>Configure <span id="configure-service-name"></span></h1>
    <div id="configure-service-loading"><tags:activityIndicator id="configure-service-loading-indicator"/> Loading properties...</div>
    <div id="configure-service-fields">
    </div>
    <div id="configure-service-controls" class="row submit">
        <tags:activityIndicator id="configure-service-commit-indicator"/>
        <input type="button" id="configure-service-commit" value="Update"/>
        <input type="button" id="configure-service-cancel" value="Cancel"/>
    </div>
</div>
</body>
</html>