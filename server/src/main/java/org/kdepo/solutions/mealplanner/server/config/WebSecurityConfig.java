package org.kdepo.solutions.mealplanner.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        // For not registered only
                        .requestMatchers("/registration").not().fullyAuthenticated()

                        // For users
                        .requestMatchers(
                                "/ingredients/create?recipe_id={id:[0-9]+}",
                                "/ingredients/{id:[0-9]+}/update",
                                "/ingredients/{id:[0-9]+}/delete",

                                "/products/create",
                                "/products/{id:[0-9]+}/update",
                                "/products/{id:[0-9]+}/delete",

                                "/recipes/create",
                                "/recipes/{id:[0-9]+}/update",
                                "/recipes/{id:[0-9]+}/delete",
                                "/recipes/{id:[0-9]+}/tags",

                                "/tags/create",
                                "/tags/{id:[0-9]+}/update",
                                "/tags/{id:[0-9]+}/delete",
                                "/tags/{id:[0-9]+}/set",
                                "/tags/{id:[0-9]+}/unset"
                        ).hasRole("USER")

                        // For admins

                        // For all
                        .requestMatchers(
                                "/login",
                                "/logout",
                                "/", "/home",

                                // Ingredient details page info
                                "/ingredients/{id:[0-9]+}",

                                // Products list page
                                "/products",

                                // Product details info page
                                "/products/{id:[0-9]+}",

                                // Recipes list page
                                "/recipes",

                                // Recipe details info page
                                "/recipes/{id:[0-9]+}",

                                // Tags list page
                                "/tags",

                                // Tag details info page
                                "/tags/{id:[0-9]+}",

                                "/styles/common.css"
                        ).permitAll()

                        // Authentication is required for all other resources
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutUrl("/logout")

                        // URL to redirect after logout
                        .logoutSuccessUrl("/login?logout")

                        // Clear user session info
                        .invalidateHttpSession(true)

                        // Clear cookies and other info
                        .deleteCookies("JSESSIONID")
                        .addLogoutHandler(new HeaderWriterLogoutHandler(
                                new ClearSiteDataHeaderWriter(
                                        ClearSiteDataHeaderWriter.Directive.COOKIES,
                                        ClearSiteDataHeaderWriter.Directive.STORAGE
                                )
                        ))
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user =
                User.withDefaultPasswordEncoder()
                        .username("user")
                        .password("password")
                        .roles("USER")
                        .build();
        UserDetails admin =
                User.withDefaultPasswordEncoder()
                        .username("admin")
                        .password("password")
                        .roles("ADMIN")
                        .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

}
