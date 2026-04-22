package com.example.sy43___ae_app.Back.FrontDTO

import org.jetbrains.exposed.v1.jdbc.Query
import java.time.LocalDateTime

data class NewUI(
    val id: Int,
    val title: String,
    val summary: String,
    val clubName: String,
    val logoUrl: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val lastUpdate: Query
)

data class ClubUI(
    val id: Int,
    val name: String,
    val logo: String,
    val url: String,
    val shortDescription: String?,
    val members: List<MemberUI>
)

data class MemberUI(
    val user:String,
    val id: Int,
    val nickname: String,
    val first_name: String,
    val last_name: String
)