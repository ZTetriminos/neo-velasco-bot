package clockvapor.telegram.markov

import clockvapor.markov.MarkovChain
import java.io.File
import java.nio.file.Paths

class MarkovFunctions {
    companion object {
        private const val DATA_PATH = "data"

        fun getMarkovPath(chatId: String, userId: String): String = Paths.get(
            getChatPath(chatId), "$userId.json").toString()

        fun getTotalMarkovPath(chatId: String): String =
            Paths.get(getChatPath(chatId), "total.json").toString()

        fun getChatPath(chatId: String): String = Paths.get(DATA_PATH, chatId).toString()
            .also { File(it).mkdirs() }

        fun getUsernamesPath(): String {
            File(DATA_PATH).mkdirs()

            return Paths.get(DATA_PATH, "usernames.json").toString()
        }

        fun readAllPersonalMarkov(chatId: String): List<MarkovChain> =
            getAllPersonalMarkovPaths(chatId).map{ MarkovChain.read(it) }

        fun getAllPersonalMarkovPaths(chatId: String): List<String> =
            File(getChatPath(chatId)).listFiles()!!.filter { !it.name.endsWith("total.json") }.map{ it.path }
    }
}