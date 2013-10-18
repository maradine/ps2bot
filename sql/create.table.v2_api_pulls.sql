CREATE TABLE `v2_api_pulls` (
  `pull_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `timestamp` int(10) unsigned DEFAULT NULL,
  `dupes` int(10) unsigned DEFAULT NULL,
  `first_timestamp_pulled` int(11) DEFAULT NULL,
  `last_timestamp_pulled` int(11) DEFAULT NULL,
  `write_time` int(10) unsigned DEFAULT NULL,
  `interval` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`pull_id`)
) ENGINE=InnoDB AUTO_INCREMENT=170574 DEFAULT CHARSET=latin1;

