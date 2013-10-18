CREATE TABLE `v2_weapons` (
  `id` int(11) NOT NULL,
  `faction_id` int(11) DEFAULT NULL,
  `image_id` int(11) DEFAULT NULL,
  `image_path` varchar(45) DEFAULT NULL,
  `image_set_id` int(11) DEFAULT NULL,
  `is_vehicle_weapon` int(11) DEFAULT NULL,
  `item_category_id` int(11) DEFAULT NULL,
  `item_type_id` int(11) DEFAULT NULL,
  `max_stack_size` int(11) DEFAULT NULL,
  `skill_set_id` int(11) DEFAULT NULL,
  `name` varchar(45) DEFAULT NULL,
  `description` varchar(512) DEFAULT NULL,
  `is_killer` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

