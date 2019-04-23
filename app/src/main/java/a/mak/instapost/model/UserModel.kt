package a.mak.instapost.model

class UserModel {
    companion object Factory {
        fun create(): UserModel = UserModel()
    }
    var user_id: String? = null
    var user_name: String? = null
    var user_nic: String? = null
    var user_email: String? = null
    var id: String? = null
}