# Text Finder

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/text-finder-plugin/master)](https://ci.jenkins.io/job/Plugins/job/text-finder-plugin/job/master/)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/text-finder.svg)](https://plugins.jenkins.io/text-finder)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/text-finder.svg)](https://plugins.jenkins.io/text-finder)

This plugin lets you search for some text using regular expressions in a set of
files or the console log. Once a match is found, you can downgrade a successful
build to a status of
[unstable](https://javadoc.jenkins-ci.org/hudson/model/Result.html#UNSTABLE),
[failure](https://javadoc.jenkins-ci.org/hudson/model/Result.html#FAILURE), or
[not built](https://javadoc.jenkins-ci.org/hudson/model/Result.html#NOT_BUILT).

For example, you can search for the string `failure` in a set of log files. If
a match is found, you can downgrade the build from success to failure. This is
handy when you have some tools in your build chain that don't properly set the
exit code.

Note that the search is always performed, even on builds which returned a
non-zero exit status, but the reclassification only applies to builds which
returned an overall exit status of zero.

See [the Text Finder Plugin page](https://wiki.jenkins-ci.org/display/JENKINS/Text-finder+Plugin)
on the Jenkins wiki for more information.
