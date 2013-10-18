DELIMITER $$
CREATE DEFINER=`fkpk`@`%` PROCEDURE `master_pull`(in weapon_id int(11), in starting_timestamp int(11), in ending_timestamp int(11), out kills int, out uniques int, out kpu float, out avgbr float, out q1kpu float, out q2kpu float, out q3kpu float, out q4kpu float)
BEGIN
	select count(*), count(distinct attacker_character_id), (count(*)/count(distinct attacker_character_id)), avg(br_value)
	into kills, uniques, kpu, avgbr
	from fkpk.v2_kills
	where attacker_weapon_id = weapon_id
	and timestamp between starting_timestamp and ending_timestamp;

	select (count(*)/count(distinct attacker_character_id))
	into q1kpu
	from fkpk.v2_kills
	where attacker_weapon_id = weapon_id
	and br_value between 1 and 25
	and timestamp between starting_timestamp and ending_timestamp;
	
	select (count(*)/count(distinct attacker_character_id))
	into q2kpu
	from fkpk.v2_kills
	where attacker_weapon_id = weapon_id
	and br_value between 26 and 50
	and timestamp between starting_timestamp and ending_timestamp;
	
	select (count(*)/count(distinct attacker_character_id))
	into q3kpu
	from fkpk.v2_kills
	where attacker_weapon_id = weapon_id
	and br_value between 51 and 75
	and timestamp between starting_timestamp and ending_timestamp;
	
	select (count(*)/count(distinct attacker_character_id))
	into q4kpu
	from fkpk.v2_kills
	where attacker_weapon_id = weapon_id
	and br_value between 76 and 100
	and timestamp between starting_timestamp and ending_timestamp;
END$$
DELIMITER ;

