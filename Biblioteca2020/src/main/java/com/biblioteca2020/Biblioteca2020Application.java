package com.biblioteca2020;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class Biblioteca2020Application implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(Biblioteca2020Application.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {

		// CREO CONTRASEÑAS PARA USUARIOS SOLO A MODO DE PRUEBA
		/*String passwordSysadmin = "sysadmin";
		String passwordAdminLocal2 = "adminLocal2";
		String passwordEmpleadoLocal2 = "empleadoLocal2";
		String passwordEmpleadaLocal3 = "empleadaLocal3";
		String passwordUsuario = "usuario";
		String passwordUsuaria = "usuaria";
		String edmech = "Sinfonico29";

		for (int i = 0; i < 1; i++) {
			String bcryptPasswordSysadmin = new BCryptPasswordEncoder().encode(passwordSysadmin);
			String bcryptPasswordAdmin = new BCryptPasswordEncoder().encode(passwordAdminLocal2);
			String bcryptPasswordEmpleado = new BCryptPasswordEncoder().encode(passwordEmpleadoLocal2);
			String bcryptPasswordEmpleada = new BCryptPasswordEncoder().encode(passwordEmpleadaLocal3);
			String bcryptPasswordUsuario = new BCryptPasswordEncoder().encode(passwordUsuario);
			String bcryptPasswordUsuaria = new BCryptPasswordEncoder().encode(passwordUsuaria);
			String bcryptPasswordEdmech = new BCryptPasswordEncoder().encode(edmech);
			System.out.println("SYSADMIN: ".concat(bcryptPasswordSysadmin));
			System.out.println("ADMIN: ".concat(bcryptPasswordAdmin));
			System.out.println("EMPLEADO: ".concat(bcryptPasswordEmpleado));
			System.out.println("EMPLEADA: ".concat(bcryptPasswordEmpleada));
			System.out.println("USUARIO: ".concat(bcryptPasswordUsuario));
			System.out.println("USUARIA: ".concat(bcryptPasswordUsuaria));
			System.out.println("EDMECH: ".concat(bcryptPasswordEdmech));
		}*/

	}

}
