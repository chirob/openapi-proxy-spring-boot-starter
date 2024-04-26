[[features]]
= Features

* Java 17
* Spring Framework 6
* Spring Boot 3
* Annotation-based proxy generation
* Proxies can be generated from OpenAPI specifications
* Proxy interceptors can be defined in order to modify HTTP Request and Response (Add/Remove Headers, Update Body)

[[building]]
= Building

:spring-cloud-build-branch: main

Spring OpenAPI Proxy is released under the non-restrictive Apache 2.0 license,
and follows a very standard Github development process, using Github
tracker for issues and merging pull requests into main. If you want
to contribute even something trivial please do not hesitate, but
follow the guidelines below.

[[sign-the-contributor-license-agreement]]
== Sign the Contributor License Agreement

Before we accept a non-trivial patch or pull request we will need you to sign the
https://cla.pivotal.io/sign/spring[Contributor License Agreement].
Signing the contributor's agreement does not grant anyone commit rights to the main
repository, but it does mean that we can accept your contributions, and you will get an
author credit if we do.  Active contributors might be asked to join the core team, and
given the ability to merge pull requests.

[[code-of-conduct]]
== Code of Conduct
This project adheres to the Contributor Covenant https://github.com/spring-cloud/spring-cloud-build/blob/main/docs/src/main/asciidoc/code-of-conduct.adoc[code of
conduct]. By participating, you  are expected to uphold this code. Please report
unacceptable behavior to spring-code-of-conduct@pivotal.io.

[[code-conventions-and-housekeeping]]
== Code Conventions and Housekeeping
None of these is essential for a pull request, but they will all help.  They can also be
added after the original pull request but before a merge.

* Use the Spring Framework code format conventions. If you use Eclipse
  you can import formatter settings using the
  `eclipse-code-formatter.xml` file from the
  https://raw.githubusercontent.com/spring-cloud/spring-cloud-build/main/spring-cloud-dependencies-parent/eclipse-code-formatter.xml[Spring
  Cloud Build] project. If using IntelliJ, you can use the
  https://plugins.jetbrains.com/plugin/6546[Eclipse Code Formatter
  Plugin] to import the same file.
* Make sure all new `.java` files to have a simple Javadoc class comment with at least an
  `@author` tag identifying you, and preferably at least a paragraph on what the class is
  for.
* Add the ASF license header comment to all new `.java` files (copy from existing files
  in the project)
* Add yourself as an `@author` to the .java files that you modify substantially (more
  than cosmetic changes).
* Add some Javadocs and, if you change the namespace, some XSD doc elements.
* A few unit tests would help a lot as well -- someone has to do it.
* If no-one else is using your branch, please rebase it against the current main (or
  other target branch in the main project).
* When writing a commit message please follow https://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html[these conventions],
  if you are fixing an existing issue please add `Fixes gh-XXXX` at the end of the commit
  message (where XXXX is the issue number).

[[checkstyle]]
== Checkstyle

Spring Cloud Build comes with a set of checkstyle rules. You can find them in the `spring-cloud-build-tools` module. The most notable files under the module are:

.spring-cloud-build-tools/
----
└── src
    ├── checkstyle
    │   └── checkstyle-suppressions.xml <3>
    └── main
        └── resources
            ├── checkstyle-header.txt <2>
            └── checkstyle.xml <1>
----
<1> Default Checkstyle rules
<2> File header setup
<3> Default suppression rules

[[checkstyle-configuration]]
=== Checkstyle configuration

Checkstyle rules are *disabled by default*. To add checkstyle to your project just define the following properties and plugins.

.pom.xml
----
<properties>
<maven-checkstyle-plugin.failsOnError>true</maven-checkstyle-plugin.failsOnError> <1>
        <maven-checkstyle-plugin.failsOnViolation>true
        </maven-checkstyle-plugin.failsOnViolation> <2>
        <maven-checkstyle-plugin.includeTestSourceDirectory>true
        </maven-checkstyle-plugin.includeTestSourceDirectory> <3>
