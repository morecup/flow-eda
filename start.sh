#!/usr/bin/env bash

# 启动统一的单体服务
docker run -d -p 9071:9071 \
  -e DB_HOST=192.168.0.5 \
  -e DB_PORT=33307 \
  -e DB_NAME=flow_eda \
  -e DB_USER=root \
  -e DB_PASSWORD=ljzh@2003 \
  -v /root/app/springboot/server/logs:/logs \
  flow-eda-server
