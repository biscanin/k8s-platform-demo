# Stage 1: build the jar (JDK + Maven wrapper)
# The jar is platform-independent, so always build on the runner's native arch (fast, no QEMU)
# and only the final runtime stage is built per target platform (amd64 + arm64).
FROM --platform=$BUILDPLATFORM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
# Copy build descriptors first so the dependency layer is cached when only code changes
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q package -DskipTests

# Stage 2: small runtime image (JRE only, non-root user)
FROM eclipse-temurin:25-jre
RUN useradd --system --uid 10001 app
USER 10001
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
