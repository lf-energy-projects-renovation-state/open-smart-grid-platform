#!/bin/bash

# SPDX-FileCopyrightText: Contributors to the GXF project
#
# SPDX-License-Identifier: Apache-2.0

JARTOSTART=dlms-device-simulator/target/dlms-device-simulator-*-standalone.jar
PORT=1024

usage()
{
  echo "Usage: $0 [OPTIONS]"
  echo ' '
  echo 'Possible OPTIONS:'
  echo ' -j <jar to start> The jar to start' 
  echo ' -p <port>         The port for the device simulator'
  exit 1
}

while getopts "hj:p:" OPTION
do
  case $OPTION in
    h)
      usage
      ;;
    p)
      PORT=$OPTARG
      ;;
    j)
      JARTOSTART=$OPTARG
      ;;
  esac
done

java -jar ${JARTOSTART} --deviceidentification.kemacode=TEST${PORT} --deviceidentification.productionyear=00 --deviceidentification.serialnumber=000000 --spring.profiles.active=default,minimumMemory --port=${PORT} --logicalDeviceIds=1
