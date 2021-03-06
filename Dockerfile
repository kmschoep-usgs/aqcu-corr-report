FROM maven:3.6.0-jdk-8-alpine AS build

#Pass build args into env vars
ARG CI
ENV CI=$CI

ARG SONAR_HOST_URL
ENV SONAR_HOST_URL=$SONAR_HOST_URL

ARG SONAR_LOGIN
ENV SONAR_LOGIN=$SONAR_LOGIN

RUN if getent ahosts "sslhelp.doi.net" > /dev/null 2>&1; then \
		wget 'https://s3-us-west-2.amazonaws.com/prod-owi-resources/resources/InstallFiles/SSL/DOIRootCA.cer' && \
		keytool -import -trustcacerts -file DOIRootCA.cer -alias DOIRootCA.cer -keystore $JAVA_HOME/jre/lib/security/cacerts -noprompt -storepass changeit; \
	fi

COPY pom.xml /build/pom.xml
WORKDIR /build

#download all maven dependencies (this will only re-run if the pom has changed)
RUN mvn -B dependency:go-offline

# copy git history into build image so that sonar can report trends over time
COPY dependency-check-suppression.xml /build
COPY .git /build
COPY src /build/src

# Release Verification
ARG RELEASE=false
ARG RELEASE_COMMAND="mvn -B -DdryRun=true release:prepare"
ARG BUILD_COMMAND="mvn -B clean verify"
RUN if $RELEASE -eq "true"; then \
		echo "Execute release build: '$RELEASE_COMMAND'" ; \
		$RELEASE_COMMAND ;\
	else \
		echo "Execute standard build: '$BUILD_COMMAND'" ; \
		$BUILD_COMMAND ; \
	fi

FROM usgswma/wma-spring-boot-base:8-jre-slim-0.0.4

ENV serverPort=7505
ENV javaToRServiceEndpoint=https://reporting-services.nwis.usgs.gov:7500/
ENV aqcuReportsWebserviceUrl=https://reporting.nwis.usgs.gov/aqcu/timeseries-ws/
ENV aquariusServiceEndpoint=http://ts.nwis.usgs.gov
ENV aquariusServiceUser=apinwisra
ENV hystrixThreadTimeout=300000
ENV hystrixMaxQueueSize=200
ENV hystrixThreadPoolSize=10
ENV oauthResourceId=resource-id
ENV oauthResourceTokenKeyUri=https://example.gov/oauth/token_key
ENV HEALTHY_RESPONSE_CONTAINS='{"status":"UP"}'
ENV HEALTH_CHECK_ENDPOINT=actuator/health

COPY --chown=1000:1000 --from=build /build/target/*.jar app.jar

HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -k "https://127.0.0.1:${serverPort}${serverContextPath}${HEALTH_CHECK_ENDPOINT}" | grep -q ${HEALTHY_RESPONSE_CONTAINS} || exit 1
