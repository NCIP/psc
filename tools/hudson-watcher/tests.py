from hudson_watcher import *
from nose.tools import *
from xml.dom.minidom import parseString

class TestBuildResult:
    def setUp(self):
        f = open('sample-rss.xml')
        self.results = read_rss(f.read(), History('test'))
        f.close()

    def actual_payload(self, i):
        return parseString(payload(self.results[i]))

    def test_result_count(self):
        eq_(len(self.results), 10)

    def test_has_build_number(self):
        eq_(self.results[1].number, 527)

    def test_is_success_for_success(self):
        assert self.results[2].is_success

    def test_is_not_success_for_failure(self):
        assert_false(self.results[4].is_success)

    def test_success_message_for_success(self):
        eq_("succeeded", self.results[1].success_message())

    def test_success_message_for_failure(self):
        eq_("failed", self.results[4].success_message())

    def test_time_string(self):
        eq_("2009-11-25T19:11:20Z", self.results[2].time_string)

    def test_message(self):
        eq_("Remote build 526 succeeded at 2009-11-25T19:11:20Z\nSee https://ctms-ci.nubic.northwestern.edu/hudson/job/PSC%20trunk%20matrix/526/\n",
            self.results[2].message())

    def test_payload_doc_elt_is_run(self):
        eq_("run", self.actual_payload(3).documentElement.tagName)

    def test_payload_result_is_zero_for_success(self):
        eq_("0", self.actual_payload(0).getElementsByTagName('result').item(0).firstChild.data)

    def test_payload_result_is_one_for_failure(self):
        eq_("1", self.actual_payload(4).getElementsByTagName('result').item(0).firstChild.data)

    def test_payload_log_element_is_hexbinary_encoded(self):
        log = self.actual_payload(3).getElementsByTagName('log').item(0)
        eq_("hexBinary", log.getAttribute("encoding"))
        eq_("52656d6f7465", log.firstChild.data[0:12])

