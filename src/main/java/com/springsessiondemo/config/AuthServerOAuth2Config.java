package com.springsessiondemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

@Configuration
@EnableAuthorizationServer
@PropertySource("classpath:application-dev.properties")
public class AuthServerOAuth2Config extends AuthorizationServerConfigurerAdapter implements WebMvcConfigurer
{
    private final AuthenticationManager authenticationManager;

    @Value("classpath:oauth_schema.sql")
    private Resource schemaScript;


    private final DataSource dataSource;

    @Autowired
    public AuthServerOAuth2Config(AuthenticationManager authenticationManager, DataSource dataSource)
    {
        this.authenticationManager = authenticationManager;
        this.dataSource = dataSource;
    }


    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception
    {
        oauthServer.tokenKeyAccess("permitAll()")
                    .checkTokenAccess("isAuthenticated()");
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception
    {
        clients.jdbc(dataSource)
                .withClient("sampleClientId")
                    .authorizedGrantTypes("implicit")
                    .scopes("read")
                    .autoApprove(true)
                .and()
                    .withClient("clientIdPassword")
                    .secret("secret")
                    .authorizedGrantTypes("password","authorization_code", "refresh_token")
                    .scopes("read");
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception
    {
        endpoints.tokenStore(tokenStore()).authenticationManager(authenticationManager);
    }

    @Bean
    public TokenStore tokenStore()
    {
        return new JdbcTokenStore(dataSource);
    }


    private static final String[] CLASSPATH_RESOURCE_LOCATIONS =
            {
                    "classpath:/META-INF/resources/", "classpath:/resources/",
                    "classpath:/static/", "classpath:/public/","classpath:/static/vendor/","classpath:/static/custom/"
            };

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher()
    {
        return new HttpSessionEventPublisher();
    }
}
