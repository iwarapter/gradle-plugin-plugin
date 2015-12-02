package com.iadams.gradle.plugins;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.sonarqube.gradle.SonarQubePlugin

/**
 * @author iwarapter
 */
public class PluginPlugin implements Plugin<Project>  {

	/**
	 * Applies BasePlugin to the project and add the tasks and extensions.
	 * @param project
	 * 	Gradle Project Object
	 */
	@Override
	void apply(Project project) {
		project.plugins.apply(GroovyPlugin.class)
		project.plugins.apply(JacocoPlugin.class)

		setupSonarQube(project)

		project.dependencies.add('compile', project.dependencies.gradleApi()) // We are a plugin after all
		project.dependencies.add('compile', project.dependencies.localGroovy())
		project.repositories.maven { url "https://plugins.gradle.org/m2/" }
		project.buildscript.repositories.maven { url "https://plugins.gradle.org/m2/" }

		if (!project.group) {
			project.group = 'com.iadams.plugins'
		}

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

		project.tasks.withType(JacocoReport){
			group = 'verification'
		}

		project.integTest.mustRunAfter project.test

		//Generate Jacoco Reports after each test task.
		project.test.finalizedBy project.jacocoTestReport
		project.integTest.finalizedBy project.jacocoIntegTestReport
		project.integTest.finalizedBy project.jacocoCombinedTestReport
	}

	void setupSonarQube(Project project){
		project.plugins.apply(SonarQubePlugin.class)

		def extension = project.extensions.findByName('sonarqube')
		extension.properties.put('sonar.jacoco.reportPath', "${project.buildDir}/jacoco/test.exec")
		extension.properties.put('sonar.jacoco.itReportPath', "${project.buildDir}/jacoco/integTest.exec")
	}
}
