package com.example.sy43___ae_app.DataBase

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
    val shortDescription = varchar("short_description", MAX_TEXT_LENGTH)

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
    val startDate = datetime("start_date") // Ou datetime() si vous parsez les dates
    val endDate = datetime("end_date")

    // Liaison 1-to-1 ou Many-to-1 avec NewsDetail
    val newsDetailId = integer("news_detail_id").references(News.id)

    override val primaryKey = PrimaryKey(id)
}