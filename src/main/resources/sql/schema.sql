CREATE TABLE IF NOT EXISTS `min_acceptable_proposal` (
 `instance_number` BIGINT NOT NULL,
 `proposal_number` BIGINT NOT NULL,
 PRIMARY KEY (`instance_number`));

CREATE TABLE IF NOT EXISTS `accepted_proposal_value` (
                                                         `instance_number` BIGINT NOT NULL,
                                                         `proposal_number` BIGINT NOT NULL,
                                                         `proposal_value` VARCHAR(255) NOT NULL,
                                                         PRIMARY KEY (`instance_number`, `proposal_number`));

CREATE TABLE IF NOT EXISTS `next_proposal_number` (
                                                      `instance_number` BIGINT NOT NULL,
                                                         `proposal_number` BIGINT NOT NULL,
                                                         PRIMARY KEY (`instance_number`));

CREATE TABLE IF NOT EXISTS `learned_value` (
                                                      `instance_number` BIGINT NOT NULL,
                                                      `proposal_value` VARCHAR(255) NOT NULL,
                                                      PRIMARY KEY (`instance_number`));