</properties>

<build>
        <plugins>
            <plugin> <4>
                <groupId>io.spring.javaformat</groupId>
                <artifactId>spring-javaformat-maven-plugin</artifactId>
            </plugin>
            <plugin> <5>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-checkstyle-plugin</artifactId>
            </plugin>
        </plugins>

    <reporting>
        <plugins>
            <plugin> <5>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-checkstyle-plugin</artifactId>
            </plugin>
        </plugins>
    </reporting>
</build>
----
<1> Fails the build upon Checkstyle errors
<2> Fails the build upon Checkstyle violations
<3> Checkstyle analyzes also the test sources
<4> Add the Spring Java Format plugin that will reformat your code to pass most of the Checkstyle formatting rules
<5> Add checkstyle plugin to your build and reporting phases

If you need to suppress some rules (e.g. line length needs to be longer), then it's enough for you to define a file under `${project.root}/src/checkstyle/checkstyle-suppressions.xml` with your suppressions. Example:

.projectRoot/src/checkstyle/checkstyle-suppresions.xml
----
<?xml version="1.0"?>
<!DOCTYPE suppressions PUBLIC
		"-//Puppy Crawl//DTD Suppressions 1.1//EN"
		"https://www.puppycrawl.com/dtds/suppressions_1_1.dtd">
<suppressions>
	<suppress files=".*ConfigServerApplication\.java" checks="HideUtilityClassConstructor"/>
	<suppress files=".*ConfigClientWatch\.java" checks="LineLengthCheck"/>
</suppressions>
----

It's advisable to copy the `${spring-cloud-build.rootFolder}/.editorconfig` and `${spring-cloud-build.rootFolder}/.springformat` to your project. That way, some default formatting rules will be applied. You can do so by running this script:

```bash
$ curl https://raw.githubusercontent.com/spring-cloud/spring-cloud-build/main/.editorconfig -o .editorconfig
$ touch .springformat
```

[[ide-setup]]
== IDE setup

[[intellij-idea]]
=== Intellij IDEA

In order to setup Intellij you should import our coding conventions, inspection profiles and set up the checkstyle plugin.
The following files can be found in the https://github.com/spring-cloud/spring-cloud-build/tree/main/spring-cloud-build-tools[Spring Cloud Build] project.

.spring-cloud-build-tools/
----
└── src
    ├── checkstyle
    │   └── checkstyle-suppressions.xml <3>
    └── main
        └── resources
            ├── checkstyle-header.txt <2>
            ├── checkstyle.xml <1>
            └── intellij
                ├── Intellij_Project_Defaults.xml <4>
                └── Intellij_Spring_Boot_Java_Conventions.xml <5>
----
<1> Default Checkstyle rules
<2> File header setup
<3> Default suppression rules
<4> Project defaults for Intellij that apply most of Checkstyle rules
<5> Project style conventions for Intellij that apply most of Checkstyle rules

.Code style

image::intellij-code-style.png[Code style]

Go to `File` -> `Settings` -> `Editor` -> `Code style`. There click on the icon next to the `Scheme` section. There, click on the `Import Scheme` value and pick the `Intellij IDEA code style XML` option. Import the `spring-cloud-build-tools/src/main/resources/intellij/Intellij_Spring_Boot_Java_Conventions.xml` file.

.Inspection profiles

image::intellij-inspections.png[Code style]

Go to `File` -> `Settings` -> `Editor` -> `Inspections`. There click on the icon next to the `Profile` section. There, click on the `Import Profile` and import the `spring-cloud-build-tools/src/main/resources/intellij/Intellij_Project_Defaults.xml` file.

.Checkstyle

To have Intellij work with Checkstyle, you have to install the `Checkstyle` plugin. It's advisable to also install the `Assertions2Assertj` to automatically convert the JUnit assertions

