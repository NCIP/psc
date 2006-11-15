<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<html>
<head>
    <title>Configure application</title>
    <style type="text/css">
        div.row {
            padding: 5px 3px;
        }
        .row .value {
            margin-left: 22%;
        }
        .row .label {
            width: 20%;
            margin-left: 1em;
            text-align: right;
        }
        p.description {
            margin: 0.25em 0 0 1em;
        }
        div.submit {
            text-align: right;
        }
        .value input {
            width: 80%;
        }
    </style>
</head>
<body>
    <h1>Configure application</h1>
    <form:form>
        <c:forEach items="${command.conf}" var="entry" varStatus="status">
            <div class="row ${commons:parity(status.count)}">
                <div class="label">
                    <form:label path="conf[${entry.key}].value">${entry.value.property.name}</form:label>
                </div>
                <div class="value">
                    <div><form:input path="conf[${entry.key}].value"/></div>
                    <p class="description">${entry.value.property.description}</p>
                    <c:if test="${not empty entry.value.default}"><p class="description">(Default: ${entry.value.default})</p></c:if>
                </div>
            </div>
        </c:forEach>
        <div class="row submit">
            <input type="submit" value="Save"/>
        </div>
    </form:form>
</body>
</html>