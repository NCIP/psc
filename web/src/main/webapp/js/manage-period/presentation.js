/*global psc jQuery */
psc.namespace("template.mpa");

psc.template.mpa.Presentation = (function ($) {
  function syncVertical() {
    var target = $('#days').scrollTop();
    $('#activities, #notes').scrollTop(target);
  }

  function syncHorizontal() {
    var target = $('#days').scrollLeft();
    $('#days-heading').scrollLeft(target);
  }

  function showSelectedTool(evt, data) {
    $('#tools-section .tool-selector').removeClass('selected');
    $('#' + data.name + "-tool").addClass('selected');

    $('#tools-section .tool-detail').stop().hide();
    $('#' + data.name + '-tool-detail').
      find('span.step-' + data.step).show().end().
      find('span:not(.step-' + data.step + ')').hide().end().
      fadeIn(250);

    $('body').attr('class', $('body').attr('class').replace(/action-\w+/g, ''));
    $('body').addClass('action-' + data.name);

    $('body').attr('class', $('body').attr('class').replace(/step-\w+/g, ''));
    $('body').addClass('step-' + data.step);
  }

  function selectTool() {
    var data = {
      name: $(this).attr('id').replace('-tool', ''),
      step: 0
    };
    $('#days').trigger('action-changed', data);
    return false;
  }

  function showSelectedNotes(noteType) {
    if (!noteType) {
      noteType = $('#notes-heading li.selected').text().toLowerCase();
    } else {
      $('#notes-heading li').removeClass('selected');
      $('#notes-heading li.' + noteType).addClass('selected');
    }

    $('#notes .' + noteType).fadeIn(250);
  }

  function selectNoteTab() {
    $('#notes .note').stop().hide()
    showSelectedNotes($(this).attr('href').replace(/^#/, ''));
  }

  function setMessage(text, klass) {
    $('#message').attr('class', '').text(text);
    if (klass) {
      $('#message').addClass(klass);
    }
  }

  function reportError(evt, data) {
    psc.template.mpa.Presentation.error(data.message);
  }

  var gridUpdates = {
    'action-started': {
      'add,0': function (evt, data) {
        var pop = data.population ? data.population : '&#215;';
        $(data.cell).addClass('in-progress').
          empty().append("<div class='marker'>" + pop + "</div>");
      },

      'move,0': function (evt, data) {
        $(data.cell).addClass('in-progress').addClass('moving').
          parent('tr').addClass('moving');
      },

      'move,1': function (evt, data) {
        var src = $(data.cell).parent('tr').find('td.cell.moving');
        src.removeClass('moving').parent('tr').removeClass('moving');
        $(data.cell).addClass('in-progress').empty().
          append(src.find('.marker'));
      },

      'update-notes,0': function (evt, data) {
        $(data.cell).addClass('in-progress');
      },

      'delete,0': function (evt, data) {
        $(data.cell).addClass('in-progress');
      }
    },

    'action-completed': {
      'add,0': function (evt, data) {
        $(data.cell).removeClass('in-progress').
          find('.marker').attr('resource-href', data.href);
      },

      'move,1': function (evt, data) {
        $(data.cell).removeClass('in-progress').parent('tr').
          find('td.cell:eq(' + data.startColumn + ')').removeClass('in-progress');
      },
      
      'update-notes,0': function (evt, data) {
        $(data.cell).removeClass('in-progress');
      },

      'delete,0': function (evt, data) {
        $(data.cell).removeClass('in-progress').empty();
      }
    },

    'action-error': {
      'add,0': function (evt, data) {
        if ($(data.cell).is('.in-progress')) {
          $(data.cell).removeClass('in-progress').
            addClass('error').empty();
        }
      },

      'move,1': function (evt, data) {
        if ($(data.cell).is('.in-progress')) {
          $(data.cell).addClass('error').removeClass('in-progress').
            parent('tr').find('td.cell:eq(' + data.startColumn + ')').
              addClass('error').removeClass('in-progress').
              empty().append($(data.cell).find('.marker'));
        }
      },

      'update-notes,0': function (evt, data) {
        if ($(data.cell).is('.in-progress')) {
          $(data.cell).removeClass('in-progress').addClass('error');
        }
      },

      'delete,0': function (evt, data) {
        if ($(data.cell).is('.in-progress')) {
          $(data.cell).removeClass('in-progress').addClass('error');
        }
      }
    }
  }

  function routeGridAction(evt, data) {
    if (evt.type in gridUpdates) {
      if (!(data.action && data.action.name)) {
        return;
      }
      var actionStepKey = data.action.name + ',' + data.action.step

      if (actionStepKey in gridUpdates[evt.type]) {
        gridUpdates[evt.type][actionStepKey](evt, data);
      } else {
        throw "Unknown action " + actionStepKey +
          " received in an " + evt.type + " event";
      }
    } else {
      throw "Attempting to route unknown event: " + evt.type;
    }
  }

  function cancelMove(evt, data) {
    if (data.name != 'move' || data.step != 1) {
      $('tr.moving').
        find('td.in-progress.moving').removeClass('in-progress').removeClass('moving').end().
        removeClass('moving');
    }
  }

  function unnew() {
    $('.new-row').removeClass('new-row')
  }

  return {
    init: function (hash) {
      $('#days').scroll(syncVertical).scroll(syncHorizontal).
        click(unnew);

      $('#days').bind('action-changed', showSelectedTool);
      $('.tool-selector').click(selectTool);
      $('#add-tool').click(); // default

      $('#days').bind('action-error', reportError).
        bind('action-started', this.clearMessage);

      $('#days').bind('action-started', routeGridAction).
        bind('action-completed', routeGridAction).
        bind('action-error', routeGridAction);
      $('#days').bind('action-changed', cancelMove).
        bind('row-added', function () {
          showSelectedNotes(null);
        }).bind('row-added', this.clearMessage).
        bind('action-started', unnew);

      $('#notes-heading li a').click(selectNoteTab);
      if (hash) {
        showSelectedNotes(hash.replace(/^#/, ''));
      } else {
        showSelectedNotes('details');
      }
    },

    error: function (message) {
      setMessage(message, 'error');
    },

    info: function (message) {
      setMessage(message, 'info');
    },

    clearMessage: function () {
      setMessage("");
    }
  };
}(jQuery));