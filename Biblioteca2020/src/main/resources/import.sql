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
INSERT INTO usuarios (apellidos, celular, direccion, email, estado, fecha_registro, foto_usuario, nombres, nro_documento, password, username) VALUES ('Apellido Usuario', 656565656, 'Av. Lima 789', 'usuario@gmail.com', 1, '2019-08-19', 'no-image.jpg', 'Nombre Usuario', 44444444, '$2a$10$VKuMvd4UAx7ZJbsZLOQ8/u1z44jmL/MxZzhtMKL8xubi4iqKpIRyO', 'usuario');
INSERT INTO usuarios (apellidos, celular, direccion, email, estado, fecha_registro, foto_usuario, nombres, nro_documento, password, username) VALUES ('Apellido Usuaria', 545454545, 'Av. Arequipa 101', 'usuaria@gmail.com', 1, '2019-08-19', 'no-image.jpg', 'Nombre Usuaria', 55555555, '$2a$10$RgVfaCIlKua4ogo4rsBfJeE5hqGT5aoEjZMyOPop4u61/KVSZeFWK', 'usuaria');
INSERT INTO usuarios (apellidos, celular, direccion, email, estado, fecha_registro, foto_usuario, nombres, nro_documento, password, username) VALUES ('Medina Chinga', 983489303, 'Jr. Los Oligistos 2339', 'edi@live.it', 1, '2020-04-13', 'no-image.jpg', 'Edinson', 47111025, '$2a$10$86PKoDGKFkbtmEXQuUaQbuJ0vzRGY/tLyUiOsu9VExoajCFsqV5yC', 'edmech');
/* CONFIRMATION_TOKEN */
INSERT INTO confirmation_token (confirmation_token, created_date, usuario_id) VALUES ('XL2UR9VI6DxHBLXj7vB0QLhqopnqaN4V4LZwu3fgU4lKu11mps', '2019-12-11', 1);
INSERT INTO confirmation_token (confirmation_token, created_date, usuario_id) VALUES ('nOO8qA9B0KcL9lWYuGBBOACHCbFZatR43Jx933FyDqKJHx46O4', '2019-12-11', 2);
INSERT INTO confirmation_token (confirmation_token, created_date, usuario_id) VALUES ('57af5545-15f1-468b-9bb0-cfd46fb41822', '2020-04-13', 3);
/* ROLES/AUTHORITIES */
INSERT INTO authorities (authority) VALUES ('ROLE_SYSADMIN');
INSERT INTO authorities (authority) VALUES ('ROLE_ADMIN');
INSERT INTO authorities (authority) VALUES ('ROLE_PRUEBA');
INSERT INTO authorities (authority) VALUES ('ROLE_EMPLEADO');
INSERT INTO authorities (authority) VALUES ('ROLE_USER');
/* USUARIOS-ROLES */
INSERT INTO usuarios_roles (usuario_id, rol_id) VALUES (1, 5);
INSERT INTO usuarios_roles (usuario_id, rol_id) VALUES (2, 5);
INSERT INTO usuarios_roles (usuario_id, rol_id) VALUES (3, 5);
/* EMPRESAS */
INSERT INTO empresas (direccion, estado, razon_social, ruc) VALUES ('Av. Lima 456', 1, 'Biblioteca2020', 11111111111);
/* LOCALES */
INSERT INTO locales (direccion, estado, observaciones, empresa_id) VALUES ('Av. Lima 456', 1, 'Sede central de Biblioteca2020', 1);
INSERT INTO locales (direccion, estado, observaciones, empresa_id) VALUES ('Av. Tacna 322', 1, 'Local anexo de Biblioteca2020 nro. 1', 1);
INSERT INTO locales (direccion, estado, observaciones, empresa_id) VALUES ('Av. Próceres De La Independencia 217', 1, 'Local anexo de Biblioteca2020 nro. 2', 1);
/* EMPLEADOS */
INSERT INTO empleados (apellidos, celular, direccion, email, estado, foto_empleado, fecha_registro, nombres, nro_documento, password, username, local_id) VALUES ('Prueba Local 2', 000000000, 'No disponible', 'biblioteca.prueba_local_2@gmail.com', 1, 'no-image.jpg', '2020-04-08', 'Prueba Local 2', 00000000, 'Prueba1', 'Prueba Local 2', 2);
INSERT INTO empleados (apellidos, celular, direccion, email, estado, foto_empleado, fecha_registro, nombres, nro_documento, password, username, local_id) VALUES ('Apellido Sysadmin', 989898989, 'No disponible', 'biblioteca.sysadmin@gmail.com', 1, 'no-image.jpg', '2019-09-10', 'Nombre Sysadmin', 10101010, '$2a$10$UCmhWuEPcAK3Es/iGP2lXeW4BWz1/ysyxl8En5VKZUgTBXjCNRNwy', 'sysadmin', 1);
INSERT INTO empleados (apellidos, celular, direccion, email, estado, foto_empleado, fecha_registro, nombres, nro_documento, password, username, local_id) VALUES ('Apellido Admin Local 2', 909090909, 'No disponible', 'biblioteca.admin_local_2@gmail.com', 1, 'tilin.jpg', '2019-08-19', 'Nombre Admin Local 2', 10000000, '$2a$10$afppFumVgceXUwvjfEJc5eTNOBCDs11oMMHjHJUOIXQLQ3DUjkr.a', 'adminlocal2', 2);
INSERT INTO empleados (apellidos, celular, direccion, email, estado, foto_empleado, fecha_registro, nombres, nro_documento, password, username, local_id) VALUES ('Prueba Local 3', 000000001, 'Direccion Prueba Local 2', 'biblioteca.prueba_local_3@gmail.com', 1, 'no-image.jpg', '2020-04-08', 'Prueba Local 3', 00000001, 'Prueba2', 'Prueba Local 3', 3);
INSERT INTO empleados (apellidos, celular, direccion, email, estado, foto_empleado, fecha_registro, nombres, nro_documento, password, username, local_id) VALUES ('Apellido Admin Local 3', 898989898, 'No disponible', 'biblioteca.admin_local_3@gmail.com', 1, 'no-image.jpg', '2019-08-19', 'Nombre Admin Local 3', 10000001, '$2a$10$DCDKy8zxMgnhv7BoTopltOq2qlT1icvmk/JMzT51BWmdWcKGOFFjS', 'adminlocal3', 3);
INSERT INTO empleados (apellidos, celular, direccion, email, estado, foto_empleado, fecha_registro, nombres, nro_documento, password, username, local_id) VALUES ('Apellido Empleado', 878787878, 'Av. El Sol 130', 'biblioteca.empleado_local_2@gmail.com', 1, 'no-image.jpg', '2019-08-19', 'Nombre Empleado Local 2', 22222222, '$2a$10$5QvXvq.NIvcczvb9e2gicuVcAjJBfmNg0A317un5O8.0YKGQfUcoC', 'empleadolocal2', 2);
INSERT INTO empleados (apellidos, celular, direccion, email, estado, foto_empleado, fecha_registro, nombres, nro_documento, password, username, local_id) VALUES ('Apellido Empleada', 767676767, 'Av. Los Duraznos 653', 'biblioteca.empleada_local_3@gmail.com', 1, 'no-image.jpg', '2019-08-19', 'Nombre Empleada Local 3', 33333333, '$2a$10$g9VrZ8dDu29kFFa4E0fPqOtpYGVdCfHbp8.5nTo6ufw53re7TD3b.', 'empleadalocal3', 3);
/* EMPLEADOS-ROLES */
INSERT INTO empleados_roles (empleado_id, rol_id) VALUES (1,3);
INSERT INTO empleados_roles (empleado_id, rol_id) VALUES (2,1);
INSERT INTO empleados_roles (empleado_id, rol_id) VALUES (3,2);
INSERT INTO empleados_roles (empleado_id, rol_id) VALUES (4,3);
INSERT INTO empleados_roles (empleado_id, rol_id) VALUES (5,2);
INSERT INTO empleados_roles (empleado_id, rol_id) VALUES (6,4);
INSERT INTO empleados_roles (empleado_id, rol_id) VALUES (7,4);
/* LIBROS */
INSERT INTO libros (autor, descripcion, estado, fecha_registro, fecha_publicacion, stock, foto_libro, titulo, categoria_id, local_id) VALUES ('Brandon Sanderson', 'En las Llanuras Quebradas se libra una guerra sin sentido. Kaladin ha sido sometido a la esclavitud, mientras diez ejércitos luchan por separado contra un solo enemigo...', 1, '2012-04-11', '2019-05-23', 100, 'el-camino-de-los-reyes.jpg', 'El camino de los reyes', 2, 2);
INSERT INTO libros (autor, descripcion, estado, fecha_registro, fecha_publicacion, stock, foto_libro, titulo, categoria_id, local_id) VALUES ('Fiódor Dostoyevski', 'Un joven soñador imagina constantemente su vejez solitaria. Una noche por las calles de San Petersburgo se encuentra con una joven, Nástenka. Él nunca había hablado con mujeres y mucho menos se había enamorado, pero hay algo de ella que le hechiza...', 1, '2014-04-11', '2019-06-13', 100, 'las-noches-blancas.jpg', 'Las noches blancas', 3, 2);
INSERT INTO libros (autor, descripcion, estado, fecha_registro, fecha_publicacion, stock, foto_libro, titulo, categoria_id, local_id) VALUES ('Fiódor Dostoyevski', 'Un joven soñador imagina constantemente su vejez solitaria. Una noche por las calles de San Petersburgo se encuentra con una joven, Nástenka. Él nunca había hablado con mujeres y mucho menos se había enamorado, pero hay algo de ella que le hechiza...', 1, '2014-04-11', '2019-06-13', 100, 'las-noches-blancas.jpg', 'Las noches blancas', 3, 3);
INSERT INTO libros (autor, descripcion, estado, fecha_registro, fecha_publicacion, stock, foto_libro, titulo, categoria_id, local_id) VALUES ('Brandon Sanderson', 'Palabras radiantes es la continuación de El camino de los reyes, la aclamada primera parte de la serie en diez volúmenes "The Stormlight Archive". En ella retrocedemos seis años en el tiempo, cuando un asesino, entre cuyos primeros objetivos se halla Dalinar, mata al rey alezi. Kaladin está al mando de los guardaespaldas reales, un puesto controvertido por su baja condición, y debe proteger al rey y a Dalinar, y al mismo tiempo dominar, en secreto, los nuevos y extraordinarios poderes vinculados a sus honorspren. Shallan tiene la misión de impedir el fin de las Desolaciones. Las Llanuras Quebradas poseen la respuesta; en ellas los parshendi están convencidos, gracias a su líder, de arriesgarlo todo en una apuesta desesperada...', 1, '2015-01-11', '2020-04-07', 100, 'palabras-radiantes.jpg', 'Palabras Radiantes', 2, 2);
INSERT INTO libros (autor, descripcion, estado, fecha_registro, fecha_publicacion, stock, foto_libro, titulo, categoria_id, local_id) VALUES ('Brandon Sanderson', 'La humanidad se enfrenta a una nueva Desolación con el regreso de los Portadores del Vacío, un enemigo tan grande en número como en sed de venganza. La victoria fugaz de los ejércitos alezi de Dalinar Kholin ha tenido consecuencias: el enemigo parshendi ha convocado la violenta tormenta eterna, que arrasa el mundo y hace que los hasta ahora pacíficos parshmenios descubran con horror que llevan un milenio esclavizados por los humanos. Al mismo tiempo, en una desesperada huida para alertar a su familia de la amenaza, Kaladin se pregunta si la repentina ira de los parshmenios está justificada. Entretanto, en la torre de la ciudad de Urithiru, a salvo de la tormenta, Shallan Davar investiga las maravillas de la antigua fortaleza de los Caballeros Radiantes y desentierra oscuros secretos que acechan en las profundidades. Dalinar descubre entonces que su sagrada misión de unificar su tierra natal de Alezkar era corta de miras. A menos que todas las naciones sean capaces de unirse y dejar de lado el pasado sangriento de Dalinar, ni siquiera la restauración de los Caballeros Radiantes conseguirá impedir el fin de la civilización.', 1, '2018-01-11', '2020-04-07', 100, 'juramentada.jpg', 'Juramentada', 2, 2);
INSERT INTO libros (autor, descripcion, estado, fecha_registro, fecha_publicacion, stock, foto_libro, titulo, categoria_id, local_id) VALUES ('Brandon Sanderson', 'Hace tres años, Lift le pidió a una diosa que no la dejara envejecer, un deseo que ella creía que le fue concedido. Ahora, en Danzante del Filo, la recién nacida Caballero Radiante encuentra que el tiempo no se detiene para nadie. A pesar de que el joven emperador Azish le concedió refugio de un verdugo al que sólo conoce como Oscuridad, la vida en la corte está sofocando al espíritu libre que es Lift, que no puede evitar dirigirse a Yeddaw cuando escucha que la inexorable Oscuridad está cazando gente como ella con poderes que empiezan a mostrarse.Los oprimidos en Yeddaw no tienen campeón, y Lift sabe que debe aprovechar esta increíble responsabilidad.', 1, '2016-01-11', '2020-04-07', 100, 'danzante-del-filo.jpg', 'Danzante Del Filo', 2, 2);
INSERT INTO libros (autor, descripcion, estado, fecha_registro, fecha_publicacion, stock, foto_libro, titulo, categoria_id, local_id) VALUES ('Brandon Sanderson', 'Hace años, el rey de Idris firmó un tratado con el reino de Hallandren. El rey Dedelin enviaría a su hija mayor, Vivenna, para casarse con Susebron, el rey-dios de Hallandren. Vivenna ha sido entrenada durante toda su vida para ser una novia adecuada para Susebron y así cumplir con su deber y ayudar a forjar una paz estable entre Hallandren e Idris. Ese era el plan hasta que el rey de Idris envía a su hija Siri, desobediente e independiente, en lugar de Vivenna. Siri intenta encontrar su lugar en la corte de Susebron, pero mientras lo intenta descubre la verdad oculta sobre el rey-dios. En Idris, su hermana Vivenna está preocupada y teme que Siri no esté preparada para esa nueva vida, por lo que decide viajar a Hallandren. Allí se reúne con la gente de Idris que trabaja en la capital, T’Telir, y comienza una nueva vida de espionaje y sabotaje. El plan de Vivenna es rescatar a Siri, aunque tal vez esta ni necesite ni desee ser salvada.', 1, '2016-01-11', '2020-04-07', 100, 'el-aliento-de-los-dioses.jpg', 'El Aliento De Los Dioses', 2, 2);
/* PRESTAMOS */
/*INSERT INTO prestamos (devolucion, fecha_despacho, fecha_devolucion, observaciones, empleado_id, libro_id, usuario_id) VALUES (0, '2019-08-19', '2020-03-10', 'El libro: El camino de los reyes, ha sido programado para su devolución el día Miércoles 18 de Marzo 2020, por el empleado: Nombre Admin Local 2, Apellido Admin Local 2 (código empleado 3) al usuario: Nombre Usuario, Apellido Usuario (código usuario 1)', 3, 1, 1);*/
/*INSERT INTO prestamos (devolucion, fecha_despacho, fecha_devolucion, observaciones, empleado_id, libro_id, usuario_id) VALUES (0, '2019-07-16', '2020-03-10', 'El libro: Las Noches blancas, ha sido programado para su devolución el día Viernes 10 de Abril 2020, por el empleado: Nombre Admin Local 3, Apellido Admin Local 3 (código empleado 5) al usuario: Nombre Usuaria, Apellido Usuaria (código usuario 2)', 5, 3, 2);*/