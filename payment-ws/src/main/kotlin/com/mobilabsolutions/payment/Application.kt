package com.mobilabsolutions.payment

import com.google.common.base.Predicates
import com.mobilabsolutions.payment.data.configuration.DataConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Import(DataConfiguration::class)
@EnableSwagger2
@SpringBootApplication
class Application {

    @Bean
    fun api(): Docket = Docket(DocumentationType.SWAGGER_2)
        .useDefaultResponseMessages(false)
        .select()
        .apis(RequestHandlerSelectors.any())
        .paths(Predicates.not(PathSelectors.regex("/error.*")))
        .paths(Predicates.not(PathSelectors.regex("/actuator.*")))
        .build()
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
