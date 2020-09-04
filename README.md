# am [![Travis CI](https://travis-ci.org/marco-schmidt/am.svg?branch=master)](https://travis-ci.org/marco-schmidt/am) [![Download](https://api.bintray.com/packages/marco-schmidt/am/maven/images/download.svg)](https://bintray.com/marco-schmidt/am/maven/) [![Download](https://maven-badges.herokuapp.com/maven-central/com.github.marco-schmidt/am/badge.svg)](https://repo1.maven.org/maven2/com/github/marco-schmidt/am/) [![Javadocs](https://javadoc.io/badge/com.github.marco-schmidt/am.svg)](https://javadoc.io/doc/com.github.marco-schmidt/am) [![Codecov](https://codecov.io/gh/marco-schmidt/am/branch/master/graphs/badge.svg?branch=master)](https://codecov.io/gh/marco-schmidt/am) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/164fe26f43f7402792bd043fe712d703)](https://www.codacy.com/manual/marco-schmidt/am?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=marco-schmidt/am&amp;utm_campaign=Badge_Grade) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![Java CI](https://github.com/marco-schmidt/am/workflows/Java%20CI/badge.svg)](https://github.com/marco-schmidt/am/actions?query=workflow%3A%22Java+CI%22) [![CodeQL](https://github.com/marco-schmidt/am/workflows/CodeQL/badge.svg)](https://github.com/marco-schmidt/am/actions?query=workflow%3ACodeQL) [![CircleCI](https://circleci.com/gh/marco-schmidt/am.svg?style=svg)](https://app.circleci.com/pipelines/github/marco-schmidt/am)

asset manager

## Status
As of September 2020, this tool is in an early development stage, to be used only by the very curious.

