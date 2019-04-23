package a.mak.instapost.model


class UploadHash {

    companion object Factory {
        fun create(): UploadHash = UploadHash()
    }
    var post_hash: String? = null
    var id: String? = null
}