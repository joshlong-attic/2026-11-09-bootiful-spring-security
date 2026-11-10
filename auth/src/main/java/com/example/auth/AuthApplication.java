package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManagerFactories;
import org.springframework.security.authorization.RequiredFactor;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authorization.EnableMultiFactorAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.security.Principal;
import java.util.Map;

//@EnableMultiFactorAuthentication(authorities = {
////        FactorGrantedAuthority.PASSWORD_AUTHORITY,
////        FactorGrantedAuthority.OTT_AUTHORITY
//})
@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // knowledge
    // possession
    // inherence

    @Bean
    Customizer<HttpSecurity> securityCustomizer() {
        var amf = AuthorizationManagerFactories.multiFactor()
                .requireFactor(RequiredFactor.Builder::ottAuthority)
                .requireFactor(RequiredFactor.Builder::passwordAuthority)
                .build();
        return http -> http
                .oauth2AuthorizationServer(a ->a.oidc(Customizer.withDefaults()))
//                .authorizeHttpRequests(a -> a
//                        .requestMatchers("/admin").access(amf.hasRole("ADMIN"))
//                )
                .webAuthn(a -> a
                        .rpName("gatewayjug")
                        .rpId("localhost")
                        .allowedOrigins("http://localhost:8080"))
                .oneTimeTokenLogin(ott -> ott
                        .tokenGenerationSuccessHandler((request, response,
                                                        oneTimeToken) -> {

                            response.getWriter().println("you've got console mail!");
                            response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);

                            IO.println(oneTimeToken.getUsername() + ", please go to http://localhost:8080/login/ott?token=" +
                                    oneTimeToken.getTokenValue());

//                            var handler = new RedirectOneTimeTokenGenerationSuccessHandler("/please-login");
//                            handler.handle(request , response, oneTimeToken);

                        }));
    }

    @Bean
    JdbcUserDetailsManager userDetailsService(DataSource dataSource) {
        var u = new JdbcUserDetailsManager(dataSource);
        u.setEnableUpdatePassword(true);
        return u;
    }
}


// authentication - wbo is making the request
// authorization - what rights do they have

