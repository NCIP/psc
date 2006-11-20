<%@page contentType="text/javascript"%>
<%@taglib prefix="sched" tagdir="/WEB-INF/tags/schedule" %>
selectArm('<sched:scheduledArmSelectUrl scheduledArmId="${empty param.scheduledArm ? scheduledCalendar.currentArm.id : param.scheduledArm}"/>')