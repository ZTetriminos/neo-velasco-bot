package oscarntnz.telegram.neovelasco

import clockvapor.markov.MarkovChain
import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.client.MongoDatabase
import org.bson.conversions.Bson
import org.litote.kmongo.eq
import org.litote.kmongo.getCollection
import oscarntnz.telegram.neovelasco.database.Chain
import oscarntnz.telegram.neovelasco.database.User
import java.io.File
import java.nio.file.Paths

class MarkovFunctions(private val database: MongoDatabase) {
    /*private fun getMarkovPath(chatId: String, userId: String): String = Paths.get(
        getChatPath(chatId), "$userId.json").toString()

    private fun getTotalMarkovPath(chatId: String): String =
        Paths.get(getChatPath(chatId), "total.json").toString()

    private fun getChatPath(chatId: String): String = Paths.get(DATA_PATH, chatId).toString()
        .also { File(it).mkdirs() }

    private fun getUsernamesPath(): String {
        File(DATA_PATH).mkdirs()

        return Paths.get(DATA_PATH, "usernames.json").toString()
    }*/

    private fun readAllPersonalMarkov(chatId: String): List<MarkovChain> {
        val chains = database.getCollection<Chain>("chains")

        return chains.find(Chain::chatId eq chatId).toList().map(Chain::toMarkovChain)
    }

    private fun readUsernames(): MutableMap<String, String> {
        val users = database.getCollection<User>("usernames")

        return users.find().associate { it.username to it.userId }.toMutableMap()
    }
/*
    private fun writeUsernames(usernames: Map<String, String>) = ObjectMapper().writeValue(
        File(getUsernamesPath()), usernames)

    private fun getOrCreateTotalMarkovChain(chatId: String): MarkovChain {
        val path = getTotalMarkovPath(chatId)
        var markovChain = tryOrNull(reportException = false) { MarkovChain.read(path) }

        if (markovChain == null) {
            markovChain = MarkovChain()

            for (personalMarkovChain in readAllPersonalMarkov(chatId))
                markovChain.add(personalMarkovChain)

            markovChain.write(path)
        }

        return markovChain
    }

    fun getAllPersonalMarkovPaths(chatId: String): List<String> =
        File(getChatPath(chatId)).listFiles()!!.filter { !it.name.endsWith("total.json") }.map{ it.path }

    fun analyzeMessage(chatId: String, userId: String, text: String) {
        val path = getMarkovPath(chatId, userId)
        val markovChain = tryOrNull(reportException = false) { MarkovChain.read(path) } ?: MarkovChain()
        val totalMarkovChain = getOrCreateTotalMarkovChain(chatId)
        val words = text.split(whitespaceRegex)

        markovChain.add(words)
        markovChain.write(path)
        totalMarkovChain.add(words)
        totalMarkovChain.write(getTotalMarkovPath(chatId))
    }

    fun generateMessage(chatId: String, userId: String): String? =
        tryOrNull(reportException = false) { MarkovChain.read(getMarkovPath(chatId, userId)) }?.generate()
            ?.takeIf { it.isNotEmpty() }?.joinToString(" ")

    fun generateMessage(chatId: String, userId: String, seed: String): MarkovChain.GenerateWithSeedResult? =
        tryOrNull(reportException = false) { MarkovChain.read(getMarkovPath(chatId, userId)) }
            ?.generateWithCaseInsensitiveSeed(seed)

    fun generateMessageTotal(chatId: String): String? =
        tryOrNull(reportException = false) { getOrCreateTotalMarkovChain(chatId) }?.generate()
            ?.takeIf { it.isNotEmpty() }?.joinToString(" ")

    fun generateMessageTotal(chatId: String, seed: String): MarkovChain.GenerateWithSeedResult? =
        tryOrNull(reportException = false) { getOrCreateTotalMarkovChain(chatId) }
            ?.generateWithCaseInsensitiveSeed(seed)

    fun getUserIdForUsername(username: String): String? =
        tryOrNull(reportException = false) { readUsernames() }?.get(username.lowercase())

    fun storeUsername(username: String, userId: String) {
        val usernames = tryOrNull(reportException = false) { readUsernames() } ?: mutableMapOf()
        usernames[username.lowercase()] = userId

        writeUsernames(usernames)
    }

    fun deleteChat(chatId: String): Boolean = File(getChatPath(chatId))
        .deleteRecursively()

    fun deleteMarkov(chatId: String, userId: String): Boolean {
        // remove personal markov chain from total markov chain
        val path = getMarkovPath(chatId, userId)
        val markovChain = tryOrNull(reportException = false) { MarkovChain.read(path) } ?: MarkovChain()
        val totalMarkovChain = getOrCreateTotalMarkovChain(chatId)

        totalMarkovChain.remove(markovChain)
        totalMarkovChain.write(getTotalMarkovPath(chatId))

        // delete personal markov chain
        return File(path).delete()
    }

    fun deleteMessage(chatId: String, userId: String, text: String) {
        val words = text.split(whitespaceRegex)

        // remove from personal markov chain
        val path = getMarkovPath(chatId, userId)
        val markovChain = tryOrNull(reportException = false) { MarkovChain.read(path) } ?: MarkovChain()

        markovChain.remove(words)
        markovChain.write(path)

        // remove from total markov chain
        val totalMarkovChain = getOrCreateTotalMarkovChain(chatId)

        totalMarkovChain.remove(words)
        totalMarkovChain.write(getTotalMarkovPath(chatId))
    }*/
}