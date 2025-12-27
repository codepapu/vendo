plugins {
    id("java")
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.5"
}

group = "com.vendo"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    
    // Database
    runtimeOnly("org.postgresql:postgresql")
    
    // Migration
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    
    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
    
    // API Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    
    // Kafka (optional, for async processing)
    implementation("org.springframework.kafka:spring-kafka")
    
    // Caching
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    
    // AWS SDK for S3
    implementation(platform("software.amazon.awssdk:bom:2.21.29"))
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:auth")
    implementation("software.amazon.awssdk:url-connection-client")
    
    // MinIO (Alternative to S3)
    implementation("io.minio:minio:8.5.7")
    
    // Utilities
    implementation("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    // JSON
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    
    // PDF Generation
    implementation("com.itextpdf:itext7-core:8.0.2")
    implementation("com.itextpdf:html2pdf:5.0.3")
    
    // Excel/CSV Processing
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")
    implementation("com.opencsv:opencsv:5.9")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
