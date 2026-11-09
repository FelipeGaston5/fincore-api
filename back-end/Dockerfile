
# ==========================================
# ESTÁGIO 1 - BUILD
# ==========================================
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copia os arquivos necessários para o Maven Wrapper
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Garante permissão de execução
RUN chmod +x mvnw

# Baixa as dependências
RUN ./mvnw -B dependency:go-offline

# Copia o código-fonte
COPY src/ src/

# Compila e empacota a aplicação
# Os testes serão executados separadamente
RUN ./mvnw -B clean package -DskipTests


# ==========================================
# ESTÁGIO 2 - RUNTIME
# ==========================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Cria usuário sem privilégios administrativos
RUN groupadd --system fincore && \
    useradd --system --gid fincore fincore

# Copia somente o JAR gerado
COPY --from=build /app/target/*.jar app.jar

# Define o usuário da aplicação
USER fincore

# Porta da API
EXPOSE 8080

# Inicializa a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
