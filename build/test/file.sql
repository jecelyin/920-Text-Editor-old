#中文ok——
# Table structure for table `CPG_categorymap`
#

CREATE TABLE IF NOT EXISTS `CPG_categorymap` (
  cid int(11) NOT NULL,
  group_id int(11) NOT NULL,
  PRIMARY KEY  (cid,group_id)
) COMMENT='Holds the categories where groups can create albums';

# Create temporary table to store messages carried over from one page to the other
CREATE TABLE CPG_temp_messages (
  message_id varchar(80) NOT NULL default '',
  user_id int(11) default '0',
  time int(11) default NULL,
  message text NOT NULL,
  PRIMARY KEY (message_id)
) COMMENT='Used to store messages from one page to the other';
# --------------------------------------------------------

ALTER TABLE CPG_filetypes DROP INDEX `EXTENSION`, ADD PRIMARY KEY ( `extension` );
ALTER TABLE CPG_filetypes ADD `player` VARCHAR( 5 ) ;
ALTER TABLE CPG_filetypes CHANGE `mime` `mime` CHAR(254) default NULL;


INSERT INTO CPG_config VALUES ('global_registration_pw','');

#movie download link -> to picinfo
INSERT INTO CPG_config VALUES ('picinfo_movie_download_link', '1');

#site token to use in forms
INSERT INTO CPG_config VALUES ('site_token', MD5(RAND()));
INSERT INTO CPG_config VALUES ('form_token_lifetime', '900');

INSERT INTO CPG_config VALUES ('rating_stars_amount', '5');
INSERT INTO CPG_config VALUES ('old_style_rating', '0');

###### watermark ########
INSERT INTO CPG_config VALUES ('enable_watermark', '0');
INSERT INTO CPG_config VALUES ('where_put_watermark', 'southeast');
INSERT INTO CPG_config VALUES ('watermark_file', 'images/watermark.png');
INSERT INTO CPG_config VALUES ('which_files_to_watermark', 'both');
INSERT INTO CPG_config VALUES ('orig_pfx', 'orig_');
INSERT INTO CPG_config VALUES ('watermark_transparency', '40');
INSERT INTO CPG_config VALUES ('reduce_watermark', '0');
INSERT INTO CPG_config VALUES ('watermark_transparency_featherx', '0');
INSERT INTO CPG_config VALUES ('watermark_transparency_feathery', '0');
INSERT INTO CPG_config VALUES ('enable_thumb_watermark', '1');
#########################

###### thumb sharpening and cropping ########
INSERT INTO CPG_config VALUES ('enable_unsharp', '0');
INSERT INTO CPG_config VALUES ('unsharp_amount', '120');
INSERT INTO CPG_config VALUES ('unsharp_radius', '0.5');
INSERT INTO CPG_config VALUES ('unsharp_threshold', '3');
INSERT INTO CPG_config VALUES ('thumb_height', '140');
#########################

# Modify structure for multi album pictures
ALTER TABLE `CPG_albums` ADD `owner` int(11)  NOT NULL DEFAULT '1' AFTER `category`;


UPDATE `CPG_config` SET value='$/\\\\:*?&quot;&#039;&lt;&gt;|` &amp;#@' WHERE name='forbiden_fname_char';


ALTER TABLE `CPG_users` ADD `user_language` varchar(40) default '' NOT NULL;

#
# Enlarge password field for MD5/SHA1 hash
#

ALTER TABLE `CPG_users` CHANGE `user_password` `user_password` VARCHAR( 40 ) NOT NULL default '';

INSERT INTO CPG_config VALUES ('login_method', 'username');

ALTER TABLE `CPG_hit_stats` ADD `uid` INT(11) NOT NULL default '0' ;

INSERT INTO CPG_config VALUES ('allow_unlogged_access', '3');

#end