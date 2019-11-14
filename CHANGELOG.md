## Changelog

### 1.12 

Release date: 2019-08-01

- Documentation updates: Improve writing of plugin description
  ([PR 28](https://github.com/jenkinsci/text-finder-plugin/pull/28))
- Internal: Text Finder now fully supports [JEP-210](https://github.com/jenkinsci/jep/tree/master/jep/210) and requires Jenkins 2.121 LTS or greater and JEP 210-compatible versions of Pipeline plugins.
  - Avoid calling Run.getLogFile [JENKINS-54128](https://issues.jenkins-ci.org/browse/JENKINS-54128) ([PR 46](https://github.com/jenkinsci/text-finder-plugin/pull/46))
  - Make naming consistent ([PR 29](https://github.com/jenkinsci/text-finder-plugin/pull/29))
- Tests: Add UI testing ([PR 41](https://github.com/jenkinsci/text-finder-plugin/pull/41))

### 1.11

Release date: 2019-06-01

-   Add pipeline support
    ([PR 12](https://github.com/jenkinsci/text-finder-plugin/pull/12))
-   Add option to set build as not built
    ([PR 16](https://github.com/jenkinsci/text-finder-plugin/pull/16))

-   Better log output to console
    ([PR 21](https://github.com/jenkinsci/text-finder-plugin/pull/21))

### 1.10

Release date: 2014-01-31

-   No longer blocking concurrent builds.
    ([JENKINS-17507](https://issues.jenkins-ci.org/browse/JENKINS-17507))
-   Cleaner config UI using help buttons
-   Japanese translation

### 1.9

Release date: 2011-02-13

-   Update link in help

### 1.8

Release date: 2010-02-06

-   Update code for more recent Hudson

### 1.7

Release date: 2009-05-04

-   Fixed a file handle leak
    ([JENKINS-3613](https://issues.jenkins-ci.org/browse/JENKINS-3613))

### 1.6

Release date: 2008-11-06

-   Modified to work with all job types, including Maven2 jobs.

### 1.5

-   Added "Unstable if found" configuration option.  Use this option to
    set build unstable instead of failing the build.  Default is off
    (previous behavior).
