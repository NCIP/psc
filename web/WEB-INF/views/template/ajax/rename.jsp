<%@ page contentType="text/javascript;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<c:if test="${not empty command.arm}">
    <jsgen:replaceHtml targetElement="arm-${command.arm.id}">${command.arm.name}</jsgen:replaceHtml>
    if (selectedArmId == ${command.arm.id}) {
        <jsgen:replaceHtml targetElement="selected-arm-header">${command.arm.qualifiedName}</jsgen:replaceHtml>
    }
</c:if>
<c:if test="${not empty command.epoch}">
    <jsgen:replaceHtml targetElement="epoch-${command.epoch.id}-name">${command.epoch.name}</jsgen:replaceHtml>
</c:if>
<c:if test="${not empty command.study}">
    <jsgen:replaceHtml targetElement="study-name">${command.study.name}</jsgen:replaceHtml>
</c:if>