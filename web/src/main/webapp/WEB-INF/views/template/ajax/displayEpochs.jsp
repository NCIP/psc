<%@page contentType="text/javascript;charset=UTF-8"%>
        <%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
        <%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
        <%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
        <%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>

        <jsgen:replaceHtml targetElement="studyName">
            ${study.name}
        </jsgen:replaceHtml>
        <jsgen:replaceHtml targetElement="selected-epochs">
        <ul class="row">
            <c:forEach items="${epochs}" var="epoch">
                    <div class="row even">
                        <templ:epochs epoch="${epoch}"/>
                    </div>
            </c:forEach>
        </ul>
        </jsgen:replaceHtml>
