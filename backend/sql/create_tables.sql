-- auto-generated definition
create table user
(
    id           bigint auto_increment
        primary key,
    userName     varchar(256)                       null,
    userAccount  varchar(256)                       null,
    avatarUrl    varchar(1024)                      null,
    gender       tinyint                            null,
    userPassword varchar(512)                       not null,
    phone        varchar(128)                       null,
    email        varchar(512)                       null,
    userStatus   int      default 0                 null comment '0 - normal status',
    createTime   datetime default CURRENT_TIMESTAMP null,
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    isDelete     tinyint  default 0                 not null,
    userRole     int      default 0                 not null comment 'user role 0- normal user 1 - manger',
    invitationCode varchar(32)                      null comment 'optional invitation code'
);

