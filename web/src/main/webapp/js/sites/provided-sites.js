if (!window.SC) { window.SC = { } }
if (!SC.RP) { SC.PS = { } }

Object.extend(SC.PS, {
  getProvidedSites : function() {
      SC.PS.providedSites = new Form.Element.Observer('site-name', 1, SC.PS.providedSitesChoices);
  },

  providedSitesChoices: function(str,callback) {
    SC.PS.findNextSites(function(data) {
       SC.PS.deleteSiteTableRows();
       if (data.length>0) {
          SC.PS.createSiteTableRows();
       }
       var lis = data.map(function(site) {
           return resigTemplate("new_site_data_row", site)
       }).join("\n")
       $('provided-sites-table').insert({ bottom:lis});
       data.each(function(site){
         SC.PS.addSiteControl(site)
       })
    })
  },

  addSiteControl: function(site) {
      if (!$('existing-site-name-'+site.name) || !$('existing-assigned-identifier-'+site.identifier)) {
         var newControl = resigTemplate("new_site_control", site)
         $('provider-'+site.identifier).insert({ after: newControl })
      } else {
         var blankCell = document.createElement("td")
         $('provider-'+site.identifier).insert({ after: blankCell })
      }
  },

  deleteSiteTableRows: function() {
    var siteTable = $('provided-sites-table')
    var rowLength = siteTable.rows.length
    if (rowLength >0) {
       var i = rowLength -1 ;
       while (i >= 0){
           siteTable.deleteRow(i)
           i = i-1;
       }
    }
  },

  createSiteTableRows: function() {
    var siteTable = $('provided-sites-table')
    var siteHeadData = ["Site Name","Assigned Identifier","Provider","Controls "]
    var newCell
    var newTHEAD = siteTable.createTHead()
    var newRow = newTHEAD.insertRow(-1)
    for (var i = 0; i < siteHeadData.length; i++) {
        newCell = newRow.insertCell(i)
        newCell.innerHTML = siteHeadData[i]
    }
  },

  addNewSite: function(siteId) {
    var href = SC.relativeUri("/pages/admin/manage/newProvidedSite")
    var name = $('site-name-'+siteId).innerHTML.strip()
    var assignedIdentifier = $('assigned-identifier-'+siteId).innerHTML.strip()
    var provider = $('provider-'+siteId).innerHTML.strip()
    var params = { }
    params.name = name
    params.assignedIdentifier = assignedIdentifier
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
  }
})
$(document).observe('dom:loaded', function() {
  SC.PS.getProvidedSites()
})