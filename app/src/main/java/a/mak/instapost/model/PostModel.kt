package a.mak.instapost.model

class PostModel {

    companion object Factory {
        fun create(): PostModel = PostModel()
    }

    var post_image: String? = null
    var post_desc: String? = null
    var post_hash: String? = null
    var id: String? = null
}