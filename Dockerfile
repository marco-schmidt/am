# https://hub.docker.com/r/adoptopenjdk/openjdk8
FROM adoptopenjdk/openjdk8:alpine-slim
LABEL maintainer="mschmidtgit@protonmail.com"

ARG BUILD_DATE

LABEL org.label-schema.schema-version="1.0"
LABEL org.label-schema.description="am (asset manager) with Java, Perl and exiftool"
LABEL org.label-schema.build-date=$BUILD_DATE
LABEL org.label-schema.name="marcoschmidt/am"
LABEL org.label-schema.vcs-url="https://github.com/marco-schmidt/am"

# Requirement: must have run
#   ./gradlew clean distTar
# so that a single file build/distributions/am-*.tar exists.

# update PATH to include paths to exiftool and am 
ENV PATH="/opt/exiftool:/opt/am/bin:$PATH"

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
  && CHECKSUM=`curl -s https://exiftool.org/checksums.txt | grep SHA1\(Image | awk -F'= ' '{print $2}'` \
  && echo "${CHECKSUM}  ${EXIFTOOL_ARCHIVE}" | /usr/bin/sha1sum -c -s - \
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
