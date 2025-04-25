#!/bin/bash

for service in auth-service commerce-service ui-service
do
  echo "🔧 Building $service..."
  cd $service
  mvn clean
  mvn package -DskipTests
  docker build -t $service .
  cd ..
done
