package com.biblioteca2020;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.biblioteca2020.auth.handler.LoginSuccessHandler;
import com.biblioteca2020.auth.handler.LogoutSuccessHandler;
import com.biblioteca2020.models.service.JpaEmpleadoDetailsService;
import com.biblioteca2020.models.service.JpaUserDetailsService;

// CLASE DE CONFIGURACIÒN DE SPRING SECURITY
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private LoginSuccessHandler loginSuccessHandler;

	@Autowired
	private LogoutSuccessHandler logoutSuccessHandler;

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// INSTANCIA DE CODIFICADOR DE CONTRASEÑAS
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	// INSTANCIA DE CONFIGURACIÓN DE USUARIOS Y ROLES CON ENTIDADES CONTROLADAS CON
	// JPA
	@Autowired
	private JpaUserDetailsService userDetailsService;

	// INSTANCIA DE CONFIGURACIÓN DE EMPLEADOS Y ROLES CON ENTIDADES CONTROLADAS CON
	// JPA
	@Autowired
	private JpaEmpleadoDetailsService empleadoDetailsService;

	// ESTE MÉTODO SIRVE PARA ESTABLECER LOS ROLES Y LA ENCRIPTACIÒN DE LA
	// CONTRASEÑA
	@Autowired
	public void configurerGlobal(AuthenticationManagerBuilder builder) throws Exception {
		builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
		builder.userDetailsService(empleadoDetailsService).passwordEncoder(passwordEncoder);
	}

	// MÉTODO DE FILTRO DE PETICIONES HTTP MEDIANTE LOS ROLES ESTABLECIDOS
	// ANTERIORMENTE
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http

				.csrf().disable()

				.authorizeRequests()
				.antMatchers("/css/**", "/js/**", "/date-picker/**", "/img/**", "/**/crear-perfil", "/**/editar-perfil",
						"/**/cuenta-verificada/**", "/**/recuperar-cuenta/")
				.permitAll()

				.and().formLogin().successHandler(loginSuccessHandler)
				// es AQUI que yo realmente gestiono mi LOGIN
				.loginPage("/login").permitAll()

				.and().logout().permitAll().logoutSuccessHandler(logoutSuccessHandler)
				
				//.and().rememberMe()

				.and().exceptionHandling().accessDeniedPage("/error_403")

				.and().authorizeRequests().anyRequest().authenticated();
	}

}
