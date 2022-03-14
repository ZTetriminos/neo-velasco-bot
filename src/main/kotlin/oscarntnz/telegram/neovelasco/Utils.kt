package oscarntnz.telegram.neovelasco

import clockvapor.markov.MarkovChain
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.MessageEntity
import com.github.kotlintelegrambot.entities.User
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow

val MarkovChain.wordCounts: Map<String, Int> get() {
    val map = hashMapOf<String, Int>()

    for ((_, dataMap) in data)
        for ((word, count) in dataMap) {
            val sanitized = word.sanitize()

            if (sanitized.isNotBlank())
                map.compute(sanitized.lowercase()) { _, n -> n?.plus(count) ?: count }
        }

    return map
}

val User.displayName: String get() = lastName?.takeIf { it.isNotBlank() }?.let { "$firstName $it" } ?: firstName

val whitespaceRegex = Regex("\\s+")

const val ANSI_RESET = "\u001B[0m"
const val ANSI_BLACK = "\u001B[30m"
const val ANSI_RED = "\u001B[31m"
const val ANSI_GREEN = "\u001B[32m"
const val ANSI_YELLOW = "\u001B[33m"

private const val punctuation = "[`~!@#$%^&*()\\-_=+\\[\\],<.>/?\\\\|;:\"]+"

fun scoreMostDistinguishingWords(user: Map<String, Int>, universe: Map<String, Int>): Map<String, Double> {
    val scores = linkedMapOf<String, Double>()
    val userTotal = user.values.sum()
    val universeTotal = universe.values.sum()

    for ((word, count) in user)
        scores[word] = count.toDouble().pow(1.1) / userTotal / (universe.getValue(word).toDouble() / universeTotal)

    return scores.toList().sortedWith { a, b ->
        val c = b.second.compareTo(a.second)
        if (c == 0)
            a.first.compareTo(b.first)
        else
            c
    }.toMap()
}

fun computeUniverse(wordCountsCollection: Collection<Map<String, Int>>): Map<String, Int> {
    val universe = hashMapOf<String, Int>()

    for (wordCounts in wordCountsCollection)
        for ((word, count) in wordCounts)
            universe.compute(word) { _, n -> n?.plus(count) ?: count }

    return universe
}

private fun String.sanitize(): String =
    replace("“", "\"").replace("”", "\"").replace("‘", "'").replace("’", "'")
        .replace(Regex("^$punctuation"), "")
        .replace(Regex("$punctuation$"), "")

fun log(s: String) {
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    println("${ANSI_YELLOW}$timestamp: ${s}$ANSI_RESET")
}

fun log(t: Throwable) {
    log(t.localizedMessage)

    t.printStackTrace(System.err)
}

inline fun tryOrLog(f: () -> Unit) =
    try {
        f()
    }
    catch (e: Exception) {
        log(e)
    }

inline fun <T> tryOrNull(reportException: Boolean = true, f: () -> T): T? =
    try {
        f()
    }
    catch (e: Exception) {
        if (reportException)
            log(e)

        null
    }

inline fun <T> tryOrDefault(default: T, reportException: Boolean = true, f: () -> T): T =
    try {
        f()
    }
    catch (e: Exception) {
        if (reportException)
            log(e)

        default
    }

inline fun <T> trySuccessful(reportException: Boolean = true, f: () -> T): Boolean =
    try {
        f()
        true
    }
    catch (e: Exception) {
        if (reportException)
            log(e)

        false
    }

fun getMessageEntityText(message: Message, entity: MessageEntity): String =
    message.text!!.substring(entity.offset, entity.offset + entity.length)

fun MessageEntity.isMention(): Boolean =
    type == MessageEntity.Type.MENTION || type == MessageEntity.Type.TEXT_MENTION

fun createInlineMention(text: String, userId: String): String = "[$text](tg://user?id=$userId)"

fun isAdmin(bot: Bot, chatId: ChatId, userId: Long): Boolean =
    bot.getChatMember(chatId, userId).get().status == "creator"

fun isCreator(bot: Bot, chatId: ChatId, userId: Long): Boolean =
    bot.getChatMember(chatId, userId).get().status == "creator"

fun matchesCommand(text: String, command: String, botUsername: String): Boolean =
    text == "/$command" || text == "/$command@$botUsername"