# Projeto Java Spring Boot

Este projeto foi desenvolvido utilizando **Java Spring Boot**, seguindo padrões de **Atomic Design** e **Arquitetura Limpa** (Clean Architecture) para melhor organização e manutenção do código. A estrutura do projeto está separada em pastas que refletem as diferentes camadas de responsabilidade (controllers, services, repositories, etc.) e facilitam a escalabilidade e compreensão.

## Sumário

- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Arquitetura e Padrões](#arquitetura-e-padrões)
- [Pré-requisitos](#pré-requisitos)
- [Como Executar Localmente](#como-executar-localmente)
- [Como Executar com Docker](#como-executar-com-docker)
- [Estrutura de Pastas](#estrutura-de-pastas)
- [Contribuindo](#contribuindo)
- [Licença](#licença)

## Tecnologias Utilizadas

- **Java 17**  
- **Spring Boot**  
- **Maven**  
- **Docker**  
- **Padrão Atomic Design**  
- **Clean Architecture**  

## Arquitetura e Padrões

- **Atomic Design**: Foca em componentes reutilizáveis, permitindo que cada parte do sistema seja desenvolvida de forma independente e coesa.
- **Clean Architecture**: Organiza o projeto em camadas claras, separando regras de negócio da infraestrutura, facilitando testes e manutenção.

## Pré-requisitos

- **Java 17** ou superior instalado.
- **Maven** (caso utilize o Maven globalmente; caso contrário, utilize o wrapper `./mvnw` incluso).
- **Docker** (caso deseje executar o projeto em contêiner).

## Como Executar Localmente

1. **Clonar o repositório**:
   ```bash
   git clone <URL_DO_REPOSITORIO>
   cd <NOME_DO_PROJETO>
   ```

2. **Instalar dependências e compilar** (opcional, pois o wrapper faz isso automaticamente):
   ```bash
   mvn clean install
   ```
   ou
   ```bash
   ./mvnw clean install
   ```

3. **Executar o projeto**:
   ```bash
   mvn spring-boot:run
   ```
   ou
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Acessar a aplicação**:
   Abra o navegador e acesse:
   ```
   http://localhost:8080
   ```

## Como Executar com Docker

O projeto contém um arquivo Dockerfile multi-stage que faz o build da aplicação e gera a imagem final.
Você pode seguir estes passos:

1. **Build da imagem Docker**:
   ```bash
   docker build -t nome-da-imagem .
   ```

2. **Executar o contêiner**:
   ```bash
   docker run -p 8080:8080 nome-da-imagem
   ```

3. **Acessar a aplicação**:
   Abra o navegador e acesse:
   ```
   http://localhost:8080
   ```

### Dockerfile (Exemplo)

```dockerfile
# Etapa 1: Build do projeto
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Rodar o jar gerado
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Estrutura de Pastas

A estrutura de pastas pode variar conforme sua implementação, mas de forma geral:

```
.
├── .mvn/                # Configurações do Maven Wrapper
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/
│   │   │   │   └── exemplo/  # Pacotes de controllers, services, repositories, etc.
│   │   └── resources/        # Arquivos de configuração (application.properties, etc.)
├── Dockerfile
├── pom.xml               # Arquivo de configuração do Maven
└── README.md             # Este arquivo
```

- **controllers**: Classes que gerenciam as requisições HTTP (camada de entrada).
- **services**: Lógica de negócio e orquestração de operações.
- **repositories**: Acesso a dados (ex.: JPA, consultas ao banco).
- **utils**: Funções auxiliares ou constantes que podem ser reutilizadas em diferentes partes do projeto.

## Contribuindo

Contribuições são bem-vindas! Para contribuir:

1. Faça um fork do projeto.
2. Crie uma branch para sua feature:
   ```bash
   git checkout -b minha-feature
   ```
3. Faça os commits das suas alterações.
4. Envie um pull request descrevendo as mudanças.

## Licença

Este projeto pode estar sob alguma licença (MIT, Apache, etc.). Se for o caso, descreva aqui ou crie um arquivo LICENSE no repositório.

Desenvolvido por Leonardo Marzeuski.
