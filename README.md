# Text Finder Plugin

[![Build Status](https://ci.jenkins.io/job/Plugins/job/text-finder-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/text-finder-plugin/job/master/)
[![Contributors](https://img.shields.io/github/contributors/jenkinsci/text-finder-plugin.svg)](https://github.com/jenkinsci/text-finder-plugin/graphs/contributors)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/text-finder.svg)](https://plugins.jenkins.io/text-finder)
[![GitHub release](https://img.shields.io/github/release/jenkinsci/text-finder-plugin.svg?label=changelog)](https://github.com/jenkinsci/text-finder-plugin/releases/latest)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/text-finder.svg?color=blue)](https://plugins.jenkins.io/text-finder)

## Introduction

This plugin lets you search for some text using regular expressions in a
set of files or the console log. Based on the outcome, you can downgrade
the build result to `UNSTABLE`, `FAILURE`, `NOT_BUILT`, or `ABORTED`.

For example, you can search for the string `failure` in a set of log
files. If a match is found, you can downgrade the build result from
`SUCCESS` to `FAILURE`. This is handy when you have some tools in your
build chain that do not properly set the exit code.

## Getting started

### [Pipeline](https://jenkins.io/doc/book/pipeline/) jobs

The basic Pipeline syntax is as follows:

```
findText(textFinders: [textFinder(regexp: '<regular expression>', fileSet: '<file set>')])
```

The regular expression uses the syntax supported by the Java
[`Pattern`](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html)
class. The file set specifies the path to the files to search, relative
to the workspace root. This can use wildcards, like `logs/**/*/*.txt`.
See the documentation for the `@includes` attribute of the Ant
[`FileSet`](https://ant.apache.org/manual/Types/fileset.html) type for
details.

To also check the console log, use the following syntax:

```
findText(textFinders: [textFinder(regexp: '<regular expression>', fileSet: '<file set>', alsoCheckConsoleOutput: true)])
```

Note that if you just want to check the console log, you can omit the
file set:

```
findText(textFinders: [textFinder(regexp: '<regular expression>', alsoCheckConsoleOutput: true)])
```

To downgrade the build result, use the following syntax:

```
findText(textFinders: [textFinder([...], buildResult: 'UNSTABLE')])
```

If a match is found, the build result will be set to this value. Note
that the build result can only get worse, so you cannot change the
result to `SUCCESS` if the current result is `UNSTABLE` or worse.

Whether or not a build is downgraded depends on its change condition. The
default change condition is `MATCH_FOUND`, which downgrades the build result if
a match is found. To downgrade the build result if a match is _not_ found, set
the change condition to `MATCH_NOT_FOUND`:

```
findText(textFinders: [textFinder([...], changeCondition: 'MATCH_NOT_FOUND', buildResult: 'UNSTABLE')])
```

To search for multiple regular expressions, use the following syntax:

```
findText(textFinders: [
  textFinder(regexp: '<regular expression 1>', [...]),
  textFinder(regexp: '<regular expression 2>', [...]),
  textFinder(regexp: '<regular expression 3>', [...])
])
```

### Freestyle jobs

This plugin provides [Job DSL](https://plugins.jenkins.io/job-dsl/)
support for Freestyle jobs using [the Dynamic
DSL](https://github.com/jenkinsci/job-dsl-plugin/wiki/Dynamic-DSL). For
example:

```
job('example') {
  publishers {
    findText {
      textFinders {
        textFinder {
          regexp '<regular expression>'
          fileSet '<file set>'
          changeCondition '<MATCH_FOUND or MATCH_NOT_FOUND>'
          alsoCheckConsoleOutput true
          buildResult 'UNSTABLE'
        }
      }
    }
  }
}
```

To search for multiple regular expressions, use the following syntax:

```
job('example') {
  publishers {
    findText {
      textFinders {
        textFinder {
          regexp '<regular expression 1>'
          [...]
        }
        textFinder {
          regexp '<regular expression 2>'
          [...]
        }
        textFinder {
          regexp '<regular expression 3>'
          [...]
        }
      }
    }
  }
}
```

## Issues

Report issues and enhancements in the [Jenkins issue tracker](https://issues.jenkins-ci.org/).

## Contributing

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md).
