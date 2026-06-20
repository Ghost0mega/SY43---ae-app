package com.example.sy43___ae_app

import com.example.sy43___ae_app.Back.DataBase.*
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class RepositoryTest {

    private lateinit var repository: Repository

    @Before
    fun setup() {
        // Connect to an in-memory database for testing
        Database.connect("jdbc:h2:mem:test;MODE=MYSQL;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        
        transaction {
            SchemaUtils.create(DbMetadata, Clubs, News, NewsPagination, Members)
            // Add required columns if not created by SchemaUtils (due to manual migrations in main code)
            exec("ALTER TABLE news_details ADD COLUMN IF NOT EXISTS is_followed BOOLEAN DEFAULT FALSE")
            exec("ALTER TABLE news_details ADD COLUMN IF NOT EXISTS latitude DOUBLE")
            exec("ALTER TABLE news_details ADD COLUMN IF NOT EXISTS longitude DOUBLE")
        }
        
        repository = Repository()
    }

    @Test
    fun testGetAllNews() {
        val now = LocalDateTime.now()
        
        transaction {
            DbMetadata.upsert {
                it[key] = "New"
                it[lastUpdate] = now
            }
            
            Clubs.upsert {
                it[id] = 1
                it[name] = "Test Club"
                it[logo] = "logo.png"
                it[url] = "http://club.com"
            }
            
            News.upsert {
                it[id] = 1
                it[title] = "Test News"
                it[summary] = "Test Summary"
                it[clubId] = 1
                it[url] = "http://news.com"
            }
            
            NewsPagination.upsert {
                it[id] = 1
                it[newsDetailId] = 1
                it[startDate] = now
                it[endDate] = now.plusHours(1)
            }
        }
        
        val allNews = repository.getAllNews()
        assertEquals(1, allNews.size)
        assertEquals("Test News", allNews[0].title)
        assertEquals("Test Club", allNews[0].clubName)
    }

    @Test
    fun testToggleFollowNews() {
        transaction {
            Clubs.upsert {
                it[id] = 1
                it[name] = "Test Club"
                it[logo] = "logo.png"
                it[url] = "http://club.com"
            }
            News.upsert {
                it[id] = 1
                it[title] = "Test News"
                it[summary] = "Test Summary"
                it[clubId] = 1
                it[url] = "http://news.com"
            }
        }
        
        repository.toggleFollowNews(1, true)
        
        transaction {
            val isFollowed = News.selectAll().where { News.id eq 1 }.single()[News.isFollowed]
            assertTrue(isFollowed)
        }
    }
}
