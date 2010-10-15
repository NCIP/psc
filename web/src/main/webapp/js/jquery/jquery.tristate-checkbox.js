/**
 * This is a tri-state checkbox which switches between
 * the states unchecked, checked, intermediate.
 *
 * When there is a state change, a 'tristate-state-change'
 * event is fired.
 */
(function($) {
    $.fn.tristate = function(settings) {
      var config = {
          initialState: 'unchecked'
      };

      if (settings) $.extend(config, settings);

      var states = {
        checked: {
          next: 'unchecked',
          checked: true
        },
        unchecked: {
          next: 'checked',
          checked: false
        },
        intermediate: {
          next: 'checked',
          checked: true
        }
      };

      var state = function(val) {
        if (val !== undefined) {
          if (!states[val]) {return;}
          $(this).attr('state', val);
          $(this).attr('checked', states[val].checked);
        }
        return $(this).attr('state');
      };

      this.each(function() {
        var initial = states[config.initialState.toLowerCase()] ? config.initialState.toLowerCase() : 'unchecked';
        state.call(this, initial);
        $(this).click(function() {
          var current = state.call(this);
          var next = states[current].next;
          state.call(this, next);
          $(this).trigger('tristate-state-change');
        });
      });
    };
})(jQuery);