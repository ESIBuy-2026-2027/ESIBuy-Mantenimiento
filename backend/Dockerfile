# Imagen del backend para Render. Dos etapas: se compila con el JDK 25 (el pom exige un toolchain 25) y se ejecuta solo con
# el JRE 25. Los tests no se ejecutan aquí: ya los pasa el CI y Render despliega solo cuando el CI ha pasado.
FROM eclipse-temurin:25-jdk AS compilacion
WORKDIR /app

# Primero el pom y el wrapper de Maven: mientras no cambien, Docker reutiliza la capa con las dependencias descargadas.
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app

# Usuario sin privilegios: si alguien consiguiera ejecutar código en la aplicación, no sería root dentro del contenedor.
RUN useradd --system --no-create-home esibuy
USER esibuy

COPY --from=compilacion /app/target/*.jar app.jar

# Los límites de memoria de la JVM se ponen con JAVA_TOOL_OPTIONS (en render.yaml), no aquí, para que la misma imagen
# sirva en local. El puerto lo decide la variable PORT (server.port en application.properties).
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
