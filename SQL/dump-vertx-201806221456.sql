-- MySQL dump 10.16  Distrib 10.1.30-MariaDB, for Win32 (AMD64)
--
-- Host: 192.168.10.10    Database: vertx
-- ------------------------------------------------------
-- Server version	5.5.60-0ubuntu0.14.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `CC_animales_comidas`
--

DROP TABLE IF EXISTS `CC_animales_comidas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CC_animales_comidas` (
  `CC_animal_ID` int(11) NOT NULL,
  `CC_comida_ID` int(11) NOT NULL,
  `status` int(11) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` int(11) NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  KEY `fk__CC_animal_comida` (`CC_animal_ID`),
  KEY `fk_CC_comida_animal` (`CC_comida_ID`),
  CONSTRAINT `fk_CC_comida_animal` FOREIGN KEY (`CC_comida_ID`) REFERENCES `CC_comida` (`ID`),
  CONSTRAINT `fk__CC_animal_comida` FOREIGN KEY (`CC_animal_ID`) REFERENCES `CC_animal` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CC_animales_comidas`
--

LOCK TABLES `CC_animales_comidas` WRITE;
/*!40000 ALTER TABLE `CC_animales_comidas` DISABLE KEYS */;
INSERT INTO `CC_animales_comidas` VALUES (1,1,1,'2018-06-22 14:47:21',1,NULL,NULL),(1,1,1,'2018-06-22 14:47:40',1,NULL,NULL),(1,1,1,'2018-06-22 14:49:14',1,NULL,NULL),(1,1,1,'2018-06-22 14:50:34',1,NULL,NULL),(1,1,1,'2018-06-22 14:52:13',1,NULL,NULL);
/*!40000 ALTER TABLE `CC_animales_comidas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CC_animal`
--

DROP TABLE IF EXISTS `CC_animal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CC_animal` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  `CC_persona_ID` int(11) NOT NULL,
  `status` int(11) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` int(11) NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `fk_CC_animal_persona` (`CC_persona_ID`),
  CONSTRAINT `fk_CC_animal_persona` FOREIGN KEY (`CC_persona_ID`) REFERENCES `CC_persona` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CC_animal`
--

LOCK TABLES `CC_animal` WRITE;
/*!40000 ALTER TABLE `CC_animal` DISABLE KEYS */;
INSERT INTO `CC_animal` VALUES (1,'Perro',1,1,'2018-06-20 18:31:24',1,NULL,NULL),(3,'Gato',2,1,'2018-06-20 18:33:04',1,NULL,NULL),(4,'León',1,1,'2018-06-20 18:33:25',1,NULL,NULL),(5,'Chango',5,1,'2018-06-22 19:32:00',1,NULL,NULL),(6,'Halcon',5,1,'2018-06-22 19:32:00',1,NULL,NULL),(7,'Tiburon',5,1,'2018-06-22 19:32:00',1,NULL,NULL);
/*!40000 ALTER TABLE `CC_animal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CC_persona`
--

DROP TABLE IF EXISTS `CC_persona`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CC_persona` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  `status` int(11) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` int(11) NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CC_persona`
--

LOCK TABLES `CC_persona` WRITE;
/*!40000 ALTER TABLE `CC_persona` DISABLE KEYS */;
INSERT INTO `CC_persona` VALUES (1,'Carlos',1,'2018-06-20 18:10:54',1,NULL,NULL),(2,'Daniel',1,'2018-06-20 18:32:47',1,NULL,NULL),(3,'CarlosContreras',1,'2018-06-22 15:43:51',1,NULL,NULL),(4,'Carlos$',1,'2018-06-22 15:44:17',1,NULL,NULL),(5,'Chuy',1,'2018-06-22 19:32:00',1,NULL,NULL);
/*!40000 ALTER TABLE `CC_persona` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CC_comida`
--

DROP TABLE IF EXISTS `CC_comida`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CC_comida` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  `status` int(11) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` int(11) NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CC_comida`
--

LOCK TABLES `CC_comida` WRITE;
/*!40000 ALTER TABLE `CC_comida` DISABLE KEYS */;
INSERT INTO `CC_comida` VALUES (1,'Pollo',1,'2018-06-22 14:45:41',1,NULL,NULL),(2,'Arroz',1,'2018-06-22 14:45:48',1,NULL,NULL);
/*!40000 ALTER TABLE `CC_comida` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-06-22 14:56:00
