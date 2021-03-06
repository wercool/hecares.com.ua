package hecares.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import hecares.security.AuthenticationFailureHandler;
import hecares.security.AuthenticationSuccessHandler;
import hecares.security.LogoutSuccess;
import hecares.security.RestAuthenticationEntryPoint;
import hecares.security.TokenAuthenticationFilter;
import hecares.service.CustomUserDetailsService;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{

    @Value("${jwt.cookie}")
    private String TOKEN_COOKIE;

    @Bean
    public TopMostServletFilter topMostServletFilter() throws Exception {
        return new TopMostServletFilter();
    }
    
    @Bean
    public TokenAuthenticationFilter jwtAuthenticationTokenFilter() throws Exception {
        return new TokenAuthenticationFilter();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private CustomUserDetailsService jwtUserDetailsService;

    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Autowired
    private LogoutSuccess logoutSuccess;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(jwtUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Autowired
    private AuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    private AuthenticationFailureHandler authenticationFailureHandler;

    @Autowired
    private Environment environment;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf()
            .ignoringAntMatchers("/api/login")
            .ignoringAntMatchers("/api/register")
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .exceptionHandling().authenticationEntryPoint(restAuthenticationEntryPoint).and()
            .addFilterAfter(topMostServletFilter(), BasicAuthenticationFilter.class)
            .addFilterAfter(jwtAuthenticationTokenFilter(), TopMostServletFilter.class)
            .authorizeRequests()
            .anyRequest()
            .authenticated().and()
            .formLogin()
            .loginPage("/api/login")
            .successHandler(authenticationSuccessHandler)
            .failureHandler(authenticationFailureHandler).and()
            .logout()
            .logoutRequestMatcher(new AntPathRequestMatcher("/api/logout"))
            .logoutSuccessHandler(logoutSuccess)
            .deleteCookies(TOKEN_COOKIE);

        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles[0].equals("dev")) {
            http.csrf().disable();
        }
    }
}
