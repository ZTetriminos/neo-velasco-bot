package oscarntnz.telegram.neovelasco

import clockvapor.markov.MarkovChain
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.*
import oscarntnz.telegram.neovelasco.database.Chain
import oscarntnz.telegram.neovelasco.database.User

class MarkovFunctions(database: MongoDatabase) {
    private var chainsCollection: MongoCollection<Chain> = database.getCollection<Chain>("chains")
    private var usersCollection: MongoCollection<User> = database.getCollection<User>("usernames")

    private fun readUsername(username: String? = null): MutableMap<String, String> =
        if(username == null) usersCollection.find().associate { it.username to it.userId }.toMutableMap()
        else usersCollection.find(User::username eq username).associate { it.username to it.userId }.toMutableMap()

    private fun checkUser(username: String, userId: String): Boolean =
        usersCollection.findOne(User::username eq username, User::userId eq userId) != null

    private fun writeUsernames(usernames: Map<String, String>) {
        val userList: Array<User> = usernames.map { User(username = it.key, userId = it.value) }.toTypedArray()

        userList.forEach {
            if(checkUser(it.username, it.userId))
                usersCollection.updateOne(and(User::username eq it.username, User::userId eq it.userId),
                set(User::username setTo it.username, User::userId setTo it.userId))
            else
                usersCollection.insertOne(it)
        }
    }

    private fun getTotalMarkovChain(chatId: String): MarkovChain? =
        Chain.listToMarkovChain(chainsCollection.find(Chain::chatId eq chatId).toList())

    private fun getMarkovChain(chatId: String, userId: String): MarkovChain? =
        chainsCollection.findOne(Chain::chatId eq chatId, Chain::userId eq userId)?.toMarkovChain()

    fun getAllPersonalMarkovChains(chatId: String): MutableMap<String, MarkovChain> =
        chainsCollection.find(Chain::chatId eq chatId, Chain::userId ne null)
            .associate { it.userId!! to it.toMarkovChain() }.toMutableMap()

    private fun getOrCreateMarkovChain(chatId: String, userId: String): MarkovChain {
        var chain = getMarkovChain(chatId, userId)

        if(chain == null)   chain = MarkovChain()

        return chain
    }

    fun analyzeMessage(chatId: String, userId: String, text: String) {
        val markovChain = getOrCreateMarkovChain(chatId, userId)
        val words = text.split(whitespaceRegex)

        markovChain.add(words)
        insertOrUpdateChain(chatId, userId, markovChain)
    }

    private fun insertOrUpdateChain(chatId: String, userId: String, chain: MarkovChain) {
        val exists = chainsCollection.findOne(Chain::chatId eq chatId, Chain::userId eq userId) != null

        if(!exists)
            chainsCollection.insertOne(Chain.fromMarkovChain(chatId, userId, chain))
        else
            chainsCollection.updateOne(and(Chain::chatId eq chatId, Chain::userId eq userId), setValue(Chain::data, chain.data))
    }

    fun generateMessage(chatId: String, userId: String): String? =
        tryOrNull(reportException = false) { getMarkovChain(chatId, userId) }?.generate()
            ?.takeIf { it.isNotEmpty() }?.joinToString(" ")

    fun generateMessage(chatId: String, userId: String, seed: String): MarkovChain.GenerateWithSeedResult? =
        tryOrNull(reportException = false) { getMarkovChain(chatId, userId) }
            ?.generateWithCaseInsensitiveSeed(seed)

    fun generateMessageTotal(chatId: String): String? =
        tryOrNull(reportException = false) { getTotalMarkovChain(chatId) }?.generate()
            ?.takeIf { it.isNotEmpty() }?.joinToString(" ")

    fun generateMessageTotal(chatId: String, seed: String): MarkovChain.GenerateWithSeedResult? =
        tryOrNull(reportException = false) { getTotalMarkovChain(chatId) }
            ?.generateWithCaseInsensitiveSeed(seed)

    fun getUserIdForUsername(username: String): String? =
        tryOrNull(reportException = false) { readUsername(username.lowercase()) }?.get(username.lowercase())

    fun storeUsername(username: String, userId: String) {
        val usernames = tryOrNull(reportException = false) { readUsername() } ?: mutableMapOf()
        usernames[username.lowercase()] = userId

        writeUsernames(usernames)
    }

    fun deleteChat(chatId: String): Boolean =
        chainsCollection.deleteMany(Chain::chatId eq chatId).deletedCount > 0

    fun deleteMarkov(chatId: String, userId: String): Boolean =
        chainsCollection.deleteOne(Chain::chatId eq chatId, Chain::userId eq userId).deletedCount > 0

    fun deleteMessage(chatId: String, userId: String, text: String) {
        val words = text.split(whitespaceRegex)

        // remove from personal markov chain
        val markovChain = getMarkovChain(chatId, userId)

        markovChain?.remove(words)
        markovChain?.let { insertOrUpdateChain(chatId, userId, it) }
    }
}