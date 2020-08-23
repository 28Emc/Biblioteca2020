package com.biblioteca2020;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

	public void addViewControllers(ViewControllerRegistry registry) {
		// MÉTODO PARA REGISTRAR COMO VISTA PERSONALIZADA LOS ERRORES
		registry.addViewController("/error/error_403").setViewName("error_403");
		registry.addViewController("/error/error_reporte").setViewName("error_reporte");
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		WebMvcConfigurer.super.addResourceHandlers(registry);
		
		// REGISTRO LA CARPETA EXTERNA PARA GUARDAR LAS FOTOS
		registry.addResourceHandler("/uploads/**")
		.addResourceLocations("file:/C:/Temp/uploads/");
		
		/*String resourcePath = Paths.get("uploads").toAbsolutePath().toUri().toString();
		registry.addResourceHandler("/uploads/**")
		.addResourceLocations(resourcePath);*/
	}
		
}
