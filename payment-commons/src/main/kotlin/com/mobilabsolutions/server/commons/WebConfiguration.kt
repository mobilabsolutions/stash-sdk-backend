/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.server.commons

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import java.util.ArrayList
import org.springframework.http.converter.ByteArrayHttpMessageConverter

@Configuration
class WebConfiguration : WebMvcConfigurer {
    companion object {
        val DEFAULT_TIMEOUT = TimeUnit.SECONDS.toMillis(5).toInt()
    }

    @Autowired
    private lateinit var jsonConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var jsonMapper: ObjectMapper

    @Bean
    fun restTemplate(): RestTemplate {
        val requestFactory = HttpComponentsClientHttpRequestFactory()
        requestFactory.setConnectTimeout(DEFAULT_TIMEOUT)
        requestFactory.setReadTimeout(DEFAULT_TIMEOUT)
        val restTemplate = RestTemplate(requestFactory)
        restTemplate.messageConverters.forEach { this.configureConverters(it) }
        return restTemplate
    }

    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.add(jsonConverter)
        converters.add(byteArrayHttpMessageConverter())
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("swagger-ui.html")
            .addResourceLocations("classpath:/META-INF/resources/")
        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/")
    }

    @Bean
    fun byteArrayHttpMessageConverter(): ByteArrayHttpMessageConverter {
        val arrayHttpMessageConverter = ByteArrayHttpMessageConverter()
        arrayHttpMessageConverter.supportedMediaTypes = getSupportedMediaTypes()
        return arrayHttpMessageConverter
    }

    private fun getSupportedMediaTypes(): List<MediaType> {
        val list = ArrayList<MediaType>()
        list.add(MediaType.IMAGE_JPEG)
        list.add(MediaType.IMAGE_PNG)
        list.add(MediaType.APPLICATION_OCTET_STREAM)
        return list
    }

    private fun configureConverters(messageConverter: HttpMessageConverter<*>) {
        if (messageConverter is MappingJackson2HttpMessageConverter) {
            messageConverter.defaultCharset = Charset.forName("UTF-8")
            messageConverter.objectMapper = jsonMapper
        }
        if (messageConverter is StringHttpMessageConverter) {
            messageConverter.defaultCharset = Charset.forName("UTF-8")
        }
    }
}
