if (!window.SC) { window.SC = { } }
if (!SC.MP) { SC.MP = { } }

Object.extend(SC.MP, {

  addLabel: function(label) {
      $('labels-autocompleter-input').value = label.name;
  },


  createLabelsAutocompleter: function() {
    SC.MP.labelsAutocompleter = new SC.FunctionalAutocompleter(
      'labels-autocompleter-input', 'labels-autocompleter-div', SC.MP.labelAutocompleterChoices, {
        select: "label-name",
        afterUpdateElement: function(input, selected) {
          var label = {
            name:   selected.select(".label-name").first().innerHTML
          }
          SC.MP.addLabel(label)
          input.focus()
        }
      }
    );
  },


  labelAutocompleterChoices: function(str, callback) {
    SC.MP.findNextLabels(function(data) {
      var lis = data.map(function(label) {

        return resigTemplate("new_label_autocompleter_row", label)
      }).join("\n")
      callback("<ul>\n" + lis + "\n</ul>")
    })
  }
})
$(document).observe('dom:loaded', function() {
  SC.MP.createLabelsAutocompleter()
})
