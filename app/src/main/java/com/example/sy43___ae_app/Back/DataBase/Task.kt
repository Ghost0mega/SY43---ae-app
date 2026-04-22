package com.example.sy43___ae_app.Back.DataBase

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime


/*
    DB local
 */
const val MAX_VARCHAR_LENGTH = 128
const val MAX_URL_LENGTH = 512
const val MAX_TEXT_LENGTH = 2048

object Clubs : Table("clubs") {
    val id = integer("id")
    val name = varchar("name", MAX_VARCHAR_LENGTH)
    val logo = varchar("logo", MAX_URL_LENGTH)
    val url = varchar("url", MAX_URL_LENGTH)
    val shortDescription = varchar("short_description", MAX_TEXT_LENGTH).nullable()

    override val primaryKey = PrimaryKey(id)
}
object Members : Table("members") {
    val id = integer("id")
    val user = varchar("user", MAX_VARCHAR_LENGTH)
    val nickname = varchar("nickname", MAX_VARCHAR_LENGTH)
    val firstName = varchar("first_name", MAX_VARCHAR_LENGTH)
    val lastName = varchar("last_name", MAX_VARCHAR_LENGTH)

    // Clé étrangère vers le club
    val clubId = integer("club_id").references(Clubs.id)

    override val primaryKey = PrimaryKey(id)
}

// 3. Table des Détails de News (Relation Many-to-One avec Club)
object News : Table("news_details") {
    val id = integer("id")
    val title = varchar("title", MAX_VARCHAR_LENGTH)
    val summary = varchar("summary", MAX_TEXT_LENGTH)
    val isPublished = bool("is_published").default(false)
    val url = varchar("url", MAX_URL_LENGTH)

    // Clé étrangère vers le club qui publie la news
    val clubId = integer("club_id").references(Clubs.id)

    override val primaryKey = PrimaryKey(id)
}

// 4. Table des Résultats de News (Dates et liaison)
object NewsPagination : Table("news_results") {
    val id = integer("id")
    val startDate = datetime("start_date")
    val endDate = datetime("end_date")

    val lastUpdate = datetime("lastUpdate")
    val newsDetailId = integer("news_detail_id").references(News.id)

    override val primaryKey = PrimaryKey(id)
}

object Groups : Table("group") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", MAX_VARCHAR_LENGTH)
    override val primaryKey = PrimaryKey(id)
}

object NewsSubscriptionGroups : Table("news_subscription_groups") {
    val groupId = integer("group_id").references(Groups.id)
    val newId = integer("new_id").references(News.id)
    override val primaryKey = PrimaryKey(groupId, newId)
}

object ClubSubscriptionGroups : Table("club_subscription_groups") {
    val groupId = integer("group_id").references(Groups.id)
    val clubId = integer("club_id").references(Clubs.id)
    override val primaryKey = PrimaryKey(groupId, clubId)
}