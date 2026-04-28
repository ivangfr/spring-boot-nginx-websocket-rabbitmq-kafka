#!/usr/bin/env bash
set -e

DOCKER_IMAGE_PREFIX="ivanfranchin"
APP_NAME="news-app"
APP_VERSION="1.0.0"
DOCKER_IMAGE_NAME="${DOCKER_IMAGE_PREFIX}/${APP_NAME}:${APP_VERSION}"

docker rmi "$DOCKER_IMAGE_NAME"
