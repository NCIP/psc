<%@page contentType="text/javascript;charset=UTF-8"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<jsgen:replaceHtml targetElement="selected-arm">
    <tags:scheduledArm arm="${arm}"/>
</jsgen:replaceHtml>
Element.addClassName($("select-arm-${arm.id}"), "selected")
