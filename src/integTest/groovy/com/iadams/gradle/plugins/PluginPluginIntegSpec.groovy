package com.iadams.gradle.plugins

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult

/**
 * @author iwarapter
 */
class PluginPluginIntegSpec extends IntegrationSpec {

	def 'setup new build and check tasks are available'() {
		setup:
		buildFile << '''
                    apply plugin: 'com.iadams.gradle-plugin-plugin'
                '''.stripIndent()

		when:
		ExecutionResult result = runTasksSuccessfully('tasks')

		then:
		result.standardOutput.contains('integTest')
		result.standardOutput.contains('jacocoTestReport')
		result.standardOutput.contains('jacocoIntegTestReport')
	}
}
