/* EL ORDEN DE LAS TABLAS ES IMPORTANTE */
/* CATEGORIAS */
INSERT INTO categorias (estado, nombre) VALUES (1, 'Terror');
INSERT INTO categorias (estado, nombre) VALUES (1, 'Fantasía');
INSERT INTO categorias (estado, nombre) VALUES (1, 'Romántico');
INSERT INTO categorias (estado, nombre) VALUES (1, 'Aventura');
INSERT INTO categorias (estado, nombre) VALUES (1, 'Policial/Thriller');
INSERT INTO categorias (estado, nombre) VALUES (1, 'Ciencia Ficción');
INSERT INTO categorias (estado, nombre) VALUES (1, 'Investigación');
INSERT INTO categorias (estado, nombre) VALUES (1, 'Biográfico');
INSERT INTO categorias (estado, nombre) VALUES (1, 'Infantil');
INSERT INTO categorias (estado, nombre) VALUES (1, 'Autoayuda');
INSERT INTO categorias (estado, nombre) VALUES (1, 'Erótico');
INSERT INTO categorias (estado, nombre) VALUES (1, 'Hogar');
INSERT INTO categorias (estado, nombre) VALUES (1, 'Enciclopedia/Manual');
INSERT INTO categorias (estado, nombre) VALUES (1, 'Política');
INSERT INTO categorias (estado, nombre) VALUES (1, 'Economía/Marketing');
INSERT INTO categorias (estado, nombre) VALUES (1, 'Sociedad');
INSERT INTO categorias (estado, nombre) VALUES (1, 'Deportes');
INSERT INTO categorias (estado, nombre) VALUES (1, 'Viajes/Cultura');
INSERT INTO categorias (estado, nombre) VALUES (1, 'Otros Temas/Varios');
/* USUARIOS */
INSERT INTO usuarios (apellidos, celular, direccion, email, estado, fecha_registro, foto_usuario, nombres, nro_documento, password, username) VALUES ('Apellido Usuario', 65656565, 'Av. Lima 789', 'usuario@gmail.com', 1, '2019-08-19', 'no-image.jpg', 'Nombre Usuario', 44444444, '$2a$10$VKuMvd4UAx7ZJbsZLOQ8/u1z44jmL/MxZzhtMKL8xubi4iqKpIRyO', 'usuario');
INSERT INTO usuarios (apellidos, celular, direccion, email, estado, fecha_registro, foto_usuario, nombres, nro_documento, password, username) VALUES ('Apellido Usuaria', 54545454, 'Av. Arequipa 101', 'usuaria@gmail.com', 1, '2019-08-19', 'no-image.jpg', 'Nombre Usuaria', 55555555, '$2a$10$RgVfaCIlKua4ogo4rsBfJeE5hqGT5aoEjZMyOPop4u61/KVSZeFWK', 'usuaria');
/* CONFIRMATION_TOKEN */
INSERT INTO confirmation_token (confirmation_token, created_date, usuario_id) VALUES ('XL2UR9VI6DxHBLXj7vB0QLhqopnqaN4V4LZwu3fgU4lKu11mps', '2019-12-11', 1);
INSERT INTO confirmation_token (confirmation_token, created_date, usuario_id) VALUES ('nOO8qA9B0KcL9lWYuGBBOACHCbFZatR43Jx933FyDqKJHx46O4', '2019-12-11', 2);
/* ROLES/AUTHORITIES */
INSERT INTO authorities (authority) VALUES ('ROLE_SYSADMIN');
INSERT INTO authorities (authority) VALUES ('ROLE_ADMIN');
INSERT INTO authorities (authority) VALUES ('ROLE_SUPERVISOR');
INSERT INTO authorities (authority) VALUES ('ROLE_EMPLEADO');
INSERT INTO authorities (authority) VALUES ('ROLE_USER');
/* USUARIOS-ROLES */
INSERT INTO usuarios_roles (usuario_id, rol_id) VALUES (1, 5);
INSERT INTO usuarios_roles (usuario_id, rol_id) VALUES (2, 5);
/* EMPRESAS */
INSERT INTO empresas (direccion, estado, razon_social, ruc) VALUES ('Av. Lima 456', 1, 'Empresa 1', 11111111111);
/* LOCALES */
INSERT INTO locales (direccion, estado, observaciones, empresa_id) VALUES ('Av. Tacna 322', 1, 'No hay observaciones para este local', 1);
INSERT INTO locales (direccion, estado, observaciones, empresa_id) VALUES ('Av. Próceres De La Independencia 217', 1, 'No hay observaciones para este local', 1);
/* EMPLEADOS */
INSERT INTO empleados (apellidos, celular, direccion, email, estado, fecha_registro, nombres, nro_documento, password, username, local_id) VALUES ('Apellido Admin', 98989898, 'No disponible', 'admin@gmail.com', 1, '2019-08-19', 'Nombre Admin', 11111111, '$2a$10$DCDKy8zxMgnhv7BoTopltOq2qlT1icvmk/JMzT51BWmdWcKGOFFjS', 'admin', 1);
INSERT INTO empleados (apellidos, celular, direccion, email, estado, fecha_registro, nombres, nro_documento, password, username, local_id) VALUES ('Apellido Supervisor', 87878787, 'Av. El Sol 130', 'supervisor@gmail.com', 1, '2019-08-19', 'nombreSupervisor', 22222222, '$2a$10$7JtXWUaIkParaGFFYZI5xeZgf4lA365rrG0Hgi3.4qymiQfy.B85O', 'supervisor', 1);
INSERT INTO empleados (apellidos, celular, direccion, email, estado, fecha_registro, nombres, nro_documento, password, username, local_id) VALUES ('Apellido Empleada', 76767676, 'Av. Los Duraznos 653', 'empleada@gmail.com', 1, '2019-08-19', 'nombreEmpleada', 33333333, '$2a$10$rE9nFn/v1kcabjkQW/QABeuKGHirYoLWjcG.WHDVSfth7bhZM7JWi', 'empleada', 2);
/* EMPLEADOS-ROLES */
INSERT INTO empleados_roles (empleado_id, rol_id) VALUES (1,2);
INSERT INTO empleados_roles (empleado_id, rol_id) VALUES (2,3);
INSERT INTO empleados_roles (empleado_id, rol_id) VALUES (3,4);
/* LIBROS */
INSERT INTO libros (autor, descripcion, estado, fecha_registro, fecha_publicacion, stock, foto_libro, titulo, categoria_id, local_id) VALUES ('Brandon Sanderson', 'En las Llanuras Quebradas se libra una guerra sin sentido. Kaladin ha sido sometido a la esclavitud, mientras diez ejércitos luchan por separado contra un solo enemigo...', 1, '2012-04-11', '2019-05-23', 99, 'el-camino-de-los-reyes.jpg', 'El camino de los reyes', 2, 1);
INSERT INTO libros (autor, descripcion, estado, fecha_registro, fecha_publicacion, stock, foto_libro, titulo, categoria_id, local_id) VALUES ('Fiódor Dostoyevski', 'Un joven soñador imagina constantemente su vejez solitaria. Una noche por las calles de San Petersburgo se encuentra con una joven, Nástenka. Él nunca había hablado con mujeres y mucho menos se había enamorado, pero hay algo de ella que le hechiza...', 1, '2014-04-11', '2019-06-13', 99, 'las-noches-blancas.jpg', 'Las noches blancas', 3, 1);
INSERT INTO libros (autor, descripcion, estado, fecha_registro, fecha_publicacion, stock, foto_libro, titulo, categoria_id, local_id) VALUES ('Fiódor Dostoyevski', 'Un joven soñador imagina constantemente su vejez solitaria. Una noche por las calles de San Petersburgo se encuentra con una joven, Nástenka. Él nunca había hablado con mujeres y mucho menos se había enamorado, pero hay algo de ella que le hechiza...', 1, '2014-04-11', '2019-06-13', 99, 'las-noches-blancas.jpg', 'Las noches blancas', 3, 2);
/* PRESTAMOS */
INSERT INTO prestamos (devolucion, fecha_despacho, fecha_devolucion, observaciones, empleado_id, libro_id, usuario_id) VALUES (0, '2019-08-19', '2020-03-10', 'El libro: El camino de los reyes, ha sido programado para su devolución el día Miércoles 18 de Marzo 2020, por el empleado: Nombre Admin, Apellido Admin (código empleado 1) al usuario: Nombre Usuario, Apellido Usuario (código usuario 1)', 1, 1, 1);
INSERT INTO prestamos (devolucion, fecha_despacho, fecha_devolucion, observaciones, empleado_id, libro_id, usuario_id) VALUES (0, '2019-07-16', '2020-03-10', 'El libro: Las Noches blancas, ha sido programado para su devolución el día Viernes 10 de Abril 2020, por el empleado: Nombre Supervisor, Apellido Supervisor (código empleado 2) al usuario: Nombre Usuaria, Apellido Usuaria (código usuario 2)', 2, 2, 2);