/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.notifications.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Configuration
@EnableSwagger2
class SwaggerConfiguration {
    @Bean
    fun api(): Docket = Docket(DocumentationType.SWAGGER_2)
        .apiInfo(
            ApiInfoBuilder()
                .title("Payment SDK Backend")
                .description("Project Wiki: https://github.com/mobilabsolutions/payment-sdk-wiki-open/wiki")
                .version("1.0")
                .contact(
                    Contact(
                        "MobiLab Solutions GmbH",
                        "https://mobilabsolutions.com/",
                        "info@mobilabsolutions.com"
                    )
                )
                .build()
        )
        .useDefaultResponseMessages(false)
        .select()
        .apis(RequestHandlerSelectors.basePackage("com.mobilabsolutions.payment.notifications.controller"))
        .build()
}
