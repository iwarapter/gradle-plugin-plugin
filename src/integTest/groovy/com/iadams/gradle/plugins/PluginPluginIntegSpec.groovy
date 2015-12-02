package com.iadams.gradle.plugins

import com.iadams.gradle.plugins.utils.TestKitBaseIntegSpec
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class PluginPluginIntegSpec extends TestKitBaseIntegSpec {

  @Unroll
  def "compatible with gradle #gradleVersion"() {
    setup:
    writeHelloWorld('com.example')
    buildFile << """
			plugins {
				id 'com.iadams.gradle-plugin-plugin'
			}
		"""

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withGradleVersion(gradleVersion)
        .withArguments('build')
        .withPluginClasspath(pluginClasspath)
        .build()

    then:
    result.task(':build').outcome == SUCCESS

    where:
    //using the new framework
    gradleVersion << ['2.8', '2.9']
  }

  @Unroll
  def "compatible with legacy gradle #gradleVersion"() {
    setup:
    def classpathString = pluginClasspath
        .collect { it.absolutePath.replace('\\', '\\\\') } // escape backslashes in Windows paths
        .collect { "'$it'" }
        .join(", ")

    writeHelloWorld('com.example')
    buildFile << """apply plugin: 'com.iadams.gradle-plugin-plugin'
        buildscript {
            dependencies {
                classpath files($classpathString)
            }
        }"""

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withGradleVersion(gradleVersion)
        .withArguments('build')
        .build()

    then:
    result.task(':build').outcome == SUCCESS

    where:
    //testing the older versions
    gradleVersion << ['2.5', '2.6', '2.7']
  }
}
