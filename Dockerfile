FROM adoptopenjdk/openjdk8:alpine-jre

ARG BUILD_DATE
ARG BUILD_REVISION

LABEL org.opencontainers.image.authors="mschmidtgit@protonmail.com"
LABEL org.opencontainers.image.vendor="Marco Schmidt"
LABEL org.opencontainers.image.title="marcoschmidt/am"
LABEL org.opencontainers.image.description="am: Asset Manager with Java, Perl and exiftool"
LABEL org.opencontainers.image.url="https://hub.docker.com/r/marcoschmidt/am"
LABEL org.opencontainers.image.source="https://github.com/marco-schmidt/am/blob/master/Dockerfile"
LABEL org.opencontainers.image.licenses="Apache-2.0"

# Requirement: must have run
#   ./gradlew clean distTar
# so that a single file build/distributions/am-*.tar exists.

# update PATH to include paths to exiftool and am 
ENV PATH="/opt/exiftool:/opt/am/bin:$PATH"

RUN set -eux \
  && java -version \
  && apk update \
  && apk upgrade \
  # install perl and curl
  && apk add --no-cache curl perl \
  && curl --version \
  && perl -v \
  # install exiftool
  && mkdir -p /opt/exiftool \
  && cd /opt/exiftool \
  && EXIFTOOL_VERSION=`curl -s https://exiftool.org/ver.txt` \
  && EXIFTOOL_ARCHIVE=Image-ExifTool-${EXIFTOOL_VERSION}.tar.gz \
  && curl -s -O https://exiftool.org/$EXIFTOOL_ARCHIVE \
  && CHECKSUM=`curl -s https://exiftool.org/checksums.txt | grep SHA1\(${EXIFTOOL_ARCHIVE} | awk -F'= ' '{print $2}'` \
  && echo "${CHECKSUM}  ${EXIFTOOL_ARCHIVE}" | /usr/bin/sha1sum -c -s - \
  && tar xzf $EXIFTOOL_ARCHIVE --strip-components=1 \
  && rm -f $EXIFTOOL_ARCHIVE \
  && exiftool -ver \
  # create user and group am and several directories
  && mkdir -p /home/am \
  && addgroup -S am \
  && adduser -S -G am -h /home/am am \
  && mkdir -p /home/am/config \
  && mkdir /home/am/db \
  && mkdir /home/am/logs \
  && chown -R am:am /home/am

# labels using arguments, changing with every build
LABEL org.opencontainers.image.created=$BUILD_DATE
LABEL org.opencontainers.image.revision=$BUILD_REVISION

# copy am distribution tar file into image
COPY build/distributions/am-*.tar /opt

# unpack am tar file and delete it
RUN mkdir -p /opt/am \
  && cd /opt/am \
  && tar xf /opt/am-*.tar --strip-components=1 \
  && rm -f /opt/am-*.tar

USER am

ENTRYPOINT ["/bin/sh", "am", "--print-env", "--config", "/home/am/config/.am.properties"]
