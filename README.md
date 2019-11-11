# Text Finder

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/text-finder-plugin/master)](https://ci.jenkins.io/job/Plugins/job/text-finder-plugin/job/master/)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/text-finder.svg)](https://plugins.jenkins.io/text-finder)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/text-finder.svg)](https://plugins.jenkins.io/text-finder)
[![Dependabot Status](https://api.dependabot.com/badges/status?host=github&repo=jenkinsci/text-finder-plugin)](https://dependabot.com)

This plugin lets you search for some text using regular expressions in a
set of files or the console log. Once a match is found, you can
downgrade a successful build to a status of 
[unstable](https://javadoc.jenkins-ci.org/hudson/model/Result.html#UNSTABLE),
[failure](https://javadoc.jenkins-ci.org/hudson/model/Result.html#FAILURE),
or [not built](https://javadoc.jenkins-ci.org/hudson/model/Result.html#NOT_BUILT).

For example, you can search for the string `failure` in a set of log
files. If a match is found, you can downgrade the build from success to
failure. This is handy when you have some tools in your build chain that
don't properly set the exit code.

Note that the search is always performed, even on builds which returned
a non-zero exit status, but the reclassification only applies to builds
which returned an overall exit status of zero.

### Usage

The basic pipeline syntax is as follows:

        findText regexp: '<regular expression>', fileSet: '<file set>'

The regular expression uses the syntax supported by the Java `Pattern`
class. The file set specifies the path to the files in which to search,
relative to the workspace root. This can use wildcards, like
`logs/**/*/*.txt`. See the Ant `FileSet` documentation for details.

To also check the console log, use the following syntax:

        findText regexp: '<regular expression>', fileSet: '<file set>', alsoCheckConsoleOutput: true

Note that if you just want to check the console log, you can omit the
file set:

        findText regexp: '<regular expression>', alsoCheckConsoleOutput: true

To customize the build result, you can use any combination of
`succeedIfFound`, `unstableIfFound`, or `notBuiltIfFound`. For example,
to mark the build as unstable if the expression is found:

        findText regexp: '<regular expression>, alsoCheckConsoleOutput: true, unstableIfFound: true

### Changelog

##### Versions 1.12 and newer

See the [GitHub release page](https://github.com/jenkinsci/text-finder-plugin/releases).

##### Version 1.11 (June 1, 2019)

-   Add pipeline support
    ([\#12](https://github.com/jenkinsci/text-finder-plugin/pull/12))
-   Add option to set build as not built
    ([\#16](https://github.com/jenkinsci/text-finder-plugin/pull/16))

-   Better log output to console
    ([\#21](https://github.com/jenkinsci/text-finder-plugin/pull/21))

##### Version 1.10 (Jan 31, 2014)

-   No longer blocking concurrent builds.
    ([JENKINS-17507](https://issues.jenkins-ci.org/browse/JENKINS-17507))
-   Cleaner config UI using help buttons
-   Japanese translation

##### Version 1.9 (Feb 13, 2011)

-   Update link in help

##### Version 1.8 (Feb 6, 2010)

-   Update code for more recent Hudson

##### Version 1.7 (May 4, 2009)

-   Fixed a file handle leak
    ([JENKINS-3613](https://issues.jenkins-ci.org/browse/JENKINS-3613))

##### Version 1.6 (Nov 6, 2008)

-   Modified to work with all job types, including Maven2 jobs.

##### Version 1.5

-   Added "Unstable if found" configuration option.  Use this option to
    set build unstable instead of failing the build.  Default is off
    (previous behavior).
