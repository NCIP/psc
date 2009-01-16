if (!window.SC) { window.SC = { } }
if (!SC.MP) { SC.MP = { } }

Object.extend(SC.MP, {
  createPALabelsAutocompleter: function() {
    SC.MP.PAlabelsAutocompleter = new SC.FunctionalAutocompleter(
     'edit-notes-labels', 'edit-notes-labels-div', SC.MP.labelAutocompleterChoices, {
        tokens: " ",
        select: "label-name",
        afterUpdateElement: function(input, selected) {
          var label = {
            name:   selected.select(".label-name").first().innerHTML
          }
          SC.MP.setLabels(label.name);
          input.focus();
        }
      }
    );
  },

  setLabels : function(labelName) {
     var seperator = " ";
     var labelValues = labelName;
     var labels = SC.MP.trimLabels($F('edit-notes-labels'));
      if ( labels.length > 1 ) {
	     labelValues  = labels.slice(0, labels.length - 1).join(seperator) + seperator + labelValues ;
	  }
      $('edit-notes-labels').value = labelValues ;
  },

  lastLabel : function(value) {
	 var labels = SC.MP.trimLabels(value);
	 return labels[labels.length - 1];
  },

  trimLabels : function(value) {
     var seperator = " ";
	 return value.split(seperator);
  },

  labelAutocompleterChoices: function(str, callback) {
    SC.MP.findNextLabels(function(data) {
      var list = data.map(function(label) {
            return resigTemplate("new_palabel_autocompleter", label)
      }).join("\n")
      callback("<ul>\n" + list + "\n</ul>")
    });
  }   
})

$(document).observe("dom:loaded", function() {
    SC.MP.createPALabelsAutocompleter();
})