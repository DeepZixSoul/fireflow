package com.igrupos.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = Migration(1, 2) { db ->
    db.execSQL("""
        CREATE TABLE IF NOT EXISTS `motors` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `group_id` INTEGER NOT NULL,
            `motor_type` TEXT NOT NULL,
            `nominal_flow` REAL NOT NULL,
            `nominal_pressure` REAL NOT NULL,
            `manometric_height` REAL NOT NULL,
            `created_at` INTEGER NOT NULL,
            `updated_at` INTEGER NOT NULL,
            FOREIGN KEY (`group_id`) REFERENCES `pressure_groups`(`id`) ON DELETE CASCADE
        )
    """.trimIndent())

    db.execSQL("CREATE INDEX IF NOT EXISTS `index_motors_group_id` ON `motors`(`group_id`)")

    db.execSQL("""
        CREATE TABLE IF NOT EXISTS `curve_points_new` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `revision_id` INTEGER NOT NULL,
            `motor_id` INTEGER NOT NULL DEFAULT 0,
            `flow` REAL NOT NULL,
            `pressure` REAL NOT NULL,
            `order_index` INTEGER NOT NULL,
            FOREIGN KEY (`revision_id`) REFERENCES `revisions`(`id`) ON DELETE CASCADE,
            FOREIGN KEY (`motor_id`) REFERENCES `motors`(`id`) ON DELETE CASCADE
        )
    """.trimIndent())

    db.execSQL("""
        INSERT INTO `curve_points_new` (`id`, `revision_id`, `motor_id`, `flow`, `pressure`, `order_index`)
        SELECT `id`, `revision_id`, 0, `flow`, `pressure`, `order_index` FROM `curve_points`
    """.trimIndent())

    db.execSQL("DROP TABLE `curve_points`")
    db.execSQL("ALTER TABLE `curve_points_new` RENAME TO `curve_points`")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_curve_points_revision_id` ON `curve_points`(`revision_id`)")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_curve_points_motor_id` ON `curve_points`(`motor_id`)")

    db.execSQL("""
        CREATE TABLE IF NOT EXISTS `pressure_groups_new` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `client_id` INTEGER NOT NULL,
            `brand` TEXT NOT NULL DEFAULT '',
            `model` TEXT NOT NULL DEFAULT '',
            `serial_number` TEXT NOT NULL DEFAULT '',
            `pump_number` TEXT NOT NULL DEFAULT '',
            `manufacturer` TEXT NOT NULL DEFAULT '',
            `power` TEXT NOT NULL DEFAULT '',
            `nominal_flow` TEXT NOT NULL DEFAULT '',
            `nominal_pressure` TEXT NOT NULL DEFAULT '',
            `manometric_height` TEXT NOT NULL DEFAULT '',
            `installation_date` INTEGER,
            `notes` TEXT NOT NULL DEFAULT '',
            `created_at` INTEGER NOT NULL,
            `updated_at` INTEGER NOT NULL,
            `is_active` INTEGER NOT NULL DEFAULT 1,
            FOREIGN KEY (`client_id`) REFERENCES `clients`(`id`) ON DELETE CASCADE
        )
    """.trimIndent())

    db.execSQL("""
        INSERT INTO `pressure_groups_new` (
            `id`, `client_id`, `brand`, `model`, `serial_number`, `pump_number`,
            `manufacturer`, `power`, `nominal_flow`, `nominal_pressure`,
            `manometric_height`, `installation_date`, `notes`, `created_at`,
            `updated_at`, `is_active`
        )
        SELECT
            `id`, `client_id`, `brand`, `model`, `serial_number`, `pump_number`,
            `manufacturer`, `power`, `nominal_flow`, `nominal_pressure`,
            `manometric_height`, `installation_date`, `notes`, `created_at`,
            `updated_at`, `is_active`
        FROM `pressure_groups`
    """.trimIndent())

    db.execSQL("DROP TABLE `pressure_groups`")
    db.execSQL("ALTER TABLE `pressure_groups_new` RENAME TO `pressure_groups`")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_pressure_groups_client_id` ON `pressure_groups`(`client_id`)")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_pressure_groups_serial_number` ON `pressure_groups`(`serial_number`)")
}

