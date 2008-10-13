if (!window.SC) { window.SC = { } }
if (!SC.RP) { SC.RP = { } }

Object.extend(SC.RP, {

  addLabel: function(label) {
      $('labels-autocompleter-input').value = label.name;
  },


  createLabelsAutocompleter: function() {
    SC.RP.labelsAutocompleter = new SC.FunctionalAutocompleter(
      'labels-autocompleter-input', 'labels-autocompleter-div', SC.RP.labelAutocompleterChoices, {
        select: "label-name",
        afterUpdateElement: function(input, selected) {
          var label = {
            name:   selected.select(".label-name").first().innerHTML
          }
          SC.RP.addLabel(label)
          input.focus()
        }
      }
    );
  },


  labelAutocompleterChoices: function(str, callback) {
    SC.RP.findNextLabels(function(data) {
      var lis = data.map(function(label) {

        return resigTemplate("new_label_autocompleter_row", label)
      }).join("\n")
      callback("<ul>\n" + lis + "\n</ul>")
    })
  }
})
$(document).observe('dom:loaded', function() {
  SC.RP.createLabelsAutocompleter()
})
