#!/bin/bash
set -e

#services=("auth-service")
services=("auth-service"  "commerce-service" "ui-service")

for service in "${services[@]}"; do
  echo "🔧 Building $service..."
  
  pushd "$service" > /dev/null
  
  mvn clean
  mvn package -DskipTests
  
  DOCKER_BUILDKIT=1 docker buildx build \
    --build-arg BUILDKIT_INLINE_CACHE=1 \
    --build-arg MAVEN_OPTS="-Dmaven.repo.local=/root/.m2/repository" \
    -t "$service" \
    --load \
    .
  
  popd > /dev/null
done