val MIGRATION_2_4 = Migration(2, 4) { db ->
    db.execSQL("""
        CREATE TABLE IF NOT EXISTS `motors_new` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `group_id` INTEGER NOT NULL,
            `motor_type` TEXT NOT NULL,
            `nominal_flow` REAL NOT NULL,
            `nominal_pressure` REAL NOT NULL,
            `manometric_height` REAL NOT NULL,
            `pressure_0` REAL,
            `pressure_50` REAL,
            `pressure_100` REAL,
            `pressure_140` REAL,
            `created_at` INTEGER NOT NULL,
            `updated_at` INTEGER NOT NULL,
            FOREIGN KEY (`group_id`) REFERENCES `pressure_groups`(`id`) ON DELETE CASCADE
        )
    """.trimIndent())

    db.execSQL("""
        INSERT INTO `motors_new` (
            `id`, `group_id`, `motor_type`, `nominal_flow`, `nominal_pressure`,
            `manometric_height`, `created_at`, `updated_at`
        )
        SELECT
            `id`, `group_id`, `motor_type`, `nominal_flow`, `nominal_pressure`,
            `manometric_height`, `created_at`, `updated_at`
        FROM `motors`
    """.trimIndent())

    db.execSQL("DROP TABLE `motors`")
    db.execSQL("ALTER TABLE `motors_new` RENAME TO `motors`")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_motors_group_id` ON `motors`(`group_id`)")

    db.execSQL("""
        CREATE TABLE IF NOT EXISTS `pressure_groups_new` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `client_id` INTEGER NOT NULL,
            `brand` TEXT NOT NULL DEFAULT '',
            `model` TEXT NOT NULL DEFAULT '',
            `serial_number` TEXT NOT NULL DEFAULT '',
            `pump_number` TEXT NOT NULL DEFAULT '',
            `manufacturer` TEXT NOT NULL DEFAULT '',
            `power` TEXT NOT NULL DEFAULT '',
            `installation_date` INTEGER,
            `maintenance_date` INTEGER,
            `created_at` INTEGER NOT NULL,
            `updated_at` INTEGER NOT NULL,
            `is_active` INTEGER NOT NULL DEFAULT 1,
            FOREIGN KEY (`client_id`) REFERENCES `clients`(`id`) ON DELETE CASCADE
        )
    """.trimIndent())

    db.execSQL("""
        INSERT INTO `pressure_groups_new` (
            `id`, `client_id`, `brand`, `model`, `serial_number`, `pump_number`,
            `manufacturer`, `power`, `installation_date`, `created_at`,
            `updated_at`, `is_active`
        )
        SELECT
            `id`, `client_id`, `brand`, `model`, `serial_number`, `pump_number`,
            `manufacturer`, `power`, `installation_date`, `created_at`,
            `updated_at`, `is_active`
        FROM `pressure_groups`
    """.trimIndent())

    db.execSQL("DROP TABLE `pressure_groups`")
    db.execSQL("ALTER TABLE `pressure_groups_new` RENAME TO `pressure_groups`")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_pressure_groups_client_id` ON `pressure_groups`(`client_id`)")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_pressure_groups_serial_number` ON `pressure_groups`(`serial_number`)")
}

