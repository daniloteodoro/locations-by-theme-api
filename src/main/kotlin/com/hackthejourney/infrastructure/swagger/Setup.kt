package com.hackthejourney.infrastructure.swagger

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@ApiIgnore
@RestController
@Configuration
@EnableSwagger2
class Setup {

    @GetMapping("/")
    fun redirectRootUrlToSwaggerUI(): ResponseEntity<Unit> {
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .header(HttpHeaders.LOCATION, "/swagger-ui.html")
                .build()
    }

    @Bean
    fun generateSwaggerDocket(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.hackthejourney"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(ApiInfoBuilder()
                        .title("Locations by Theme API")
                        .version("1.0.0")
                        .build())
    }

}