## Purpose
  * Command-line asset manager, managing files, checking their integrity, extracting metadata.

  * Scan one or more directory trees (called volumes) for new, modified and deleted files.

  * Extract metadata using command-line tool [exiftool](https://www.sno.phy.queensu.ca/~phil/exiftool/).

  * Create SHA-256 hash values and determine changes.

  * Optionally validate directory structure and file names against a ruleset.

  * Query [Wikidata](https://www.wikidata.org/) to automatically retrieve semantic information. 

  * Store results in an embedded [sqlite](https://www.sqlite.org/fileformat2.html) database. In addition to am itself data can thus be accessed using a more convenient database browser.

  * Log runs to files in a log directory.

## Prerequisites
* Version 8 JDK installed and in path. Check: ``javac -version``

* Version control tool git installed and in path. Check: ``git --version``

* Configuration text file ``.am.properties`` in home directory (see section Configuration below).

* Optional but highly recommended: command-line tool [exiftool](https://www.sno.phy.queensu.ca/~phil/exiftool/) installed and  path to executable defined in configuration file. Check: ``exiftool -ver``

## Tools
  * Java provided by [AdoptOpenJDK](https://adoptopenjdk.net/)

  * Integrated development environment [Eclipse](https://www.eclipse.org/)

  * Version control system [git](https://git-scm.com)

  * Built with [gradle](https://gradle.org/)

  * Code analysis with the [checkstyle plugin](https://docs.gradle.org/current/userguide/checkstyle_plugin.html)

  * Code analysis with the [spotbugs plugin](https://spotbugs.readthedocs.io/en/stable/introduction.html)

  * [Dependency verification](https://docs.gradle.org/6.2-rc-3/userguide/dependency_verification.html)

  * Releases with the [release plugin](https://github.com/researchgate/gradle-release)

  * Code formatting with the [spotless plugin](https://github.com/diffplug/spotless)

  * Coverage report with the [JaCoCo](https://www.eclemma.org/jacoco/) plugin

  * Avoid using questionable code with the [Forbidden APIs](https://plugins.gradle.org/plugin/de.thetaphi.forbiddenapis) plugin

  * Check against the National Vulnerability Database (NVD) with the [OWASP dependency check plugin](https://github.com/jeremylong/dependency-check-gradle)

  * Create report and check licenses against whitelist with the [gradle license report plugin](https://github.com/jk1/Gradle-License-Report)

  * Find dependency updates with the [gradle versions plugin](https://plugins.gradle.org/plugin/com.github.ben-manes.versions)

## Services
* Hosted at [GitHub](https://github.com/)
* Continuous integration at
    * [Travis CI](https://travis-ci.org/marco-schmidt/am)
    * [Circle CI](https://app.circleci.com/pipelines/github/marco-schmidt/am)
    * [GitHub Action Java CI](https://github.com/marco-schmidt/am/actions?query=workflow%3A%22Java+CI%22)
    * [JitCI](https://jitci.com/gh/marco-schmidt/am) [![](https://jitci.com/gh/marco-schmidt/am/svg)](https://jitci.com/gh/marco-schmidt/am)
* Dependency version update checks by
    * [GitHub Dependabot](https://github.com/marketplace/dependabot-preview) and
    * [Libraries.io](https://libraries.io/github/marco-schmidt/am) [![Libraries.io dependency status for latest release](https://img.shields.io/librariesio/release/github/marco-schmidt/am)](https://libraries.io/github/marco-schmidt/am)
* Binaries hosted at
    * [Bintray JCenter](https://jcenter.bintray.com/com/github/marco-schmidt/am/)
    * [Maven Central](https://repo.maven.apache.org/maven2/com/github/marco-schmidt/am/)
    * [GitHub Packages](https://github.com/marco-schmidt/am/packages/57499)
    + [Docker Hub](https://hub.docker.com/r/marcoschmidt/am)
* API documentation generated by javadoc hosted at [javadoc.io](https://javadoc.io/doc/com.github.marco-schmidt/am/)
* Code coverage report hosted at [codecov.io](https://codecov.io/gh/marco-schmidt/am)
* Code quality report hosted at [codacy.com](https://app.codacy.com/manual/marco-schmidt/am/dashboard)

## Usage
Clone am:
```
git clone https://github.com/marco-schmidt/am.git
```
If you already have a copy that may be out of date, get the most recent changes:
```
git pull
```

Now build and install the tool:
```
./gradlew install
```

Create directories for log files and database as well as a minimal configuration file:
```
> mkdir -p ~/am/logs
> mkdir ~/am/database
> cat >~/.am.properties <<EOF
logDir=/home/johndoe/am/logs
databaseDir=/home/johndoe/am/database
exiftoolPath=/usr/local/bin/exiftool
createHashes=1%
ignoreDirNames=@eaDir
ignoreFileNames=.DS_Store,Thumbs.db
wikidata=true
EOF
```

Go to the installation directory:
```
cd build/install/am/bin
```

Define a movie volume (leave out the last two arguments if your media files are not movies):
```
./am --add-volume /home/johndoe/movies --set-validator MovieValidator
```

Alternatively, define a television series volume:
```
./am --add-volume /home/johndoe/tv --set-validator TvSeriesValidator
```

The previous calls just added a single record about a new volume to the database. Now have am scan that new volume, run exiftool on files, create some hash values:
```
./am
```

## Configuration
Application configuration information is read from a text file in [.properties format](https://en.wikipedia.org/wiki/.properties) named ``.am.properties`` in the current user's home directory.

Note that backslashes must be escaped using a second backslash, e.g. to express Windows paths.

```.properties
# store collected information in an sqlite database in this directory
# directory must exist and be writable for user running am
databaseDir=/home/johndoe/am/database

# write a log file for each program run into this directory
# directory must exist and be writable for user running am
logDir=/home/johndoe/am/log

# absolute path to exiftool executable including file extension like .exe if applicable
exiftoolPath=/usr/local/bin/exiftool

# create hash values for files: always|never|percentage
#  always:     compute hash values for all files with each run
#  never:      do not compute hash values at all
#  percentage: during each program run compute hash values for this percentage of the overall amount of data
createHashes=0.5%

# ignore files with a name from this comma-separated list of names
ignoreFileNames=.DS_Store,Thumbs.db

# ignore directory tree below a directory with a name from this comma-separated list of names
ignoreDirNames=@eaDir

# query Wikidata to find entity id values in combination with the movie validator
wikidata=true
```

## Docker

This project contains a Dockerfile bundling Perl, exiftool, Java and am.

The Circle CI pipeline uploads a new image to [Docker Hub](https://hub.docker.com/r/marcoschmidt/am).

Some commands:
  * Login at Docker Hub: ``docker login -u USER -p PASSWORD``
  * Create new image: ``docker build -t marcoschmidt/am .``
  * Step into shell: ``docker run -it --entrypoint sh marcoschmidt/am``
  * Upload image to Docker Hub: ``docker push marcoschmidt/am``

Within container:
  * Print environment: ``am --print-env``
  * Create minimal configuration: ``printf "databaseDir=/home/am/db\nlogDir=/home/am/logs\n" > /home/am/config/.am.properties``
  * Create new volume with am: ``am --config /home/am/config/.am.properties --add-volume /opt/java``
  * Make am add volume's files: ``am --config /home/am/config/.am.properties check``

## Troubleshooting

### Character Encoding Issues

With Unix-based operating systems, such exceptions may occur when trying to open files with names containing non-ASCII characters:
```
ERROR	Error converting path "/mnt/ext1/P��re.jpg" for opening a file to compute a hash value.
java.nio.file.InvalidPathException: Malformed input or input contains unmappable characters: /mnt/ext1/P��re.jpg
	at sun.nio.fs.UnixPath.encode(UnixPath.java:147)
	at sun.nio.fs.UnixPath.<init>(UnixPath.java:71)
	at sun.nio.fs.UnixFileSystem.getPath(UnixFileSystem.java:281)
	at java.io.File.toPath(File.java:2234)
	at am.processor.hashes.HashCreation.update(HashCreation.java:64)
	at am.processor.hashes.HashCreation.update(HashCreation.java:49)
	at am.processor.hashes.HashProcessor.compute(HashProcessor.java:102)
	at am.processor.hashes.HashProcessor.update(HashProcessor.java:57)
	at am.app.App.processVolumes(App.java:129)
	at am.app.App.process(App.java:193)
	at am.app.App.main(App.java:212)
 ```

These environment variable settings may solve that issue:
```
export LC_ALL="en_US.UTF-8"
export LANG="en_US.UTF-8"
```

## Development

### Upgrade Gradle Wrapper

  * Figure out a valid checksum for the new version by looking up its SHA-256 value for *Binary-only (-bin) ZIP Checksum* at the [Gradle distribution and wrapper JAR checksum reference](https://gradle.org/release-checksums/)

  * Run the wrapper task with both version and the checksum value:

    ``./gradlew wrapper --gradle-version 6.4 --gradle-distribution-sha256-sum b888659f637887e759749f6226ddfcb1cb04f828c58c41279de73c463fdbacc9``

### Checking for New Versions of Dependencies

In order to figure out if there are updates

* check the [pull request tab of the am project](https://github.com/marco-schmidt/am/pulls), it may contain Dependabot pull requests about dependency upgrades, or
* run ``./gradlew depUpd`` to use the gradle versions plugin checking for the same thing.

### Upgrading Dependency Versions

Once you've modified the version of a dependency library or plug-in by editing version numbers in build.gradle, run
``./gradlew build --write-verification-metadata sha256`` to both check if the build still works and upgrade verification-metadata.xml. 

### Setup of Integrated Development Environment Eclipse

Make sure a version 8 JDK is in the path and [Eclipse](https://www.eclipse.org/downloads/) is installed on the system.

Open a shell and change to the directory where you checked out am (see Usage above).

Make gradle create an Eclipse workspace:
```
./gradlew eclipse
```

Open Eclipse and choose ``File | Import...`` from the menu. The ``Import``dialog should pop up with heading ``Select``.

Choose ``General | Existing Projects into Workspace`` and press button ``Next >``. The dialog should switch  the heading to ``Import``.

Make sure that the radio button ``Select root directory:`` is selected. Either type in the directory where you just generated Eclipse workspace files, or pick it with the dialog behind the ``Browse`` button.

Press the tab key. Under ``Projects`` am should now show up. Check the box next to it.

Press button ``Next``.

TODO

### Release a New Version

* Make sure that the working copy of master is clean (no unversioned, modified or deleted files).
* Try building that version: ``./gradlew clean build``
* Have properties bintrayUser and bintrayApiKey defined in ``~/.gradle/gradle.properties`` (when logged in at bintray, the API key can be found under ``Edit Profile`` / ``API Key``).
* Run ``./gradlew release`` It will prompt twice to confirm the release version (current version minus ``-SNAPSHOT``) and the next snapshot version (typically the right-most version number part increased by one, plus ``-SNAPSHOT``).
* Log into bintray, go the page of the new version. There should be a link to Maven Central. Follow that link. (As of October 2019, make sure that the Old Look is enabled as shown in the green menu bar at the top, otherwise that link is missing.)
* Enter your Sonatype OSS credentials if you have not provided them in the profile's ``Accounts`` section. Press the Sync button.
