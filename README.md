# Text Finder Plugin

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/text-finder-plugin/master)](https://ci.jenkins.io/job/Plugins/job/text-finder-plugin/)
[![Jenkins Plugins](https://img.shields.io/jenkins/plugin/v/text-finder.svg)](https://plugins.jenkins.io/text-finder)

This plugin lets you search keywords in the files you specified and use that to downgrade a "successful" build to be unstable or a failure.

This is handy when you have some tools in your build chain that don't use the exit code properly.

The search is always performed, even on builds which returned a non-zero exit status, but the reclassification only applies to builds which returned an overall exit status of zero.


See the documentation and release notes at [Text-finder Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Text-finder+Plugin) on the Jenkins Wiki for more information.