val MIGRATION_3_4 = Migration(3, 4) { db ->
    db.execSQL("""
        CREATE TABLE IF NOT EXISTS `motors_new` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `group_id` INTEGER NOT NULL,
            `motor_type` TEXT NOT NULL,
            `nominal_flow` REAL NOT NULL,
            `nominal_pressure` REAL NOT NULL,
            `manometric_height` REAL NOT NULL,
            `pressure_0` REAL,
            `pressure_50` REAL,
            `pressure_100` REAL,
            `pressure_140` REAL,
            `created_at` INTEGER NOT NULL,
            `updated_at` INTEGER NOT NULL,
            FOREIGN KEY (`group_id`) REFERENCES `pressure_groups`(`id`) ON DELETE CASCADE
        )
    """.trimIndent())

    db.execSQL("""
        INSERT INTO `motors_new` (
            `id`, `group_id`, `motor_type`, `nominal_flow`, `nominal_pressure`,
            `manometric_height`, `created_at`, `updated_at`
        )
        SELECT
            `id`, `group_id`, `motor_type`, `nominal_flow`, `nominal_pressure`,
            `manometric_height`, `created_at`, `updated_at`
        FROM `motors`
    """.trimIndent())

    db.execSQL("DROP TABLE `motors`")
    db.execSQL("ALTER TABLE `motors_new` RENAME TO `motors`")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_motors_group_id` ON `motors`(`group_id`)")
}

val MIGRATION_4_5 = Migration(4, 5) { db ->
    db.execSQL("""
        CREATE TABLE IF NOT EXISTS `pressure_measurements` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `motor_id` INTEGER NOT NULL,
            `year` INTEGER NOT NULL,
            `pressure_at_0` REAL,
            `pressure_at_50` REAL,
            `pressure_at_100` REAL,
            `pressure_at_140` REAL,
            `is_active` INTEGER NOT NULL DEFAULT 1,
            `created_at` INTEGER NOT NULL,
            `updated_at` INTEGER NOT NULL,
            FOREIGN KEY (`motor_id`) REFERENCES `motors`(`id`) ON DELETE CASCADE
        )
    """.trimIndent())

    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_pressure_measurements_motor_year` ON `pressure_measurements`(`motor_id`, `year`)")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_pressure_measurements_motor_id` ON `pressure_measurements`(`motor_id`)")

    db.execSQL("""
        INSERT INTO `pressure_measurements` (`motor_id`, `year`, `pressure_at_0`, `pressure_at_50`, `pressure_at_100`, `pressure_at_140`, `is_active`, `created_at`, `updated_at`)
        SELECT `id`, CAST(strftime('%Y', 'now', 'localtime') AS INTEGER), `pressure_0`, `pressure_50`, `pressure_100`, `pressure_140`, 1, `created_at`, `updated_at`
        FROM `motors`
        WHERE `pressure_0` IS NOT NULL OR `pressure_50` IS NOT NULL OR `pressure_100` IS NOT NULL OR `pressure_140` IS NOT NULL
    """.trimIndent())

    db.execSQL("""
        CREATE TABLE IF NOT EXISTS `motors_new` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `group_id` INTEGER NOT NULL,
            `motor_type` TEXT NOT NULL,
            `nominal_flow` REAL NOT NULL,
            `manometric_height` REAL NOT NULL,
            `created_at` INTEGER NOT NULL,
            `updated_at` INTEGER NOT NULL,
            FOREIGN KEY (`group_id`) REFERENCES `pressure_groups`(`id`) ON DELETE CASCADE
        )
    """.trimIndent())

    db.execSQL("""
        INSERT INTO `motors_new` (`id`, `group_id`, `motor_type`, `nominal_flow`, `manometric_height`, `created_at`, `updated_at`)
        SELECT `id`, `group_id`, `motor_type`, `nominal_flow`, `manometric_height`, `created_at`, `updated_at`
        FROM `motors`
    """.trimIndent())

    db.execSQL("DROP TABLE `motors`")
    db.execSQL("ALTER TABLE `motors_new` RENAME TO `motors`")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_motors_group_id` ON `motors`(`group_id`)")
}

val MIGRATION_5_6 = Migration(5, 6) { db ->
    db.execSQL("ALTER TABLE `users` ADD COLUMN `must_change_password` INTEGER NOT NULL DEFAULT 0")
}

val MIGRATION_6_7 = Migration(6, 7) { db ->
    db.execSQL("ALTER TABLE `clients` ADD COLUMN `is_dirty` INTEGER NOT NULL DEFAULT 0")
    db.execSQL("ALTER TABLE `pressure_groups` ADD COLUMN `is_dirty` INTEGER NOT NULL DEFAULT 0")
    db.execSQL("ALTER TABLE `revisions` ADD COLUMN `is_dirty` INTEGER NOT NULL DEFAULT 0")
    db.execSQL("ALTER TABLE `curve_points` ADD COLUMN `is_dirty` INTEGER NOT NULL DEFAULT 0")
}
