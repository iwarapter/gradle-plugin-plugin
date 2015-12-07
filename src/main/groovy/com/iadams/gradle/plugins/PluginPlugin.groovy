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

import com.gradle.publish.PublishPlugin
import com.iadams.gradle.plugins.tasks.SetupPluginTask
import nl.javadude.gradle.plugins.license.LicensePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.tasks.testing.Test
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.sonarqube.gradle.SonarQubePlugin

/**
 * @author iwarapter
 */
public class PluginPlugin implements Plugin<Project> {

  static final SETUP_PLUGIN_TASK = 'setupPlugin'

  /**
   * Applies BasePlugin to the project and add the tasks and extensions.
   * @param project - Gradle Project Object
   */
  @Override
  void apply(Project project) {
    project.plugins.apply(GroovyPlugin.class)
    project.plugins.apply(JacocoPlugin.class)
    project.plugins.apply(JavaGradlePluginPlugin.class)
    project.plugins.apply(PublishPlugin.class)
    project.plugins.apply(LicensePlugin.class)
    def licenseExt = project.extensions.findByName('license')
    licenseExt.includes(["**/*.java", "**/*.groovy"])

    project.dependencies.add('compile', project.dependencies.gradleApi()) // We are a plugin after all
    project.dependencies.add('compile', project.dependencies.localGroovy())
    project.dependencies.add('testCompile', project.dependencies.gradleTestKit())

    project.repositories.jcenter()
    project.repositories.maven { url "https://plugins.gradle.org/m2/" }
    project.buildscript.repositories.maven { url "https://plugins.gradle.org/m2/" }

    if (!project.group) {
      project.group = 'com.example'
    }

    setupSonarQube(project)
    setupTesting(project)
    addTasks(project)
  }

  void setupTesting(Project project) {
    project.sourceSets {
      integTest {
        groovy.srcDir project.file('src/integTest/groovy')
        resources.srcDir project.file('src/integTest/resources')
        compileClasspath = project.sourceSets.main.output + project.configurations.testRuntime
        runtimeClasspath = output + compileClasspath
      }
    }

    project.task('integTest', type: Test) {
      group = 'verification'
      description = "Runs the tests in the 'integTest' sourceset."
      testClassesDir = project.sourceSets.integTest.output.classesDir
      classpath = project.sourceSets.integTest.runtimeClasspath
      reports.html.destination = project.file("${project.buildDir}/reports/integ")
    }


    project.task('jacocoIntegTestReport', type: JacocoReport) {
      sourceSets project.sourceSets.main
      executionData project.integTest
      reports {
        xml.enabled true
        csv.enabled false
        html.destination "${project.buildDir}/reports/jacocoInteg"
      }
    }

    project.task('jacocoCombinedTestReport', type: JacocoReport) {
      sourceSets project.sourceSets.main
      executionData project.test, project.integTest
      reports {
        xml.enabled true
        csv.enabled false
        html.destination "${project.buildDir}/reports/jacocoCombined"
      }
    }

    project.tasks.withType(JacocoReport) {
      group = 'verification'
    }

    project.integTest.mustRunAfter project.test

    //Generate Jacoco Reports after each test task.
    project.test.finalizedBy project.jacocoTestReport
    project.integTest.finalizedBy project.jacocoIntegTestReport
    project.integTest.finalizedBy project.jacocoCombinedTestReport


    //TODO This is a temporary solution until a provide gradle alternative is available.
    project.task('createClasspathManifest') {
      def outputDir = project.file("${project.buildDir}/${project.name}")
      inputs.files project.sourceSets.main.runtimeClasspath
      outputs.dir outputDir
      doLast {
        outputDir.mkdirs()
        project.file("$outputDir/plugin-classpath.txt").text = project.sourceSets.main.runtimeClasspath.join("\n")
      }
    }
    project.dependencies.add('testRuntime',  project.files(project.tasks.findByName('createClasspathManifest')))
  }

  static void setupSonarQube(Project project) {
    project.plugins.apply(SonarQubePlugin.class)

    def extension = project.extensions.findByName('sonarqube')
    extension.properties.put('sonar.jacoco.reportPath', "${project.buildDir}/jacoco/test.exec")
    extension.properties.put('sonar.jacoco.itReportPath', "${project.buildDir}/jacoco/integTest.exec")
  }

  static void addTasks(Project project) {

    project.task(SETUP_PLUGIN_TASK, type: SetupPluginTask) {
      description = "Generates an example 'helloworld' plugin."
      group = 'Setup'
    }
  }
}
