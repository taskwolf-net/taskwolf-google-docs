FROM alpine

COPY /build/libs/google-docs-1.0.0-SNAPSHOT.jar google-docs.jar
COPY /locale/ /locale/