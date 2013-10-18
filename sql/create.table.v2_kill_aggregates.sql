CREATE TABLE `v2_kill_aggregates` (
  `item_id` int(11) NOT NULL,
  `period` int(11) NOT NULL,
  `starting_timestamp` int(11) NOT NULL,
  `ending_timestamp` int(11) NOT NULL,
  `kills` int(11) DEFAULT NULL,
  `uniques` int(11) DEFAULT NULL,
  `kpu` float DEFAULT NULL,
  `avgbr` float DEFAULT NULL,
  `q1kpu` float DEFAULT NULL,
  `q2kpu` float DEFAULT NULL,
  `q3kpu` float DEFAULT NULL,
  `q4kpu` float DEFAULT NULL,
  PRIMARY KEY (`item_id`,`period`),
  KEY `period` (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

