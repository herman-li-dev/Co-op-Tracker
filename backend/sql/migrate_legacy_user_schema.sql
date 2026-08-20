-- Run once only for databases created before the invitation-code rename.
-- Back up the database first. This syntax is compatible with MySQL 5.7 and 8.x.
ALTER TABLE user CHANGE COLUMN planetCode invitationCode varchar(32) null comment 'optional invitation code';
