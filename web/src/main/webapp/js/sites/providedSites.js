if (!window.SC) { window.SC = { } }
if (!SC.RP) { SC.PS = { } }

Object.extend(SC.PS, {
  getProvidedSites : function() {
      SC.PS.providedSites = new Form.Element.Observer('site-name', 0.4, SC.PS.providedSitesChoices);
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
       $('providedSitesTable').insert({ bottom:lis});
       data.each(function(site){
         SC.PS.addSiteControl(site)
       })
    })
  },

  addSiteControl: function(site) {
      if (!$('existingSiteName'+site.name) || !$('existingAssignedIdentifier'+site.identifier)) {
         var newControl = resigTemplate("new_site_control", site)
         $('assignedIdentifier'+site.identifier).insert({ after: newControl })
      } else {
         var blankCell = document.createElement("td")
         $('assignedIdentifier'+site.identifier).insert({ after: blankCell })
      }
  },

  deleteSiteTableRows: function() {
    var siteTable = $('providedSitesTable')
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
    var siteTable = $('providedSitesTable')
    var siteHeadData = ["Site Name","Assigned Identifier","Controls "]
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
    var name = $('siteName'+siteId).innerHTML.strip()
    var assignedIdentifier = $('assignedIdentifier'+siteId).innerHTML.strip()
    var params = { }
    params.name = name
    params.assignedIdentifier = assignedIdentifier
    SC.asyncRequest(href, {
       method: 'POST',
       parameters:params,
       onSuccess : function() {
         window.location = SC.relativeUri("/pages/admin/manage/sites")
       }
    })
  },

  addNewSiteSetup : function(siteId) {
    var name = $('siteName'+siteId).innerHTML.strip()
    var assignedIdentifier = $('assignedIdentifier'+siteId).innerHTML.strip()
    $('site-name').value = name;
    $('assigned-Identifier').value = assignedIdentifier
  },
    
  findNextSites: function(receiver) {
    var searchString = $F("site-name")
    var uri = SC.relativeUri("/api/v1/providedSites")

    if (searchString.blank()) {
       receiver([]);
       return;
    }
    var params = { };
    if (!searchString.blank()) params.q = searchString;
    SC.asyncRequest(uri, {
       method: "GET",
       parameters: params,
       onSuccess: function(response) {
         var doc = response.responseXML;
         var sites = SC.objectifyXml("site", doc, function(elt, site) {
             site.name = elt.getAttribute("site-name")
             site.identifier = elt.getAttribute("assigned-identifier")
         })
         receiver(sites)
       }
    })
  }
})
$(document).observe('dom:loaded', function() {
  SC.PS.getProvidedSites()
})