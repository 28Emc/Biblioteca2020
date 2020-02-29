/* EL ORDEN DE LAS TABLAS ES IMPORTANTE */
/* USUARIOS */
INSERT INTO usuarios (apellidos, celular, direccion, email, estado, fecha_registro, nombres, nro_documento, password, username) VALUES ('apellidoUsuario', 65656565, 'Av. Lima 789', 'usuario@gmail.com', 1, '2019-08-19', 'nombreUsuario', 44444444, '$2a$10$VKuMvd4UAx7ZJbsZLOQ8/u1z44jmL/MxZzhtMKL8xubi4iqKpIRyO', 'usuario');
INSERT INTO usuarios (apellidos, celular, direccion, email, estado, fecha_registro, nombres, nro_documento, password, username) VALUES ('apellidoUsuaria', 54545454, 'Av. Lima 101', 'usuaria@gmail.com', 1, '2019-08-19', 'nombreUsuaria', 55555555, '$2a$10$RgVfaCIlKua4ogo4rsBfJeE5hqGT5aoEjZMyOPop4u61/KVSZeFWK', 'usuaria');
/* ROLES/AUTHORITIES */
INSERT INTO authorities (authority) VALUES ('ROLE_ADMIN');
INSERT INTO authorities (authority) VALUES ('ROLE_SUPERVISOR');
INSERT INTO authorities (authority) VALUES ('ROLE_EMPLEADO');
INSERT INTO authorities (authority) VALUES ('ROLE_USER');
/* USUARIOS-ROLES */
INSERT INTO usuarios_roles (usuario_id, rol_id) VALUES (1, 4);
INSERT INTO usuarios_roles (usuario_id, rol_id) VALUES (2, 4);
/* EMPRESAS */
INSERT INTO empresas (direccion, estado, razon_social, ruc) VALUES ('Av. Lima 456', 1, 'Empresa Ejemplo 1', 11111111111);
INSERT INTO empresas (direccion, estado, razon_social, ruc) VALUES ('Av. Arequipa 789', 1, 'Empresa Ejemplo 2', 22222222222);
/* LOCALES */
INSERT INTO locales (direccion, estado, observaciones, empresa_id) VALUES ('Direccion Local 1.1', 1, 'Observaciones Local 1.1', 1);
INSERT INTO locales (direccion, estado, observaciones, empresa_id) VALUES ('Direccion Local 1.2', 1, 'Observaciones Local 1.2', 1);
INSERT INTO locales (direccion, estado, observaciones, empresa_id) VALUES ('Direccion Local 2.1', 1, 'Observaciones Local 2.1', 2);
/* EMPLEADOS */
INSERT INTO empleados (apellidos, celular, direccion, email, estado, fecha_registro, nombres, nro_documento, password, username, empresa_id) VALUES ('apellidoAdmin', 98989898, 'No disponible', 'admin@gmail.com', 1, '2019-08-19', 'nombreAdmin', 11111111, '$2a$10$DCDKy8zxMgnhv7BoTopltOq2qlT1icvmk/JMzT51BWmdWcKGOFFjS', 'admin', 1);
INSERT INTO empleados (apellidos, celular, direccion, email, estado, fecha_registro, nombres, nro_documento, password, username, empresa_id) VALUES ('apellidoEmpleado', 87878787, 'Av. Lima 123', 'empleado@gmail.com', 1, '2019-08-19', 'nombreEmpleado', 22222222, '$2a$10$QWZkTkJyiKy55FcJwk1yAeUDTdAx7U4wifIaZT9kbicQmhbGvyhpa', 'empleado', 1);
INSERT INTO empleados (apellidos, celular, direccion, email, estado, fecha_registro, nombres, nro_documento, password, username, empresa_id) VALUES ('apellidoEmpleada', 76767676, 'Av. Lima 456', 'empleada@gmail.com', 1, '2019-08-19', 'nombreEmpleada', 33333333, '$2a$10$rE9nFn/v1kcabjkQW/QABeuKGHirYoLWjcG.WHDVSfth7bhZM7JWi', 'empleada', 2);
/* EMPLEADOS-ROLES */
INSERT INTO empleados_roles (empleado_id, rol_id) VALUES (1,1);
INSERT INTO empleados_roles (empleado_id, rol_id) VALUES (2,3);
INSERT INTO empleados_roles (empleado_id, rol_id) VALUES (3,3);
/* LIBROS */
INSERT INTO libros (autor, categoria, descripcion, estado, fecha_registro, fecha_publicacion, titulo) VALUES ('Autor Libro 1', 'Fantasía', 'Descripcion Libro 1', 1, '2012-04-11', '2019-05-23', 'El camino de los reyes');
INSERT INTO libros (autor, categoria, descripcion, estado, fecha_registro, fecha_publicacion, titulo) VALUES ('Autor Libro 2', 'Novela Corta', 'Descripcion Libro 2', 1, '2014-04-11', '2019-06-13', 'Las noches blancas');
/* LOCALES-LIBROS */
INSERT INTO locales_libros (local_id, libro_id) VALUES (1, 1);
INSERT INTO locales_libros (local_id, libro_id) VALUES (1, 2);
INSERT INTO locales_libros (local_id, libro_id) VALUES (2, 1);
INSERT INTO locales_libros (local_id, libro_id) VALUES (2, 2);
/* PRESTAMOS */
INSERT INTO prestamos (devolucion, fecha_despacho, observaciones, empleado_id, libro_id, usuario_id) VALUES (0, '2019-08-19', 'Observaciones prèstamo 1', 1, 1, 1);
INSERT INTO prestamos (devolucion, fecha_despacho, observaciones, empleado_id, libro_id, usuario_id) VALUES (0, '2019-07-16', 'Observaciones prèstamo 1', 2, 2, 2);