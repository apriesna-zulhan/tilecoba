-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Dec 03, 2025 at 05:13 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `shadowtrail`
--

-- --------------------------------------------------------

--
-- Table structure for table `levels`
--

CREATE TABLE `levels` (
  `id` int(11) NOT NULL,
  `grid_rows` int(11) NOT NULL,
  `grid_cols` int(11) NOT NULL,
  `hazard_count` int(11) NOT NULL,
  `reveal_time_ms` int(11) NOT NULL,
  `min_path_len` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `levels`
--

INSERT INTO `levels` (`id`, `grid_rows`, `grid_cols`, `hazard_count`, `reveal_time_ms`, `min_path_len`) VALUES
(1, 5, 5, 3, 1200, 4),
(2, 6, 6, 5, 1200, 5),
(3, 7, 7, 7, 1200, 6),
(4, 8, 8, 10, 1200, 7),
(5, 9, 9, 13, 1200, 8);

-- --------------------------------------------------------

--
-- Table structure for table `level_progress`
--

CREATE TABLE `level_progress` (
  `id` bigint(20) NOT NULL,
  `save_id` bigint(20) NOT NULL,
  `level_id` int(11) NOT NULL,
  `current_round` int(11) DEFAULT 1,
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `level_scores`
--

CREATE TABLE `level_scores` (
  `id` bigint(20) NOT NULL,
  `save_id` bigint(20) NOT NULL,
  `level_id` int(11) NOT NULL,
  `level_score` bigint(20) NOT NULL DEFAULT 0,
  `completed_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `is_completed` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `level_scores`
--

INSERT INTO `level_scores` (`id`, `save_id`, `level_id`, `level_score`, `completed_at`, `is_completed`) VALUES
(21, 10, 1, 600, '2025-12-03 02:19:22', 1),
(22, 10, 2, 470, '2025-12-03 02:20:05', 1),
(23, 10, 3, 275, '2025-12-03 03:10:41', 1),
(29, 10, 4, -115, '2025-12-03 03:57:33', 1),
(32, 11, 1, 600, '2025-12-03 04:02:18', 1),
(33, 11, 2, 600, '2025-12-03 04:02:49', 1),
(34, 11, 3, 405, '2025-12-03 04:03:40', 1);

-- --------------------------------------------------------

--
-- Table structure for table `level_temp_score`
--

CREATE TABLE `level_temp_score` (
  `save_id` bigint(20) NOT NULL,
  `level_id` int(11) NOT NULL,
  `temp_score` bigint(20) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `level_temp_score`
--

INSERT INTO `level_temp_score` (`save_id`, `level_id`, `temp_score`) VALUES
(10, 1, 600),
(10, 2, 405),
(10, 3, 15);

-- --------------------------------------------------------

--
-- Table structure for table `rounds`
--

CREATE TABLE `rounds` (
  `id` bigint(20) NOT NULL,
  `save_id` bigint(20) NOT NULL,
  `level` int(11) NOT NULL,
  `round_idx` int(11) NOT NULL,
  `round_elapsed_ms` bigint(20) NOT NULL,
  `round_score` bigint(20) NOT NULL,
  `steps` int(11) NOT NULL DEFAULT 0,
  `hazard_touches` int(11) NOT NULL DEFAULT 0,
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `rounds`
--

INSERT INTO `rounds` (`id`, `save_id`, `level`, `round_idx`, `round_elapsed_ms`, `round_score`, `steps`, `hazard_touches`, `created_at`) VALUES
(203, 10, 1, 1, 4378, 100, 5, 0, '2025-12-03 02:19:01'),
(204, 10, 1, 2, 4234, 100, 5, 0, '2025-12-03 02:19:06'),
(205, 10, 1, 3, 4480, 100, 7, 0, '2025-12-03 02:19:10'),
(206, 10, 1, 4, 4657, 100, 7, 0, '2025-12-03 02:19:15'),
(207, 10, 1, 5, 3324, 100, 5, 0, '2025-12-03 02:19:18'),
(208, 10, 1, 6, 4094, 100, 6, 0, '2025-12-03 02:19:22'),
(209, 10, 2, 1, 7519, 100, 8, 0, '2025-12-03 02:19:32'),
(210, 10, 2, 2, 4786, 100, 7, 0, '2025-12-03 02:19:37'),
(211, 10, 2, 3, 4951, 100, 6, 0, '2025-12-03 02:19:42'),
(212, 10, 2, 4, 4617, 100, 7, 1, '2025-12-03 02:19:52'),
(213, 10, 2, 5, 4904, 100, 8, 1, '2025-12-03 02:19:57'),
(214, 10, 2, 6, 4347, 100, 6, 2, '2025-12-03 02:20:05'),
(215, 10, 3, 1, 7778, 100, 11, 0, '2025-12-03 02:20:18'),
(216, 10, 3, 2, 5488, 100, 8, 2, '2025-12-03 02:21:00'),
(217, 10, 3, 3, 7907, 100, 9, 3, '2025-12-03 02:21:14'),
(218, 10, 3, 4, 6011, 100, 8, 3, '2025-12-03 02:21:20'),
(219, 10, 3, 5, 5008, 100, 8, 3, '2025-12-03 02:21:25'),
(220, 10, 3, 6, 5400, 100, 8, 3, '2025-12-03 02:21:31'),
(221, 10, 1, 1, 4244, 100, 5, 0, '2025-12-03 02:22:09'),
(222, 10, 1, 2, 3558, 100, 5, 0, '2025-12-03 02:22:13'),
(223, 10, 1, 3, 4034, 100, 6, 0, '2025-12-03 02:22:17'),
(224, 10, 1, 4, 4605, 100, 7, 0, '2025-12-03 02:22:21'),
(225, 10, 1, 5, 3536, 100, 5, 1, '2025-12-03 02:22:29'),
(226, 10, 1, 6, 3490, 100, 5, 1, '2025-12-03 02:22:32'),
(227, 10, 2, 1, 4278, 100, 6, 0, '2025-12-03 02:22:48'),
(228, 10, 2, 2, 6434, 100, 6, 0, '2025-12-03 02:22:54'),
(229, 10, 2, 3, 6473, 100, 9, 0, '2025-12-03 02:23:01'),
(230, 10, 3, 1, 10824, 100, 8, 0, '2025-12-03 02:24:22'),
(231, 10, 3, 1, 5033, 100, 8, 1, '2025-12-03 02:39:08'),
(232, 10, 3, 2, 4696, 100, 7, 1, '2025-12-03 02:39:13'),
(233, 10, 3, 3, 5291, 100, 8, 4, '2025-12-03 02:39:34'),
(234, 10, 3, 4, 5608, 100, 9, 4, '2025-12-03 02:39:39'),
(235, 10, 3, 5, 4319, 100, 7, 7, '2025-12-03 02:40:01'),
(236, 10, 3, 6, 4535, 100, 7, 7, '2025-12-03 02:40:05'),
(237, 10, 3, 1, 4752, 100, 7, 0, '2025-12-03 02:40:15'),
(238, 10, 3, 2, 5012, 100, 8, 0, '2025-12-03 02:40:20'),
(239, 10, 3, 3, 5584, 100, 8, 1, '2025-12-03 02:40:30'),
(240, 10, 3, 4, 4611, 100, 7, 2, '2025-12-03 02:40:40'),
(241, 10, 3, 5, 5190, 100, 9, 2, '2025-12-03 02:40:45'),
(242, 10, 3, 6, 4509, 100, 7, 2, '2025-12-03 02:40:50'),
(243, 10, 3, 1, 8510, 100, 8, 0, '2025-12-03 02:50:15'),
(244, 10, 3, 2, 5752, 100, 10, 1, '2025-12-03 02:50:53'),
(245, 10, 3, 3, 5609, 100, 9, 1, '2025-12-03 02:50:58'),
(246, 10, 3, 4, 5292, 100, 8, 5, '2025-12-03 02:51:21'),
(247, 10, 3, 5, 4305, 100, 7, 6, '2025-12-03 02:51:32'),
(248, 10, 3, 6, 7077, 100, 10, 6, '2025-12-03 02:51:39'),
(249, 10, 3, 1, 7371, 100, 7, 0, '2025-12-03 03:08:29'),
(250, 10, 3, 2, 6004, 100, 7, 0, '2025-12-03 03:08:35'),
(251, 10, 3, 3, 5773, 100, 10, 1, '2025-12-03 03:08:47'),
(252, 10, 3, 4, 5644, 100, 7, 4, '2025-12-03 03:09:06'),
(253, 10, 3, 5, 6694, 100, 9, 6, '2025-12-03 03:09:21'),
(254, 10, 3, 6, 8961, 100, 14, 6, '2025-12-03 03:09:30'),
(255, 10, 3, 1, 5386, 100, 8, 0, '2025-12-03 03:09:51'),
(256, 10, 3, 2, 5046, 100, 7, 3, '2025-12-03 03:10:10'),
(257, 10, 3, 3, 4489, 100, 7, 3, '2025-12-03 03:10:15'),
(258, 10, 3, 4, 5034, 100, 8, 3, '2025-12-03 03:10:20'),
(259, 10, 3, 5, 5243, 100, 8, 5, '2025-12-03 03:10:35'),
(260, 10, 3, 6, 5665, 100, 9, 5, '2025-12-03 03:10:41'),
(261, 10, 3, 1, 10728, 100, 7, 0, '2025-12-03 03:41:59'),
(262, 10, 3, 2, 4781, 100, 7, 1, '2025-12-03 03:42:30'),
(263, 10, 3, 3, 6560, 100, 7, 1, '2025-12-03 03:42:37'),
(264, 10, 3, 4, 5869, 100, 8, 6, '2025-12-03 03:43:14'),
(265, 10, 3, 5, 5502, 100, 8, 7, '2025-12-03 03:43:26'),
(266, 10, 3, 6, 5243, 100, 7, 9, '2025-12-03 03:43:44'),
(267, 10, 4, 1, 5049, 100, 8, 0, '2025-12-03 03:44:05'),
(268, 10, 4, 2, 5902, 100, 10, 0, '2025-12-03 03:44:11'),
(269, 10, 4, 3, 8956, 100, 8, 0, '2025-12-03 03:45:05'),
(270, 10, 4, 4, 5420, 100, 9, 6, '2025-12-03 03:45:41'),
(271, 10, 4, 5, 7045, 100, 12, 19, '2025-12-03 03:46:56'),
(272, 10, 4, 6, 6167, 100, 8, 20, '2025-12-03 03:47:10'),
(273, 10, 4, 1, 5199, 100, 8, 0, '2025-12-03 03:51:05'),
(274, 10, 4, 2, 5576, 100, 8, 1, '2025-12-03 03:51:17'),
(275, 10, 4, 3, 5800, 100, 9, 2, '2025-12-03 03:51:28'),
(276, 10, 4, 4, 5297, 100, 8, 6, '2025-12-03 03:51:57'),
(277, 10, 4, 5, 6159, 100, 8, 7, '2025-12-03 03:52:07'),
(278, 10, 4, 6, 6086, 100, 8, 10, '2025-12-03 03:52:28'),
(279, 10, 1, 1, 4988, 100, 6, 0, '2025-12-03 03:54:33'),
(280, 10, 1, 2, 4690, 100, 5, 0, '2025-12-03 03:54:38'),
(281, 10, 1, 3, 4738, 100, 6, 0, '2025-12-03 03:54:43'),
(282, 10, 1, 4, 3895, 100, 6, 0, '2025-12-03 03:54:47'),
(283, 10, 1, 5, 4439, 100, 7, 0, '2025-12-03 03:54:51'),
(284, 10, 1, 6, 4161, 100, 6, 0, '2025-12-03 03:54:55'),
(285, 10, 2, 4, 4179, 100, 7, 0, '2025-12-03 03:55:11'),
(286, 10, 2, 5, 6289, 100, 9, 0, '2025-12-03 03:55:17'),
(287, 10, 2, 6, 5244, 100, 9, 3, '2025-12-03 03:55:37'),
(288, 10, 4, 1, 5164, 100, 9, 1, '2025-12-03 03:56:04'),
(289, 10, 4, 2, 6236, 100, 11, 3, '2025-12-03 03:56:26'),
(290, 10, 4, 3, 6297, 100, 9, 4, '2025-12-03 03:56:36'),
(291, 10, 4, 4, 5019, 100, 8, 8, '2025-12-03 03:57:02'),
(292, 10, 4, 5, 5522, 100, 9, 10, '2025-12-03 03:57:18'),
(293, 10, 4, 6, 5766, 100, 10, 11, '2025-12-03 03:57:33'),
(294, 11, 1, 1, 4534, 100, 6, 0, '2025-12-03 04:01:56'),
(295, 11, 1, 2, 6668, 100, 11, 0, '2025-12-03 04:02:03'),
(296, 11, 1, 3, 4456, 100, 6, 0, '2025-12-03 04:02:07'),
(297, 11, 1, 4, 3381, 100, 5, 0, '2025-12-03 04:02:11'),
(298, 11, 1, 5, 4386, 100, 7, 0, '2025-12-03 04:02:15'),
(299, 11, 1, 6, 3346, 100, 5, 0, '2025-12-03 04:02:18'),
(300, 11, 2, 1, 4463, 100, 7, 0, '2025-12-03 04:02:25'),
(301, 11, 2, 2, 3919, 100, 6, 0, '2025-12-03 04:02:29'),
(302, 11, 2, 3, 5937, 100, 7, 0, '2025-12-03 04:02:34'),
(303, 11, 2, 4, 4801, 100, 6, 0, '2025-12-03 04:02:39'),
(304, 11, 2, 5, 4306, 100, 6, 0, '2025-12-03 04:02:44'),
(305, 11, 2, 6, 5532, 100, 9, 0, '2025-12-03 04:02:49'),
(306, 11, 3, 1, 6195, 100, 10, 1, '2025-12-03 04:03:03'),
(307, 11, 3, 2, 4434, 100, 7, 1, '2025-12-03 04:03:07'),
(308, 11, 3, 3, 4498, 100, 7, 1, '2025-12-03 04:03:12'),
(309, 11, 3, 4, 5298, 100, 10, 1, '2025-12-03 04:03:17'),
(310, 11, 3, 5, 5669, 100, 9, 2, '2025-12-03 04:03:28'),
(311, 11, 3, 6, 6751, 100, 7, 3, '2025-12-03 04:03:40');

-- --------------------------------------------------------

--
-- Table structure for table `saves`
--

CREATE TABLE `saves` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `name` varchar(120) NOT NULL,
  `unlocked_level` int(11) NOT NULL DEFAULT 1,
  `total_score` bigint(20) NOT NULL DEFAULT 0,
  `total_time_ms` bigint(20) NOT NULL DEFAULT 0,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `saves`
--

INSERT INTO `saves` (`id`, `user_id`, `name`, `unlocked_level`, `total_score`, `total_time_ms`, `created_at`, `updated_at`) VALUES
(10, 14, 'zulhan', 4, 1230, 0, '2025-12-03 02:18:56', '2025-12-03 03:57:33'),
(11, 15, 'mami', 3, 1605, 0, '2025-12-03 04:01:50', '2025-12-03 04:03:40');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL,
  `username` varchar(64) NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `created_at`) VALUES
(14, 'zulhan', '2025-12-03 02:18:56'),
(15, 'mami', '2025-12-03 04:01:50');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `levels`
--
ALTER TABLE `levels`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `level_progress`
--
ALTER TABLE `level_progress`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_save_level` (`save_id`,`level_id`),
  ADD KEY `idx_save_id` (`save_id`);

--
-- Indexes for table `level_scores`
--
ALTER TABLE `level_scores`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_save_level` (`save_id`,`level_id`),
  ADD KEY `idx_save_id` (`save_id`),
  ADD KEY `idx_level_id` (`level_id`);

--
-- Indexes for table `level_temp_score`
--
ALTER TABLE `level_temp_score`
  ADD PRIMARY KEY (`save_id`,`level_id`);

--
-- Indexes for table `rounds`
--
ALTER TABLE `rounds`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_round_save` (`save_id`);

--
-- Indexes for table `saves`
--
ALTER TABLE `saves`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_saves_user` (`user_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `level_progress`
--
ALTER TABLE `level_progress`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=165;

--
-- AUTO_INCREMENT for table `level_scores`
--
ALTER TABLE `level_scores`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=35;

--
-- AUTO_INCREMENT for table `rounds`
--
ALTER TABLE `rounds`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=312;

--
-- AUTO_INCREMENT for table `saves`
--
ALTER TABLE `saves`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `level_progress`
--
ALTER TABLE `level_progress`
  ADD CONSTRAINT `level_progress_ibfk_1` FOREIGN KEY (`save_id`) REFERENCES `saves` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `level_scores`
--
ALTER TABLE `level_scores`
  ADD CONSTRAINT `level_scores_ibfk_1` FOREIGN KEY (`save_id`) REFERENCES `saves` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `level_temp_score`
--
ALTER TABLE `level_temp_score`
  ADD CONSTRAINT `level_temp_score_ibfk_1` FOREIGN KEY (`save_id`) REFERENCES `saves` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `rounds`
--
ALTER TABLE `rounds`
  ADD CONSTRAINT `fk_round_save` FOREIGN KEY (`save_id`) REFERENCES `saves` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `saves`
--
ALTER TABLE `saves`
  ADD CONSTRAINT `fk_saves_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
