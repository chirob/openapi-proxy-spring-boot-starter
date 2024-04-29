[[features]]
= Features

* Java 17
* Spring Framework 6
* Spring Boot 3
* Annotation-based proxy generation
* Proxies can be generated from OpenAPI specifications
* Proxy interceptors can be defined in order to modify HTTP Request and Response (Add/Remove Headers, Update Body)

OpenAPI Proxy Spring Boot Starter is released under the non-restrictive Apache 2.0 license,
and follows a very standard Github development process, using Github
tracker for issues and merging pull requests into main. If you want
to contribute even something trivial please do not hesitate, but
follow the guidelines below.

image::https://circleci.com/gh/spring-cloud/spring-cloud-config/tree/master.svg?style=svg["CircleCI", link="https://circleci.com/gh/spring-cloud/spring-cloud-config/tree/master"]
image::https://codecov.io/gh/spring-cloud/spring-cloud-config/branch/master/graph/badge.svg["Codecov", link="https://codecov.io/gh/spring-cloud/spring-cloud-config/branch/master"]
image::https://api.codacy.com/project/badge/Grade/f064024a072c477e97dca6ed5a70fccd?branch=master["Codacy code quality", link="https://www.codacy.com/app/Spring-Cloud/spring-cloud-config?branch=master&utm_source=github.com&utm_medium=referral&utm_content=spring-cloud/spring-cloud-config&utm_campaign=Badge_Grade"]

OpenAPI Proxy Spring Boot Starter offers the following benefits:

* HTTP resource-based API for external configuration (name-value pairs or equivalent YAML content)
* Encrypt and decrypt property values (symmetric or asymmetric)
* Embeddable easily in a Spring Boot application using `@EnableConfigServer`

[[quick-start]]
= Quick Start

[[sample-application]]
== Sample Application

You can find a sample application https://github.com/spring-cloud/spring-cloud-config/tree/master/spring-cloud-config-sample[here].
It is a Spring Boot application, so you can run it by using the usual mechanisms (for instance, `mvn spring-boot:run`).
When it runs, it looks for the config server on `http://localhost:8888` (a configurable default), so you can run the server as well to see it all working together.

The sample has a test case where the config server is also started in the same JVM (with a different port), and the test asserts that an
environment property from the git configuration repo is present.
To change the location of the config server, you can set `spring.cloud.config.uri` in `bootstrap.yml` (or in system properties and other places).

The test case has a `main()` method that runs the server in the same way (watch the logs for its port), so you can run the whole system in one process and play with it (for example, you can run the `main()` method in your IDE).
The `main()` method uses `target/config` for the working directory of the git repository, so you can make local changes there and see them reflected in the running app. The following example shows a session of tinkering with the test case:

----
$ curl localhost:8080/env/sample
mytest
$ vi target/config/mytest.properties
.. change value of "sample", optionally commit
$ curl -X POST localhost:8080/refresh
["sample"]
$ curl localhost:8080/env/sample
sampleValue
----

The refresh endpoint reports that the "sample" property changed.

[[building]]
= Building

:jdkversion: 17

[[basic-compile-and-test]]
== Basic Compile and Test

To build the source you will need to install JDK {jdkversion}.

Spring Cloud uses Maven for most build-related activities, and you
should be able to get off the ground quite quickly by cloning the
project you are interested in and typing

----
$ ./mvnw install
----

NOTE: You can also install Maven (>=3.3.3) yourself and run the `mvn` command
in place of `./mvnw` in the examples below. If you do that you also
might need to add `-P spring` if your local Maven settings do not
contain repository declarations for spring pre-release artifacts.

NOTE: Be aware that you might need to increase the amount of memory
available to Maven by setting a `MAVEN_OPTS` environment variable with
a value like `-Xmx512m -XX:MaxPermSize=128m`. We try to cover this in
the `.mvn` configuration, so if you find you have to do it to make a
build succeed, please raise a ticket to get the settings added to
source control.

