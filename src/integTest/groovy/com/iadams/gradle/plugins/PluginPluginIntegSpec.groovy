/*
 * Gradle Plugin Plugin
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Iain Adams
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.iadams.gradle.plugins

import com.iadams.gradle.plugins.utils.TestKitBaseIntegSpec
import org.gradle.testkit.runner.GradleRunner

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class PluginPluginIntegSpec extends TestKitBaseIntegSpec {

  def setup() {
    buildFile << '''
			plugins {
				id 'com.iadams.gradle-plugin-plugin'
			}
		'''.stripIndent()
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
        .withArguments('licenseFormat','build')
        .withPluginClasspath(pluginClasspath)
        .build()

    then:
    result.task(':licenseFormatMain').outcome == SUCCESS
    result.task(':build').outcome == SUCCESS
  }

  def "the setup plugin task uses group if set"(){
    when:
    buildFile << "group = 'org.other'"
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments('setupPlugin')
        .withPluginClasspath(pluginClasspath)
        .build()

    then:
    result.task(':setupPlugin').outcome == SUCCESS
    file('src/main/groovy/org/other/MyPlugin.groovy').exists()
  }

  def "we can run an integration test"(){
    when:
    settingsFile << "rootProject.name = 'MyPlugin'"
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments('setupPlugin', 'licenseFormat', 'build')
        .withPluginClasspath(pluginClasspath)
        .build()

    then:
    result.task(':setupPlugin').outcome == SUCCESS

    when:
    buildFile << '''dependencies {
  testCompile 'org.spockframework:spock-core:1.0-groovy-2.4', {
    exclude module: 'groovy-all'
  }
}'''
    file('src/integTest/groovy/com/example/ExampleIntegSpec.groovy') << ''' package com.example
import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class BuildLogicFunctionalTest extends Specification {
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "hello world task prints hello world"() {
        given:
        buildFile << """
            task helloWorld {
                doLast {
                    println 'Hello world!'
                }
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('helloWorld')
            .build()

        then:
        result.output.contains('Hello world!')
        result.task(":helloWorld").outcome == SUCCESS
    }
}'''
    result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments('integTest')
        .withPluginClasspath(pluginClasspath)
        .build()

    then:
    file('build/test-results/TEST-com.example.ExampleIntegSpec.xml').exists()
    result.task(':integTest').outcome == SUCCESS
  }
}
