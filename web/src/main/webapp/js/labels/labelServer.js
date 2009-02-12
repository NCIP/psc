if (!window.SC) { window.SC = { } }
if (!SC.RP) { SC.RP = { } }

Object.extend(SC.RP, {

  // Finds the set of labels
  findNextLabels: function(receiver) {
    var searchString = $F("labels-autocompleter-input")
    if (searchString == "Search for label") {
      searchString = ""
    }

    var uri = SC.relativeUri("/api/v1/labels")

    if (searchString.blank()) {
      receiver([]);
      return;
    }

    var params = { };
    if (!searchString.blank()) params.q = searchString;

    SC.asyncRequest(uri, {
      method: "GET", parameters: params,
      onSuccess: function(response) {
        var doc = response.responseXML;
        var labels = SC.objectifyXml("label", doc, function(elt, label) {
          label = elt.parentNode.getAttribute("name")
        })

        receiver(labels)
      }
    })
  }
})