<%@page contentType="text/javascript;charset=UTF-8"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
<jsgen:replaceHtml targetElement="selected-arm">
    <templ:arm arm="${arm}"/>
</jsgen:replaceHtml>
Element.addClassName("arm-${arm.base.id}-item", "selected")
SC.slideAndShow('selected-arm-content')