image::intellij-checkstyle.png[Checkstyle]

Go to `File` -> `Settings` -> `Other settings` -> `Checkstyle`. There click on the `+` icon in the `Configuration file` section. There, you'll have to define where the checkstyle rules should be picked from. In the image above, we've picked the rules from the cloned Spring Cloud Build repository. However, you can point to the Spring Cloud Build's GitHub repository (e.g. for the `checkstyle.xml` : `https://raw.githubusercontent.com/spring-cloud/spring-cloud-build/main/spring-cloud-build-tools/src/main/resources/checkstyle.xml`). We need to provide the following variables:

- `checkstyle.header.file` - please point it to the Spring Cloud Build's, `spring-cloud-build-tools/src/main/resources/checkstyle-header.txt` file either in your cloned repo or via the `https://raw.githubusercontent.com/spring-cloud/spring-cloud-build/main/spring-cloud-build-tools/src/main/resources/checkstyle-header.txt` URL.
- `checkstyle.suppressions.file` - default suppressions. Please point it to the Spring Cloud Build's, `spring-cloud-build-tools/src/checkstyle/checkstyle-suppressions.xml` file either in your cloned repo or via the `https://raw.githubusercontent.com/spring-cloud/spring-cloud-build/main/spring-cloud-build-tools/src/checkstyle/checkstyle-suppressions.xml` URL.
- `checkstyle.additional.suppressions.file` - this variable corresponds to suppressions in your local project. E.g. you're working on `spring-cloud-contract`. Then point to the `project-root/src/checkstyle/checkstyle-suppressions.xml` folder. Example for `spring-cloud-contract` would be: `/home/username/spring-cloud-contract/src/checkstyle/checkstyle-suppressions.xml`.

IMPORTANT: Remember to set the `Scan Scope` to `All sources` since we apply checkstyle rules for production and test sources.

[[duplicate-finder]]
== Duplicate Finder

Spring Cloud Build brings along the  `basepom:duplicate-finder-maven-plugin`, that enables flagging duplicate and conflicting classes and resources on the java classpath.

[[duplicate-finder-configuration]]
=== Duplicate Finder configuration

Duplicate finder is *enabled by default* and will run in the `verify` phase of your Maven build, but it will only take effect in your project if you add the `duplicate-finder-maven-plugin` to the `build` section of the projecst's `pom.xml`.

.pom.xml
[source,xml]
----
<build>
    <plugins>
        <plugin>
            <groupId>org.basepom.maven</groupId>
            <artifactId>duplicate-finder-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
----

For other properties, we have set defaults as listed in the https://github.com/basepom/duplicate-finder-maven-plugin/wiki[plugin documentation].

You can easily override them but setting the value of the selected property prefixed with `duplicate-finder-maven-plugin`. For example, set `duplicate-finder-maven-plugin.skip` to `true` in order to skip duplicates check in your build.

If you need to add `ignoredClassPatterns` or `ignoredResourcePatterns` to your setup, make sure to add them in the plugin configuration section of your project:

[source,xml]
----
<build>
    <plugins>
        <plugin>
            <groupId>org.basepom.maven</groupId>
            <artifactId>duplicate-finder-maven-plugin</artifactId>
            <configuration>
                <ignoredClassPatterns>
                    <ignoredClassPattern>org.joda.time.base.BaseDateTime</ignoredClassPattern>
                    <ignoredClassPattern>.*module-info</ignoredClassPattern>
                </ignoredClassPatterns>
                <ignoredResourcePatterns>
                    <ignoredResourcePattern>changelog.txt</ignoredResourcePattern>
                </ignoredResourcePatterns>
            </configuration>
        </plugin>
    </plugins>
</build>


----


[[contributing]]
= Contributing

Unresolved directive in <stdin> - include::https://raw.githubusercontent.com/spring-cloud/spring-cloud-build/main/docs/src/main/asciidoc/contributing.adoc[]
