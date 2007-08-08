<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <style type="text/css">
        #roles-swimlanes {
            display: block;
            margin: 1em auto;
        }

        p.demo-link {
            background-color: #cfc;
            border: 1px solid #9c6;
            margin: 1em 3em;
            padding: 1em;
            font-size: 1.2em;
        }

        table {
            border-collapse: collapse;
            border: 1px solid #9f6;
            margin: 1em auto;
        }

        td, th {
            border: 1px solid #9c6;
            padding: 3px;
        }

        th {
            background-color: #cfc
        }
    </style>
</head>


<body>
<h1>Patient Study Calendar - Public Test Site</h1>

<p>
    Thank you for testing the Patient Study Calendar. The PSC team is developing this
    software over a six-month period which has been divided into four releases--one every six weeks.
    The software you are about to test is the result of Release 3. Your feedback will help guide our
    development efforts.
</p>

<p>
	This release has two major components:
</p>
<ol>
	<li>Improved user interface for the creation and management of study templates and participant calendars.</li>
	<li>All interfaces to manage access rights to the various components of the application across a multi-site environment.</li>
</ol>

<p>
    Two considerations before you begin:
</p>
<ol>
    <li>This is a public instance. You may encounter sample study calendars created by other users.
        Other users will be able to see data that you enter. Please do not enter any confidential
        information.</li>
    <li>The big, beautiful green box at the bottom of each screen contains debugging information and
        will not be visible in the final release of the Patient Study Calendar.</li>
</ol>

<p class="demo-link">
    Let the testing begin: <a href="<c:url value="/pages/studyList"/>" target="_blank">Public Test Site - start
    page</a>
</p>

<h2>To test the creation and management of study templates and participant calendars:</h2>

<ol>
    <li>Login using <kbd>superuser</kbd> as both the username and password.</li>
    <li>Click "Create New Study Template" to begin the process of creating a new study.</li>
    <li>You will be presented with a blank study.  Using the on-screen buttons, rename the study, add and rename epochs, and add arms.</li>
    <li>You can also reorder the epochs and arms.</li>
    <li>When you click on an arm, you can add periods of time to that arm.  You can then select the periods to add activities.</li>
    <li>Click "Mark this template as complete" when you have finished creating the template.</li>
	<li>Because a template must be associated with a site before participants can be added, find your template in the list of completed templates and click the "Assign Sites" link next to the template.  Assign a Site to your template.</li>  
    <li>You can click on your template in the list of "Completed Templates" and then click "Assign Participants" within the Template view.</li>
    <li>Assign a participant, schedule arms, and click on the activities to see how more information can be recorded.</li>
    <li>Please record any bugs, suggestions, or requests for new features in our <a href="http://gforge.nci.nih.gov/tracker/?func=add&group_id=31&atid=1043">GForge tracking system</a>.</li>
    <li>You may also send feedback to <a href="mailto:s-whitaker@northwestern.edu">s-whitaker@northwestern.edu</a></li>
</ol>

<h2>To test access rights management (see diagram below for more details):</h2>

<ol>
	<li>Login as a Study Coordinator (username and password of <kbd>studycd_1</kbd>), create a study template, and mark it as complete.  Logout.</li>
	<li>Login as a Study Administrator (username and password of <kbd>study_admin1</kbd>).  Click "Manage Sites" and add your site to the list by creating a new site.</li>
	<li>Click the "Assign Site Coordinator" link next to your site and add a Site Coordinator to your Site.  Return to the Study Menu.</li>
	<li>Click on the "Assign to Site" link next to the study template that you created above.  Assign it to your Site.  Logout.</li>
	<li>Login as the Site Coordinator from above (username and password of <kbd>sitecd_1</kbd>).  Near the bottom of the screen click Assign Participant Coordinators to Site and assign one to your Site.</li>
	<li>Next to the template that you created, click "Assign Participant Coordinator" and assign a Participant Coordinator.  Logout.</li>
	<li>Login as the Participant Coordinator (username and password of <kbd>participantcd_1</kbd>)and click "assign participants."</li>
    <li>Please record any bugs, suggestions, or requests for new features in our <a href="http://gforge.nci.nih.gov/tracker/?func=add&group_id=31&atid=1043">GForge tracking system</a>.</li>
    <li>You may also send feedback to <a href="mailto:s-whitaker@northwestern.edu">s-whitaker@northwestern.edu</a></li>
</ol>
<p>
    This Activity Diagram explains the way that each user interacts with the Patient Study Calendar:
</p>
<a id="roles-swimlanes" href="https://svn.bioinformatics.northwestern.edu/studycalendar/trunk/documents/SRS_FR_SwimLanes.jpg"> <img
    src="https://svn.bioinformatics.northwestern.edu/studycalendar/trunk/documents/SRS_FR_SwimLanes.jpg"
    alt="PSC roles"  width="800"/> </a>

<p class="demo-link">
    In case you missed the first one:
    <a href="<c:url value="/pages/studyList"/>" target="_blank">Public Test Site - start page</a>
</p>

<p>
    Thank you for your participation. Please send feedback to <a
    href="mailto:s-whitaker@northwestern.edu">s-whitaker@northwestern.edu</a>.
</p>

</body>
</html>