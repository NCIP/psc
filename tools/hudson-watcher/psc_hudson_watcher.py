#!/usr/bin/env python

# Sugar to simplify the invocation of hudson_watcher.py for PSC's builds

import sys, urllib
from hudson_watcher import Checker

if __name__ == '__main__':
    if len(sys.argv) <> 2:
        print "Usage: %s 'job name'" % sys.argv[0]
        sys.exit(1)
    job_path = urllib.quote(sys.argv[1])
    Checker(
        "https://ctms-ci.nubic.northwestern.edu/hudson/job/%s/rssAll" % job_path,
        "http://ncias-d228-v.nci.nih.gov:48080/hudson/job/%s%%20(external)/postBuildResult" % job_path
    ).check_and_exit();
