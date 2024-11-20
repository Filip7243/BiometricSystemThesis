-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Nov 19, 2024 at 01:12 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `test`
--

-- --------------------------------------------------------

--
-- Table structure for table `building`
--

CREATE TABLE `building` (
  `id` bigint(20) NOT NULL,
  `building_number` varchar(255) DEFAULT NULL,
  `street` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Table structure for table `device`
--

CREATE TABLE `device` (
  `device_hardware_id` bigint(20) DEFAULT NULL,
  `room_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Table structure for table `fingerprint`
--

CREATE TABLE `fingerprint` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  `finger_type` enum('INDEX','MIDDLE','THUMB') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Table structure for table `room`
--

CREATE TABLE `room` (
  `floor` int(11) DEFAULT NULL,
  `building_id` bigint(20) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  `room_number` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `id` bigint(20) NOT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `pesel` varchar(255) DEFAULT NULL,
  `role` enum('ADMIN','USER') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Table structure for table `user_rooms`
--

CREATE TABLE `user_rooms` (
  `room_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `building`
--
ALTER TABLE `building`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `device`
--
ALTER TABLE `device`
  ADD PRIMARY KEY (`room_id`);

--
-- Indexes for table `fingerprint`
--
ALTER TABLE `fingerprint`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK93rd2gj6n8aoiy8i471xtkepx` (`user_id`);

--
-- Indexes for table `room`
--
ALTER TABLE `room`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK4kmfw73x2vpfymk0ml875rh2q` (`building_id`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `user_rooms`
--
ALTER TABLE `user_rooms`
  ADD PRIMARY KEY (`room_id`,`user_id`),
  ADD KEY `FKnaagcdybm13h6qo6whb8t8hyx` (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `building`
--
ALTER TABLE `building`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `fingerprint`
--
ALTER TABLE `fingerprint`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `room`
--
ALTER TABLE `room`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `device`
--
ALTER TABLE `device`
  ADD CONSTRAINT `FK8nnipnjiephps7atqdnvmfndp` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`);

--
-- Constraints for table `fingerprint`
--
ALTER TABLE `fingerprint`
  ADD CONSTRAINT `FK93rd2gj6n8aoiy8i471xtkepx` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Constraints for table `room`
--
ALTER TABLE `room`
  ADD CONSTRAINT `FK4kmfw73x2vpfymk0ml875rh2q` FOREIGN KEY (`building_id`) REFERENCES `building` (`id`);

--
-- Constraints for table `user_rooms`
--
ALTER TABLE `user_rooms`
  ADD CONSTRAINT `FK9jaw0akshh0uea53aj3je1shc` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`),
  ADD CONSTRAINT `FKnaagcdybm13h6qo6whb8t8hyx` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
