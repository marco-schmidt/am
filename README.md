# am [![Travis CI](https://travis-ci.org/marco-schmidt/am.svg?branch=master)](https://travis-ci.org/marco-schmidt/am)
asset manager

## Status
As of July 2019, this tool is in an early development stage, to be used only by the very curious.

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
> cat >~/.am.properties <<EOL
logDir=/home/johndoe/am/logs
databaseDir=/home/johndoe/am/database
exiftoolPath=/usr/local/bin/exiftool
createHashes=1%
ignoreDirNames=@eaDir
ignoreFileNames=.DS_Store,Thumbs.db
EOL
```

Go to the installation directory:
```
cd build/install/am/bin
```

Define a movie volume (leave out the last two arguments if your media files are not movies):
```
./am --add-volume /home/johndoe/movies --set-validator MovieValidator
```

Have am scan the new volume, run exiftool on files, create some hash values:
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
```
