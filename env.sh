#!/usr/bin/env bash

#启动该项目需要的相关服务

# kill all thread ;
qq

#mysql
nohup sh /shell/workershell/mysql.sh &>env.log &
nohup sh /shell/workershell/zk.sh  &>env.log &
nohup sh /shell/workershell/kafak.sh  &>env.log &
nohup sh /shell/workershell/mongodb.sh  &>env.log &

tail -f env.log




