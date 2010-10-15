/**
 * This is a tri-state checkbox which switches between
 * the states unchecked, checked, intermediate.
 *
 * When there is a state change, a 'tristate-state-change'
 * event is fired.
 */
(function($) {
     var config = {
          initialState: 'unchecked'
      };

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

    $.fn.tristate = function(method) {

      var methods = {
        init: function(options) {
          options = $.extend({}, config, options);
          
          var initial = states[options.initialState.toLowerCase()] ? options.initialState.toLowerCase() : 'unchecked';
          methods.state.call(this, initial, true);
          $(this).click(function() {
            var current = methods.state.call(this);
            var next = states[current].next;
            methods.state.call(this, next);
          });
          return this;
        },
        state: function(val, suppressChangeEvent) {
          if (val) {
            if (!states[val]) {return;}
            $(this).attr('state', val);
            $(this).attr('checked', states[val].checked);
            if (!suppressChangeEvent) {
              $(this).trigger('tristate-state-change', val);
            }
          }
          return $(this).attr('state');
        }
      };

      // Method calling logic
      if ( methods[method] ) {
        return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
      } else if ( typeof method === 'object' || ! method ) {
        return methods.init.apply( this, arguments );
      } else {
        $.error( 'Method ' +  method + ' does not exist on jQuery.tooltip' );
      }
    };
})(jQuery);