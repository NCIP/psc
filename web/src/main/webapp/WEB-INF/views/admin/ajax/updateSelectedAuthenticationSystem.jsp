<%@page contentType="text/javascript;charset=UTF-8" language="java"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="admin" tagdir="/WEB-INF/tags/admin"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<tags:noform>
    <jsgen:replaceHtml targetElement="errors">
        <tags:errors path="*"/>
    </jsgen:replaceHtml>
    <jsgen:replaceHtml targetElement="system-configuration">
        <admin:authenticationSystemOptions/>
    </jsgen:replaceHtml>
</tags:noform>
