package clockvapor.telegram.markov

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import kotlin.properties.Delegates

class Config {
    lateinit var telegramBotToken: String
    lateinit var webhookURL: String
    lateinit var databaseName: String
    var replyFrecuence by Delegates.notNull<Int>()
    var chatFrecuence by Delegates.notNull<Int>()

    companion object {
        fun read(path: String): Config = ObjectMapper().readValue(File(path), Config::class.java)
    }
}
