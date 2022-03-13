package oscarntnz.telegram.neovelasco.database

import clockvapor.markov.MarkovChain
import org.litote.kmongo.Id
import org.litote.kmongo.newId

data class Chain(val _id: Id<Chain> = newId(), val chatId: String, val userId: String, val data: MutableMap<String, Int>) {
    fun toMarkovChain(): MarkovChain = MarkovChain(mutableMapOf(Pair("data", data)))
}