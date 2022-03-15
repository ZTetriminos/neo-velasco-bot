package oscarntnz.telegram.neovelasco

import clockvapor.markov.MarkovChain
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.MessageEntity
import com.github.kotlintelegrambot.entities.User
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import kotlin.math.pow
import kotlin.text.Regex.Companion.escape

val MarkovChain.wordCounts: Map<String, Int> get() {
    val map = hashMapOf<String, Int>()

    for ((_, dataMap) in data)
        for ((word, count) in dataMap) {
            val sanitized = word.sanitize()

            if (!word.isMedia() && sanitized.isNotBlank())
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
const val ANSI_BLUE = "\u001B[34m"
const val ANSI_PURPLE = "\u001B[35m"
const val ANSI_CYAN = "\u001B[36m"
const val ANSI_WHITE = "\u001B[37m"

private val GIF_BRACKETS = Pair("[GIF]", "[/GIF]")
private val STICKER_BRACKETS = Pair("[STICKER]", "[/STICKER]")
private val VIDEO_BRACKETS = Pair("[VIDEO]", "[/VIDEO]")
private val AUDIO_BRACKETS = Pair("[AUDIO]", "[/AUDIO]")
private val VOICE_BRACKETS = Pair("[VOICE]", "[/VOICE]")
private val PHOTO_BRACKETS = Pair("[PHOTO]", "[/PHOTO]")
private val VIDEO_NOTE_BRACKETS = Pair("[VIDEO_NOTE]", "[/VIDEO_NOTE]")
enum class MessageType { GIF, STICKER, VIDEO, AUDIO, VOICE, PHOTO, VIDEO_NOTE }

private val BRACKETS_MAP = mapOf(
    Pair(MessageType.GIF, GIF_BRACKETS),
    Pair(MessageType.STICKER, STICKER_BRACKETS),
    Pair(MessageType.VIDEO, VIDEO_BRACKETS),
    Pair(MessageType.AUDIO, AUDIO_BRACKETS),
    Pair(MessageType.VOICE, VOICE_BRACKETS),
    Pair(MessageType.PHOTO, PHOTO_BRACKETS),
    Pair(MessageType.VIDEO_NOTE, VIDEO_NOTE_BRACKETS)
)

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

    println("${ANSI_PURPLE}$timestamp: ${s}$ANSI_RESET")
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
    bot.getChatMember(chatId, userId).get().status.matches(Regex("creator|administrator"))

fun isCreator(bot: Bot, chatId: ChatId, userId: Long): Boolean =
    bot.getChatMember(chatId, userId).get().status == "creator"

fun matchesCommand(text: String, command: String, botUsername: String): Boolean =
    text == "/$command" || text == "/$command@$botUsername"

fun String.toMarkovString(type: MessageType): String {
    val brackets = BRACKETS_MAP[type]

    return if(brackets != null)
        "${brackets.first}${this}${brackets.second}"
    else
        this
}

fun String.isMedia(type: MessageType): Boolean {
    val brackets = BRACKETS_MAP[type]

    if(brackets != null) {
        val regex = Regex("^${escape(brackets.first)}(.*)${escape(brackets.second)}\$")

        return this.contains(regex)
    }

    return false
}

fun String.isMedia(): Boolean = BRACKETS_MAP.keys.count { this.isMedia(it) } > 0

fun String.getMediaType(): MessageType? = BRACKETS_MAP.keys.find { this.isMedia(it) }

fun String.removeBrackets(type: MessageType): String {
    val brackets = BRACKETS_MAP[type]
    log("Detected this brackets")
    if(brackets != null) {
        val regex = Regex("^${Pattern.quote(brackets.first)}|${Pattern.quote(brackets.second)}\$")
        log("This is the resulting regex: $regex")
        return this.replace(regex, "")
    }

    return this
}