The projects that require middleware (i.e. Redis) for testing generally
require that a local instance of [Docker](https://www.docker.com/get-started) is installed and running.

[[documentation]]
== Documentation

The spring-cloud-build module has a "docs" profile, and if you switch
that on it will try to build asciidoc sources using https://docs.antora.org/antora/latest/[Antora] from
`modules/ROOT/`.

As part of that process it will look for a
`docs/src/main/asciidoc/README.adoc` and process it by loading all the includes, but not
parsing or rendering it, just copying it to `${main.basedir}`
(defaults to `$\{basedir}`, i.e. the root of the project). If there are
any changes in the README it will then show up after a Maven build as
a modified file in the correct place. Just commit it and push the change.

[[working-with-the-code]]
== Working with the code
If you don't have an IDE preference we would recommend that you use
https://www.springsource.com/developer/sts[Spring Tools Suite] or
https://eclipse.org[Eclipse] when working with the code. We use the
https://eclipse.org/m2e/[m2eclipse] eclipse plugin for maven support. Other IDEs and tools
should also work without issue as long as they use Maven 3.3.3 or better.

[[activate-the-spring-maven-profile]]
=== Activate the Spring Maven profile
Spring Cloud projects require the 'spring' Maven profile to be activated to resolve
the spring milestone and snapshot repositories. Use your preferred IDE to set this
profile to be active, or you may experience build errors.

[[importing-into-eclipse-with-m2eclipse]]
=== Importing into eclipse with m2eclipse
We recommend the https://eclipse.org/m2e/[m2eclipse] eclipse plugin when working with
eclipse. If you don't already have m2eclipse installed it is available from the "eclipse
marketplace".

NOTE: Older versions of m2e do not support Maven 3.3, so once the
projects are imported into Eclipse you will also need to tell
m2eclipse to use the right profile for the projects.  If you
see many different errors related to the POMs in the projects, check
that you have an up to date installation.  If you can't upgrade m2e,
add the "spring" profile to your `settings.xml`. Alternatively you can
copy the repository settings from the "spring" profile of the parent
pom into your `settings.xml`.

[[importing-into-eclipse-without-m2eclipse]]
=== Importing into eclipse without m2eclipse
If you prefer not to use m2eclipse you can generate eclipse project metadata using the
following command:

[indent=0]
----
    $ ./mvnw eclipse:eclipse
----

The generated eclipse projects can be imported by selecting `import existing projects`
from the `file` menu.


[[jce]]
== JCE

If you get an exception due to "Illegal key size" and you are using Sun’s JDK, you need to install the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files.
See the following links for more information:

https://www.oracle.com/technetwork/java/javase/downloads/jce-6-download-429243.html[Java 6 JCE]

https://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html[Java 7 JCE]

https://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html[Java 8 JCE]

Extract the JCE files into the `JDK/jre/lib/security` folder for whichever version of JRE/JDK x64/x86 you use.

[[contributing]]
= Contributing

:spring-cloud-build-branch: main

Spring Cloud is released under the non-restrictive Apache 2.0 license,
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
    │   └── checkstyle-suppressions.xml <3>
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
    │   └── checkstyle-suppressions.xml <3>
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

image::https://raw.githubusercontent.com/spring-cloud/spring-cloud-build/main/docs/modules/ROOT/assets/images/intellij-code-style.png[Code style]

Go to `File` -> `Settings` -> `Editor` -> `Code style`. There click on the icon next to the `Scheme` section. There, click on the `Import Scheme` value and pick the `Intellij IDEA code style XML` option. Import the `spring-cloud-build-tools/src/main/resources/intellij/Intellij_Spring_Boot_Java_Conventions.xml` file.

.Inspection profiles

image::https://raw.githubusercontent.com/spring-cloud/spring-cloud-build/main/docs/modules/ROOT/assets/images/intellij-inspections.png[Code style]

Go to `File` -> `Settings` -> `Editor` -> `Inspections`. There click on the icon next to the `Profile` section. There, click on the `Import Profile` and import the `spring-cloud-build-tools/src/main/resources/intellij/Intellij_Project_Defaults.xml` file.

.Checkstyle

To have Intellij work with Checkstyle, you have to install the `Checkstyle` plugin. It's advisable to also install the `Assertions2Assertj` to automatically convert the JUnit assertions

image::https://raw.githubusercontent.com/spring-cloud/spring-cloud-build/main/docs/modules/ROOT/assets/images/intellij-checkstyle.png[Checkstyle]

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

