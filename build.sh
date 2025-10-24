#!/usr/bin/env bash

# 构建统一的单体服务
docker build -t flow-eda-server --build-arg APP_NAME='server' --build-arg APP_PORT=9071 .

# 构建网关服务
docker build -t flow-eda-gateway --build-arg APP_NAME='gateway' --build-arg APP_PORT=8090 .
