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
     var tbl =  $('propertyTable');
     var lastRow = tbl.rows.length;
     var row = tbl.insertRow(lastRow);
     var cell1 = row.insertCell(0);
     if(lastRow%2==0) {
        var newListIndex = "odd property list-" + lastRow;
     } else {
        var newListIndex = "even property list-" + lastRow;
     }
     row.setAttribute('class',newListIndex)
     row.setAttribute('id','newUri')
     row.setAttribute('width','100%')
     var tableDiv = new Element('div', { 'class':'property-content'})
     var templateSpan = new Element('span',{'class':'templateValue'})
     var textSpan = new Element('span',{'class':'textValue'})
     var textNameSpan = new Element('span',{'class':'textName'})
     var templateNameSpan = new Element('span',{'class':'templateName'})
     var a = new Element('a', { 'class': 'property-edit', 'id': 'property-edit', href: '#property-edit', 'style':'display: none' }).update("Edit");
     var br = new Element('br')
     Element.observe(a, "click", SC.AP.clickEditButton)
     tableDiv.appendChild(textNameSpan)
     tableDiv.appendChild(textSpan)
     tableDiv.appendChild(br)
     tableDiv.appendChild(templateNameSpan)
     tableDiv.appendChild(templateSpan)
     tableDiv.appendChild(a)
     cell1.appendChild(tableDiv)
     SC.AP.editProperty(row)
   },

 editProperty: function(property) {
    var rowClass = SC.AP.findSelectIndexClass(property)
    SC.AP.PROPERTY_OBSERVERS = { }
    $w("templateValue textValue").each(function(propertyType) {
      var propertySpan = property.select(".property-content ." + propertyType).first();
      var propertyInput = $('edit-property-' + propertyType);
      propertyInput.value = propertySpan.innerHTML.unescapeHTML().strip().replace(/\s+/g, " ")
      SC.applyInputHint(propertyInput)
      SC.AP.PROPERTY_OBSERVERS[propertyType] = new Form.Element.Observer(
        propertyInput,
        0.4,
        function(e, value) {
          if (propertyInput.hasClassName("input-hint")) {
            propertySpan.innerHTML = ""
           } else {
            propertySpan.innerHTML = value.strip()
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
     var classValue = "property".concat(" ").concat(rowClass)
     $$('#oldUri').each(function(oldId){
            if(oldId.className.match(classValue)) {
                $$('#listKey').each(function(keyId) {
                     if(keyId.parentNode.parentNode.className.match(classValue)) {
                         SC.AP.addValues(keyId.value)
                     }
                })
           }
     })
     $$('#newUri').each(function(newId) {
            if(newId.className.match(classValue)) {
                var spanChild = new Array()
                var spanIndex = 0;
                $w("templateValue textValue").each(function(spanValue) {
                       spanChild[spanIndex++] = newId.select(".property-content ." + spanValue).first().innerHTML.empty();
                })
                var emptyFlag = "true"
                for (var i=0;i<spanChild.length;i++) {
                      if(!spanChild[i]) {
                            emptyFlag = "false";
                            break;
                      }
                }
                if(emptyFlag.match("false")) {
                        newId.select(".property-content .textName").first().innerHTML = 'Text: '
                        newId.select(".property-content .templateName").first().innerHTML = 'Template: '
                        newId.select(".property-content .property-edit").first().style.display = 'inline'
                        SC.AP.addNewValues(rowIndex)
                } else {
                        $('propertyTable').deleteRow(rowIndex);
                }
            }
      })
    },

  addValues: function(inputIndex) {
    $w("templateValue textValue").each(function(propertyValue) {
        var propertyInput = $('edit-property-' + propertyValue)
        if(!propertyInput.value.match("None")) {
            var hName ="existingUri["+inputIndex+"]."+propertyValue
            if ($(hName) != null ) {
                if($(hName).identify() == hName) {
                    $(hName).remove()
                }
            }
            var hInput = document.createElement('input')
            hInput.type = 'hidden'
            hInput.name = hName
            hInput.id = hName
            hInput.value = propertyInput.value
            $('uriProperties').appendChild(hInput)
        }
   })
  },

  addNewValues: function(inputIndex) {
    $w("templateValue textValue").each(function(propertyValue) {
        var propertyInput = $('edit-property-' + propertyValue)
        if(!propertyInput.value.match("None")) {
            var hName ="newUri["+inputIndex+"]."+propertyValue
            if ($(hName) != null ) {
                if($(hName).identify() == hName) {
                    $(hName).remove()
                }
            }
            var hInput = document.createElement('input')
            hInput.type = 'hidden'
            hInput.name = hName
            hInput.id = hName
            hInput.value = propertyInput.value
            $('uriProperties').appendChild(hInput)
        }
   })
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