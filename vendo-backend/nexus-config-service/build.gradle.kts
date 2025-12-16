dependencies {
    // Spring Cloud Config Server
    implementation("org.springframework.cloud:spring-cloud-config-server")
    
    // Actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    // Service Discovery
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    
    // Utilities
    implementation("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
