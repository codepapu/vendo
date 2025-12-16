dependencies {
    // Common module
    implementation(project(":nexus-common"))
    
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    
    // Spring Authorization Server
    implementation("org.springframework.security:spring-security-oauth2-authorization-server:1.2.0")
    implementation("org.springframework.security:spring-security-oauth2-resource-server:6.2.0")
    implementation("org.springframework.security:spring-security-oauth2-jose:6.2.0")
    
    // Database
    runtimeOnly("org.postgresql:postgresql")
    
    // Service Discovery
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    
    // Configuration Server Client
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    
    // Utilities
    implementation("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
