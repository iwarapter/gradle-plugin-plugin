package com.iadams.gradle.plugins.utils

import org.apache.commons.io.FileUtils
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class TestKitBaseIntegSpec extends Specification {
  @Rule
  TemporaryFolder testProjectDir
  File buildFile
  File settingsFile

  List<File> pluginClasspath

  def setup() {
    buildFile = testProjectDir.newFile('build.gradle')

    def pluginClasspathResource = getClass().classLoader.findResource("plugin-classpath.txt")
    if (pluginClasspathResource == null) {
      throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
    }

    pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }

    settingsFile = testProjectDir.newFile('settings.gradle')
  }

  /**
   * Copy a given set of files/directories
   *
   * @param srcDir
   * @param destination
   */
  protected void copyResources(String srcDir, String destination) {
    ClassLoader classLoader = getClass().getClassLoader()
    URL resource = classLoader.getResource(srcDir)
    if (resource == null) {
      throw new TeskKitBaseIntegSpecException("Could not find classpath resource: $srcDir")
    }

    File destinationFile = file(destination)
    File resourceFile = new File(resource.toURI())
    if (resourceFile.file) {
      FileUtils.copyFile(resourceFile, destinationFile)
    } else {
      FileUtils.copyDirectory(resourceFile, destinationFile)
    }
  }

  protected void writeHelloWorld(String packageDotted, String baseDir = '') {
    def path = baseDir + '/src/main/java/' + packageDotted.replace('.', '/')
    directory(path)
    def javaFile = testProjectDir.newFile(path + '/HelloWorld.java')
    javaFile << """package ${packageDotted};
            public class HelloWorld {
                public static void main(String[] args) {
                    System.out.println("Hello Integration Test");
                }
            }
        """.stripIndent()
  }

  protected void writeGroovyHelloWorld(String packageDotted, String baseDir = '') {
    def path = baseDir + '/src/main/groovy/' + packageDotted.replace('.', '/')
    directory(path)
    def javaFile = testProjectDir.newFile(path + '/HelloWorld.groovy')
    javaFile << """package ${packageDotted}
            class HelloWorld {
                void main(String[] args) {
                    println 'Hello Integration Test'
                }
            }
        """.stripIndent()
  }

  protected File file(String path, File baseDir = testProjectDir.root) {
    def splitted = path.split('/')
    def directory = splitted.size() > 1 ? directory(splitted[0..-2].join('/'), baseDir) : baseDir
    def file = new File(directory, splitted[-1])
    file.createNewFile()
    file
  }

  protected File directory(String path, File baseDir = testProjectDir.root) {
    new File(baseDir, path).with {
      mkdirs()
      it
    }
  }

  protected File addSubproject(String subprojectName, String subBuildGradleText) {
    def subProjFolder = testProjectDir.newFolder(subprojectName)
    testProjectDir.newFile("$subprojectName/build.gradle") << "$subBuildGradleText\n"
    settingsFile << "include '$subprojectName'\n"
    subProjFolder
  }

  protected File addSubproject(String subprojectName) {
    def subProjFolder = testProjectDir.newFolder(subprojectName)
    settingsFile << "include '$subprojectName'\n"
    subProjFolder
  }
}

class TeskKitBaseIntegSpecException extends Exception {
  TeskKitBaseIntegSpecException(String message, Throwable cause) {
    super(message, cause)
  }

  TeskKitBaseIntegSpecException(String message) {
    super(message)
  }
}