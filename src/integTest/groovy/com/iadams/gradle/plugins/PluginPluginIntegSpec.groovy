package com.iadams.gradle.plugins

import com.iadams.gradle.plugins.utils.TestKitBaseIntegSpec
import org.gradle.testkit.runner.GradleRunner

import static org.gradle.testkit.runner.TaskOutcome.FAILED

class PluginPluginIntegSpec extends TestKitBaseIntegSpec {

  def setup() {
    writeHelloWorld('com.example')
    buildFile << """
			plugins {
				id 'com.iadams.gradle-plugin-plugin'
			}
		"""
  }

  def "the sonarqube task can be run"() {
    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments('sonarqube')
        .withPluginClasspath(pluginClasspath)
        .buildAndFail()

    then:
    result.task(':sonarqube').outcome == FAILED
    result.output.contains("ERROR: Sonar server 'http://localhost:9000' can not be reached")
  }
}
