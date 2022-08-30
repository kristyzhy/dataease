INSERT INTO `sys_menu` VALUES (700, 1, 2, 1, '系统配置', 'sys-settings', 'system/settings/index', 12, 'sys-tools', 'system-settings', b'0', b'0', b'0', NULL, NULL, NULL, NULL, NULL);

INSERT INTO `sys_menu` VALUES (730, 1, 0, 1, '数据同步表单', 'sys-task-ds-form', 'system/task/form', 11, NULL, '/task-ds-form', b'1', b'0', b'1', NULL, NULL, NULL, NULL, NULL);

UPDATE `sys_menu` set pid = 700, menu_sort = 1 where menu_id = 6 and `name` = 'system-param';



ALTER TABLE `sys_theme`
DROP COLUMN `img`,
DROP COLUMN `img_id`,
ADD COLUMN `senior` TINYINT(1) NULL DEFAULT NULL AFTER `status`;


update sys_background_image set classification ='商务';
update sys_background_image set name ='边框10' where id ='dark_1';


INSERT INTO `sys_menu` VALUES (750, 2, 0, 2, '导入用户', NULL, NULL, 999, NULL, NULL, b'0', b'0', b'0', 'user:import', NULL, NULL, 1614930935529, 1614930935529);


update system_parameter set sort  = (sort + 1) where sort > 3;

update system_parameter set sort = 4 where param_key = 'ui.favicon';

INSERT INTO `system_parameter`(`param_key`, `param_value`, `type`, `sort`) VALUES ('ui.showFoot', NULL, 'text', 18);
INSERT INTO `system_parameter`(`param_key`, `param_value`, `type`, `sort`) VALUES ('ui.footContent', NULL, 'blob', 19);

CREATE TABLE IF NOT EXISTS `sys_param_assist` (
    `id` BIGINT(21) NOT NULL AUTO_INCREMENT,
    `content` MEDIUMBLOB COMMENT '内容',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

ALTER TABLE `sys_task_email`
MODIFY COLUMN `view_ids` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '关联视图' AFTER `task_id`;

BEGIN;
update `sys_menu` set icon = 'plugins-new' where `menu_id` = 101;
update `sys_menu` set icon = 'sys-setting' where `menu_id` = 700;
update `sys_menu` set icon = 'sys-param' where `menu_id` = 6;
update `sys_menu` set icon = 'display-setting' where `menu_id` = 710;
COMMIT;

ALTER TABLE `de_driver_details` ADD COLUMN `is_trans_name` TINYINT(1) NULL AFTER `driver_class`;
ALTER TABLE `de_driver_details` ADD COLUMN `trans_name` VARCHAR(255) NULL AFTER `driver_class`;

BEGIN;
UPDATE `sys_theme_item` set `val` = '#3370FF' where `theme_id` = 1 and `key` = 'primary' and `val` = '#409EFF';
UPDATE `sys_theme_item` set `val` = '#34C724' where `theme_id` = 1 and `key` = 'deSuccess' and `val` = '#67C23A';
UPDATE `sys_theme_item` set `val` = '#FF8800' where `theme_id` = 1 and `key` = 'deWarning' and `val` = '#E6A23C';
UPDATE `sys_theme_item` set `val` = '#F54A45' where `theme_id` = 1 and `key` = 'deDanger' and `val` = '#F56C6C';

UPDATE `sys_theme_item` set `val` = '#1F2329' where `theme_id` = 1 and `key` = 'deTextPrimary' and `val` = '#303133';
UPDATE `sys_theme_item` set `val` = '#646A73' where `theme_id` = 1 and `key` = 'deTextSecondary' and `val` = '#909399';
UPDATE `sys_theme_item` set `val` = '#8F959E' where `theme_id` = 1 and `key` = 'deTextPlaceholder' and `val` = '#C0C4CC';
INSERT INTO `sys_theme_item` VALUES (1, 'deTextDisable', '#BBBFC4');

INSERT INTO `sys_theme_item` VALUES (1, 'deComBorderColor', '#BBBFC4');
INSERT INTO `sys_theme_item` VALUES (1, 'deCardStrokeColor', '#DEE0E3');
UPDATE `sys_theme_item` set `val` = '#1F2329' where `theme_id` = 1 and `key` = 'deBlack' and `val` = '#000000';
UPDATE `sys_theme_item` set `val` = '#F5F6F7' where `theme_id` = 1 and `key` = 'deBackgroundBase' and `val` = '#F5F7FA';
INSERT INTO `sys_theme_item` VALUES (1, 'deInputDisableBackground', '#EFF0F1');

COMMIT;


ALTER TABLE `sys_theme` ADD COLUMN `origin_id` bigint(20) NULL COMMENT '源主题Id' AFTER `senior`;

BEGIN;
UPDATE `sys_theme` set `origin_id` = 1 where `id` = 1;
UPDATE `sys_theme` set `origin_id` = 2 where `id` = 2;
UPDATE `sys_theme` set `origin_id` = 1 where origin_id is null;
COMMIT;
