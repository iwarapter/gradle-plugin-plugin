package com.iadams.gradle.plugins

import com.iadams.gradle.plugins.utils.TestKitBaseIntegSpec
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class GradleCompatabilityIntegSpec extends TestKitBaseIntegSpec {

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
}
