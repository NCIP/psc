<%--
    Template and behavior for ICS helper dialog.
    Expects to be invoked from an anchor whose HREF is the desired ICS URI.
    Depends on jquery-ui and jquery-cookie
--%>
<%@tag%>

<script type="text/html" id="ics_help">
    <h1>Subscribing to a schedule</h1>
    <div id="ics-tabs">
        <ul>
            <li><a href="#ics-outlook">Outlook 2007</a></li>
            <li><a href="#ics-ical">iCal</a></li>
            <li><a href="#ics-otherapps">Other applications</a></li>
        </ul>

        <div id="ics-outlook">
            <ul>
                <li>
                    <a href="[#= webcal_href #]">Subscribe in Outlook 2007</a>
                    [# if (https) { #]
                    <p>Note: this link may only work in Internet Explorer</p>
                    [# } #]
                </li>
                [# if (!https) { #]
                <li>
                    Manually:
                    <ol>
                        <li>Copy this URL <a href="[#= ics_href #]">[#= ics_href #]</a></li>
                        <li>Go to Tools > Account Settings</li>
                        <li>Select the "Internet Calendars" tab</li>
                        <li>Select "New..."</li>
                        <li>Paste in the URL from step 1</li>
                        <li>Click "Add"</li>
                    </ol>
                </li>
                [# } #]
            </ul>
            <p>
                Previous versions of Outlook are not capable of subscribing to ICS feeds.  You may be able
                to <a href="[#= ics_href #]">download</a> the ICS and then import it into
                an older version.  Note that you will not automatically receive updates if you import it
                this way.
            </p>
        </div>

        <div id="ics-ical">
            <ul>
                [# if (!https) { #]
                <li><a href="[#= webcal_href #]">Subscribe in iCal</a></li>
                [# } #]
                <li>Manually:
                    <ol>
                        <li>Copy this URL <a href="[#= ics_href #]">[#= ics_href #]</a></li>
                        <li>Go to Calendar > Subscribe...</li>
                        <li>Paste in the URL from the step 1</li>
                        <li>Click "Subscribe"</li>
                    </ol>
                </li>
            </ul>
            <p>
                This procedure is for iCal 3.0 (the version that comes with OS X 10.5).
                The steps may be different on older versions of iCal.
                [# if (https) { #]
                Also, older versions of iCal have problems with <tt>https</tt>
                URLs (like the one used by this deployment of PSC), so subscribing
                may not work.
                [# } #]
            </p>
        </div>

        <div id="ics-otherapps">
            <p>
                Other applications might support subscribing by:
            </p>
            <ul>
                <li>clicking on this <a href="[#= webcal_href #]">webcal link</a></li>
                <li>copying this ICS URL <a href="[#= ics_href #]">[#= ics_href #]</a> into a
                particular place in the application</li>
            </ul>
            <p>
                Check the application's documentation for more information.
            </p>
            <p>
                If your desired application supports importing ICS (but not subscribing),
                you can <a href="[#= ics_href #]">download</a> the ICS and import it.
                Note that you will not automatically receive updates if you import it
                this way.
            </p>
        </div>
    </div>
    <div class="closer">
        <input type="button" class="close" value="Close"/>
    </div>
</script>

<style type="text/css">
    #lightbox.ics #ics-tabs {
        font-size: 1.0em;
        margin: 0 0.5em;
    }

    #lightbox.ics .closer {
        text-align: right;
        padding: 0.5em;
    }
</style>

<script type="text/javascript">
    (function ($) {
        psc.namespace("ics");

        psc.ics.COOKIE_NAME = 'psc-ics-tab';

        psc.ics.displayInstructions = function () {
            var path = $(this).attr('href');
            var base = window.location.toString().match(/^https?:\/\/[^\/]+/)[0] + path;
            var instructions = resigTemplate("ics_help", {
                https: base.match(/^https/),
                ics_href: base,
                webcal_href: base.replace(/^http/, 'webcal')
            });

            $('#lightbox').html(instructions).addClass('ics');
            $('#ics-tabs').tabs({
                // The tab control's built in cookie support doesn't work if you
                // open more than one tab set using the same cookie name in the same
                // page.  Seems to be because it clears the cookie value in #destroy.
                selected: parseInt($.cookie(psc.ics.COOKIE_NAME), 10) || 0
            });
            $('#lightbox .close').click(psc.ics.hideInstructions);
            LB.Lightbox.activate();

            return false;
        }

        psc.ics.hideInstructions = function () {
            $.cookie(psc.ics.COOKIE_NAME,
                $('#ics-tabs').tabs('option', 'selected'), { expires: 90 });
            LB.Lightbox.deactivate();
            $('#lightbox').removeClass("ics");
        }

        $(window).load(function () {
            $('a.control.ics-subscribe').click(psc.ics.displayInstructions);
        })
    }(jQuery));
</script>