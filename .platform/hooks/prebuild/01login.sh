#!/bin/bash
DOCKER_LOGIN=/opt/elasticbeanstalk/bin/get-config environment -k DOCKER_LOGIN
DOCKER_REPOSITORY=/opt/elasticbeanstalk/bin/get-config environment -k DOCKER_REPOSITORY
/opt/elasticbeanstalk/bin/get-config environment -k DOCKER_PASSWORD | docker login -u $DOCKER_LOGIN --password-stdin $DOCKER_REPOSITORY