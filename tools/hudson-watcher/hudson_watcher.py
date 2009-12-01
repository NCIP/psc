#!/usr/bin/python

# Mirrors hudson build results (but not build details) from one hudson
# instance to another.  Similar to http://wiki.hudson-ci.org/display/HUDSON/Build+Publisher+Plugin ,
# except that it doesn't require that the publishing hudson be able to talk
# directly to the subscribing one.
#
# Specifically, this script:
# - Examines the RSS feed for a job in hudson A
# - If there are new entries, POSTs them to an external-monitor job
#   in hudson B
#
# This script is intended to be run as a cron job.

from xml.dom.minidom import parseString
from urlparse import urlparse, urlunparse
import re, binascii, os, os.path, httplib, urllib, md5, sys

def read_rss(rssText):
    doc = parseString(rssText)
    results = []
    for node in doc.getElementsByTagName("entry"):
        results.append(BuildResult(node))
    return results

class BuildResult:
    def __init__(self, rssNode):
        self.title = rssNode.getElementsByTagName("title").item(0).firstChild.data
        match = re.search('.*? #(\d+) \((\w+)\)', self.title)
        self.number = int(match.group(1))
        self.is_success = match.group(2) == "SUCCESS"
        self.url = rssNode.getElementsByTagName("link").item(0).getAttribute('href')
        self.time_string = rssNode.getElementsByTagName("published").item(0).firstChild.data

    def success_message(self):
        if self.is_success:
            return "succeeded"
        else:
            return "failed"

    def message(self):
        return "Remote build %d %s at %s\nSee %s\n" % (self.number, self.success_message(), self.time_string, self.url)

    def payload(self):
        if self.is_success:
            result = 0
        else:
            result = 1
        return """
        <run>
            <log encoding="hexBinary">%s</log>
            <result>%s</result>
        </run>
        """ % (binascii.hexlify(self.message()), result)

class History:
    def __init__(self, name):
        self.name = name
        self.read()

    def filename(self):
        return "%s/.hudson_watcher/%s.history" % (os.environ['HOME'], self.name)

    def has_seen(self, result):
        return result.number in self.seen

    def add(self, result):
        self.seen.append(result.number)
        self.write()

    def read(self):
        self.seen = []
        try:
            f = open(self.filename())
            for line in f:
                self.seen.append(int(line))
            f.close
        except IOError, e:
            pass

    def write(self):
        if not os.path.isdir(os.path.dirname(self.filename())):
            os.makedirs(os.path.dirname(self.filename()))
        f = open(self.filename(), 'w')
        for n in self.seen:
            f.write("%d\n" % n)
        f.close()

class Checker:
    def __init__(self, rss_url, monitor_endpoint):
        self.rss_url = urlparse(rss_url)
        self.monitor_endpoint = urlparse(monitor_endpoint)
        self.history = History(md5.new(rss_url + monitor_endpoint).hexdigest())

    def connect(self, http_url):
        if http_url[0] == 'https':
            return httplib.HTTPSConnection(http_url[1], strict=False)
        else:
            return httplib.HTTPConnection(http_url[1])

    def read_rss(self):
        conn = self.connect(self.rss_url)
        conn.request("GET", self.rss_url[2])
        response = conn.getresponse()
        if response.status == 200:
            self.results = read_rss(response.read())
            success = True
        else:
            sys.stderr.write("Reading RSS from %s failed: %s %s\n" %
                (urlunparse(self.rss_url), response.status, response.reason))
            success = False
        conn.close()
        return success

    def notify(self, result):
        conn = self.connect(self.monitor_endpoint)
        conn.request("POST", self.monitor_endpoint[2], result.payload(), { "Content-Type": "text/xml" })
        response = conn.getresponse()
        if response.status == 200:
            self.history.add(result)
            success = True
        else:
            sys.stderr.write("Notification of %s failed: %s %s\n" % (urlunparse(self.monitor_endpoint), response.status, response.reason))
            success = False
        conn.close()
        return success

    def check(self):
        if not self.read_rss():
            return False
        sys.stderr.write("%d builds in source RSS %s\n" % (len(self.results), urlunparse(self.rss_url)))
        success = True
        # check each entry against the history
        for r in self.results:
            if not self.history.has_seen(r):
                sys.stderr.write("Notifying about build #%d\n" % r.number)
                success = success & self.notify(r)
            else:
                sys.stderr.write("Already notified about build #%d\n" % r.number)
        return success

    def check_and_exit(self):
        if self.check():
            sys.exit(0)
        else:
            print "Check failed (see above)"
            sys.exit(1)

if __name__ == '__main__':
    if len(sys.argv) <> 3:
        print "Usage: %s source-job-rss-url target-job-post-build-url" % sys.argv[0]
        sys.exit(1)
    Checker(sys.argv[1], sys.argv[2]).check_and_exit()
