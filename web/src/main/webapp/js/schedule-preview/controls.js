psc.namespace("schedule.preview");

psc.schedule.preview.Controls = (function ($) {
  var p = psc.schedule.preview.Parameters; // alias for clarity

  function refreshList() {
    $('ul#preview-segments li').remove('.preview-segment');
    p.requestedSegments().each(function (v) { addSegmentEntry(v, true) });
    updateStatus();
  }

  function addSegmentEntry(values, inPreview) {
    var name = nameForSegmentIdent(values.segment);
    $('ul#preview-segments').find('li#next-segment').before(
      resigTemplate('preview_segment_entry', {
        id: values.segment,
        name: name,
        start_date: psc.tools.Dates.utcToDisplayDate(psc.tools.Dates.apiDateToUtc(values.start_date))
      })
    ).end().find('li.preview-segment:last').data('id', values.segment);
    if (!inPreview) {
      $('li.preview-segment:last').addClass('newly-added');
    }
  }

  function nameForSegmentIdent(id) {
    return $('#preview-segment-selector option[value=' + id + ']').text();
  }

  function updateStatus() {
    if (p.pending()) {
      $('#refresh-preview-control .notice').show();
    } else {
      $('#refresh-preview-control .notice').hide();
    }

    if ($('li.preview-segment:not(.removed)').length == 1) {
      $('li.preview-segment:not(.removed) .remove :button').attr('disabled', true);
    } else {
      $('li.preview-segment .remove :button').attr('disabled', false);
    }
  }

  function addSegmentFromForm() {
    var newItem = {
      segment: $('#next-segment :selected').val(),
      start_date: psc.tools.Dates.displayDateToApiDate($('#next-segment input.date').val())
    };
    p.add(newItem);
    addSegmentEntry(newItem);
    updateStatus();
  }

  function handleRemove() {
    var li = $(this).parents('li:first')
    var pair = {
      segment: li.data('id'),
      start_date: psc.tools.Dates.displayDateToApiDate(li.find('.segment-date .date').text())
    }
    if (li.is('.removed')) {
      p.add(pair)
      li.removeClass('removed').
        find('.remove :button').val('Remove');
    } else {
      p.remove(pair);
      li.addClass('removed').
        find('.remove :button').val('Restore');
    }
    updateStatus();
  }

  function refresh() {
    $('#schedule').trigger('schedule-load-start');
  }

  function refreshSuccessful() {
    $('#preview-segments li').removeClass('newly-added').remove('.removed');
    window.location.hash = p.toQueryString().replace(/^\?/, '');
    updateStatus();
  }

  return {
    init: function(hash) {
      p.init(hash);
      refreshList();

      $('#next-segment #add-button').click(addSegmentFromForm);
      $('#preview-segments .remove.control :button').live('click', handleRemove);
      $('#refresh-preview-control :button').click(refresh);
      $('#schedule').bind('schedule-ready', refreshSuccessful);
      $('#toggle-plan-days').click(function () {
        $('.event-details.plan-day').toggle();
        if ($(this).text().match(/Show/)) {
          $(this).text($(this).text().replace(/Show/, 'Hide'));
        } else {
          $(this).text($(this).text().replace(/Hide/, 'Show'));
        }
        return false;
      });
    }
  };
}(jQuery));