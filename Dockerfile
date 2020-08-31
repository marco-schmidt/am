# https://hub.docker.com/r/adoptopenjdk/openjdk8
FROM adoptopenjdk/openjdk8:alpine-slim

LABEL description="am (asset manager) with Java, Perl and exiftool"
LABEL maintainer="mschmidtgit@protonmail.com"

# Requirement: must have run
#   ./gradlew clean distTar
# so that a single file build/distributions/am-*.tar exists.

# update PATH to include paths to exiftool and am 
ENV PATH="/opt/exiftool:/opt/am/bin:$PATH"

# TODO: download exiftool checksum file and validate exiftool archive checksum
#       https://exiftool.org/checksums.txt

# install perl and exiftool
RUN set -eux \
  && java -version \
  && apk update \
  && apk upgrade \
  && apk add --no-cache curl perl \
  && curl --version \
  && perl -v \
  && mkdir -p /opt/exiftool \
  && cd /opt/exiftool \
  && EXIFTOOL_VERSION=`curl -s https://exiftool.org/ver.txt` \
  && EXIFTOOL_ARCHIVE=Image-ExifTool-${EXIFTOOL_VERSION}.tar.gz \
  && curl -s -O https://exiftool.org/$EXIFTOOL_ARCHIVE \
  && tar xzf $EXIFTOOL_ARCHIVE --strip-components=1 \
  && rm -f $EXIFTOOL_ARCHIVE \
  && exiftool -ver

# copy am distribution tar file into image
COPY build/distributions/am-*.tar /opt

# unpack am tar file and delete it
RUN mkdir -p /opt/am \
  && cd /opt/am \
  && tar xf /opt/am-*.tar --strip-components=1 \
  && rm -f /opt/am-*.tar

ENTRYPOINT ["/bin/sh", "am", "--version", "--print-env"]
