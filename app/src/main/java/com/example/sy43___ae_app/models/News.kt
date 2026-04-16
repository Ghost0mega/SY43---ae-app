package com.example.sy43___ae_app.models

data class News(
    val ID: Int,
    val club: Club,
    val title: String,
    val summary: String,
    val is_published: Boolean
)
