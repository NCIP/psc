psc.namespace('template.mpa');

/** TODO: this module needs tests */

psc.template.mpa.ActivityNotes = (function ($) {
  var NOTE_TYPES = ['details', 'condition', 'labels', 'weight'];
  var Model = psc.template.mpa.Model;
  var notesObservers;

  function updateNotePreview() {
    var rowN = Model.rowNumberFor(this);
    var notesRow = $(this).closest('tr');
    var activity = Model.activity(rowN);

    $(NOTE_TYPES).each(function () {
      var content = notesRow.find('.' + this).text().trim()
      var elt = $('#' + this + "-preview")
      if (content.length === 0) {
        elt.text("None").addClass("none")
      } else {
        elt.text(content).removeClass("none")
      }
    });
    $('#notes-preview').attr('row', rowN).show().find('h2').text(activity.name);
  }
  
  function editNotes(rowN) {
    var activity = Model.activity(rowN);
    var notesRow = Model.findRow('notes', rowN);
    $(["notes", "days", "activities"]).each(function () {
      Model.findRow(this, rowN).addClass('emphasized');
    });
    
    $("#edit-notes-lightbox .activity-name").text(activity.name);
    notesObservers = []
    $(NOTE_TYPES).each(function() {
      var noteSpan = notesRow.find(".notes-content ." + this);
      var noteInput = $('#edit-notes-' + this);
      // copy in current values from spans
      noteInput.val(noteSpan.text().unescapeHTML().trim().replace(/\s+/g, " "));
      SC.applyInputHint(noteInput[0])
      // observe the text field and update the span
      notesObservers.push(new Form.Element.Observer(
        noteInput[0],
        0.4,
        function(e, value) {
          if (noteInput.is(".input-hint")) {
            noteSpan.empty();
          } else {
            noteSpan.text(value.trim());
          }
        }
      ));
    })
    $('#edit-notes-lightbox').attr('row', rowN).show().find('input').attr('disabled', null);
    LB.Lightbox.activate();
  }
  
  function finishEditingNotes() {
    $("#edit-notes-lightbox input").attr("disabled", "disabled");
    $(notesObservers).each(function () { this.stop() });
    
    Model.findRow('days', $('#edit-notes-lightbox').attr('row')).
      find('td.cell .marker').each(function () {
        var data = Model.cellData($(this).parent('td.cell'));
        data.action = { name: 'update-notes', step: 0 };
        $('#days').trigger('action-started', data);
      });
    
    $('tr.activity.emphasized').removeClass('emphasized');
    LB.Lightbox.deactivate();
  }

  function registerNotesPreviewHandlers() {
    $('.notes-edit').unbind().hover(
      updateNotePreview,
      function () { $('#notes-preview').hide() }
    ).click(function () {
      editNotes(Model.rowNumberFor(this));
      return false;
    });
  }

  return {
    init: function () {
      registerNotesPreviewHandlers();
      
      $('#days').bind('row-added', registerNotesPreviewHandlers);

      $('#notes-preview').hover(
        function () { $('#notes-preview').show() },
        function () { $('#notes-preview').hide() }
      ).click(function () {
        editNotes($(this).attr('row'));
        return false;
      });
      
      $('#edit-notes-done').click(finishEditingNotes);
    },
    
  };
}(jQuery));