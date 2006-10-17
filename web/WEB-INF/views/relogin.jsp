<%@ page contentType="text/javascript;charset=UTF-8" language="java" %>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<jsgen:replaceHtml targetElement="lightbox">
    <h1>Session timed out</h1>
    <div id="lightbox-content">
        <p>
            Your session has expired.  For security, please log in again
            and then retry your last action.
        </p>
        <tags:loginForm ajax="${true}"/>
    </div>
</jsgen:replaceHtml>
LB.Lightbox.activate()
Event.observe('login', 'submit', function(e) {
    $('login-indicator').reveal()
    Event.stop(e);
    LB.Lightbox.deactivate()
    SC.asyncSubmit('login', {
        onComplete: function() {
            $('login-indicator').conceal()
        },
        onFailure: function() {
            // TODO:
            alert("Need error handling")
        }
    })
})
Event.observe('login-cancel-button', 'click', function() { LB.Lightbox.deactivate() });