delimiter $$

CREATE TABLE `v2_kills` (
  `attacker_character_id` bigint(20) NOT NULL,
  `attacker_vehicle_id` int(11) DEFAULT NULL,
  `attacker_weapon_id` int(11) DEFAULT NULL,
  `character_id` bigint(20) NOT NULL,
  `is_critical` int(11) DEFAULT NULL,
  `is_headshot` int(11) DEFAULT NULL,
  `timestamp` int(11) NOT NULL,
  `world_id` int(11) DEFAULT NULL,
  `zone_id` int(11) DEFAULT NULL,
  `faction_id` int(11) DEFAULT NULL,
  `br_value` int(11) DEFAULT NULL,
  PRIMARY KEY (`attacker_character_id`,`character_id`,`timestamp`),
  KEY `weapon` (`attacker_weapon_id`),
  KEY `time` (`timestamp`),
  KEY `br` (`br_value`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1$$


