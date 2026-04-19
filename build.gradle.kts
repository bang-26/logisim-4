/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

import java.text.SimpleDateFormat
import java.util.Date

plugins {
  checkstyle
  id("com.github.ben-manes.versions") version "0.53.0"
  java
  application
  id("com.gradleup.shadow") version "9.4.1"
  id("org.sonarqube") version "7.2.3.7755"
}

repositories {
  mavenCentral()
}

application {
  mainClass.set("com.cburch.logisim.Main")
}

dependencies {
  implementation("org.hamcrest:hamcrest:3.0")
  implementation("javax.help:javahelp:2.0.05")
  implementation("com.fifesoft:rsyntaxtextarea:3.6.2")
  implementation("net.sf.nimrod:nimrod-laf:1.2")
  implementation("org.drjekyll:colorpicker:2.0.1")
  implementation("at.swimmesberger:swingx-core:1.6.8")
  implementation("org.scijava:swing-checkbox-tree:1.0.2")
  implementation("org.slf4j:slf4j-api:2.0.17")
  implementation("org.slf4j:slf4j-simple:2.0.17")
  implementation("com.formdev:flatlaf:3.7.1")
  implementation("commons-cli:commons-cli:1.11.0")
  implementation("com.vladsch.flexmark:flexmark-all:0.64.8")
  implementation("org.apache.commons:commons-text:1.15.0")

  // NOTE: Be aware of reported issues with Eclipse and Batik
  // See: https://github.com/logisim-evolution/logisim-evolution/issues/709
  // implementation("org.apache.xmlgraphics:batik-swing:1.14")

  testImplementation(platform("org.junit:junit-bom:6.0.3"))
  testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")
  testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

/**
 * Strings used as keys to reference shared variables (via `ext.*`)
 */
val APP_DIR_NAME = "appDirName"
val APP_VERSION = "appVersion"
val APP_VERSION_SHORT = "appVersionShort"
val APP_URL = "appUrl"
val BUILD_DIR = "buildDir"
val JDEPS = "jdeps"
val JDEPS_FILE = "jdepsFile"
val JPACKAGE = "jpackage"
val LIBS_DIR = "libsDir"
val LINUX_PARAMS = "linuxParameters"
val OS_ARCH = "osArch"
val PACKAGE_INPUT_DIR = "packageInputDir"
val SHADOW_JAR_FILE_NAME = "shadowJarFilename"
val SHARED_PARAMS = "sharedParameters"
val SUPPORT_DIR = "supportDir"
val TARGET_DIR = "targetDir"
val TARGET_FILE_PATH_BASE = "targetFilePathBase"
val TARGET_FILE_PATH_BASE_SHORT = "targetFilePathBaseShort"
val UPPERCASE_PROJECT_NAME = "uppercaseProjectName"

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
  
  sourceSets["main"].java {
    val buildDir = layout.buildDirectory.get().asFile
    srcDir("${buildDir}/generated/logisim/java")
    srcDir("${buildDir}/generated/sources/srcgen")
  }
}

/**
 * Setting up all shared vars and parameters.
 */
