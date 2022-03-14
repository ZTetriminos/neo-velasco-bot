package oscarntnz.telegram.neovelasco.database

import clockvapor.markov.MarkovChain
import org.litote.kmongo.Id
import org.litote.kmongo.newId

data class Chain(val _id: Id<Chain> = newId(), val chatId: String, val userId: String?, val data: MutableMap<String, MutableMap<String, Int>>) {
    companion object {
        fun fromMarkovChain(chatId: String, userId: String?, chain: MarkovChain): Chain = Chain(chatId = chatId, userId = userId, data = chain.data)

        fun listToMarkovChain(chains: List<Chain>?): MarkovChain? {
            if(chains != null) {
                val total = MarkovChain()

                chains.forEach() { total.add(it.toMarkovChain()) }

                return total
            }

            return null
        }
    }

    fun toMarkovChain(): MarkovChain = MarkovChain(data)
}