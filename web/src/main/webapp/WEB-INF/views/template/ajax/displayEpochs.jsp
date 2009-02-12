<%@page contentType="text/javascript;charset=UTF-8"%>
        <%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
        <%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
        <%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
        <%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <jsgen:replaceHtml targetElement="selected-epochs">
        <c:forEach items="${epochs}" var="epoch">
        <ul class="row">
                <div class="row odd">
                        <label>${epoch.name}</label>
                        </div>
                <div class="row even">
                        <templ:epochs epoch="${epoch}"/>

                        </div>
                </ul>

        </c:forEach>

        </jsgen:replaceHtml>
