package com.iadams.gradle.plugins

import nebula.test.PluginProjectSpec

/**
 * @author iwarapter
 */
class PluginPluginSpec extends PluginProjectSpec {

	static final String PLUGIN_ID = 'com.iadams.gradle-plugin-plugin'

	@Override
	String getPluginName() {
		return PLUGIN_ID
	}

	def setup() {
		project.apply plugin: pluginName
	}

	def "the plugin has an 'integTest' sourceSet"(){
		expect:
		project.tasks.findByName('integTest')
	}
}
