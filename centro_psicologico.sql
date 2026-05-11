
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
DROP TABLE IF EXISTS `categoria_gasto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categoria_gasto` (
  `id_categoria_gasto` bigint NOT NULL AUTO_INCREMENT,
  `estado` bit(1) NOT NULL,
  `descripcion` varchar(150) DEFAULT NULL,
  `nombre` varchar(100) NOT NULL,
  PRIMARY KEY (`id_categoria_gasto`),
  UNIQUE KEY `UKpyh4ovvkbr3tqcqmpgk44d2ic` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `categoria_gasto` WRITE;
/*!40000 ALTER TABLE `categoria_gasto` DISABLE KEYS */;
INSERT INTO `categoria_gasto` VALUES (1,_binary '','Pago mensual del local','Alquiler');
INSERT INTO `categoria_gasto` VALUES (2,_binary '','Luz, agua, internet y telefonía','Servicios');
INSERT INTO `categoria_gasto` VALUES (3,_binary '','Publicidad y campañas','Marketing');
INSERT INTO `categoria_gasto` VALUES (4,_binary '','Útiles, papelería y recursos clínicos','Materiales');
INSERT INTO `categoria_gasto` VALUES (5,_binary '','Limpieza y reparaciones','Mantenimiento');
/*!40000 ALTER TABLE `categoria_gasto` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `cita`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cita` (
  `id_cita` bigint NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `hora_fin` time(6) NOT NULL,
  `observacion` varchar(255) DEFAULT NULL,
  `motivo_consulta` varchar(255) DEFAULT NULL,
  `hora_inicio` time(6) NOT NULL,
  `estado_cita` enum('ATENDIDA','CANCELADA','NO_ASISTIO','PROGRAMADA','REPROGRAMADA') NOT NULL,
  `id_paciente` bigint NOT NULL,
  `id_psicologo` bigint NOT NULL,
  `id_servicio` bigint NOT NULL,
  `pagado` bit(1) NOT NULL,
  `monto_pagado` decimal(38,2) DEFAULT NULL,
  `fecha_pago` date DEFAULT NULL,
  `metodo_pago` varchar(50) DEFAULT NULL,
  `codigo_operacion` varchar(100) DEFAULT NULL,
  `fecha_hora_pago` datetime(6) DEFAULT NULL,
  `observacion_pago` varchar(255) DEFAULT NULL,
  `registrado_por` varchar(120) DEFAULT NULL,
  `estado_pago` varchar(30) DEFAULT NULL,
  `saldo_pendiente` decimal(38,2) DEFAULT NULL,
  `monto_total` decimal(38,2) DEFAULT NULL,
  PRIMARY KEY (`id_cita`),
  KEY `FK7fljkhue1c7r80b4li70f6fh3` (`id_paciente`),
  KEY `FKdabo2f2md0epcragtcaeqhtt3` (`id_psicologo`),
  KEY `FK68785k2hlh38mhiq3u2ny82p4` (`id_servicio`),
  CONSTRAINT `FK68785k2hlh38mhiq3u2ny82p4` FOREIGN KEY (`id_servicio`) REFERENCES `servicio` (`id_servicio`),
  CONSTRAINT `FK7fljkhue1c7r80b4li70f6fh3` FOREIGN KEY (`id_paciente`) REFERENCES `paciente` (`id_paciente`),
  CONSTRAINT `FKdabo2f2md0epcragtcaeqhtt3` FOREIGN KEY (`id_psicologo`) REFERENCES `psicologo` (`id_psicologo`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `cita` WRITE;
/*!40000 ALTER TABLE `cita` DISABLE KEYS */;
INSERT INTO `cita` VALUES (1,'2026-05-03','10:00:00.000000','Primera sesión','Ansiedad laboral','09:00:00.000000','ATENDIDA',1,1,1,_binary '',80.00,'2026-05-09','EFECTIVO','','2026-05-09 07:55:27.340447','','Rosa Paredes',NULL,NULL,NULL);
INSERT INTO `cita` VALUES (2,'2026-05-03','12:00:00.000000','Sesión atendida','Problemas de pareja','10:30:00.000000','ATENDIDA',2,2,2,_binary '',120.00,'2026-05-03','YAPE','YAPE-874521','2026-05-04 02:56:38.573404','Pago confirmado por recepción','Rosa Paredes',NULL,NULL,NULL);
INSERT INTO `cita` VALUES (3,'2026-05-03','14:00:00.000000','Paciente canceló','Evaluación inicial','12:30:00.000000','CANCELADA',3,1,3,_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO `cita` VALUES (4,'2026-05-03','16:00:00.000000','Acude con apoderado','Conducta infantil','15:00:00.000000','NO_ASISTIO',4,3,4,_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO `cita` VALUES (5,'2026-05-04','10:00:00.000000','Seguimiento','Estrés académico','09:00:00.000000','PROGRAMADA',5,1,1,_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO `cita` VALUES (6,'2026-05-04','12:30:00.000000','Primera sesión','Conflicto familiar','11:00:00.000000','PROGRAMADA',6,2,5,_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO `cita` VALUES (7,'2026-04-30','11:00:00.000000','Mejor evolución','Seguimiento ansiedad','10:00:00.000000','ATENDIDA',1,1,1,_binary '',80.00,'2026-05-03','EFECTIVO','EFECTIVO','2026-05-04 02:56:38.573404','Pago en caja','Rosa Paredes',NULL,NULL,NULL);
INSERT INTO `cita` VALUES (8,'2026-04-28','13:30:00.000000','No asistió','Terapia de pareja','12:00:00.000000','NO_ASISTIO',2,2,2,_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO `cita` VALUES (9,'2026-04-23','17:30:00.000000','Proceso completado','Evaluación psicológica','16:00:00.000000','ATENDIDA',3,1,3,_binary '',150.00,'2026-05-03','TRANSFERENCIA','BCP-99887766','2026-05-04 02:56:38.573404','Transferencia verificada','Admin Principal',NULL,NULL,NULL);
INSERT INTO `cita` VALUES (10,'2026-04-18','10:30:00.000000','Participación activa','Terapia infantil','09:30:00.000000','ATENDIDA',4,3,4,_binary '',100.00,'2026-05-03','PLIN','PLIN-552211','2026-05-04 02:56:38.573404','Pago por apoderado','Rosa Paredes',NULL,NULL,NULL);
INSERT INTO `cita` VALUES (11,'2026-05-20','10:00:00.000000','Me tiene totalmente inquieto','Ansiedad','09:00:00.000000','PROGRAMADA',3,1,1,_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO `cita` VALUES (12,'2026-05-15','18:00:00.000000','observacion','motivo x','16:30:00.000000','PROGRAMADA',5,1,3,_binary '\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO `cita` VALUES (13,'2026-05-12','18:00:00.000000','atencion rapida','TERAPIA','17:00:00.000000','PROGRAMADA',6,3,4,_binary '\0',50.00,'2026-05-10','YAPE','514','2026-05-10 06:07:58.984710','Esperemos la buena atención','Admin Principal','PARCIAL',50.00,100.00);
INSERT INTO `cita` VALUES (14,'2026-05-29','16:30:00.000000','Cita creada desde pre-reserva #6','Terapia de Pareja','15:00:00.000000','PROGRAMADA',9,1,2,_binary '\0',24.00,'2026-05-10','YAPE','464','2026-05-11 03:55:14.306782','Adelanto validado desde pre-reserva','Admin Principal','PARCIAL',96.00,120.00);
/*!40000 ALTER TABLE `cita` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `disponibilidad_psicologo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `disponibilidad_psicologo` (
  `id_disponibilidad` bigint NOT NULL AUTO_INCREMENT,
  `estado` bit(1) NOT NULL,
  `dia_semana` enum('FRIDAY','MONDAY','SATURDAY','SUNDAY','THURSDAY','TUESDAY','WEDNESDAY') NOT NULL,
  `hora_fin` time(6) NOT NULL,
  `hora_inicio` time(6) NOT NULL,
  `id_psicologo` bigint NOT NULL,
  PRIMARY KEY (`id_disponibilidad`),
  KEY `FKi1hde5eonfesyxkmnxlw5oh4a` (`id_psicologo`),
  CONSTRAINT `FKi1hde5eonfesyxkmnxlw5oh4a` FOREIGN KEY (`id_psicologo`) REFERENCES `psicologo` (`id_psicologo`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `disponibilidad_psicologo` WRITE;
/*!40000 ALTER TABLE `disponibilidad_psicologo` DISABLE KEYS */;
INSERT INTO `disponibilidad_psicologo` VALUES (1,_binary '','MONDAY','13:00:00.000000','09:00:00.000000',1);
INSERT INTO `disponibilidad_psicologo` VALUES (2,_binary '','WEDNESDAY','13:00:00.000000','09:00:00.000000',1);
INSERT INTO `disponibilidad_psicologo` VALUES (3,_binary '','FRIDAY','19:00:00.000000','15:00:00.000000',1);
INSERT INTO `disponibilidad_psicologo` VALUES (4,_binary '','TUESDAY','13:00:00.000000','09:00:00.000000',2);
INSERT INTO `disponibilidad_psicologo` VALUES (5,_binary '','THURSDAY','18:00:00.000000','14:00:00.000000',2);
INSERT INTO `disponibilidad_psicologo` VALUES (6,_binary '','SATURDAY','12:00:00.000000','09:00:00.000000',2);
INSERT INTO `disponibilidad_psicologo` VALUES (7,_binary '','MONDAY','18:00:00.000000','14:00:00.000000',3);
INSERT INTO `disponibilidad_psicologo` VALUES (8,_binary '','TUESDAY','18:00:00.000000','14:00:00.000000',3);
INSERT INTO `disponibilidad_psicologo` VALUES (9,_binary '','FRIDAY','13:00:00.000000','09:00:00.000000',3);
/*!40000 ALTER TABLE `disponibilidad_psicologo` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `gasto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gasto` (
  `id_gasto` bigint NOT NULL AUTO_INCREMENT,
  `estado` bit(1) NOT NULL,
  `monto` decimal(38,2) NOT NULL,
  `fecha` date NOT NULL,
  `descripcion` varchar(150) NOT NULL,
  `responsable` varchar(100) DEFAULT NULL,
  `id_categoria_gasto` bigint DEFAULT NULL,
  PRIMARY KEY (`id_gasto`),
  KEY `FKsddhx3qwetqp9bwnl023ggl5k` (`id_categoria_gasto`),
  CONSTRAINT `FKsddhx3qwetqp9bwnl023ggl5k` FOREIGN KEY (`id_categoria_gasto`) REFERENCES `categoria_gasto` (`id_categoria_gasto`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `gasto` WRITE;
/*!40000 ALTER TABLE `gasto` DISABLE KEYS */;
INSERT INTO `gasto` VALUES (1,_binary '',1200.00,'2026-05-01','Alquiler del consultorio','Admin Principal',1);
INSERT INTO `gasto` VALUES (2,_binary '',180.00,'2026-05-03','Pago de internet','Rosa Paredes',2);
INSERT INTO `gasto` VALUES (3,_binary '',220.00,'2026-05-05','Servicio de luz','Rosa Paredes',2);
INSERT INTO `gasto` VALUES (4,_binary '',350.00,'2026-05-08','Publicidad en redes sociales','Admin Principal',3);
INSERT INTO `gasto` VALUES (5,_binary '',140.00,'2026-05-10','Material para evaluaciones','Rosa Paredes',4);
INSERT INTO `gasto` VALUES (6,_binary '',250.00,'2026-05-12','Limpieza del local','Admin Principal',5);
/*!40000 ALTER TABLE `gasto` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `historia_clinica`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `historia_clinica` (
  `id_historia` bigint NOT NULL AUTO_INCREMENT,
  `estado` bit(1) NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `diagnostico` text,
  `evolucion` text,
  `psicologo` varchar(120) DEFAULT NULL,
  `motivo` varchar(255) DEFAULT NULL,
  `recomendaciones` text,
  `id_paciente` bigint NOT NULL,
  PRIMARY KEY (`id_historia`),
  KEY `FKgoiecadrbrvupi3bns92s3dxp` (`id_paciente`),
  CONSTRAINT `FKgoiecadrbrvupi3bns92s3dxp` FOREIGN KEY (`id_paciente`) REFERENCES `paciente` (`id_paciente`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `historia_clinica` WRITE;
/*!40000 ALTER TABLE `historia_clinica` DISABLE KEYS */;
INSERT INTO `historia_clinica` VALUES (1,_binary '','2026-05-04 02:56:38.703434','Ansiedad leve','Paciente refiere mejora en manejo del estrés.','Luis Gomez','Ansiedad laboral','Aplicar técnicas de respiración diaria.',1);
INSERT INTO `historia_clinica` VALUES (2,_binary '','2026-05-04 02:56:38.712399','Dificultad de comunicación','Se identifican patrones de discusión repetitivos.','Maria Torres','Conflictos de pareja','Realizar ejercicios de escucha activa.',2);
INSERT INTO `historia_clinica` VALUES (3,_binary '','2026-05-04 02:56:38.725402','Evaluación inicial','Paciente colaborador durante evaluación.','Luis Gomez','Evaluación psicológica','Continuar con entrevistas clínicas.',3);
INSERT INTO `historia_clinica` VALUES (4,_binary '','2026-05-04 02:56:38.734431','Dificultades de regulación emocional','Menor participa con acompañamiento.','Sofia Herrera','Conducta infantil','Trabajo conjunto con familia.',4);
/*!40000 ALTER TABLE `historia_clinica` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `ingreso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ingreso` (
  `id_ingreso` bigint NOT NULL AUTO_INCREMENT,
  `estado` bit(1) NOT NULL,
  `monto` decimal(38,2) NOT NULL,
  `fecha` date NOT NULL,
  `descripcion` varchar(150) NOT NULL,
  `metodo_pago` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id_ingreso`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `ingreso` WRITE;
/*!40000 ALTER TABLE `ingreso` DISABLE KEYS */;
INSERT INTO `ingreso` VALUES (1,_binary '',120.00,'2026-05-03','Pago de cita #2 - Ana Lopez | Método: YAPE | Op: YAPE-874521','YAPE');
INSERT INTO `ingreso` VALUES (2,_binary '',80.00,'2026-04-30','Pago de cita #7 - Carlos Ramirez | Método: EFECTIVO','EFECTIVO');
INSERT INTO `ingreso` VALUES (3,_binary '',150.00,'2026-04-23','Pago de cita #9 - Rodrigo Cornejo | Método: TRANSFERENCIA | Op: BCP-99887766','TRANSFERENCIA');
INSERT INTO `ingreso` VALUES (4,_binary '',100.00,'2026-04-18','Pago de cita #10 - Rene Juarez | Método: PLIN | Op: PLIN-552211','PLIN');
INSERT INTO `ingreso` VALUES (5,_binary '',850.00,'2026-04-27','Taller grupal de manejo de estrés','TRANSFERENCIA');
INSERT INTO `ingreso` VALUES (6,_binary '',600.00,'2026-05-01','Evaluación psicológica externa','TARJETA');
INSERT INTO `ingreso` VALUES (7,_binary '',1200.00,'2026-04-29','Paquete mensual de terapia individual','YAPE');
INSERT INTO `ingreso` VALUES (8,_binary '',1500.00,'2026-04-25','Convenio empresarial - sesiones psicológicas','TRANSFERENCIA');
INSERT INTO `ingreso` VALUES (9,_binary '',2500.00,'2026-05-03','Convenio empresarial mensual - programa psicológico','TRANSFERENCIA');
INSERT INTO `ingreso` VALUES (10,_binary '',1800.00,'2026-05-03','Paquete mensual de terapia individual','YAPE');
INSERT INTO `ingreso` VALUES (11,_binary '',1200.00,'2026-05-03','Taller grupal de manejo de estrés','TRANSFERENCIA');
INSERT INTO `ingreso` VALUES (12,_binary '',900.00,'2026-05-03','Evaluación psicológica corporativa','TARJETA');
INSERT INTO `ingreso` VALUES (13,_binary '',80.00,'2026-05-09','Pago de cita #1 - Carlos Ramirez | Método: EFECTIVO','EFECTIVO');
INSERT INTO `ingreso` VALUES (14,_binary '',50.00,'2026-05-10','Pago de cita #13 - Mario Vargas | Método: YAPE | Estado pago: PARCIAL | Op: 514','YAPE');
INSERT INTO `ingreso` VALUES (15,_binary '',24.00,'2026-05-10','Adelanto pre-reserva convertida a cita #14','YAPE');
/*!40000 ALTER TABLE `ingreso` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `interesado`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interesado` (
  `id_interesado` bigint NOT NULL AUTO_INCREMENT,
  `centro` varchar(150) DEFAULT NULL,
  `fecha_registro` datetime(6) NOT NULL,
  `porcentaje_descuento` double DEFAULT NULL,
  `correo` varchar(120) NOT NULL,
  `precio_estimado` double DEFAULT NULL,
  `nombres` varchar(120) NOT NULL,
  `mensaje` varchar(700) DEFAULT NULL,
  `telefono` varchar(30) DEFAULT NULL,
  `cantidad_psicologos` int DEFAULT NULL,
  `servicio_interes` varchar(150) DEFAULT NULL,
  `estado` varchar(30) NOT NULL,
  `modalidad` varchar(50) DEFAULT NULL,
  `tipo_atencion` varchar(150) DEFAULT NULL,
  `fecha_preferida` varchar(20) DEFAULT NULL,
  `hora_preferida` varchar(20) DEFAULT NULL,
  `psicologo_preferido` varchar(150) DEFAULT NULL,
  `monto_adelanto` double DEFAULT NULL,
  `porcentaje_adelanto` double DEFAULT NULL,
  `codigo_operacion` varchar(100) DEFAULT NULL,
  `metodo_pago` varchar(50) DEFAULT NULL,
  `estado_pago` varchar(40) DEFAULT NULL,
  `precio_servicio` double DEFAULT NULL,
  `id_servicio` bigint DEFAULT NULL,
  `id_psicologo` bigint DEFAULT NULL,
  `id_cita` bigint DEFAULT NULL,
  PRIMARY KEY (`id_interesado`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `interesado` WRITE;
/*!40000 ALTER TABLE `interesado` DISABLE KEYS */;
INSERT INTO `interesado` VALUES (1,NULL,'2026-05-10 19:40:47.000593',NULL,'royerhancco@gmail.com',NULL,'Royer Jimenez Hancco','necesito aclarar mis ideas','941221483',NULL,NULL,'PRE_RESERVADO','Presencial','Evaluación psicológica',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO `interesado` VALUES (2,NULL,'2026-05-10 20:03:07.507351',NULL,'luissuarez15@gmail.com',NULL,'Luis Suarez','Aclaracion de ideas','954653211',NULL,NULL,'CONTACTADO','Presencial','Evaluación psicológica',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO `interesado` VALUES (3,NULL,'2026-05-10 21:42:30.617397',NULL,'elmerperez93@gmail.com',NULL,'Elmer Diaz Perez','Deseo consultar disponibilidad para una atención con Luis Gomez en el horario de 10:30.','954321654',NULL,NULL,'AGENDADO','Presencial','Terapia de Pareja',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO `interesado` VALUES (4,NULL,'2026-05-10 22:01:53.519081',NULL,'jackmen15@gmail.com',NULL,'Jack Mendez','Deseo pre-reservar una atención con Luis Gomez para el 2026-05-18 a las 11:00.','952311477',NULL,NULL,'NUEVO','Presencial','Terapia Individual','2026-05-18','11:00','Luis Gomez',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO `interesado` VALUES (5,NULL,'2026-05-11 00:24:14.293221',NULL,'Eusres92@gmail.com',NULL,'Eustaquio Flores','Deseo pre-reservar una atención con Luis Gomez para el 2026-05-22 a las 16:30.','963225874',NULL,NULL,'PRE_RESERVADO','Presencial','Orientación Familiar','2026-05-22','16:30','Luis Gomez',26,20,'964','YAPE','PAGO_VALIDADO',130,NULL,NULL,NULL);
INSERT INTO `interesado` VALUES (6,NULL,'2026-05-11 03:49:14.443540',NULL,'romanreyes05@gmail.com',NULL,'Roman Reyes','Deseo pre-reservar una atención con Luis Gomez para el 2026-05-29 a las 15:00.','9994542',NULL,NULL,'AGENDADO','Presencial','Terapia de Pareja','2026-05-29','15:00','Luis Gomez',24,20,'464','YAPE','PAGO_VALIDADO',120,2,1,NULL);
/*!40000 ALTER TABLE `interesado` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `notificacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notificacion` (
  `id_notificacion` bigint NOT NULL AUTO_INCREMENT,
  `estado` bit(1) NOT NULL,
  `fecha_creacion` datetime(6) NOT NULL,
  `mensaje` varchar(500) NOT NULL,
  `leido` bit(1) NOT NULL,
  `correo_destino` varchar(120) DEFAULT NULL,
  `rol_destino` varchar(50) NOT NULL,
  `titulo` varchar(120) NOT NULL,
  `tipo` varchar(50) NOT NULL,
  PRIMARY KEY (`id_notificacion`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `notificacion` WRITE;
/*!40000 ALTER TABLE `notificacion` DISABLE KEYS */;
INSERT INTO `notificacion` VALUES (1,_binary '','2026-05-09 07:55:27.390154','Se registró el pago de la cita #1 del paciente Carlos Ramirez por S/ 80 mediante EFECTIVO.',_binary '',NULL,'ADMIN','Pago registrado','PAGO_REGISTRADO');
INSERT INTO `notificacion` VALUES (2,_binary '','2026-05-09 07:55:27.410987','Se registró el pago de la cita #1 del paciente Carlos Ramirez por S/ 80 mediante EFECTIVO.',_binary '',NULL,'RECEPCIONISTA','Pago registrado','PAGO_REGISTRADO');
INSERT INTO `notificacion` VALUES (3,_binary '','2026-05-09 13:49:41.190932','Nueva cita programada para Rodrigo Cornejo con Luis Gomez el 2026-05-20 de 09:00 a 10:00.',_binary '',NULL,'ADMIN','Nueva cita registrada','CITA_CREADA');
INSERT INTO `notificacion` VALUES (4,_binary '','2026-05-09 13:49:41.202931','Nueva cita programada para Rodrigo Cornejo con Luis Gomez el 2026-05-20 de 09:00 a 10:00.',_binary '',NULL,'RECEPCIONISTA','Nueva cita registrada','CITA_CREADA');
INSERT INTO `notificacion` VALUES (5,_binary '','2026-05-09 13:49:41.216905','Nueva cita programada para Rodrigo Cornejo con Luis Gomez el 2026-05-20 de 09:00 a 10:00.',_binary '','psicologo@centro.com','USER','Nueva cita asignada','CITA_ASIGNADA');
INSERT INTO `notificacion` VALUES (6,_binary '','2026-05-09 13:50:29.476306','La cita #1 de Carlos Ramirez con Luis Gomez fue marcada como atendida.',_binary '',NULL,'ADMIN','Estado de cita actualizado','CITA_ESTADO');
INSERT INTO `notificacion` VALUES (7,_binary '','2026-05-09 13:50:29.494306','La cita #1 de Carlos Ramirez con Luis Gomez fue marcada como atendida.',_binary '',NULL,'RECEPCIONISTA','Estado de cita actualizado','CITA_ESTADO');
INSERT INTO `notificacion` VALUES (8,_binary '','2026-05-09 13:50:29.506308','La cita #1 de Carlos Ramirez con Luis Gomez fue marcada como atendida.',_binary '','psicologo@centro.com','USER','Estado de cita actualizado','CITA_ESTADO');
INSERT INTO `notificacion` VALUES (9,_binary '','2026-05-10 01:37:58.182427','Nueva cita programada para Lucia Mendoza con Luis Gomez el 2026-05-15 de 16:30 a 18:00.',_binary '',NULL,'ADMIN','Nueva cita registrada','CITA_CREADA');
INSERT INTO `notificacion` VALUES (10,_binary '','2026-05-10 01:37:58.203397','Nueva cita programada para Lucia Mendoza con Luis Gomez el 2026-05-15 de 16:30 a 18:00.',_binary '',NULL,'RECEPCIONISTA','Nueva cita registrada','CITA_CREADA');
INSERT INTO `notificacion` VALUES (11,_binary '','2026-05-10 01:37:58.230400','Nueva cita programada para Lucia Mendoza con Luis Gomez el 2026-05-15 de 16:30 a 18:00.',_binary '','psicologo@centro.com','USER','Nueva cita asignada','CITA_ASIGNADA');
INSERT INTO `notificacion` VALUES (12,_binary '','2026-05-10 06:07:06.709800','Nueva cita programada para Mario Vargas con Sofia Herrera el 2026-05-12 de 17:00 a 18:00.',_binary '',NULL,'ADMIN','Nueva cita registrada','CITA_CREADA');
INSERT INTO `notificacion` VALUES (13,_binary '','2026-05-10 06:07:06.719796','Nueva cita programada para Mario Vargas con Sofia Herrera el 2026-05-12 de 17:00 a 18:00.',_binary '',NULL,'RECEPCIONISTA','Nueva cita registrada','CITA_CREADA');
INSERT INTO `notificacion` VALUES (14,_binary '','2026-05-10 06:07:06.725800','Nueva cita programada para Mario Vargas con Sofia Herrera el 2026-05-12 de 17:00 a 18:00.',_binary '\0','sofia@centro.com','USER','Nueva cita asignada','CITA_ASIGNADA');
INSERT INTO `notificacion` VALUES (15,_binary '','2026-05-10 06:07:59.001710','Se registró un pago de S/ 50 para la cita #13 del paciente Mario Vargas mediante YAPE. Estado: adelanto registrado. Saldo pendiente: S/ 50.00.',_binary '',NULL,'ADMIN','Pago registrado','PAGO_REGISTRADO');
INSERT INTO `notificacion` VALUES (16,_binary '','2026-05-10 06:07:59.005807','Se registró un pago de S/ 50 para la cita #13 del paciente Mario Vargas mediante YAPE. Estado: adelanto registrado. Saldo pendiente: S/ 50.00.',_binary '',NULL,'RECEPCIONISTA','Pago registrado','PAGO_REGISTRADO');
INSERT INTO `notificacion` VALUES (17,_binary '','2026-05-10 20:03:07.581328','Se registró una nueva solicitud de Luis Suarez para Evaluación psicológica en modalidad Presencial.',_binary '',NULL,'ADMIN','Nueva solicitud de orientación','SOLICITUD_ORIENTACION');
INSERT INTO `notificacion` VALUES (18,_binary '','2026-05-10 21:42:30.706402','Se registró una nueva pre-reserva de Elmer Diaz Perez para Terapia de Pareja en modalidad Presencial.',_binary '',NULL,'ADMIN','Nueva pre-reserva de atención','PRE_RESERVA');
INSERT INTO `notificacion` VALUES (19,_binary '','2026-05-10 22:01:53.575094','Se registró una nueva pre-reserva de Jack Mendez para Terapia Individual. Fecha: 2026-05-18, hora: 11:00.',_binary '',NULL,'ADMIN','Nueva pre-reserva de atención','PRE_RESERVA');
INSERT INTO `notificacion` VALUES (20,_binary '','2026-05-10 22:38:20.136099','Se registró al paciente Jack Mendez con DNI 75475655',_binary '',NULL,'ADMIN','Nuevo paciente registrado','PACIENTE_CREADO');
INSERT INTO `notificacion` VALUES (21,_binary '','2026-05-10 22:38:20.145097','Se registró al paciente Jack Mendez con DNI 75475655',_binary '',NULL,'RECEPCIONISTA','Nuevo paciente registrado','PACIENTE_CREADO');
INSERT INTO `notificacion` VALUES (22,_binary '','2026-05-11 00:24:14.371219','Se registró una nueva pre-reserva de Eustaquio Flores para Orientación Familiar. Adelanto: S/ 26.00 mediante YAPE. Código: 964.',_binary '',NULL,'ADMIN','Nueva pre-reserva con adelanto','PRE_RESERVA_PAGO');
INSERT INTO `notificacion` VALUES (23,_binary '','2026-05-11 03:49:14.522722','Se registró una nueva pre-reserva de Roman Reyes para Terapia de Pareja. Adelanto: S/ 24.00 mediante YAPE. Código: 464.',_binary '',NULL,'ADMIN','Nueva pre-reserva con adelanto','PRE_RESERVA_PAGO');
INSERT INTO `notificacion` VALUES (24,_binary '','2026-05-11 03:54:29.331493','Se registró al paciente Roman Reyes con DNI 74547544',_binary '',NULL,'ADMIN','Nuevo paciente registrado','PACIENTE_CREADO');
INSERT INTO `notificacion` VALUES (25,_binary '','2026-05-11 03:54:29.343401','Se registró al paciente Roman Reyes con DNI 74547544',_binary '\0',NULL,'RECEPCIONISTA','Nuevo paciente registrado','PACIENTE_CREADO');
/*!40000 ALTER TABLE `notificacion` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `paciente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `paciente` (
  `id_paciente` bigint NOT NULL AUTO_INCREMENT,
  `estado` bit(1) NOT NULL,
  `direccion` varchar(150) DEFAULT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `dni` varchar(15) NOT NULL,
  `correo` varchar(120) DEFAULT NULL,
  `contacto_emergencia` varchar(120) DEFAULT NULL,
  `telefono_emergencia` varchar(20) DEFAULT NULL,
  `nombres` varchar(100) NOT NULL,
  `sexo` varchar(20) DEFAULT NULL,
  `apellidos` varchar(100) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id_paciente`),
  UNIQUE KEY `UKwr6kxhpayd3jdludsytbn8ag` (`dni`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `paciente` WRITE;
/*!40000 ALTER TABLE `paciente` DISABLE KEYS */;
INSERT INTO `paciente` VALUES (1,_binary '','Av. Los Olivos 123','1995-04-12','12345678','carlos@gmail.com','Marta Ramirez','999888777','Carlos','MASCULINO','Ramirez','999111222');
INSERT INTO `paciente` VALUES (2,_binary '','Jr. Lima 456','1998-08-22','87654321','ana@gmail.com','Pedro Lopez','988111222','Ana','FEMENINO','Lopez','988777666');
INSERT INTO `paciente` VALUES (3,_binary '','Av. Arequipa 789','1992-01-18','74241521','rodrigo@gmail.com','Lucia Cornejo','941000111','Rodrigo','MASCULINO','Cornejo','941223541');
INSERT INTO `paciente` VALUES (4,_binary '','Calle San Martin 321','1989-11-03','74142564','rene5741@gmail.com','Elena Juarez','914555888','Rene','MASCULINO','Juarez','914227845');
INSERT INTO `paciente` VALUES (5,_binary '','Urb. Primavera 222','2001-06-14','70654312','lucia@gmail.com','Carla Mendoza','956888999','Lucia','FEMENINO','Mendoza','956321478');
INSERT INTO `paciente` VALUES (6,_binary '','Av. Central 909','1985-02-07','73451289','mario@gmail.com','Rosa Vargas','987222333','Mario','MASCULINO','Vargas','987654321');
INSERT INTO `paciente` VALUES (7,_binary '','av.los alamos','1996-08-25','74752141','sandrosuarez@gmail.com','946311321','965321231','Sandro','MASCULINO','Suarez','954554211');
INSERT INTO `paciente` VALUES (8,_binary '','Av Bolivar 934',NULL,'75475655','jackmen15@gmail.com','','','Jack','MASCULINO','Mendez','952311477');
INSERT INTO `paciente` VALUES (9,_binary '','mz 14 los olvidados',NULL,'74547544','romanreyes05@gmail.com','954221215','954554654','Roman','MASCULINO','Reyes','9994542');
/*!40000 ALTER TABLE `paciente` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `psicologo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `psicologo` (
  `id_psicologo` bigint NOT NULL AUTO_INCREMENT,
  `estado` bit(1) DEFAULT NULL,
  `correo` varchar(255) DEFAULT NULL,
  `nombres` varchar(255) NOT NULL,
  `apellidos` varchar(255) NOT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  `especialidad` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_psicologo`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `psicologo` WRITE;
/*!40000 ALTER TABLE `psicologo` DISABLE KEYS */;
INSERT INTO `psicologo` VALUES (1,_binary '','psicologo@centro.com','Luis','Gomez','999000111','Ansiedad y estrés');
INSERT INTO `psicologo` VALUES (2,_binary '','maria@centro.com','Maria','Torres','977888999','Terapia de pareja');
INSERT INTO `psicologo` VALUES (3,_binary '','sofia@centro.com','Sofia','Herrera','966777555','Psicología infantil');
/*!40000 ALTER TABLE `psicologo` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `servicio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `servicio` (
  `id_servicio` bigint NOT NULL AUTO_INCREMENT,
  `estado` bit(1) DEFAULT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `duracion_minutos` int DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `costo` double DEFAULT NULL,
  PRIMARY KEY (`id_servicio`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `servicio` WRITE;
/*!40000 ALTER TABLE `servicio` DISABLE KEYS */;
INSERT INTO `servicio` VALUES (1,_binary '','Sesión personalizada para un paciente',60,'Terapia Individual',80);
INSERT INTO `servicio` VALUES (2,_binary '','Sesión orientada a parejas',90,'Terapia de Pareja',120);
INSERT INTO `servicio` VALUES (3,_binary '','Evaluación inicial y diagnóstico',90,'Evaluación Psicológica',150);
INSERT INTO `servicio` VALUES (4,_binary '','Sesión especializada para niños',60,'Terapia Infantil',100);
INSERT INTO `servicio` VALUES (5,_binary '','Sesión de orientación familiar',90,'Orientación Familiar',130);
/*!40000 ALTER TABLE `servicio` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id_usuario` bigint NOT NULL AUTO_INCREMENT,
  `estado` bit(1) NOT NULL,
  `correo` varchar(120) NOT NULL,
  `nombres` varchar(100) NOT NULL,
  `apellidos` varchar(100) NOT NULL,
  `contrasena` varchar(255) NOT NULL,
  `rol` varchar(50) NOT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `UK2mlfr087gb1ce55f2j87o74t` (`correo`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,_binary '','admin@centro.com','Admin','Principal','$2a$10$Lqvox/ysVPLIurBD8QFHp.FxzgAsmqh1HExVA1cPtVpj8ygPpSDhS','ADMIN');
INSERT INTO `usuario` VALUES (2,_binary '','recepcion@centro.com','Rosa','Paredes','$2a$10$my1i3XjJct/TKRBaAOuQx.k95F0AYtDOqXhzS7khEmo5zQ408Cju6','RECEPCIONISTA');
INSERT INTO `usuario` VALUES (3,_binary '','psicologo@centro.com','Luis','Gomez','$2a$10$YT8WGqjqiPZNR1CeaP6a7O7l0wRTB7PngF/oxgSjxLcBzZ1hgjXj2','PSICOLOGO');
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

