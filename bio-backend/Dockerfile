# Etap budowania
FROM maven:3.8.6-eclipse-temurin-17-alpine AS build

# Skopiuj cały projekt (w tym pom.xml)
WORKDIR /app
COPY . .

# Utwórz katalog na biblioteki natywne ZACHOWUJĄC strukturę katalogów
RUN mkdir -p /app/resources/native/linux-x86-64

# Zbuduj aplikację
RUN mvn clean package -DskipTests

# Etap uruchomieniowy
FROM openjdk:17-slim

# Instalacja niezbędnych zależności
RUN apt-get update && apt-get install -y \
    libc6 \
    libstdc++6 \
    && rm -rf /var/lib/apt/lists/*

# Skopiuj biblioteki natywne ZACHOWUJĄC strukturę katalogów
COPY src/main/resources/native/linux-x86-64/* /app/resources/native/linux-x86-64/

# Skopiuj plik licencji
COPY docker_files/license/Trial_PC__Valid-until_2025-06-04_5170789018306794686_internet_license.lic /app/license.lic

# Skopiuj zbudowany JAR z etapu budowania
COPY --from=build /app/target/*.jar /app/app.jar

# Copy the wait-for-it.sh script to the final image
COPY wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

# Ustaw ścieżki do bibliotek natywnych
ENV JAVA_LIBRARY_PATH=/app/resources/native
ENV JNA_LIBRARY_PATH=/app/resources/native

# Dodaj ścieżkę do bibliotek systemowych
ENV LD_LIBRARY_PATH=/app/resources/native/linux-x86-64:${LD_LIBRARY_PATH}

WORKDIR /app
EXPOSE 8080

# Dodaj możliwość debugowania bibliotek
ENTRYPOINT [ "./wait-for-it.sh", "mysql:3306", "--", "java", "-Djava.library.path=/app/resources/native/linux-x86-64", "-Djna.library.path=/app/resources/native/linux-x86-64", "-verbose:jni", "-jar", "app.jar" ]

#ENTRYPOINT ["java", \
#    "-Djava.library.path=/app/resources/native/linux-x86-64", \
#    "-Djna.library.path=/app/resources/native/linux-x86-64", \
#    "-verbose:jni", \
#    "-jar", "app.jar"]