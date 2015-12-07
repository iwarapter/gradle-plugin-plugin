Gradle Plugin Plugin
=========

This is a gradle plugin for building plugins. It saves on boiler plate build file setup and provides helpful quick start task.

[![Build Status](https://travis-ci.org/iwarapter/gradle-plugin-plugin.svg?branch=master)](https://travis-ci.org/iwarapter/gradle-plugin-plugin)
[![Coverage Status](https://coveralls.io/repos/iwarapter/gradle-plugin-plugin/badge.svg?branch=master&service=github)](https://coveralls.io/github/iwarapter/gradle-plugin-plugin?branch=master)

Usage
-----------

Build script snippet for use in all Gradle versions:
```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.iadams.gradle.plugins:gradle-plugin-plugin:0.1"
  }
}

apply plugin: "com.iadams.gradle-plugin-plugin"
```
Build script snippet for new, incubating, plugin mechanism introduced in Gradle 2.1:
```
plugins {
  id "com.iadams.gradle-plugin-plugin" version "0.1"
}
```

Tasks
-----------
```
Setup tasks
-----------
setupPlugin - Generates an example 'helloworld' plugin.
```