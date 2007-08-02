//////////// STUDY CALENDAR JS STYLES

var SC = new Object();

SC.slideAndHide = function(element, options) {
    var e = $(element);
    new Effect.Parallel(
        [
            new Effect.BlindUp(e, {sync:true}),
            new Effect.Fade(e, {sync:true})
        ], $H(options).merge({
            duration:1.0
        })
    );
}

SC.slideAndShow = function(element, options) {
    var e = $(element);
    new Effect.Parallel(
        [
            new Effect.BlindDown(e, {sync:true}),
            new Effect.Appear(e, {sync:true})
        ], $H(options).merge({
            duration:1.0
        })
    );
}

//////////// COOKIES

/** Main fns based on http://www.quirksmode.org/js/cookies.html */
var Cookies = {
    add: function(name, value, days) {
        if (days) {
            var date = new Date();
            date.setTime(date.getTime() + (days*24*60*60*1000));
            var expires = "; expires=" + date.toGMTString();
        }
        else var expires = "";
        document.cookie = name + "=" + value + expires + "; path=/";
    },

    get: function(name) {
        var nameEQ = name + "=";
        var ca = document.cookie.split(';');
        for(var i=0;i < ca.length;i++) {
            var c = ca[i];
            while (c.charAt(0) == ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
        }
        return null;
    },

    clear: function(name) {
        Cookies.add(name, "", -1);
    },

    set: function(name, value, days) {
        Cookies.clear(name)
        Cookies.add(name, value, days)
    }
}