extra.apply {
  // NOTE: optional suffix is prefixed with `-` (because of how LogisimVersion class parses it), which
  // I remove here because `jpackage` tool does not like it when used to build the RPM package.
  // Do NOT use `project.version` instead.
  val appVersion = (project.version as String).replace("-", "")
  set(APP_VERSION, appVersion)
  logger.info("appVersion: ${appVersion}")

  val appUrl = findProperty("url")
  set(APP_URL, appUrl)
  logger.info("appUrl: ${appUrl}")

  // Short (with suffix removed) version string, i.e. for "3.6.0beta1", short form is "3.6.0".
  // This is used by createApp and createMsi as version numbering is pretty strict on macOS and Windows.
  // Do NOT use `project.version` instead.
  val appVersionShort = (project.version as String).split('-')[0]
  set(APP_VERSION_SHORT, appVersionShort)
  logger.info("appVersionShort: ${appVersionShort}")

  // Architecture used for build
  val osArch = providers.systemProperty("os.arch").get()
  set(OS_ARCH, osArch)

  // Build Directory
  val buildDir = layout.buildDirectory.get().asFile.toString()
  set(BUILD_DIR, buildDir)

  // Destination folder where packages are stored.
  val targetDir="${buildDir}/dist"
  set(TARGET_DIR, targetDir)

  // JAR folder.
  val libsDir="${buildDir}/libs"
  set(LIBS_DIR, libsDir)

  // PackageInput folder that hold the shadowJar
  val packageInputDir="${buildDir}/packageInput"
  set(PACKAGE_INPUT_DIR, packageInputDir)

  // The root dir for jpackage extra files.
  val supportDir="${projectDir}/support/jpackage"
  set(SUPPORT_DIR, supportDir)

  // Project name with uppercase first letter
  val uppercaseProjectName = project.name.replaceFirstChar { it.uppercase() }.trim()
  set(UPPERCASE_PROJECT_NAME, uppercaseProjectName)

  // Base name of produced artifacts. Suffixes will be added later by relevant tasks.
  val baseFilename = "${project.name}-${appVersion}"
  set(TARGET_FILE_PATH_BASE, "${targetDir}/${baseFilename}")
  logger.debug("targetFilePathBase: \"${targetDir}/${baseFilename}\"")

  val baseFilenameShort = "${project.name}-${appVersionShort}"
  set(TARGET_FILE_PATH_BASE_SHORT, "${targetDir}/${baseFilenameShort}")
  logger.debug("targetFilePathBaseShort: \"${targetDir}/${baseFilenameShort}\"")

  // Name of application shadowJar file.
  val shadowJarFilename = "${baseFilename}-all.jar"
  set(SHADOW_JAR_FILE_NAME, shadowJarFilename)
  logger.debug("shadowJarFilename: \"${shadowJarFilename}\"")

  // JDK/jpackage vars
  val javaHome = providers.systemProperty("java.home").get()
  val jpackage = "${javaHome}/bin/jpackage"
  set(JPACKAGE, jpackage)
  val jdeps = "${javaHome}/bin/jdeps"
  set(JDEPS, jdeps)
  val jdepsFile = "${buildDir}/neededJavaModules.txt"
  set(JDEPS_FILE, jdepsFile)

  // Copyrights note.
  val copyrights = "Copyright ©2001–${SimpleDateFormat("yyyy").format(Date())} ${project.name} developers"

  // Platform-agnostic jpackage parameters shared across all the builds.
  var params = listOf(
      jpackage,
      // NOTE: we cannot use --app-version as part of platform agnostic set as i.e. both macOS and
      // Windows packages do not allow use of any suffixes like "-dev" etc, so --app-version is set
      // in these builders separately.
      "--input", packageInputDir,
      "--main-class", "com.cburch.logisim.Main",
      "--main-jar", shadowJarFilename,
      "--copyright", copyrights,
      "--description", "Digital logic design tool and simulator",
      "--vendor", "${project.name} developers",
  )
  if (logger.isDebugEnabled()) {
    params += listOf("--verbose")
  }
  set(SHARED_PARAMS, params)

  // Linux (DEB/RPM) specific settings for jpackage.
  val linuxParams = params + listOf(
      "--name", project.name,
      "--dest", targetDir,
      "--app-version", appVersion,
      "--install-dir", "/opt",
      "--linux-shortcut"
  )
  set(LINUX_PARAMS, linuxParams)

  // All the macOS specific stuff.
  set(APP_DIR_NAME, "${buildDir}/macOS-${osArch}/${uppercaseProjectName}.app")
}

tasks.withType<Checkstyle> {
  reports {
    xml.required.set(true)
    html.required.set(true)
  }
}

tasks.test {
  useJUnitPlatform()
  testLogging {
    events("passed", "skipped", "failed")
  }
}

tasks.shadowJar {
  archiveFileName.set("${project.name}-${(project.version as String).replace("-", "")}-all.jar")
  mergeServiceFiles()
}
