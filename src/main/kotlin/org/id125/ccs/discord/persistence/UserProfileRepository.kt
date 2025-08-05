package org.id125.ccs.discord.persistence

import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import org.id125.ccs.discord.profile.UserProfile

class UserProfileRepository(
    private val collection: MongoCollection<UserProfile>
) {
    suspend fun findById(id: Long): UserProfile? {
        return collection.find(org.bson.Document("discordId", id)).firstOrNull()
    }

    suspend fun findByEmail(email: String): UserProfile? {
        return collection.find(org.bson.Document("email", email)).firstOrNull()
    }

    suspend fun insert(userProfile: UserProfile) {
        collection.insertOne(userProfile)
    }

    suspend fun update(userProfile: UserProfile) {
        collection.replaceOne(org.bson.Document("discordId", userProfile.discordId), userProfile)
    }

    suspend fun deleteById(id: Long) {
        collection.deleteOne(org.bson.Document("discordId", id))
    }
}