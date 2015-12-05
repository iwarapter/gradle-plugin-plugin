package com.iadams.gradle.plugins

import com.iadams.gradle.plugins.utils.TestKitBaseIntegSpec
import org.gradle.testkit.runner.GradleRunner

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class PluginPluginIntegSpec extends TestKitBaseIntegSpec {

  def setup() {
    buildFile << """
			plugins {
				id 'com.iadams.gradle-plugin-plugin'
			}
		"""
  }

  def "the sonarqube task can be run"() {
    when:
    writeHelloWorld('com.example')
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments('sonarqube')
        .withPluginClasspath(pluginClasspath)
        .buildAndFail()

    then:
    result.task(':sonarqube').outcome == FAILED
    result.output.contains("ERROR: Sonar server 'http://localhost:9000' can not be reached")
  }

  def "we can setup a quick-start plugin"(){
    when:
    settingsFile << "rootProject.name = 'MyPlugin'"
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments('setupPlugin')
        .withPluginClasspath(pluginClasspath)
        .build()

    then:
    result.task(':setupPlugin').outcome == SUCCESS

    when: 'we can then build the simple plugin'
    result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments('build')
        .withPluginClasspath(pluginClasspath)
        .build()

    then:
    result.task(':build').outcome == SUCCESS
    println result.output
  }
}
