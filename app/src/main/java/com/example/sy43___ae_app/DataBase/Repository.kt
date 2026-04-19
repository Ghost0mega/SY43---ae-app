package com.example.sy43___ae_app.DataBase

import com.example.sy43___ae_app.DataBase.FrontDTO.ClubUI
import com.example.sy43___ae_app.DataBase.FrontDTO.MemberUI
import com.example.sy43___ae_app.DataBase.FrontDTO.NewUI
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

/*
*   Class to UI DTO
*
*
 */
class Repository {
    /*
    * Get a list of NewUI
    *
     */
    fun getAllNews(): List<NewUI> {
        return transaction {
            (News innerJoin Clubs
            innerJoin NewsPagination).selectAll().map { row ->
                NewUI(
                    id = row[News.id],
                    title = row[News.title],
                    summary = row[News.summary],
                    clubName = row[Clubs.name],
                    logoUrl = row[Clubs.logo],
                    startDate = row[NewsPagination.startDate],
                    endDate = row[NewsPagination.endDate]
                )
            }
        }
    }

    /*
    *
    *  Get a list of ClubUI
     */
    fun getAllClubs(): List<ClubUI> {
        return transaction {
            Clubs.selectAll().map { clubRow ->
                val currentClubId = clubRow[Clubs.id]
                val membersForThisClub = Members
                    .selectAll()
                    .where { Members.clubId eq currentClubId }
                    .map { memberRow ->
                        MemberUI(
                            user = memberRow[Members.user],
                            id = memberRow[Members.id],
                            nickname = memberRow[Members.nickname],
                            first_name = memberRow[Members.firstName],
                            last_name = memberRow[Members.lastName]
                        )
                    }

                ClubUI(
                    id = currentClubId,
                    name = clubRow[Clubs.name],
                    logo = clubRow[Clubs.logo],
                    url = clubRow[Clubs.url],
                    short_description = clubRow[Clubs.shortDescription],
                    members = membersForThisClub
                )
            }
        }
    }
}