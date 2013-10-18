DELIMITER $$
CREATE DEFINER=`fkpk`@`%` PROCEDURE `get_time_brackets`(out front int, out back int, out week int)
BEGIN
	SELECT UNIX_TIMESTAMP(NOW()) into back;
	SELECT UNIX_TIMESTAMP(NOW() - INTERVAL 24 HOUR) into front;
	SELECT UNIX_TIMESTAMP(NOW() - INTERVAL 1 WEEK) into week;


END$$
DELIMITER ;

