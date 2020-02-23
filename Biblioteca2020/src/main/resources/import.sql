/* EL ORDEN DE LAS TABLAS ES IMPORTANTE */
/* USUARIOS */
INSERT INTO usuarios (estado, password, username) VALUES (1,'$2a$10$DCDKy8zxMgnhv7BoTopltOq2qlT1icvmk/JMzT51BWmdWcKGOFFjS', 'admin');
INSERT INTO usuarios (estado, password, username) VALUES (1, '$2a$10$QWZkTkJyiKy55FcJwk1yAeUDTdAx7U4wifIaZT9kbicQmhbGvyhpa', 'empleado');
INSERT INTO usuarios (estado, password, username) VALUES (1, '$2a$10$rE9nFn/v1kcabjkQW/QABeuKGHirYoLWjcG.WHDVSfth7bhZM7JWi', 'empleada');
INSERT INTO usuarios (estado, password, username) VALUES (1, '$2a$10$VKuMvd4UAx7ZJbsZLOQ8/u1z44jmL/MxZzhtMKL8xubi4iqKpIRyO', 'usuario');
INSERT INTO usuarios (estado, password, username) VALUES (1, '$2a$10$RgVfaCIlKua4ogo4rsBfJeE5hqGT5aoEjZMyOPop4u61/KVSZeFWK', 'usuaria');
/* ROLES/AUTHORITIES */
INSERT INTO authorities (authority, usuario_id) VALUES ('ROLE_ADMIN', 1);
INSERT INTO authorities (authority, usuario_id) VALUES ('ROLE_EMPLEADO', 2);
INSERT INTO authorities (authority, usuario_id) VALUES ('ROLE_EMPLEADO', 3);
INSERT INTO authorities (authority, usuario_id) VALUES ('ROLE_USER', 4);
INSERT INTO authorities (authority, usuario_id) VALUES ('ROLE_USER', 5);
/* PERSONAS */
INSERT INTO personas (apellidos, celular, direccion, email, estado, fecha_registro, nombres, nro_documento, usuario_id) VALUES ('Admin Ape.', 999999999, 'No disponible', 'admin@gmail.com', true, '2012-04-11', 'Admin', 99999999, 1);
INSERT INTO personas (apellidos, celular, direccion, email, estado, fecha_registro, nombres, nro_documento, usuario_id) VALUES ('Hombre', 888888888, 'Av. Lima 123', 'empleado1@gmail.com', true, '2012-07-20','Empleado', 66666666, 2);
INSERT INTO personas (apellidos, celular, direccion, email, estado, fecha_registro, nombres, nro_documento, usuario_id) VALUES ('Mujer', 777777777, 'Av. Arequipa 456', 'empleada2@gmail.com', true, '2012-01-10', 'Empleada', 55555555, 3);
INSERT INTO personas (apellidos, celular, direccion, email, estado, fecha_registro, nombres, nro_documento, usuario_id) VALUES ('De La Torre Ugarte', 123456789, 'Direccion Suscriptor 1', 'suscriptor1@gmail.com', 1, '2012-09-10', 'Alfredo', 87654321, 4);
INSERT INTO personas (apellidos, celular, direccion, email, estado, fecha_registro, nombres, nro_documento, usuario_id) VALUES ('Mercedes Rojas', 987654321, 'Direccion Suscriptor 2', 'suscriptor2@gmail.com', 1, '2018-08-18', 'Rosa', 24682468, 5);
/* EMPRESAS */
INSERT INTO empresas (direccion, estado, razon_social, ruc) VALUES ('Av. Lima 456', 1, 'Empresa Ejemplo 1', 11111111111);
INSERT INTO empresas (direccion, estado, razon_social, ruc) VALUES ('Av. Arequipa 789', 1, 'Empresa Ejemplo 2', 22222222222);
/* LIBROS */
INSERT INTO libros (autor, categoria, descripcion, estado, fecha_registro, fecha_publicacion, titulo) VALUES ('Autor Libro 1', 'Fantasía', 'Descripcion Libro 1', 1, '2012-04-11', '2019-05-23', 'El camino de los reyes');
INSERT INTO libros (autor, categoria, descripcion, estado, fecha_registro, fecha_publicacion, titulo) VALUES ('Autor Libro 2', 'Novela Corta', 'Descripcion Libro 2', 1, '2014-04-11', '2019-06-13', 'Las noches blancas');
/* LOCALES */
INSERT INTO locales (direccion, estado, observaciones, empresa_id) VALUES ('Direccion Local 1', 1, 'Observaciones Local 1', 1);
INSERT INTO locales (direccion, estado, observaciones, empresa_id) VALUES ('Direccion Local 2', 1, 'Observaciones Local 2', 2);
/* LOCALES-LIBROS */
INSERT INTO locales_libros (locales_id, libros_id) VALUES (1, 1);
INSERT INTO locales_libros (locales_id, libros_id) VALUES (1, 2);
INSERT INTO locales_libros (locales_id, libros_id) VALUES (2, 1);
INSERT INTO locales_libros (locales_id, libros_id) VALUES (2, 2);
/* PRESTAMOS */
INSERT INTO prestamos (devolucion, fecha_despacho, libro_id, observaciones, usuario_id) VALUES (0, '2019-08-19', 1, 'Observaciones prèstamo 1', 4);
INSERT INTO prestamos (devolucion, fecha_despacho, libro_id, observaciones, usuario_id) VALUES (0, '2019-07-16', 2, 'Observaciones prèstamo 1', 5);