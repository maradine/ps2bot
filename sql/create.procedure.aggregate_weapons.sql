DELIMITER $$
CREATE DEFINER=`fkpk`@`%` PROCEDURE `aggregate_weapons`(in period int(11), in starting_timestamp int(11), in ending_timestamp int(11))
BEGIN

	declare w int;
	DECLARE no_more_rows BOOLEAN;

	declare weapons cursor for select id from fkpk.v2_weapons where is_killer=1 order by id asc;

	DECLARE CONTINUE HANDLER FOR NOT FOUND
    SET no_more_rows = TRUE;
	
	open weapons;
	
	do_loop: loop
		fetch weapons into w;

		IF no_more_rows THEN
			CLOSE weapons;
			LEAVE do_loop;
		END IF;
		call fkpk.master_pull(w, starting_timestamp, ending_timestamp, @kills, @uniques, @kpu, @avgbr, @q1kpu, @q2kpu, @q3kpu, @q4kpu);
	
		insert into fkpk.v2_kill_aggregates
		values (w, period, starting_timestamp, ending_timestamp, @kills, @uniques, @kpu, @avgbr, @q1kpu, @q2kpu, @q3kpu, @q4kpu);
	end loop;


END$$
DELIMITER ;

