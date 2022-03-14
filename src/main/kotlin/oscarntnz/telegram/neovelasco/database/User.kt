package oscarntnz.telegram.neovelasco.database

import org.litote.kmongo.Id
import org.litote.kmongo.newId

data class User(val _id: Id<User> = newId(), val username: String, val userId: String)