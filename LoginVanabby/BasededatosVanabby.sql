CREATE DATABASE  IF NOT EXISTS `pame4` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `pame4`;
-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: pame4
-- ------------------------------------------------------
-- Server version	8.4.3

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `agenda`
--

DROP TABLE IF EXISTS `agenda`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `agenda` (
  `agenda_id` int NOT NULL AUTO_INCREMENT,
  `venta_id` int DEFAULT NULL,
  `fecha_programada` timestamp NOT NULL,
  `responsable_empleado_id` int DEFAULT NULL,
  `estado_agenda_id` int DEFAULT NULL,
  `notas` text,
  PRIMARY KEY (`agenda_id`),
  KEY `venta_id` (`venta_id`),
  KEY `responsable_empleado_id` (`responsable_empleado_id`),
  KEY `estado_agenda_id` (`estado_agenda_id`),
  CONSTRAINT `agenda_ibfk_1` FOREIGN KEY (`venta_id`) REFERENCES `venta` (`venta_id`) ON DELETE CASCADE,
  CONSTRAINT `agenda_ibfk_2` FOREIGN KEY (`responsable_empleado_id`) REFERENCES `empleado` (`empleado_id`),
  CONSTRAINT `agenda_ibfk_3` FOREIGN KEY (`estado_agenda_id`) REFERENCES `estado_agenda` (`estado_agenda_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `agenda`
--

LOCK TABLES `agenda` WRITE;
/*!40000 ALTER TABLE `agenda` DISABLE KEYS */;
INSERT INTO `agenda` VALUES (1,1,'2025-01-20 22:00:00',3,1,'Pedido para evento'),(2,2,'2025-01-23 03:00:00',3,1,'Entrega a domicilio'),(3,3,'2025-01-23 21:00:00',9,1,'Recoger en tienda'),(4,4,'2025-01-26 06:00:00',3,1,'Evento corporativo'),(5,5,'2025-01-27 00:00:00',9,1,'Cumpleaños');
/*!40000 ALTER TABLE `agenda` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `anticipo`
--

DROP TABLE IF EXISTS `anticipo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `anticipo` (
  `anticipo_id` int NOT NULL AUTO_INCREMENT,
  `venta_id` int DEFAULT NULL,
  `cliente_id` int DEFAULT NULL,
  `monto_anticipo` decimal(12,2) NOT NULL,
  `monto_restante` decimal(12,2) NOT NULL,
  `fecha_registro` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_limite` timestamp NULL DEFAULT NULL,
  `estado_anticipo_id` int DEFAULT NULL,
  `metodo_pago_id` int DEFAULT NULL,
  PRIMARY KEY (`anticipo_id`),
  KEY `venta_id` (`venta_id`),
  KEY `cliente_id` (`cliente_id`),
  KEY `metodo_pago_id` (`metodo_pago_id`),
  KEY `estado_anticipo_id` (`estado_anticipo_id`),
  CONSTRAINT `anticipo_ibfk_1` FOREIGN KEY (`venta_id`) REFERENCES `venta` (`venta_id`),
  CONSTRAINT `anticipo_ibfk_2` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`cliente_id`),
  CONSTRAINT `anticipo_ibfk_3` FOREIGN KEY (`metodo_pago_id`) REFERENCES `metodo_pago` (`metodo_pago_id`),
  CONSTRAINT `anticipo_ibfk_4` FOREIGN KEY (`estado_anticipo_id`) REFERENCES `estado_anticipo` (`estado_anticipo_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `anticipo`
--

LOCK TABLES `anticipo` WRITE;
/*!40000 ALTER TABLE `anticipo` DISABLE KEYS */;
INSERT INTO `anticipo` VALUES (1,1,1,300.00,0.00,'2025-11-21 20:49:59','2025-01-20 12:00:00',NULL,1),(2,2,2,500.00,700.00,'2025-11-21 20:49:59','2025-01-22 12:00:00',1,1),(3,3,3,200.00,250.00,'2025-11-21 20:49:59','2025-01-23 12:00:00',1,1),(4,4,1,400.00,580.00,'2025-11-21 20:49:59','2025-01-25 12:00:00',1,1),(5,5,2,100.00,550.00,'2025-11-21 20:49:59','2025-01-26 12:00:00',1,1),(6,6,3,600.00,700.00,'2025-11-21 20:49:59','2025-01-27 12:00:00',1,1),(8,20,8,1950.00,0.00,'2026-06-02 20:28:20','2026-06-15 06:00:00',NULL,NULL),(9,23,9,504.00,1176.00,'2026-06-02 22:52:23','2026-06-03 06:00:00',1,NULL);
/*!40000 ALTER TABLE `anticipo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bloqueo`
--

DROP TABLE IF EXISTS `bloqueo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bloqueo` (
  `bloqueo_id` int NOT NULL AUTO_INCREMENT,
  `empleado_id` int DEFAULT NULL,
  `cliente_id` int DEFAULT NULL,
  `fecha_bloqueo` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `intentos_acumulados` int NOT NULL,
  `desbloqueado_por_empleado_id` int DEFAULT NULL,
  `fecha_desbloqueo` timestamp NULL DEFAULT NULL,
  `motivo` text,
  PRIMARY KEY (`bloqueo_id`),
  KEY `empleado_id` (`empleado_id`),
  KEY `cliente_id` (`cliente_id`),
  KEY `desbloqueado_por_empleado_id` (`desbloqueado_por_empleado_id`),
  CONSTRAINT `bloqueo_ibfk_1` FOREIGN KEY (`empleado_id`) REFERENCES `empleado` (`empleado_id`),
  CONSTRAINT `bloqueo_ibfk_2` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`cliente_id`),
  CONSTRAINT `bloqueo_ibfk_3` FOREIGN KEY (`desbloqueado_por_empleado_id`) REFERENCES `empleado` (`empleado_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bloqueo`
--

LOCK TABLES `bloqueo` WRITE;
/*!40000 ALTER TABLE `bloqueo` DISABLE KEYS */;
INSERT INTO `bloqueo` VALUES (1,3,NULL,'2025-01-05 20:30:00',3,NULL,NULL,'Intentos fallidos de acceso'),(2,4,NULL,'2025-01-06 21:10:00',3,NULL,NULL,'Intentos fallidos de acceso');
/*!40000 ALTER TABLE `bloqueo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categoria`
--

DROP TABLE IF EXISTS `categoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categoria` (
  `categoria_id` int NOT NULL AUTO_INCREMENT,
  `nombre_categoria` varchar(80) NOT NULL,
  PRIMARY KEY (`categoria_id`),
  UNIQUE KEY `nombre_categoria` (`nombre_categoria`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categoria`
--

LOCK TABLES `categoria` WRITE;
/*!40000 ALTER TABLE `categoria` DISABLE KEYS */;
INSERT INTO `categoria` VALUES (3,'Cupcakes'),(5,'Galletas'),(7,'Panadería'),(1,'Pasteles'),(4,'Postres'),(6,'Tartas'),(2,'Tortas');
/*!40000 ALTER TABLE `categoria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cliente`
--

DROP TABLE IF EXISTS `cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cliente` (
  `cliente_id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(80) NOT NULL,
  `ap_paterno` varchar(60) DEFAULT NULL,
  `ap_materno` varchar(60) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `correo` varchar(120) DEFAULT NULL,
  `creado_en` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`cliente_id`),
  KEY `idx_cliente_correo` (`correo`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cliente`
--

LOCK TABLES `cliente` WRITE;
/*!40000 ALTER TABLE `cliente` DISABLE KEYS */;
INSERT INTO `cliente` VALUES (1,'María','López','Ruiz','5510102020','maria@example.com','2025-11-21 20:49:59'),(2,'Juan','Pérez','Garcia','5520203030','juan@example.com','2025-11-21 20:49:59'),(3,'Ana','Torres','Martinez','5530304040','ana@example.com','2025-11-21 20:49:59'),(4,'Carlos','Ruiz','Hernandez','5540405050','carlos@example.com','2025-11-21 20:49:59'),(5,'Laura','García','Fernandez','5550506060','laura@example.com','2025-11-21 20:49:59'),(6,'Marco','Jimenez','Juarez','9514147400','ssjdarkdeath20@gmail.com','2026-05-21 00:36:27'),(8,'Jaime','Martinez','Ochoa','9513097729','jaimemrtz29@gmail.com','2026-05-22 13:50:14'),(9,'Sebastian','Perez','Sanchez','9512368974','23161125@itoaxaca.edu.mx','2026-06-02 22:47:50'),(10,'Eliseo','Pacheco','Cortes','9516071063','eliseodecancer@gmail.com','2026-09-08 14:58:20');
/*!40000 ALTER TABLE `cliente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `compra`
--

DROP TABLE IF EXISTS `compra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `compra` (
  `compra_id` int NOT NULL AUTO_INCREMENT,
  `proveedor_id` int DEFAULT NULL,
  `creado_en` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_esperada` date DEFAULT NULL,
  `estado_compra_id` int DEFAULT NULL,
  PRIMARY KEY (`compra_id`),
  KEY `proveedor_id` (`proveedor_id`),
  KEY `estado_compra_id` (`estado_compra_id`),
  CONSTRAINT `compra_ibfk_1` FOREIGN KEY (`proveedor_id`) REFERENCES `proveedor` (`proveedor_id`),
  CONSTRAINT `compra_ibfk_2` FOREIGN KEY (`estado_compra_id`) REFERENCES `estado_compra` (`estado_compra_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `compra`
--

LOCK TABLES `compra` WRITE;
/*!40000 ALTER TABLE `compra` DISABLE KEYS */;
INSERT INTO `compra` VALUES (1,1,'2025-11-21 20:49:59','2025-02-01',1),(2,2,'2025-11-21 20:49:59','2025-02-03',1),(3,1,'2025-11-21 20:49:59','2025-02-05',1),(4,2,'2025-11-21 20:49:59','2025-02-06',1),(5,1,'2025-11-21 20:49:59','2025-02-07',2),(6,2,'2025-11-21 20:49:59','2025-02-08',1),(7,1,'2025-11-21 20:49:59','2025-02-10',1),(8,2,'2025-11-21 20:49:59','2025-02-12',1),(9,1,'2025-11-21 20:49:59','2025-02-14',2),(10,2,'2025-11-21 20:49:59','2025-02-15',1);
/*!40000 ALTER TABLE `compra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_compra`
--

DROP TABLE IF EXISTS `detalle_compra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_compra` (
  `detalle_compra_id` int NOT NULL AUTO_INCREMENT,
  `compra_id` int DEFAULT NULL,
  `ingrediente_id` int DEFAULT NULL,
  `cantidad` decimal(12,4) NOT NULL,
  `precio_unitario` decimal(12,6) NOT NULL,
  `subtotal` decimal(12,2) GENERATED ALWAYS AS ((`cantidad` * `precio_unitario`)) VIRTUAL,
  PRIMARY KEY (`detalle_compra_id`),
  KEY `compra_id` (`compra_id`),
  KEY `ingrediente_id` (`ingrediente_id`),
  CONSTRAINT `det_compra_ibfk_1` FOREIGN KEY (`compra_id`) REFERENCES `compra` (`compra_id`) ON DELETE CASCADE,
  CONSTRAINT `det_compra_ibfk_2` FOREIGN KEY (`ingrediente_id`) REFERENCES `ingrediente` (`ingrediente_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_compra`
--

LOCK TABLES `detalle_compra` WRITE;
/*!40000 ALTER TABLE `detalle_compra` DISABLE KEYS */;
INSERT INTO `detalle_compra` (`detalle_compra_id`, `compra_id`, `ingrediente_id`, `cantidad`, `precio_unitario`) VALUES (1,1,1,5000.0000,0.001800),(2,1,2,2000.0000,0.000900),(3,2,3,1000.0000,0.007500),(4,2,4,200.0000,1.400000),(5,3,5,3000.0000,0.002000),(6,3,6,500.0000,0.012000),(7,4,8,800.0000,0.009000),(8,5,9,1200.0000,0.009500),(9,5,10,1500.0000,0.002500),(10,6,11,600.0000,0.035000),(11,6,12,1500.0000,0.004000),(12,7,13,200.0000,0.110000),(13,7,14,2500.0000,0.018000),(14,8,15,700.0000,0.004000),(15,8,16,500.0000,0.018000),(16,9,17,1200.0000,0.009000),(17,9,18,3000.0000,0.002500),(18,10,19,800.0000,0.025000),(19,10,20,100.0000,0.018000);
/*!40000 ALTER TABLE `detalle_compra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_venta`
--

DROP TABLE IF EXISTS `detalle_venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_venta` (
  `detalle_venta_id` int NOT NULL AUTO_INCREMENT,
  `venta_id` int DEFAULT NULL,
  `producto_id` int DEFAULT NULL,
  `cantidad` int NOT NULL,
  `precio_unitario` decimal(10,2) DEFAULT NULL,
  `subtotal` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`detalle_venta_id`),
  KEY `venta_id` (`venta_id`),
  KEY `producto_id` (`producto_id`),
  CONSTRAINT `det_venta_ibfk_1` FOREIGN KEY (`venta_id`) REFERENCES `venta` (`venta_id`) ON DELETE CASCADE,
  CONSTRAINT `det_venta_ibfk_2` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`producto_id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_venta`
--

LOCK TABLES `detalle_venta` WRITE;
/*!40000 ALTER TABLE `detalle_venta` DISABLE KEYS */;
INSERT INTO `detalle_venta` VALUES (1,1,1,1,650.00,650.00),(2,1,3,2,100.00,200.00),(3,2,1,1,650.00,650.00),(4,2,8,1,550.00,550.00),(5,3,4,3,150.00,450.00),(6,4,2,2,450.00,900.00),(7,4,6,1,80.00,80.00),(8,5,5,1,650.00,650.00),(9,6,1,1,650.00,650.00),(10,6,3,3,120.00,360.00),(11,6,7,1,290.00,290.00),(12,7,4,2,150.00,300.00),(13,8,9,1,700.00,700.00),(14,8,3,2,120.00,240.00),(15,8,6,2,80.00,160.00),(16,9,10,2,140.00,280.00),(17,9,5,1,500.00,500.00),(18,10,2,1,450.00,450.00),(19,10,8,1,550.00,550.00),(20,2,3,5,120.00,600.00),(21,19,4,1,50.00,50.00),(22,20,1,10,650.00,6500.00),(23,21,11,5,255.00,1275.00),(24,22,6,2,520.00,1040.00),(25,23,8,7,240.00,1680.00),(26,24,4,6,50.00,300.00),(27,25,3,15,120.00,1800.00),(28,26,4,3,50.00,150.00),(29,27,4,3,50.00,150.00);
/*!40000 ALTER TABLE `detalle_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `direccion_cliente`
--

DROP TABLE IF EXISTS `direccion_cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `direccion_cliente` (
  `direccion_id` int NOT NULL AUTO_INCREMENT,
  `cliente_id` int NOT NULL,
  `alias` varchar(40) DEFAULT 'Principal',
  `calle` varchar(150) DEFAULT NULL,
  `numero` varchar(20) DEFAULT NULL,
  `colonia` varchar(100) DEFAULT NULL,
  `ciudad` varchar(80) DEFAULT NULL,
  `estado_geo` varchar(80) DEFAULT NULL,
  `cp` varchar(10) DEFAULT NULL,
  `es_principal` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`direccion_id`),
  KEY `cliente_id` (`cliente_id`),
  CONSTRAINT `dir_cli_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`cliente_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `direccion_cliente`
--

LOCK TABLES `direccion_cliente` WRITE;
/*!40000 ALTER TABLE `direccion_cliente` DISABLE KEYS */;
INSERT INTO `direccion_cliente` VALUES (1,1,'Principal','Calle Falsa','123','Colonia Narvarte','Ciudad de México','CDMX','03020',1),(2,2,'Principal','Av. Reforma','200','Juárez','Ciudad de México','CDMX','06600',1),(3,3,'Principal','Col. Centro','55','Centro Histórico','Ciudad de México','CDMX','06010',1),(4,4,'Principal','Av. Universidad','77','Copilco','Ciudad de México','CDMX','04360',1),(5,5,'Principal','Calle Hidalgo','99','Santa María la Ribera','Ciudad de México','CDMX','06400',1),(6,6,'Principal','Antonio de León','705','Centro','Oaxaca de Juárez','Oaxaca','68000',1),(7,8,'Principal','La Luz #33','15','San Felipe del Agua','Oaxaca de Juárez','Oaxaca','68020',1),(8,9,'Principal','Manzana L #23',NULL,'Solidaridad','Solidaridad','Oaxaca','68018',1),(9,10,'Principal','sabinos #85',NULL,'La Cienega','La Cienega','Oaxaca','71260',1);
/*!40000 ALTER TABLE `direccion_cliente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `direccion_empleado`
--

DROP TABLE IF EXISTS `direccion_empleado`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `direccion_empleado` (
  `direccion_id` int NOT NULL AUTO_INCREMENT,
  `empleado_id` int NOT NULL,
  `alias` varchar(40) DEFAULT 'Principal',
  `calle` varchar(150) DEFAULT NULL,
  `numero` varchar(20) DEFAULT NULL,
  `colonia` varchar(100) DEFAULT NULL,
  `ciudad` varchar(80) DEFAULT NULL,
  `estado_geo` varchar(80) DEFAULT NULL,
  `cp` varchar(10) DEFAULT NULL,
  `es_principal` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`direccion_id`),
  KEY `empleado_id` (`empleado_id`),
  CONSTRAINT `dir_emp_ibfk_1` FOREIGN KEY (`empleado_id`) REFERENCES `empleado` (`empleado_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `direccion_empleado`
--

LOCK TABLES `direccion_empleado` WRITE;
/*!40000 ALTER TABLE `direccion_empleado` DISABLE KEYS */;
INSERT INTO `direccion_empleado` VALUES (1,3,'Casa','Calle Morelos','14','Centro','Oaxaca de Juárez','Oaxaca','68000',1),(2,4,'Casa','Av. Juárez','88','Reforma','Oaxaca de Juárez','Oaxaca','68050',1),(3,7,'Casa','Calle Independencia','5','Ex Marquesado','Oaxaca de Juárez','Oaxaca','68020',1),(4,1,'Casa','Calle Constitución','12','Centro Histórico','Oaxaca de Juárez','Oaxaca','68000',1),(5,2,'Casa','Av. Ferrocarril','33','Sta. Rosa Panzacola','Oaxaca de Juárez','Oaxaca','68040',1),(6,5,'Casa','Calz. Madero','55','Trinidad de las Huertas','Oaxaca de Juárez','Oaxaca','68030',1),(7,6,'Casa','Calle Tinoco y Palacios','8','Centro','Oaxaca de Juárez','Oaxaca','68000',1),(8,8,'Casa','Blvd. Eduardo Vasconcelos','100','Estrella','Oaxaca de Juárez','Oaxaca','68070',1),(9,9,'Casa','Calle Pino Suárez','22','Volcanes','Oaxaca de Juárez','Oaxaca','68060',1),(10,10,'Casa','Periférico','300','Montoya','Oaxaca de Juárez','Oaxaca','68080',1);
/*!40000 ALTER TABLE `direccion_empleado` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `direccion_proveedor`
--

DROP TABLE IF EXISTS `direccion_proveedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `direccion_proveedor` (
  `direccion_id` int NOT NULL AUTO_INCREMENT,
  `proveedor_id` int NOT NULL,
  `alias` varchar(40) DEFAULT 'Principal',
  `calle` varchar(150) DEFAULT NULL,
  `numero` varchar(20) DEFAULT NULL,
  `colonia` varchar(100) DEFAULT NULL,
  `ciudad` varchar(80) DEFAULT NULL,
  `estado_geo` varchar(80) DEFAULT NULL,
  `cp` varchar(10) DEFAULT NULL,
  `es_principal` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`direccion_id`),
  KEY `proveedor_id` (`proveedor_id`),
  CONSTRAINT `dir_prov_ibfk_1` FOREIGN KEY (`proveedor_id`) REFERENCES `proveedor` (`proveedor_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `direccion_proveedor`
--

LOCK TABLES `direccion_proveedor` WRITE;
/*!40000 ALTER TABLE `direccion_proveedor` DISABLE KEYS */;
INSERT INTO `direccion_proveedor` VALUES (1,1,'Bodega principal','Blvd. Industrial','200','Industrial Vallejo','Ciudad de México','CDMX','02300',1),(2,2,'Planta','Av. Lácteos','45','El Álamo','Guadalajara','Jalisco','44490',1);
/*!40000 ALTER TABLE `direccion_proveedor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `empleado`
--

DROP TABLE IF EXISTS `empleado`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `empleado` (
  `empleado_id` int NOT NULL AUTO_INCREMENT,
  `nombre_usuario` varchar(80) NOT NULL,
  `clave_hash` varchar(255) NOT NULL,
  `nombre` varchar(80) NOT NULL DEFAULT '',
  `ap_paterno` varchar(60) DEFAULT NULL,
  `ap_materno` varchar(60) DEFAULT NULL,
  `rol_id` int DEFAULT NULL,
  `bloqueado` tinyint(1) DEFAULT '0',
  `intentos_fallidos` int DEFAULT '0',
  `ultimo_intento` timestamp NULL DEFAULT NULL,
  `creado_en` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`empleado_id`),
  UNIQUE KEY `nombre_usuario` (`nombre_usuario`),
  KEY `idx_empleado_usuario` (`nombre_usuario`),
  KEY `rol_id` (`rol_id`),
  CONSTRAINT `empleado_ibfk_1` FOREIGN KEY (`rol_id`) REFERENCES `rol` (`rol_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `empleado`
--

LOCK TABLES `empleado` WRITE;
/*!40000 ALTER TABLE `empleado` DISABLE KEYS */;
INSERT INTO `empleado` VALUES (1,'admin','$2a$10$adminhash','Carlos','Mendoza','Ríos',1,0,0,'2026-05-18 23:01:05','2025-11-21 20:49:59'),(2,'gerente1','$2a$10$gerentehash','Ana','Sánchez','Vidal',2,0,0,NULL,'2025-11-21 20:49:59'),(3,'repostero1','$2a$10$reposthash','Luis','Ramírez','Cruz',3,0,0,NULL,'2025-11-21 20:49:59'),(4,'cajero1','$2a$10$cajerohash','Marta','Flores','Gutiérrez',4,0,0,'2026-05-27 23:46:16','2025-11-21 20:49:59'),(5,'super1','$2a$10$suphash','Juan','Herrera','Morales',5,0,0,NULL,'2025-11-21 20:49:59'),(6,'marketing1','$2a$10$markhash','Rosa','Peña','Jiménez',6,0,0,NULL,'2025-11-21 20:49:59'),(7,'repartidor1','$2a$10$rephash','Pedro','Castro','López',7,0,0,NULL,'2025-11-21 20:49:59'),(8,'empleado8','$2a$10$empleado8','Sofía','Martínez','Ortega',4,0,0,NULL,'2025-11-21 20:49:59'),(9,'empleado9','$2a$10$empleado9','Diego','Torres','Vargas',3,0,0,NULL,'2025-11-21 20:49:59'),(10,'empleado10','UtCR7tr#','Valeria','Gómez','Reyes',2,0,0,NULL,'2025-11-21 20:49:59'),(11,'Eliseo1','hola123','Eliseo','Pacheco','Cortes',3,0,0,NULL,'2026-06-02 20:33:56');
/*!40000 ALTER TABLE `empleado` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `estado_agenda`
--

DROP TABLE IF EXISTS `estado_agenda`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estado_agenda` (
  `estado_agenda_id` int NOT NULL AUTO_INCREMENT,
  `nombre_estado` varchar(30) NOT NULL,
  PRIMARY KEY (`estado_agenda_id`),
  UNIQUE KEY `nombre_estado` (`nombre_estado`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estado_agenda`
--

LOCK TABLES `estado_agenda` WRITE;
/*!40000 ALTER TABLE `estado_agenda` DISABLE KEYS */;
INSERT INTO `estado_agenda` VALUES (3,'CANCELADO'),(2,'COMPLETADO'),(1,'PROGRAMADO'),(4,'REPROGRAMADO');
/*!40000 ALTER TABLE `estado_agenda` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `estado_anticipo`
--

DROP TABLE IF EXISTS `estado_anticipo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estado_anticipo` (
  `estado_anticipo_id` int NOT NULL AUTO_INCREMENT,
  `nombre_estado` varchar(30) NOT NULL,
  PRIMARY KEY (`estado_anticipo_id`),
  UNIQUE KEY `nombre_estado` (`nombre_estado`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estado_anticipo`
--

LOCK TABLES `estado_anticipo` WRITE;
/*!40000 ALTER TABLE `estado_anticipo` DISABLE KEYS */;
INSERT INTO `estado_anticipo` VALUES (3,'CANCELADO'),(2,'LIQUIDADO'),(4,'PARCIAL'),(1,'PENDIENTE');
/*!40000 ALTER TABLE `estado_anticipo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `estado_compra`
--

DROP TABLE IF EXISTS `estado_compra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estado_compra` (
  `estado_compra_id` int NOT NULL AUTO_INCREMENT,
  `nombre_estado` varchar(30) NOT NULL,
  PRIMARY KEY (`estado_compra_id`),
  UNIQUE KEY `nombre_estado` (`nombre_estado`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estado_compra`
--

LOCK TABLES `estado_compra` WRITE;
/*!40000 ALTER TABLE `estado_compra` DISABLE KEYS */;
INSERT INTO `estado_compra` VALUES (1,'ABIERTO'),(3,'CANCELADO'),(4,'PARCIAL'),(2,'RECIBIDO');
/*!40000 ALTER TABLE `estado_compra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `estado_venta`
--

DROP TABLE IF EXISTS `estado_venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estado_venta` (
  `estado_venta_id` int NOT NULL AUTO_INCREMENT,
  `nombre_estado` varchar(30) NOT NULL,
  PRIMARY KEY (`estado_venta_id`),
  UNIQUE KEY `nombre_estado` (`nombre_estado`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estado_venta`
--

LOCK TABLES `estado_venta` WRITE;
/*!40000 ALTER TABLE `estado_venta` DISABLE KEYS */;
INSERT INTO `estado_venta` VALUES (3,'CANCELADO'),(4,'EN_PREPARACION'),(5,'ENTREGADO'),(2,'PAGADO'),(1,'PENDIENTE');
/*!40000 ALTER TABLE `estado_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ingrediente`
--

DROP TABLE IF EXISTS `ingrediente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ingrediente` (
  `ingrediente_id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(150) NOT NULL,
  `unidad_medida_id` int NOT NULL,
  `costo_por_unidad` decimal(12,6) DEFAULT '0.000000',
  PRIMARY KEY (`ingrediente_id`),
  UNIQUE KEY `nombre` (`nombre`),
  KEY `idx_ingrediente_nombre` (`nombre`),
  KEY `unidad_medida_id` (`unidad_medida_id`),
  CONSTRAINT `ingrediente_ibfk_1` FOREIGN KEY (`unidad_medida_id`) REFERENCES `unidad_medida` (`unidad_medida_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ingrediente`
--

LOCK TABLES `ingrediente` WRITE;
/*!40000 ALTER TABLE `ingrediente` DISABLE KEYS */;
INSERT INTO `ingrediente` VALUES (1,'Harina',1,0.002000),(2,'Azúcar',1,0.001000),(3,'Mantequilla',1,0.008000),(4,'Huevos',5,1.500000),(5,'Leche',3,0.003000),(6,'Cocoa',1,0.010000),(7,'Vainilla',3,0.120000),(8,'Chocolate',1,0.020000),(9,'Queso Crema',1,0.010000),(10,'Galleta Molida',1,0.003000),(11,'Fresas',1,0.030000),(12,'Limón (jugo)',3,0.005000),(13,'Aceite Vegetal',3,0.004000),(14,'Avena',1,0.002000),(15,'Azúcar Glass',1,0.002000),(16,'Colorante Rojo',1,0.020000),(17,'Crema Batida',3,0.010000),(18,'Miel',1,0.006000),(19,'Nuez Picada',1,0.040000),(20,'Frutas Mixtas',1,0.030000);
/*!40000 ALTER TABLE `ingrediente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventario_producto`
--

DROP TABLE IF EXISTS `inventario_producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventario_producto` (
  `inv_producto_id` int NOT NULL AUTO_INCREMENT,
  `producto_id` int NOT NULL,
  `stock_disponible` int NOT NULL DEFAULT '0',
  `stock_minimo` int NOT NULL DEFAULT '0',
  `actualizado_en` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`inv_producto_id`),
  UNIQUE KEY `uk_producto` (`producto_id`),
  CONSTRAINT `invprod_ibfk_1` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`producto_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventario_producto`
--

LOCK TABLES `inventario_producto` WRITE;
/*!40000 ALTER TABLE `inventario_producto` DISABLE KEYS */;
INSERT INTO `inventario_producto` VALUES (1,1,1,2,'2026-06-02 20:38:39'),(2,2,25,5,'2026-05-24 01:46:05'),(3,3,0,5,'2026-09-08 14:52:04'),(4,4,18,10,'2026-09-08 14:59:27'),(5,5,20,5,'2026-05-24 01:46:05'),(6,6,6,2,'2026-06-02 22:42:33'),(7,7,12,2,'2026-05-24 01:46:05'),(8,8,43,10,'2026-06-02 22:51:27'),(9,9,18,5,'2026-05-24 01:46:05'),(10,10,22,5,'2026-05-24 01:46:05'),(11,11,2,0,'2026-06-02 20:40:00');
/*!40000 ALTER TABLE `inventario_producto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lote_inventario`
--

DROP TABLE IF EXISTS `lote_inventario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lote_inventario` (
  `lote_id` int NOT NULL AUTO_INCREMENT,
  `ingrediente_id` int DEFAULT NULL,
  `numero_lote` varchar(50) DEFAULT NULL,
  `cantidad` decimal(12,4) DEFAULT NULL,
  `unidad_medida_id` int DEFAULT NULL,
  `fecha_caducidad` date DEFAULT NULL,
  `creado_en` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`lote_id`),
  KEY `idx_lote_ingrediente` (`ingrediente_id`),
  KEY `unidad_medida_id` (`unidad_medida_id`),
  CONSTRAINT `lote_inv_ibfk_1` FOREIGN KEY (`ingrediente_id`) REFERENCES `ingrediente` (`ingrediente_id`),
  CONSTRAINT `lote_inv_ibfk_2` FOREIGN KEY (`unidad_medida_id`) REFERENCES `unidad_medida` (`unidad_medida_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lote_inventario`
--

LOCK TABLES `lote_inventario` WRITE;
/*!40000 ALTER TABLE `lote_inventario` DISABLE KEYS */;
INSERT INTO `lote_inventario` VALUES (1,1,'L-HAR-001',4600.0000,1,'2026-06-01','2025-11-21 20:49:59'),(2,1,'L-HAR-002',3000.0000,1,'2026-08-01','2025-11-21 20:49:59'),(3,2,'L-AZU-001',9800.0000,1,'2027-01-01','2025-11-21 20:49:59'),(4,3,'L-MAN-001',1750.0000,1,'2025-12-01','2025-11-21 20:49:59'),(5,4,'L-HUE-001',200.0000,5,'2025-11-30','2025-11-21 20:49:59'),(6,5,'L-LEC-001',5000.0000,3,'2025-09-15','2025-11-21 20:49:59'),(7,6,'L-COC-001',920.0000,1,'2026-02-01','2025-11-21 20:49:59'),(8,7,'L-VAN-001',500.0000,3,'2027-01-01','2025-11-21 20:49:59'),(9,8,'L-CHO-001',2800.0000,1,'2026-11-01','2025-11-21 20:49:59'),(10,9,'L-QC-001',1500.0000,1,'2025-12-30','2025-11-21 20:49:59'),(11,10,'L-GAL-001',2000.0000,1,'2026-03-01','2025-11-21 20:49:59'),(12,11,'L-FRU-001',2500.0000,1,'2025-11-25','2025-11-21 20:49:59'),(13,12,'L-LIM-001',2000.0000,3,'2025-12-01','2025-11-21 20:49:59'),(14,13,'L-ACE-001',3000.0000,3,'2026-06-01','2025-11-21 20:49:59'),(15,14,'L-MIE-001',1000.0000,1,'2026-07-01','2025-11-21 20:49:59'),(16,15,'L-AZGL-001',5000.0000,1,'2026-11-01','2025-11-21 20:49:59'),(17,16,'L-COL-001',100.0000,1,'2027-01-01','2025-11-21 20:49:59'),(18,17,'L-CRE-001',2000.0000,3,'2025-12-20','2025-11-21 20:49:59'),(19,18,'L-MIE2-001',800.0000,1,'2026-07-01','2025-11-21 20:49:59'),(20,19,'L-NUE-001',800.0000,1,'2026-05-01','2025-11-21 20:49:59');
/*!40000 ALTER TABLE `lote_inventario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metodo_pago`
--

DROP TABLE IF EXISTS `metodo_pago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `metodo_pago` (
  `metodo_pago_id` int NOT NULL AUTO_INCREMENT,
  `nombre_metodo` varchar(50) NOT NULL,
  PRIMARY KEY (`metodo_pago_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metodo_pago`
--

LOCK TABLES `metodo_pago` WRITE;
/*!40000 ALTER TABLE `metodo_pago` DISABLE KEYS */;
INSERT INTO `metodo_pago` VALUES (1,'Efectivo'),(2,'Tarjeta de crédito'),(3,'Transferencia'),(4,'Mercado Pago');
/*!40000 ALTER TABLE `metodo_pago` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pago_venta`
--

DROP TABLE IF EXISTS `pago_venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pago_venta` (
  `pago_venta_id` int NOT NULL AUTO_INCREMENT,
  `venta_id` int DEFAULT NULL,
  `metodo_pago_id` int DEFAULT NULL,
  `monto` decimal(12,2) NOT NULL,
  `pagado_en` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `registrado_por` int DEFAULT NULL,
  PRIMARY KEY (`pago_venta_id`),
  KEY `venta_id` (`venta_id`),
  KEY `metodo_pago_id` (`metodo_pago_id`),
  KEY `registrado_por` (`registrado_por`),
  CONSTRAINT `pago_vta_ibfk_1` FOREIGN KEY (`venta_id`) REFERENCES `venta` (`venta_id`) ON DELETE CASCADE,
  CONSTRAINT `pago_vta_ibfk_2` FOREIGN KEY (`metodo_pago_id`) REFERENCES `metodo_pago` (`metodo_pago_id`),
  CONSTRAINT `pago_vta_ibfk_3` FOREIGN KEY (`registrado_por`) REFERENCES `empleado` (`empleado_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pago_venta`
--

LOCK TABLES `pago_venta` WRITE;
/*!40000 ALTER TABLE `pago_venta` DISABLE KEYS */;
INSERT INTO `pago_venta` VALUES (1,1,1,850.00,'2025-11-21 20:49:59',4),(2,2,2,1200.00,'2025-11-21 20:49:59',4),(3,3,1,450.00,'2025-11-21 20:49:59',4),(4,4,2,980.00,'2025-11-21 20:49:59',4),(5,5,1,650.00,'2025-11-21 20:49:59',4),(6,6,2,1300.00,'2025-11-21 20:49:59',4),(7,7,1,300.00,'2025-11-21 20:49:59',4),(8,8,3,1100.00,'2025-11-21 20:49:59',4),(9,9,1,780.00,'2025-11-21 20:49:59',4),(10,10,2,1020.00,'2025-11-21 20:49:59',4);
/*!40000 ALTER TABLE `pago_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `paso_receta`
--

DROP TABLE IF EXISTS `paso_receta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `paso_receta` (
  `paso_id` int NOT NULL AUTO_INCREMENT,
  `receta_id` int NOT NULL,
  `numero_paso` int NOT NULL,
  `descripcion` varchar(255) NOT NULL,
  PRIMARY KEY (`paso_id`),
  UNIQUE KEY `uk_receta_paso` (`receta_id`,`numero_paso`),
  CONSTRAINT `paso_receta_ibfk_1` FOREIGN KEY (`receta_id`) REFERENCES `receta` (`receta_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paso_receta`
--

LOCK TABLES `paso_receta` WRITE;
/*!40000 ALTER TABLE `paso_receta` DISABLE KEYS */;
INSERT INTO `paso_receta` VALUES (1,1,1,'Mezclar ingredientes secos'),(2,1,2,'Hornear a 180°C'),(3,1,3,'Rellenar con chocolate'),(4,1,4,'Decorar con betún'),(5,2,1,'Preparar base de galleta'),(6,2,2,'Mezclar queso crema'),(7,2,3,'Hornear y refrigerar'),(8,3,1,'Preparar masa'),(9,3,2,'Llenar moldes'),(10,3,3,'Hornear'),(11,3,4,'Decorar (6 unidades)'),(12,4,1,'Mezclar ingredientes'),(13,4,2,'Hornear'),(14,4,3,'Cortar en porciones'),(15,5,1,'Mezclar avena con ingredientes'),(16,5,2,'Dar forma'),(17,5,3,'Hornear'),(18,6,1,'Preparar base'),(19,6,2,'Montar crema'),(20,6,3,'Colocar frutas frescas'),(21,7,1,'Preparar masa Red Velvet'),(22,7,2,'Hornear'),(23,7,3,'Decorar'),(24,8,1,'Preparar mezcla de macarons'),(25,8,2,'Hornear'),(26,8,3,'Rellenar (12 unidades)'),(27,9,1,'Preparar masa'),(28,9,2,'Fermentar'),(29,9,3,'Freír'),(30,9,4,'Glasear'),(31,10,1,'Mezclar ingredientes'),(32,10,2,'Hornear'),(33,10,3,'Glasear con limón');
/*!40000 ALTER TABLE `paso_receta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permiso`
--

DROP TABLE IF EXISTS `permiso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permiso` (
  `permiso_id` int NOT NULL AUTO_INCREMENT,
  `nombre_permiso` varchar(100) NOT NULL,
  `descripcion` text,
  PRIMARY KEY (`permiso_id`),
  UNIQUE KEY `nombre_permiso` (`nombre_permiso`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permiso`
--

LOCK TABLES `permiso` WRITE;
/*!40000 ALTER TABLE `permiso` DISABLE KEYS */;
INSERT INTO `permiso` VALUES (1,'producto.crear','Crear productos en el catálogo'),(2,'producto.editar','Editar información de productos'),(3,'producto.eliminar','Eliminar productos del catálogo'),(4,'producto.ver','Ver productos'),(5,'receta.crear','Crear recetas'),(6,'receta.editar','Editar recetas'),(7,'receta.eliminar','Eliminar recetas'),(8,'receta.ver','Ver recetas'),(9,'inventario.administrar','Administrar inventario y lotes'),(10,'inventario.ver','Ver inventario'),(11,'venta.crear','Registrar ventas'),(12,'venta.editar','Editar ventas'),(13,'venta.cancelar','Cancelar ventas'),(14,'venta.ver','Ver ventas'),(15,'usuario.administrar','Crear y administrar usuarios'),(16,'reporte.ver','Ver reportes'),(17,'envio.ver','Ver pedidos para envío'),(18,'envio.actualizar','Actualizar estado de envío');
/*!40000 ALTER TABLE `permiso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `producto`
--

DROP TABLE IF EXISTS `producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `producto` (
  `producto_id` int NOT NULL AUTO_INCREMENT,
  `sku` varchar(30) DEFAULT NULL,
  `nombre` varchar(150) NOT NULL,
  `descripcion` text,
  `imagen` varchar(255) DEFAULT NULL,
  `categoria_id` int DEFAULT NULL,
  `precio` decimal(10,2) NOT NULL,
  `tiempo_preparacion_min` int DEFAULT NULL,
  `creado_en` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`producto_id`),
  UNIQUE KEY `sku` (`sku`),
  KEY `idx_producto_nombre` (`nombre`),
  KEY `categoria_id` (`categoria_id`),
  CONSTRAINT `producto_ibfk_1` FOREIGN KEY (`categoria_id`) REFERENCES `categoria` (`categoria_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `producto`
--

LOCK TABLES `producto` WRITE;
/*!40000 ALTER TABLE `producto` DISABLE KEYS */;
INSERT INTO `producto` VALUES (1,'P001','Pastel Chocolate','Pastel relleno de chocolate y betún','pastelChocolate.jpg',1,650.00,120,'2025-11-21 20:49:59'),(2,'P002','Cheesecake','Cheesecake clásico con base de galleta','cheesecake.jpg',2,450.00,90,'2025-11-21 20:49:59'),(3,'P003','Cupcakes Vainilla','Caja 6 cupcakes de vainilla','cupcakesVainilla.jpg',3,120.00,5,'2025-11-21 20:49:59'),(4,'P004','Brownie','Brownie de chocolate por pieza','brownie.jpg',4,50.00,15,'2025-11-21 20:49:59'),(5,'P005','Galletas Avena','Paquete de 10 galletas de avena','galletasavena.jpg',5,90.00,40,'2025-11-21 20:49:59'),(6,'P006','Tarta de Frutas','Tarta con crema y frutas frescas','tartadefrutas.jpg',6,520.00,110,'2025-11-21 20:49:59'),(7,'P007','Pastel Red Velvet','Pastel Red Velvet mediano','pastelredvelvet.jpg',1,700.00,130,'2025-11-21 20:49:59'),(8,'P008','Macarons','Set 12 macarons surtidos','macarons.jpg',4,240.00,50,'2025-11-21 20:49:59'),(9,'P009','Donas Glaseadas','Caja 6 donas variadas','donas_glaseadas.jpg',7,70.00,20,'2025-11-21 20:49:59'),(10,'P010','Panqué Limón','Panqué con glaseado de limón','panquélimón.jpg',7,85.00,45,'2025-11-21 20:49:59'),(11,'VN-1VJRIY','Donas','Donas de azucar con canela',NULL,4,255.00,9,'2026-06-02 20:35:45');
/*!40000 ALTER TABLE `producto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `proveedor`
--

DROP TABLE IF EXISTS `proveedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proveedor` (
  `proveedor_id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(150) NOT NULL,
  `contacto_nombre` varchar(80) DEFAULT NULL,
  `contacto_ap_paterno` varchar(60) DEFAULT NULL,
  `contacto_ap_materno` varchar(60) DEFAULT NULL,
  `telefono` varchar(40) DEFAULT NULL,
  `correo` varchar(120) DEFAULT NULL,
  `notas` text,
  PRIMARY KEY (`proveedor_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `proveedor`
--

LOCK TABLES `proveedor` WRITE;
/*!40000 ALTER TABLE `proveedor` DISABLE KEYS */;
INSERT INTO `proveedor` VALUES (1,'Harineras SA','Luis','Pérez','Gonzales','5512345678','ventas@harineras.example','Proveedor de harina'),(2,'LacteosMex','Mariana','Ruiz','Ruiz','5598765432','contacto@lacteos.example','Proveedor de lácteos');
/*!40000 ALTER TABLE `proveedor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receta`
--

DROP TABLE IF EXISTS `receta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `receta` (
  `receta_id` int NOT NULL AUTO_INCREMENT,
  `producto_id` int DEFAULT NULL,
  `rendimiento` int DEFAULT NULL,
  `tiempo_estimado_min` int DEFAULT NULL,
  PRIMARY KEY (`receta_id`),
  KEY `producto_id` (`producto_id`),
  CONSTRAINT `receta_ibfk_1` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`producto_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receta`
--

LOCK TABLES `receta` WRITE;
/*!40000 ALTER TABLE `receta` DISABLE KEYS */;
INSERT INTO `receta` VALUES (1,1,1,120),(2,2,1,90),(3,3,6,30),(4,4,1,15),(5,5,10,40),(6,6,1,110),(7,7,1,130),(8,8,12,50),(9,9,6,20),(10,10,1,45);
/*!40000 ALTER TABLE `receta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receta_ingrediente`
--

DROP TABLE IF EXISTS `receta_ingrediente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `receta_ingrediente` (
  `receta_id` int NOT NULL,
  `ingrediente_id` int NOT NULL,
  `cantidad` decimal(12,4) NOT NULL,
  PRIMARY KEY (`receta_id`,`ingrediente_id`),
  KEY `ingrediente_id` (`ingrediente_id`),
  CONSTRAINT `receta_ing_ibfk_1` FOREIGN KEY (`receta_id`) REFERENCES `receta` (`receta_id`) ON DELETE CASCADE,
  CONSTRAINT `receta_ing_ibfk_2` FOREIGN KEY (`ingrediente_id`) REFERENCES `ingrediente` (`ingrediente_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receta_ingrediente`
--

LOCK TABLES `receta_ingrediente` WRITE;
/*!40000 ALTER TABLE `receta_ingrediente` DISABLE KEYS */;
INSERT INTO `receta_ingrediente` VALUES (1,1,400.0000),(1,2,200.0000),(1,3,250.0000),(1,6,80.0000),(1,8,200.0000),(2,2,120.0000),(2,9,400.0000),(2,10,150.0000),(3,1,200.0000),(3,2,100.0000),(3,4,2.0000),(3,5,50.0000),(3,7,5.0000),(4,1,100.0000),(4,2,80.0000),(4,6,50.0000),(5,2,90.0000),(5,14,180.0000),(5,16,10.0000),(6,1,200.0000),(6,11,300.0000),(6,17,150.0000),(7,1,350.0000),(7,2,200.0000),(7,16,2.0000),(8,1,120.0000),(8,2,80.0000),(8,7,10.0000),(9,1,300.0000),(9,2,180.0000),(9,13,100.0000),(10,1,150.0000),(10,12,30.0000);
/*!40000 ALTER TABLE `receta_ingrediente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rol`
--

DROP TABLE IF EXISTS `rol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol` (
  `rol_id` int NOT NULL AUTO_INCREMENT,
  `nombre_rol` varchar(50) NOT NULL,
  `descripcion` text,
  PRIMARY KEY (`rol_id`),
  UNIQUE KEY `nombre_rol` (`nombre_rol`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol`
--

LOCK TABLES `rol` WRITE;
/*!40000 ALTER TABLE `rol` DISABLE KEYS */;
INSERT INTO `rol` VALUES (1,'ADMIN','Administrador total del sistema'),(2,'GERENTE','Control y supervisión general'),(3,'REPOSTERO','Personal de producción (repostería)'),(4,'CAJERO','Atiende ventas y caja'),(5,'SUPERVISOR','Supervisa la operación'),(6,'MARKETING','Marketing y promociones'),(7,'REPARTIDOR','Entrega de pedidos');
/*!40000 ALTER TABLE `rol` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rol_permiso`
--

DROP TABLE IF EXISTS `rol_permiso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol_permiso` (
  `rol_id` int NOT NULL,
  `permiso_id` int NOT NULL,
  PRIMARY KEY (`rol_id`,`permiso_id`),
  KEY `permiso_id` (`permiso_id`),
  CONSTRAINT `rol_permiso_ibfk_1` FOREIGN KEY (`rol_id`) REFERENCES `rol` (`rol_id`) ON DELETE CASCADE,
  CONSTRAINT `rol_permiso_ibfk_2` FOREIGN KEY (`permiso_id`) REFERENCES `permiso` (`permiso_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol_permiso`
--

LOCK TABLES `rol_permiso` WRITE;
/*!40000 ALTER TABLE `rol_permiso` DISABLE KEYS */;
INSERT INTO `rol_permiso` VALUES (1,1),(2,1),(1,2),(2,2),(1,3),(2,3),(1,4),(2,4),(5,4),(6,4),(1,5),(2,5),(1,6),(2,6),(1,7),(2,7),(1,8),(2,8),(3,8),(5,8),(1,9),(2,9),(1,10),(2,10),(3,10),(5,10),(1,11),(4,11),(1,12),(2,12),(4,12),(1,13),(1,14),(2,14),(4,14),(5,14),(1,15),(1,16),(2,16),(5,16),(6,16),(1,17),(7,17),(1,18),(7,18);
/*!40000 ALTER TABLE `rol_permiso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `unidad_medida`
--

DROP TABLE IF EXISTS `unidad_medida`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `unidad_medida` (
  `unidad_medida_id` int NOT NULL AUTO_INCREMENT,
  `simbolo` varchar(10) NOT NULL,
  `descripcion` varchar(60) DEFAULT NULL,
  PRIMARY KEY (`unidad_medida_id`),
  UNIQUE KEY `simbolo` (`simbolo`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `unidad_medida`
--

LOCK TABLES `unidad_medida` WRITE;
/*!40000 ALTER TABLE `unidad_medida` DISABLE KEYS */;
INSERT INTO `unidad_medida` VALUES (1,'g','Gramos'),(2,'kg','Kilogramos'),(3,'ml','Mililitros'),(4,'l','Litros'),(5,'u','Unidad(es)');
/*!40000 ALTER TABLE `unidad_medida` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `v_clientes`
--

DROP TABLE IF EXISTS `v_clientes`;
/*!50001 DROP VIEW IF EXISTS `v_clientes`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_clientes` AS SELECT 
 1 AS `cliente_id`,
 1 AS `nombre_completo`,
 1 AS `nombre`,
 1 AS `ap_paterno`,
 1 AS `ap_materno`,
 1 AS `telefono`,
 1 AS `correo`,
 1 AS `calle`,
 1 AS `numero`,
 1 AS `colonia`,
 1 AS `ciudad`,
 1 AS `cp`,
 1 AS `creado_en`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_empleados`
--

DROP TABLE IF EXISTS `v_empleados`;
/*!50001 DROP VIEW IF EXISTS `v_empleados`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_empleados` AS SELECT 
 1 AS `empleado_id`,
 1 AS `nombre_usuario`,
 1 AS `nombre_completo`,
 1 AS `nombre`,
 1 AS `ap_paterno`,
 1 AS `ap_materno`,
 1 AS `nombre_rol`,
 1 AS `bloqueado`,
 1 AS `creado_en`,
 1 AS `dir_alias`,
 1 AS `dir_calle`,
 1 AS `dir_numero`,
 1 AS `dir_colonia`,
 1 AS `dir_ciudad`,
 1 AS `dir_estado`,
 1 AS `dir_cp`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_proveedores`
--

DROP TABLE IF EXISTS `v_proveedores`;
/*!50001 DROP VIEW IF EXISTS `v_proveedores`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_proveedores` AS SELECT 
 1 AS `proveedor_id`,
 1 AS `nombre`,
 1 AS `contacto_completo`,
 1 AS `contacto_nombre`,
 1 AS `contacto_ap_paterno`,
 1 AS `contacto_ap_materno`,
 1 AS `telefono`,
 1 AS `correo`,
 1 AS `dir_alias`,
 1 AS `dir_calle`,
 1 AS `dir_numero`,
 1 AS `dir_colonia`,
 1 AS `dir_ciudad`,
 1 AS `dir_estado`,
 1 AS `dir_cp`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `venta`
--

DROP TABLE IF EXISTS `venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `venta` (
  `venta_id` int NOT NULL AUTO_INCREMENT,
  `empleado_id` int DEFAULT NULL,
  `cliente_id` int DEFAULT NULL,
  `metodo_pago_id` int DEFAULT NULL,
  `estado_venta_id` int DEFAULT NULL,
  `fecha_venta` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_entrega_programada` timestamp NULL DEFAULT NULL,
  `total` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`venta_id`),
  KEY `empleado_id` (`empleado_id`),
  KEY `cliente_id` (`cliente_id`),
  KEY `metodo_pago_id` (`metodo_pago_id`),
  KEY `estado_venta_id` (`estado_venta_id`),
  CONSTRAINT `venta_ibfk_1` FOREIGN KEY (`empleado_id`) REFERENCES `empleado` (`empleado_id`),
  CONSTRAINT `venta_ibfk_2` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`cliente_id`),
  CONSTRAINT `venta_ibfk_3` FOREIGN KEY (`metodo_pago_id`) REFERENCES `metodo_pago` (`metodo_pago_id`),
  CONSTRAINT `venta_ibfk_4` FOREIGN KEY (`estado_venta_id`) REFERENCES `estado_venta` (`estado_venta_id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `venta`
--

LOCK TABLES `venta` WRITE;
/*!40000 ALTER TABLE `venta` DISABLE KEYS */;
INSERT INTO `venta` VALUES (1,4,1,1,2,'2025-01-10 12:00:00','2025-01-16 01:14:45',850.00),(2,4,2,2,1,'2025-01-11 12:00:00','2025-01-16 01:20:45',1200.00),(3,4,3,1,1,'2025-01-12 12:00:00','2025-01-16 01:22:45',450.00),(4,4,4,2,1,'2025-01-12 12:00:00','2025-01-16 01:30:45',980.00),(5,4,5,1,1,'2025-01-13 12:00:00','2025-01-16 01:01:45',650.00),(6,4,1,2,1,'2025-01-13 12:00:00','2025-01-16 01:10:45',1300.00),(7,4,2,1,1,'2025-01-14 12:00:00','2025-01-16 01:29:45',300.00),(8,4,3,3,1,'2025-01-14 12:00:00','2025-01-16 01:45:45',1100.00),(9,4,4,1,1,'2025-01-15 12:00:00','2025-01-16 01:35:45',780.00),(10,4,5,2,2,'2025-01-15 12:00:00','2025-01-16 01:02:45',1020.00),(19,4,6,1,2,'2026-05-27 23:41:07','2026-05-30 23:41:14',50.00),(20,4,8,1,2,'2026-06-02 06:00:00','2026-06-15 06:00:00',6500.00),(21,NULL,1,1,2,'2026-06-02 06:00:00','2026-06-03 06:00:00',1275.00),(22,NULL,1,1,2,'2026-06-02 06:00:00','2026-06-03 06:00:00',1040.00),(23,4,9,2,2,'2026-06-02 06:00:00','2026-06-03 22:48:18',1680.00),(24,4,6,1,2,'2026-06-05 06:00:00','2026-06-13 03:30:20',300.00),(25,NULL,1,1,1,'2026-09-08 06:00:00','2026-09-09 06:00:00',1800.00),(26,4,10,1,2,'2026-09-08 06:00:00','2026-09-18 06:00:00',150.00),(27,4,10,1,2,'2026-09-08 06:00:00','2026-09-18 06:00:00',150.00);
/*!40000 ALTER TABLE `venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Final view structure for view `v_clientes`
--

/*!50001 DROP VIEW IF EXISTS `v_clientes`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb3 */;
/*!50001 SET character_set_results     = utf8mb3 */;
/*!50001 SET collation_connection      = utf8mb3_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_clientes` AS select `c`.`cliente_id` AS `cliente_id`,concat_ws(' ',`c`.`nombre`,`c`.`ap_paterno`,`c`.`ap_materno`) AS `nombre_completo`,`c`.`nombre` AS `nombre`,`c`.`ap_paterno` AS `ap_paterno`,`c`.`ap_materno` AS `ap_materno`,`c`.`telefono` AS `telefono`,`c`.`correo` AS `correo`,`d`.`calle` AS `calle`,`d`.`numero` AS `numero`,`d`.`colonia` AS `colonia`,`d`.`ciudad` AS `ciudad`,`d`.`cp` AS `cp`,`c`.`creado_en` AS `creado_en` from (`cliente` `c` left join `direccion_cliente` `d` on(((`c`.`cliente_id` = `d`.`cliente_id`) and (`d`.`es_principal` = 1)))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_empleados`
--

/*!50001 DROP VIEW IF EXISTS `v_empleados`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb3 */;
/*!50001 SET character_set_results     = utf8mb3 */;
/*!50001 SET collation_connection      = utf8mb3_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_empleados` AS select `e`.`empleado_id` AS `empleado_id`,`e`.`nombre_usuario` AS `nombre_usuario`,concat_ws(' ',`e`.`nombre`,`e`.`ap_paterno`,`e`.`ap_materno`) AS `nombre_completo`,`e`.`nombre` AS `nombre`,`e`.`ap_paterno` AS `ap_paterno`,`e`.`ap_materno` AS `ap_materno`,`r`.`nombre_rol` AS `nombre_rol`,`e`.`bloqueado` AS `bloqueado`,`e`.`creado_en` AS `creado_en`,`d`.`alias` AS `dir_alias`,`d`.`calle` AS `dir_calle`,`d`.`numero` AS `dir_numero`,`d`.`colonia` AS `dir_colonia`,`d`.`ciudad` AS `dir_ciudad`,`d`.`estado_geo` AS `dir_estado`,`d`.`cp` AS `dir_cp` from ((`empleado` `e` left join `rol` `r` on((`e`.`rol_id` = `r`.`rol_id`))) left join `direccion_empleado` `d` on(((`e`.`empleado_id` = `d`.`empleado_id`) and (`d`.`es_principal` = 1)))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_proveedores`
--

/*!50001 DROP VIEW IF EXISTS `v_proveedores`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb3 */;
/*!50001 SET character_set_results     = utf8mb3 */;
/*!50001 SET collation_connection      = utf8mb3_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_proveedores` AS select `p`.`proveedor_id` AS `proveedor_id`,`p`.`nombre` AS `nombre`,concat_ws(' ',`p`.`contacto_nombre`,`p`.`contacto_ap_paterno`,`p`.`contacto_ap_materno`) AS `contacto_completo`,`p`.`contacto_nombre` AS `contacto_nombre`,`p`.`contacto_ap_paterno` AS `contacto_ap_paterno`,`p`.`contacto_ap_materno` AS `contacto_ap_materno`,`p`.`telefono` AS `telefono`,`p`.`correo` AS `correo`,`d`.`alias` AS `dir_alias`,`d`.`calle` AS `dir_calle`,`d`.`numero` AS `dir_numero`,`d`.`colonia` AS `dir_colonia`,`d`.`ciudad` AS `dir_ciudad`,`d`.`estado_geo` AS `dir_estado`,`d`.`cp` AS `dir_cp` from (`proveedor` `p` left join `direccion_proveedor` `d` on(((`p`.`proveedor_id` = `d`.`proveedor_id`) and (`d`.`es_principal` = 1)))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-08  9:33:26
