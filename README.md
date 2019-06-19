# am [![Travis CI](https://travis-ci.org/marco-schmidt/am.svg?branch=master)](https://travis-ci.org/marco-schmidt/am)
asset manager

## Status
As of June 2019, this tool is in an early development stage, to be used only by the very curious.

## Purpose
* Command-line asset manager, managing files, checking their integrity, extracting metadata. 
* Scan one or more directory trees (called volumes) for new, modified and deleted files.
* Extract metadata using command-line tool [exiftool](https://www.sno.phy.queensu.ca/~phil/exiftool/).
* Create SHA-256 hash values and determine changes.
* Store results in grep-able tsv files.
* Log runs to files in a log directory.

## Prerequisites
* Version 8 JDK installed and in path. Check: ``javac -version``
* Version control tool git installed and in path. Check: ``git --version``
* Configuration text file ``.am.properties`` (see section Configuration below).
* Optional but highly recommended: command-line tool [exiftool](https://www.sno.phy.queensu.ca/~phil/exiftool/) installed and in path. Check: ``exiftool -ver``

## Usage
Clone am:
```
git clone https://github.com/marco-schmidt/am.git
```
If you already have a copy that may be out of date, get the most recent changes:
```
git pull
```

Now build and run the tool:
```
./gradlew run
```

## Configuration
Application configuration information is read from a text file in [.properties format](https://en.wikipedia.org/wiki/.properties) named ``.am.properties`` in the current user's home directory.

Note that backslashes must be escaped using a second backslash, e.g. to express Windows paths.

```.properties
# define volumes, directory trees to be scanned
# use key 'volume' with consecutive numbers 1, 2, 3, and so on
# each directory must exist and be readable for user running am
volume1=/home/johndoe/Pictures
volume2=/home/johndoe/Music

# write collected information about files to tab-separated value files into this directory
# directory must exist and be writable for user running am
tsvDir=/home/johndoe/am/tsv

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
