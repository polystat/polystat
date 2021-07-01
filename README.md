<img src="https://raw.githubusercontent.com/polystat/polystat.github.io/master/logo.svg" height="64px"/>

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](http://www.rultor.com/b/polystat/polystat)](http://www.rultor.com/p/polystat/polystat)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![Build Status](https://travis-ci.org/polystat/polystat.svg?branch=master)](https://travis-ci.org/polystat/polystat)
[![PDD status](http://www.0pdd.com/svg?name=polystat/polystat)](http://www.0pdd.com/p?name=polystat/polystat)
[![Hits-of-Code](https://hitsofcode.com/github/polystat/polystat)](https://hitsofcode.com/view/github/polystat/polystat)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/polystat/polystat/blob/master/LICENSE.txt)
![Lines of code](https://img.shields.io/tokei/lines/github/polystat/polystat)
[![Maven Central](https://img.shields.io/maven-central/v/org.polystat/polystat.svg)](https://maven-badges.herokuapp.com/maven-central/org.polystat/polystat)

This is an experimental polystat static analyzer.

Download `polystat-0.1-jar-with-dependencies.jar` 
from Maven Central and then run:

```bash
$ java -jar polystat-0.1-jar-with-dependencies.jar src temp
```

The `src/foo.eo` file must contain the code in EOLANG, for example
try this simple program that has a "division by zero" bug:

```
[x] > test
  div. > @
    5
    add.
      42
      x
```

The output of Polystat will show you which `x` may cause
this program to crash. The directory `temp` will be created
automatically and will contain temporary files.

You can also play with it by editing the files in `sandbox/`
and then running `./try.sh`.
