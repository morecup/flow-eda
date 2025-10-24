#!/usr/bin/env bash

# 构建统一的单体服务
docker build -t flow-eda-server --build-arg APP_NAME='server' --build-arg APP_PORT=9071 .
