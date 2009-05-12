if (!window.SC) { window.SC = { } }
if (!SC.AP) { SC.AP = { } }

Object.extend(SC.AP, {
 findSelectIndexClass: function(tr) {
    return $w(tr.className).detect(function(clz) { return clz.substring(0, 5) == "list-" })
  },

 clickEditButton: function(evt) {
    Event.stop(evt)
    var editButton = Event.element(evt)
    SC.AP.editProperty(editButton.up("tr"))
  },

 clickNewButton: function(evt) {
    Event.stop(evt)
    var row = SC.AP.addNewRowToUriTable()
    SC.AP.editProperty(row)
  },

 editProperty: function(property) {
    var rowClass = SC.AP.findSelectIndexClass(property)
    SC.AP.PROPERTY_OBSERVERS = { }
    $w ("templateValue textValue").each(function(propertyType) {
        var propertySpan = $(property).select("." + propertyType).first();
        var propertyInput = $('edit-property-' + propertyType);
        propertyInput.value = propertySpan.innerHTML.unescapeHTML().strip().replace(/\s+/g, " ")
        SC.applyInputHint(propertyInput)
        SC.AP.PROPERTY_OBSERVERS[propertyType] = new Form.Element.Observer(propertyInput, 0.4,
            function(e, value) {
                if (propertyInput.hasClassName("input-hint")) {
                    propertySpan.update("")
                } else {
                    propertySpan.update(value.strip())
                }
            }
        )
    })
    $('edit-property-lightbox').addClassName(rowClass)
    $('edit-property-lightbox').show()
    $$("#edit-property-lightbox input").invoke("enable")
    LB.Lightbox.activate()
  },

 finishEditingProperty: function(evt) {
    $$("#edit-property-lightbox input").invoke("disable")
    Object.values(SC.AP.PROPERTY_OBSERVERS).invoke("stop")

    var rowClass = SC.AP.findSelectIndexClass($('edit-property-lightbox'))
    $('edit-property-lightbox').removeClassName(rowClass)

    var rowIndex = rowClass.substring(5)
    LB.Lightbox.deactivate()

    var oldUri = $('oldUri ' +rowClass);
    if (oldUri != null) {
       var keyValue = $(oldUri).select('.listKey').first().value
       SC.AP.addValues(keyValue, "existingUri")
    }

    var newId = $('newUri ' +rowClass);
    if (newId != null) {
       if (!$(newId).select('.textValue').first().innerHTML.empty()
           && !$(newId).select('.templateValue').first().innerHTML.empty()) {
           $(newId).select(".textName").first().update("Text: ")
           $(newId).select(".templateName").first().update("Template: ")
           newId.select(".property-edit").first().style.display = 'inline'
           SC.AP.addValues(rowIndex, "newUri")
       } else {
           $('propertyTable').deleteRow(rowIndex);
       }
    }
  },

  addValues: function(inputIndex, mapName) {
    $w ("templateValue textValue").each(function(propertyValue) {
        var propertyInput = $('edit-property-' + propertyValue)
        if(!propertyInput.value.match("None")) {
            var hName =mapName +"["+inputIndex+"]."+propertyValue
            if ($(hName) != null ) {
                if ($(hName).identify() == hName) {
                    $(hName).remove()
                }
            }
            var hInput = new Element('input', { 'type':'hidden', 'id': hName,
                            'name': hName, 'value' : propertyInput.value})
            $('uriProperties').appendChild(hInput)
        }
    })
  },

  addNewRowToUriTable : function() {
    var tbl =  $('propertyTable');
    var lastRow = tbl.rows.length;
    var row = tbl.insertRow(lastRow);
    var col = row.insertCell(0);
    if (lastRow%2==0) {
        row.className = "odd property list-" +lastRow
    } else {
        row.className = "even property list-" +lastRow
    }
    row.setAttribute('id','newUri list-' +lastRow)
    row.setAttribute('width','100%')
    var a = new Element('a', { 'class': 'property-edit', 'id': 'property-edit',
                        href: '#property-edit', 'style':'display: none' }).update("Edit")
    Element.observe(a, "click", SC.AP.clickEditButton)
    col.appendChild(new Element('span',{'class':'textName'}))
    col.appendChild(new Element('span',{'class':'textValue'}))
    col.appendChild(new Element('br'))
    col.appendChild(new Element('span',{'class':'templateName'}))
    col.appendChild(new Element('span',{'class':'templateValue'}))
    col.appendChild(a)
    return row;
  },

  applyActivityPropertyIndex :  function(container) {
    var rows = $(container).select("tr.property")
    for (var i = 0 ; i < rows.length ; i++) {
      var row = rows[i]
      $w(row.className).each(function(className) {
        if (className.substring(0, 5) == "list-") { row.removeClassName(className) }
      })
      row.addClassName("list-" + i)
    }
  }

})

$(document).observe("dom:loaded",function() {
   $w("uriProperties").each(SC.AP.applyActivityPropertyIndex)
   $$(".property-edit").each(function(button) { button.observe("click", SC.AP.clickEditButton) })
   $$(".newUriBtn").each(function(button) { button.observe("click", SC.AP.clickNewButton) })
   $('edit-property-done').observe('click', SC.AP.finishEditingProperty)
   $$('#edit-property-lightbox input').each(function(elt) {
    elt.observe('keyup', function(evt) {
      if (evt.keyCode == Event.KEY_RETURN) { SC.AP.finishEditingProperty(evt) }
    })
  })
})