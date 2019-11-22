CREATE DATABASE IF NOT EXISTS ztk_deploy;

use ztk_deploy;

# Dump of table scm_instance_ip
# ------------------------------------------------------------

CREATE TABLE `scm_instance_ip` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip` varchar(45) DEFAULT NULL COMMENT 'server ip',
  `instance_id` varchar(45) DEFAULT NULL COMMENT '所属实例ID',
  `create_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`,`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table scm_instance_log
# ------------------------------------------------------------

CREATE TABLE `scm_instance_log` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `instance_id` varchar(45) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `oper_type` int(11) DEFAULT NULL,
  `log_message` varchar(1000) DEFAULT NULL,
  `create_date` timestamp NULL DEFAULT NULL,
  `create_by` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `instance_create_date` (`instance_id`,`create_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table scm_menu
# ------------------------------------------------------------

CREATE TABLE `scm_menu` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `menu_name` varchar(100) NOT NULL,
  `menu_url` varchar(45) NOT NULL,
  `level` int(11) NOT NULL,
  `remark` varchar(45) NOT NULL,
  `create_by` varchar(45) NOT NULL,
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table scm_project
# ------------------------------------------------------------

CREATE TABLE `scm_project` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `create_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` varchar(45) DEFAULT NULL COMMENT '创建人',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `git_url` varchar(200) NOT NULL COMMENT '工程的git地址',
  `current_tag` varchar(45) DEFAULT NULL COMMENT '该工程的当前最大tag号',
  `project_name` varchar(45) NOT NULL,
  `newest_tag` varchar(45) DEFAULT NULL,
  `test_branch` varchar(45) DEFAULT NULL,
  `develop_branch` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `git_url` (`git_url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table scm_project_permissions
# ------------------------------------------------------------

CREATE TABLE `scm_project_permissions` (
  `user_code` varchar(45) NOT NULL,
  `project_id` varchar(45) NOT NULL,
  `permissions` varchar(45) DEFAULT NULL,
  `create_by` varchar(45) DEFAULT NULL,
  `create_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_code`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table scm_server_instance
# ------------------------------------------------------------

CREATE TABLE `scm_server_instance` (
  `id` varchar(45) NOT NULL COMMENT '服务器实例表',
  `main_class` varchar(100) DEFAULT NULL COMMENT '工程主执行类',
  `main_args` varchar(400) DEFAULT NULL COMMENT '工程参数',
  `jvm_args` varchar(1000) DEFAULT NULL COMMENT 'java虚拟机参数 -D参数,如gc的配置',
  `create_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `create_by` varchar(45) DEFAULT NULL COMMENT '创建人',
  `source_path` varchar(100) DEFAULT NULL COMMENT 'server的部署程序路径',
  `server_mode` varchar(45) DEFAULT NULL COMMENT '运行模式：online，test，devlop',
  `server_name` varchar(45) NOT NULL COMMENT '实例的server名称,唯一存在的',
  `project_name` varchar(45) DEFAULT NULL COMMENT '实例project name即git@10.10.76.42:sohu-scm.git 中的 sohu-scm ,该属性作为一个组的作用存在',
  `remark` varchar(300) DEFAULT NULL COMMENT '实例备注',
  `logformat` tinyint(1) DEFAULT '0' COMMENT '日志合法',
  PRIMARY KEY (`id`),
  UNIQUE KEY `server_name` (`server_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table scm_server_instance_permissions
# ------------------------------------------------------------

CREATE TABLE `scm_server_instance_permissions` (
  `user_code` varchar(45) NOT NULL,
  `instance_id` varchar(45) NOT NULL,
  `permissions` int(11) DEFAULT NULL,
  `create_by` varchar(45) DEFAULT NULL,
  `create_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`instance_id`,`user_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table scm_ticket
# ------------------------------------------------------------

CREATE TABLE `scm_ticket` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `server_name` varchar(100) DEFAULT NULL,
  `project_name` varchar(100) DEFAULT NULL,
  `module` varchar(100) DEFAULT NULL,
  `release_log` varchar(1000) DEFAULT NULL,
  `create_by` varchar(100) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `tester` varchar(100) DEFAULT NULL,
  `deployer` varchar(100) DEFAULT NULL,
  `branch` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table scm_ticket_log
# ------------------------------------------------------------

CREATE TABLE `scm_ticket_log` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `ticket_id` int(11) NOT NULL,
  `note` varchar(1000) DEFAULT NULL,
  `create_by` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table scm_user
# ------------------------------------------------------------

CREATE TABLE `scm_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `passport` varchar(100) DEFAULT NULL COMMENT '账户名,要求唯一',
  `status` varchar(45) DEFAULT NULL COMMENT '账户状态1:可用 0:不可用',
  `create_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` varchar(100) DEFAULT NULL,
  `user_name` varchar(45) NOT NULL,
  `passport_inc` varchar(100) DEFAULT NULL COMMENT '域账号',
  `role` int(4) DEFAULT '1' COMMENT '权限',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_name_UNIQUE` (`passport`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table scm_user_menu
# ------------------------------------------------------------

CREATE TABLE `scm_user_menu` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_code` varchar(45) DEFAULT NULL,
  `menu_id` varchar(45) DEFAULT NULL,
  `create_by` varchar(45) DEFAULT NULL,
  `create_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


