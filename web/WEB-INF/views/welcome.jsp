<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Patient Study Calendar - Public Test Site</title>
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
    Thank you for testing the Patient Study Calendar Module. The PSC team is developing this
    software over a six-month period which has been divided into four releases--one every six weeks.
    The software you are about to test is the result of Release 3. Your feedback will help guide our
    development efforts.
</p>

<p>
	This release has two major components:
</p>
<ol>
	<li>Improved user interface for the creation of study templates and the creation and management of participant calendars.</li>
	<li>All interfaces to manage access rights to the various components of the application across a multi-site environment.</li>
</ol>

<p>
    A few considerations before you begin:
</p>
<ol>
    <li>This is a public instance. You may encounter sample study calendars created by other users.
        Other users will be able to see data that you enter. Please do not enter any confidential
        information.</li>
    <li>The big, beautiful green box at the bottom of each screen contains debugging information and
        will not be visible in the final release of the Patient Study Calendar Module.</li>
    <li>We have focused on functionality rather than visual design in our development efforts thus
        far. However, please feel free to make design recommendations if you so desire.</li>
</ol>

<p class="demo-link">
    Let the testing begin: <a href="<c:url value="/pages/studyList"/>" target="_blank">Public Test Site - start
    page</a>
</p>

<h2>To test the creation and management of study templates and participant calendars:</h2>

<ol>
    <li>Log in using <kbd>superuser</kbd> as both the username and password.</li>
    <li>Click "Create New Study Template" to begin the process of creating a new study.</li>
    <li>You will be presented with a blank study.  Using the on-screen buttons, rename the study, add and rename epochs, and add arms.</li>
    <li>You can also reorder the epochs and arms.</li>
    <li>When you click on an arm, you can add periods of time to that arm.  You can then select the periods to add activities.</li>
    <li>The "Mark this template as complete" link is a way to make a note that the template has been completed.</li>
    <li>After a template has been marked as complete, you can select it from the "Completed Templates" list in the Study Menu.</li>
    <li>Assign a participant, schedule arms, and click on the activities to see how more information can be recorded.</li>
    <li>Please record any bugs in our <a href="https://gforge.nci.nih.gov/tracker/?group_id=31">GForge bug tracking system</a>.  You will need to login to use the system.</li>
    <li>You may also send feedback to <a href="mailto:s-whitaker@northwestern.edu">s-whitaker@northwestern.edu</a>
    </li>
</ol>

<h2>To test our implementation of roles within the Study Calendar Module</h2>

<p>
    Here are the roles that we are able to capture with the current Study Calendar Module:
</p>
<img
    src="https://svn.bioinformatics.northwestern.edu/studycalendar/trunk/documents/SRS_FR_SwimLanes.jpg"
    alt="PSC roles" id="roles-swimlanes" width="800"/>
<table>
    <tr>
        <th>To log in as ...</th><th>... use this as username and password</th>
    </tr><tr>
        <td>Study Administrator</td><td><kbd>study_admin1</kbd></td>
    </tr><tr>
        <td>Study Coordinator</td><td><kbd>studycd_1</kbd></td>
    </tr><tr>
        <td>Participant Coordinator</td><td><kbd>participantcd_1</kbd></td>
    </tr><tr>
        <td>Research Associate</td>     <td><kbd>ra_1</kbd></td>
    </tr>
</table>
<p>
    You should find the features of the module limited to those that are assigned to that particular
    user.
</p>

<p class="demo-link">
    In case you missed the first one:
    <a href="<c:url value="/pages/studyList"/>" target="_blank">Public Demo - start page</a>
</p>

<p>
    Thank you for your participation. Please send feedback to <a
    href="mailto:s-whitaker@northwestern.edu">s-whitaker@northwestern.edu</a>.
</p>

</body>
</html>