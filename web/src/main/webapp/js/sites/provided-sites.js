if (!window.SC) { window.SC = { } }
if (!SC.RP) { SC.PS = { } }
var bundleList;

Object.extend(SC.PS, {
  getProvidedSites : function() {
      SC.PS.providedSites = new Form.Element.Observer('site-name', 0.4, SC.PS.providedSitesChoices);
  },

  providedSitesChoices: function(str,callback) {
      SC.PS.findNextSitesUpdated (function(data) {
    })
  },

  addNewSite: function(name, assignedId, provider) {
    var href = SC.relativeUri("/pages/admin/manage/newProvidedSite")
    var params = { }
    params.name = name
    params.assignedIdentifier = assignedId
    params.provider = provider
    SC.asyncRequest(href, {
       method: 'POST',
       parameters:params,
       onSuccess : function() {
         window.location = SC.relativeUri("/pages/admin/manage/sites")
       }
    })
  },

  addNewSiteSetup : function(siteId) {
    var name = $('site-name-'+siteId).innerHTML.strip()
    var assignedIdentifier = $('assigned-identifier-'+siteId).innerHTML.strip()
    $('site-name').value = name;
    $('assigned-identifier').value = assignedIdentifier
  },
    
  findNextSites: function(receiver) {
    var searchString = $F("site-name")
    var uri = SC.relativeUri("/api/v1/provided-sites")

    if (searchString.blank()) {
       receiver([]);
       return;
    }
    var params = { };
    if (!searchString.blank()) params.q = searchString;
    $('provided-site-search-indicator').reveal()
    SC.asyncRequest(uri, {
       method: "GET",
       parameters: params,
       onSuccess: function(response) {
         $('provided-site-search-indicator').conceal()
         var doc = response.responseXML;
         var sites = SC.objectifyXml("site", doc, function(elt, site) {
             site.name = elt.getAttribute("site-name")
             site.identifier = elt.getAttribute("assigned-identifier")
             site.provider = elt.getAttribute("provider")
         })
         receiver(sites)
       }
    })
  },

  findNextSitesUpdated: function(receiver) {
    var searchString = $F("site-name")
    var uri = SC.relativeUri("/api/v1/provided-sites")

    if (searchString.blank()) {
       receiver([]);
       return;
    }
    var params = { };
    if (!searchString.blank()) params.q = searchString;
    $('provided-site-search-indicator').reveal()
    SC.asyncRequest(uri+".json", {
       method: "GET",
       parameters: params,
       onSuccess: function(response) {
         $('provided-site-search-indicator').conceal()

         var bundleListColumns = [
           { key: "site_name", label: "Site Name", sortable: true},
           { key: "assigned_identifier", label: "Assigned Identifier", sortable: true},
           { key: "provider", label: "Provider", sortable: true},
           { key: "button", label: "Controls", sortable:true, formatter:myButtonFormatter}
         ];

         var myDataSource = new YAHOO.util.DataSource(response.responseJSON);
         myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;

         myDataSource.responseSchema = {
             resultsList : "sites",
             fields : [
                 { key: "site_name"},
                 { key: "assigned_identifier"},
                 { key: "provider"},
                 { key: "button", formatter:myButtonFormatter}
             ]
         };

         bundleList = new YAHOO.widget.DataTable("site-response", bundleListColumns, myDataSource);
         receiver(response)
       }
    })

    var myButtonFormatter = function (elCell, oRecord, oColumn, oData) {
      var siteName = oRecord.getData('site_name');
      siteName = siteName.replace(/\s/g, "_");
      var siteId = oRecord.getData('assigned_identifier');
      var existingSiteName = 'existing-site-name-'+siteName;
      var existingSiteAssignedId = 'existing-assigned-identifier-'+siteId;
      if (!$(existingSiteName) || !$(existingSiteAssignedId)) {
        var container = jQuery('<div class="row" />')
        var divSubmit = jQuery('<div class="submit" />')
        var submitButton = jQuery('<input type="submit" value="Create" />')

        divSubmit.append(submitButton);
        container.append(divSubmit);


        jQuery(elCell).append(container);
        YAHOO.util.Event.addListener( submitButton, "click", myClickHandler, oRecord);
      }
    }

    var myClickHandler = function(event, oRecord){
        var assignedId = oRecord.getData('assigned_identifier');
        var provider = oRecord.getData('provider');
        var name = oRecord.getData('site_name');

        SC.PS.addNewSite(name, assignedId, provider);
    };
  }
})
$(document).observe('dom:loaded', function() {
  SC.PS.getProvidedSites()
})