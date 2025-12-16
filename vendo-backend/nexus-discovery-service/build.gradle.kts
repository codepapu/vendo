dependencies {
    // Eureka Server
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")
    
    // Actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    // Utilities
    implementation("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
