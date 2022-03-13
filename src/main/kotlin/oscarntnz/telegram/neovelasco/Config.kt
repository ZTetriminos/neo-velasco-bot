package oscarntnz.telegram.neovelasco

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import java.io.File
import kotlin.properties.Delegates

class Config {
    lateinit var telegramBotToken: String
    lateinit var webhookURL: String
    lateinit var databaseName: String
    var replyFrecuence by Delegates.notNull<Int>()
    var chatFrecuence by Delegates.notNull<Int>()
    var ownerChatId by Delegates.notNull<Long>()

    companion object {
        fun read(path: String): Config = ObjectMapper(JsonFactory()).readValue(File(path), Config::class.java)
    }
}
