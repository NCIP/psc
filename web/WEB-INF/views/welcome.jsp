<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Patient Study Calendar - Public Demo</title>
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
<h1>Patient Study Calendar - Public Demo</h1>

<p>
    Thank you for testing the Patient Study Calendar Module. The PSC team is developing this
    software over a six-month period which has been divided into four releases--one every six weeks.
    The software you are about to test is the result of Release 2. Your feedback will help guide our
    development efforts.
</p>

<p>
    We are debuting a few new features as well as the ability to use the application from the
    perspective of various roles within a clinical research organization. You will find a few
    guiding instructions below.
</p>

<p>
    A few considerations before you begin:
</p>
<ol>
    <li>This is a public instance. You may encounter sample study calendars created by other users.
        Other users will be able to see data that you enter. Please do not enter any confidential
        information.</li>
    <li>The big, beautiful green box at the bottom of each screen contains debugging information and
        will not be visible in the final release of the Study Calendar Module.</li>
    <li>We have focused on functionality rather than visual design in our development efforts thus
        far. However, please feel free to make design recommendations if you so desire.</li>
</ol>

<p class="demo-link">
    Let the testing begin: <a href="<c:url value="/pages/studyList"/>" target="_blank">Public Demo - start
    page</a>
</p>

<h2>To test all of the features of the Study Calendar Module in a single session</h2>

<ol>
    <li>Log in using <kbd>superuser</kbd> as both the username and password.</li>
    <li>Click "New study" to begin the process of creating a new study.</li>
    <li>You can name the study, create epochs (screening, treatment, follow-up, etc...), and add arms
        to the study from this page.</li>
    <li>The next page is the "Template page." You'll come back to this now and then. You'll want to
        add some periods to the arms of your study in order to make this more interesting.</li>
    <li>When you've finished creating your periods, go ahead and select one from the template in
        order to add activities to it. Add some activities by selecting it from the drop box,
        clicking the Add button, and specifying how times you would like that activity to occur on
        any day within the period. If you don't see the activity that you want, feel free to create
        a new one.</li>
    <li>The "Mark this template as complete" link is a way to make a note that the template has been
        reviewed and is both correct and complete.</li>
    <li>Please also try adding some participants to the study. You can create new participants,
        too.</li>
    <li>Please send your feedback to <a href="mailto:s-whitaker@northwestern.edu">s-whitaker@northwestern.edu</a>
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