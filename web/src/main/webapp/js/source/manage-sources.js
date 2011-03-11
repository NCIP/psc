psc.namespace("source");

psc.source.ManageSources = (function () {
    function updateManualTarget(){
        var checkedElt = getManualTargetSourceElement()
        var sourceName = checkedElt.attr('sourceName')
        var params = {
            manual_target: true
        };
        var url = psc.tools.Uris.relative('/api/v1/activities/'
                +psc.tools.Uris.escapePathElement(sourceName)+'/manual-target')
        var msg = "This will make " +sourceName +
                      " as a manual activity target source"
                       + "\nAre you sure want to proceed?"
        
        if (window.confirm(msg)) {
            jQuery.ajax({
                url: url,
                type: 'PUT',
                data: Object.toJSON(params),
                contentType: 'application/json',
                sucess: function() {
                    window.location = psc.tools.Uris.relative("/pages/admin/manage/sources")
                }
            });
            return true;
        } else {
            return false;
        }
    }

    function getManualTargetSourceElement() {
        return jQuery("input[name='radioGroup']:checked");
    }

    function displaySourcesList() {
        var columnDefs = [
	            { key: "name", label: "Source Name ", sortable: true },
                { key: "manual_target", label: "Manual Activity Target" }
            ];
        var dataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("source-list-table"));
        dataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
        dataSource.responseSchema = {
            fields: [
                { key: "name" },
                { key: "manual_target" }
            ]
	    };
        new YAHOO.widget.DataTable("source-list", columnDefs, dataSource, {
            sortedBy: { key: 'name' }
        });
  }

  return {
    init: function () {
       displaySourcesList()
       Event.observe('update-manual-activity-target', 'click', updateManualTarget)
    }
  };
}());

(function ($) {
  $(window).load(function () {
    psc.source.ManageSources.init();
  });
}(jQuery));

