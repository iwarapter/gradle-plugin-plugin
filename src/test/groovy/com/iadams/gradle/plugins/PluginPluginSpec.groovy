package com.iadams.gradle.plugins

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class PluginPluginSpec extends spock.lang.Specification {

  static final String PLUGIN_ID = 'com.iadams.gradle-plugin-plugin'
  Project project

  def setup() {
    project = ProjectBuilder.builder().build()
    project.pluginManager.apply PLUGIN_ID
  }

  def "the plugin has an 'integTest' sourceSet"() {
    expect:
    project.tasks.findByName('integTest')
  }
}
