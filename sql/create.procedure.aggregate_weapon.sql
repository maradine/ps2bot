DELIMITER $$
CREATE DEFINER=`fkpk`@`%` PROCEDURE `aggregate_weapon`(in weapon_id int(11), in starting_timestamp int(11), in ending_timestamp int(11))
BEGIN
	DECLARE period_id int;
	
	INSERT INTO fkpk.v2_time_periods (starting_time, ending_time) VALUES (@front, @back);
	SELECT last_insert_id() into period_id;

	call fkpk.master_pull(weapon_id, starting_timestamp, ending_timestamp, @kills, @uniques, @kpu, @avgbr, @q1kpu, @q2kpu, @q3kpu, @q4kpu);
	insert into fkpk.v2_kill_aggregates
	values (weapon_id, period_id, starting_timestamp, ending_timestamp, @kills, @uniques, @kpu, @avgbr, @q1kpu, @q2kpu, @q3kpu, @q4kpu);


END$$
DELIMITER ;

