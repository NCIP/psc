/*
Created By: Chris Campbell
Website: http://particletree.com
Date: 2/1/2006

Inspired by the lightbox implementation found at http://www.huddletogether.com/projects/lightbox/
*/

/*
    Extensively modified for study calendar.
    - Invoked explicitly by RJS-style AJAX calls instead of through implicit link behavior modification.
    - Globals shifted into separate namespace
*/

/*-------------------------------GLOBAL VARIABLES------------------------------------*/

var LB = {
    detect: navigator.userAgent.toLowerCase(),
    OS: null,
    browser: null,
    version: null,
    total: null,
    thestring: null
}

/*-----------------------------------------------------------------------------------------------*/

//Browser detect script origionally created by Peter Paul Koch at http://www.quirksmode.org/

LB.getBrowserInfo = function() {
    if (LB.checkIt('konqueror')) {
        LB.browser = "Konqueror";
        LB.OS = "Linux";
    }
    else if (LB.checkIt('safari'))  LB.browser = "Safari"
    else if (LB.checkIt('omniweb')) LB.browser = "OmniWeb"
    else if (LB.checkIt('opera'))   LB.browser = "Opera"
    else if (LB.checkIt('webtv'))   LB.browser = "WebTV";
    else if (LB.checkIt('icab'))    LB.browser = "iCab"
    else if (LB.checkIt('msie'))    LB.browser = "Internet Explorer"
    else if (!LB.checkIt('compatible')) {
        LB.browser = "Netscape Navigator"
        LB.version = LB.detect.charAt(8);
    }
    else LB.browser = "An unknown browser";

    if (!LB.version) LB.version = LB.detect.charAt(LB.place + LB.thestring.length);

    if (!LB.OS) {
        if (LB.checkIt('linux'))    LB.OS = "Linux";
        else if (LB.checkIt('x11')) LB.OS = "Unix";
        else if (LB.checkIt('mac')) LB.OS = "Mac"
        else if (LB.checkIt('win')) LB.OS = "Windows"
        else                        LB.OS = "an unknown operating system";
    }
}

LB.checkIt = function(string) {
    LB.place = LB.detect.indexOf(string) + 1;
    LB.thestring = string;
    return LB.place;
}

/*-----------------------------------------------------------------------------------------------*/

LB.Lightbox = {

    yPos : 0,
    xPos : 0,

    // Turn everything on - mainly the IE fixes
    activate: function() {
        if (LB.browser == 'Internet Explorer'){
            this.getScroll();
            this.prepareIE('100%', 'hidden');
            this.setScroll(0, 0);
            this.hideSelects('hidden');
        }
        this.displayLightbox("block");
    },

    // Ie requires height to 100% and overflow hidden or else you can scroll down past the lightbox
    prepareIE: function(height, overflow) {
        var bod = document.getElementsByTagName('body')[0];
        bod.style.height = height;
        bod.style.overflow = overflow;
  
        var htm = document.getElementsByTagName('html')[0];
        htm.style.height = height;
        htm.style.overflow = overflow;
    },

    // In IE, select elements hover on top of the lightbox
    hideSelects: function(visibility) {
        var selects = document.getElementsByTagName('select');
        for(var i = 0; i < selects.length; i++) {
            selects[i].style.visibility = visibility;
        }
    },

    // Taken from lightbox implementation found at http://www.huddletogether.com/projects/lightbox/
    getScroll: function() {
        if (self.pageYOffset) {
            this.yPos = self.pageYOffset;
        } else if (document.documentElement && document.documentElement.scrollTop){
            this.yPos = document.documentElement.scrollTop;
        } else if (document.body) {
            this.yPos = document.body.scrollTop;
        }
    },

    setScroll: function(x, y) {
        window.scrollTo(x, y);
    },

    displayLightbox: function(display) {
        $('overlay').style.display = display;
        $('lightbox').style.display = display;
    },

    deactivate: function() {
        if (LB.browser == "Internet Explorer"){
            this.setScroll(0, this.yPos);
            this.prepareIE("auto", "auto");
            this.hideSelects("visible");
        }

        this.displayLightbox("none");
    }
}

/*-----------------------------------------------------------------------------------------------*/

// Add in markup necessary to make this work. Basically two divs:
// Overlay holds the shadow
// Lightbox is the centered square that the content is put into.
LB.addLightboxMarkup = function() {
    var bod = document.getElementsByTagName('body')[0];
    var overlay = document.createElement('div');
    overlay.id = 'overlay';

    var lb = document.createElement('div');
    lb.id = 'lightbox';
    lb.className= 'loading';
    bod.appendChild(overlay);
    bod.appendChild(lb);
}

Event.observe(window, 'load', LB.addLightboxMarkup);
Event.observe(window, 'load', LB.getBrowserInfo);
// Remove to avoid 'handler has no properties' JS errors with prototype 1.6.
// See http://groups.google.com/group/rubyonrails-spinoffs/browse_thread/thread/9462257e3df7975e
// Event.observe(window, 'unload', Event.unloadCache);
