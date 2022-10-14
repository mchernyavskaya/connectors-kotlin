package org.elasticsearch.ingestion.service

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.web.SecurityFilterChain
import java.util.*


@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeRequests(
                Customizer {
                    it.anyRequest().authenticated()
                }
            )
            .oauth2Login()
            .clientRegistrationRepository(clientRegistrationRepository())
            .authorizedClientService(authorizedClientService())
        return http.build()
    }

    @Bean
    fun authorizedClientService(): OAuth2AuthorizedClientService? {
        return InMemoryOAuth2AuthorizedClientService(
            clientRegistrationRepository()
        )
    }

    @Bean
    fun clientRegistrationRepository(): ClientRegistrationRepository? {
        val registrations = listOf<ClientRegistration>(
            ClientRegistration.withRegistrationId("wordpress")
                .clientId("client-id")
                .clientSecret("client-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://public-api.wordpress.com/oauth2/authorize")
                .tokenUri("https://public-api.wordpress.com/oauth2/token")
                .userInfoUri("https://public-api.wordpress.com/rest/v1/me")
                .userNameAttributeName("username")
                .jwkSetUri("https://public-api.wordpress.com/oauth2/jwks")
                .clientName("WordPress")
                .build()
        )
        return InMemoryClientRegistrationRepository(registrations)
    }
}
