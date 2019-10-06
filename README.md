# am [![Travis CI](https://travis-ci.org/marco-schmidt/am.svg?branch=master)](https://travis-ci.org/marco-schmidt/am) [ ![Download](https://api.bintray.com/packages/marco-schmidt/am/maven/images/download.svg) ](https://bintray.com/marco-schmidt/am/maven/) [ ![Download](https://maven-badges.herokuapp.com/maven-central/com.github.marco-schmidt/am/badge.svg) ](https://repo1.maven.org/maven2/com/github/marco-schmidt/am/) [![Javadocs](https://javadoc.io/badge/com.github.marco-schmidt/am.svg)](https://javadoc.io/doc/com.github.marco-schmidt/am)
asset manager

## Status
As of October 2019, this tool is in an early development stage, to be used only by the very curious.

## Purpose
* Command-line asset manager, managing files, checking their integrity, extracting metadata.
* Scan one or more directory trees (called volumes) for new, modified and deleted files.
* Extract metadata using command-line tool [exiftool](https://www.sno.phy.queensu.ca/~phil/exiftool/).
* Create SHA-256 hash values and determine changes.
* Optionally validate directory structure and file names against a ruleset.
* Query Wikidata to automatically retrieve semantic information. 
* Store results in an embedded [sqlite](https://www.sqlite.org/fileformat2.html) database. In addition to am itself data can thus be accessed using a more convenient database browser.
* Log runs to files in a log directory.

## Prerequisites
* Version 8 JDK installed and in path. Check: ``javac -version``
* Version control tool git installed and in path. Check: ``git --version``
* Configuration text file ``.am.properties`` in home directory (see section Configuration below).
* Optional but highly recommended: command-line tool [exiftool](https://www.sno.phy.queensu.ca/~phil/exiftool/) installed and  path to executable defined in configuration file. Check: ``exiftool -ver``

## Tools and Services
* Java provided by [AdoptOpenJDK](https://adoptopenjdk.net/)
* Integrated development environment [Eclipse](https://www.eclipse.org/)
* Version control system [git](https://git-scm.com)
* Hosted at [github](https://github.com/)
* Built with [gradle](https://gradle.org/)
  * Code analysis with the [checkstyle plugin](https://docs.gradle.org/current/userguide/checkstyle_plugin.html)
  * Code analysis with the [spotbugs plugin](https://spotbugs.readthedocs.io/en/stable/introduction.html)
  * Releases with the [release plugin](https://github.com/researchgate/gradle-release)
  * Code formatting with the [spotless plugin](https://github.com/diffplug/spotless)
  * Check against the National Vulnerability Database with the [OWASP dependency check plugin](https://github.com/jeremylong/dependency-check-gradle)
  * Report and check licenses with the [gradle license report plugin](https://github.com/jk1/Gradle-License-Report)
* Continuous integration at [Travis CI](https://travis-ci.org/marco-schmidt/am)
* Binaries hosted at [Bintray JCenter](https://jcenter.bintray.com/com/github/marco-schmidt/am/) and [Maven Central](https://repo.maven.apache.org/maven2/com/github/marco-schmidt/am/)
* API documentation generated by javadoc hosted at [javadoc.io](https://javadoc.io/doc/com.github.marco-schmidt/am/)

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

## Development

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
