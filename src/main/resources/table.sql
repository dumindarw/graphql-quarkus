CREATE TABLE `tbl_mygroups` (
	`id` VARCHAR(50) NOT NULL DEFAULT '',
	`name` VARCHAR(100) NULL DEFAULT NULL,
	`createdBy` VARCHAR(50) NULL DEFAULT NULL,
	`createdDate` DATE NULL DEFAULT NULL,
	`blacklisted` INT(11) NULL DEFAULT NULL,
	PRIMARY KEY (`id`)
)
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;
