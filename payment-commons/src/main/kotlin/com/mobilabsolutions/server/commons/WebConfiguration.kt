package com.mobilabsolutions.server.commons

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.util.concurrent.TimeUnit

@Configuration
@EnableSwagger2
class WebConfiguration : WebMvcConfigurer {
    companion object {
        val DEFAULT_TIMEOUT = TimeUnit.SECONDS.toMillis(5).toInt()
    }

    @Autowired
    private lateinit var jsonConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var jsonMapper: ObjectMapper

    @Bean
    fun api(): Docket = Docket(DocumentationType.SWAGGER_2)
        .apiInfo(
            ApiInfoBuilder()
                .title("Payment SDK Backend")
                .description("Project Wiki: https://github.com/mobilabsolutions/payment-sdk-wiki-open")
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
        .apis(RequestHandlerSelectors.basePackage("com.mobilabsolutions.payment.controller"))
        .build()

    @Bean
    fun restTemplate(): RestTemplate {
        val restTemplateBuilder = RestTemplateBuilder()
        val restTemplate = restTemplateBuilder.messageConverters(MappingJackson2HttpMessageConverter(jsonMapper)).build()
        val requestFactory = HttpComponentsClientHttpRequestFactory()
        requestFactory.setConnectTimeout(DEFAULT_TIMEOUT)
        requestFactory.setReadTimeout(DEFAULT_TIMEOUT)
        restTemplate.requestFactory = requestFactory
        return restTemplate
    }

    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.add(jsonConverter)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("swagger-ui.html")
            .addResourceLocations("classpath:/META-INF/resources/")
        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/")
    }
}