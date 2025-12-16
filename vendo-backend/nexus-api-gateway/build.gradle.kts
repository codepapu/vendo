dependencies {
    // Common module
    implementation(project(":nexus-common"))
    
    // Spring Cloud Gateway
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    
    // Service Discovery
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    
    // Circuit Breaker
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")
    
    // Rate Limiting
    implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0")
    
    // Load Balancing
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
    
    // Actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    // Redis for rate limiting
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    
    // Utilities
    implementation("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
