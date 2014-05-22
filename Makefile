TOPDIR=src/main/java/org/noisesmith/
SRCDIRS=${TOPDIR}projector/*.java
TARGET=target/uber-projector-1.0-SNAPSHOT.jar
MAIN=org.noisesmith.noisespew.Projector
MAVEN=mvn

default: ${TARGET}

run: quiet
	java -jar ${TARGET}

${TARGET}: ${SRCDIRS} pom.xml
	${MAVEN} package

quiet: ${SRCDIRS} pom.xml
	${MAVEN} -q package
clean:
	rm -rf target/
plugindeps:
	${MAVEN} org.apache.maven.plugins:maven-dependency-plugin:2.6:resolve-plugins
