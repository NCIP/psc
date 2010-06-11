<%@page contentType="text/javascript" %>

psc.namespace('configuration');

psc.configuration.calendarDateFormat = function() {
    var dateFormat = '${configuration.map.displayDateFormat}';
    if (dateFormat.toLowerCase() == "mm/dd/yyyy") {
        return "%m/%d/%Y";
    } else if (dateFormat.toLowerCase() == "dd/mm/yyyy") {
        return "%d/%m/%Y";
    } else {
        javascript:alert("Data format error: Unsupported date format - should be either dd/mm/yyyy or mm/dd/yyyy")
    }

};

