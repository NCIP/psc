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
import re, binascii, os, os.path, httplib, urllib, md5, sys, time, ConfigParser, traceback

def read_rss(rssText, history):
    doc = parseString(rssText)
    results = []
    for node in doc.getElementsByTagName("entry"):
        results.append(BuildResult(node, history))
    return results

def payload(build):
    if build.is_success:
        result = 0
    else:
        result = 1
    return """
    <run>
        <log encoding="hexBinary">%s</log>
        <result>%s</result>
    </run>
    """ % (binascii.hexlify(build.message()), result)

class BuildResult:
    def __init__(self, rssNode, history):
        self.history = history
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

    def log_name(self):
        return "build #%d" % self.number

    def should_report(self):
        return not self.history.has_seen(self)

    def notification_successful(self):
        self.history.add(self)

class BuildAccessFailure:
    SECTION = "failure"
    PERSISTENT_ATTR = ["start_time", "most_recent_time", "last_reason"]
    NOTIFICATION_INTERVAL = 2 * 60 * 60
    fn = classmethod(lambda cls, name: "%s/.hudson_watcher/%s.failure" % (os.environ['HOME'], name))
    def clean(cls, name):
        n = cls.fn(name)
        if os.path.exists(n):
            os.remove(n)
    clean = classmethod(clean)

    def __init__(self, rss_url, name, reason):
        self.rss_url_s = rss_url
        self.new_reason = reason
        self.new_time = int(time.time())
        self.is_success = False
        self.name = name
        self.read()

    def read(self):
        self.record = ConfigParser.RawConfigParser()
        self.record.read(self.filename())
        if not self.record.has_section(BuildAccessFailure.SECTION):
            self.record.add_section(BuildAccessFailure.SECTION)
            self.start_time = int(time.time())
            self.new_time = self.start_time

    def write(self):
        f = open(self.filename(), 'w')
        self.record.write(f)
        f.close()

    def __setattr__(self, name, value):
        if name in BuildAccessFailure.PERSISTENT_ATTR:
            self.record.set(BuildAccessFailure.SECTION, name, value)
        else:
            self.__dict__[name] = value

    def __getattr__(self, name):
        if name in BuildAccessFailure.PERSISTENT_ATTR:
            try:
                return self.record.get(BuildAccessFailure.SECTION, name)
            except ConfigParser.NoOptionError:
                return None
        else:
            raise AttributeError("No attr %s" % name)

    def filename(self):
        return BuildAccessFailure.fn(self.name)

    def log_name(self):
        return "failure %s at %s" % (self.new_reason, time.asctime(time.localtime(self.new_time)))

    def should_report(self):
        return (not self.most_recent_time) or \
            (int(self.most_recent_time) + BuildAccessFailure.NOTIFICATION_INTERVAL < self.new_time) or \
            (self.last_reason != self.new_reason)

    def message(self):
        return "Reading from source %s failed with %s\n" % (self.rss_url_s, self.new_reason)

    def notification_successful(self):
        self.most_recent_time = self.new_time
        self.last_reason = self.new_reason
        self.write()

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
        self.rss_url_s = rss_url
        self.rss_url = urlparse(rss_url)
        self.monitor_endpoint_s = monitor_endpoint
        self.monitor_endpoint = urlparse(monitor_endpoint)
        self.history = History(self.id())

    def id(self):
        return md5.new(self.rss_url_s + self.monitor_endpoint_s).hexdigest()

    def connect(self, http_url):
        if http_url[0] == 'https':
            return httplib.HTTPSConnection(http_url[1], strict=False)
        else:
            return httplib.HTTPConnection(http_url[1])

    def read_rss(self):
        try:
            conn = self.connect(self.rss_url)
            conn.request("GET", self.rss_url[2])
            response = conn.getresponse()
            if response.status == 200:
                self.results = read_rss(response.read(), self.history)
                self.results.reverse()
                BuildAccessFailure.clean(self.id())
            else:
                reason = "HTTP status %s %s" % (response.status, response.reason)
                sys.stderr.write("Reading RSS from %s failed: %s\n" %
                    (self.rss_url_s, reason))
                self.results = [BuildAccessFailure(self.rss_url_s, self.id(), reason)]
            conn.close()
        except Exception, e:
            sys.stderr.write("Reading RSS from %s failed with exception: %s\n" % (self.rss_url_s, e))
            traceback.print_stack(file=sys.stderr)
            self.results = [BuildAccessFailure(self.rss_url_s, self.id(), "exception \"%s\"" % e)]

    def notify(self, result):
        conn = self.connect(self.monitor_endpoint)
        conn.request("POST", self.monitor_endpoint[2], payload(result), { "Content-Type": "text/xml" })
        response = conn.getresponse()
        if response.status == 200:
            result.notification_successful()
            success = True
        else:
            sys.stderr.write("Notification of %s failed: %s %s\n" % (self.monitor_endpoint_s, response.status, response.reason))
            success = False
        conn.close()
        return success

    def check(self):
        self.read_rss()
        success = True
        for r in self.results:
            if r.should_report():
                sys.stderr.write("Notifying about %s\n" % r.log_name())
                success = success & self.notify(r)
            else:
                sys.stderr.write("Not notifying about %s\n" % r.log_name())
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
