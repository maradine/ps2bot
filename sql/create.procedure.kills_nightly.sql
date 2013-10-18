DELIMITER $$
CREATE DEFINER=`fkpk`@`%` PROCEDURE `kills_nightly`(out deleted_rows int, out time_elapsed int)
BEGIN
	DECLARE start_stamp int;
	DECLARE end_stamp int;
	DECLARE period_id int;

	SELECT UNIX_TIMESTAMP(NOW()) into start_stamp;

	-- get the timestamps
	CALL fkpk.get_time_brackets(@front, @back, @week);
	
	-- create a new time period

	INSERT INTO fkpk.v2_time_periods (start_time, end_time, is_daily) VALUES (@front, @back, 1);
	SELECT last_insert_id() into period_id;


	-- run aggregates over timestamps
	CALL fkpk.aggregate_weapons(period_id, @front, @back);

	-- delete before weekly
	DELETE FROM fkpk.v2_kills
	WHERE timestamp < @week;
	SELECT row_count() INTO deleted_rows;

	SELECT UNIX_TIMESTAMP(NOW()) into end_stamp;
	SET time_elapsed = end_stamp - start_stamp;

END$$
DELIMITER ;

