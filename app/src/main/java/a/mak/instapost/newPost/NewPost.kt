package a.mak.instapost.newPost

import a.mak.instapost.model.PostModel
import a.mak.instapost.model.UploadHash
import a.mak.instapost.R
import a.mak.instapost.stats.Statisctics
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatButton
import android.view.View

import android.widget.*
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.IOException
import java.util.ArrayList

class NewPost : AppCompatActivity() {
    private lateinit var mDatabase: DatabaseReference
    private val mPERMISSION_REQUEST_CODE = 1001
    private val mPICK_IMAGE_REQUEST = 900
    private lateinit var filePath: Uri

    private var mAuth: FirebaseAuth? = null
    private lateinit var imageview: ImageView
    private  lateinit var mDescription: EditText
    private lateinit var mHashTags: EditText
    private lateinit var upload: AppCompatButton
    private lateinit var select: AppCompatButton
    private lateinit var storage: FirebaseStorage

    private lateinit var reference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var arrayList: MutableList<String>

    private lateinit var mProgress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post)
        arrayList = ArrayList()
        mProgress = findViewById(R.id.mProgress)
        mProgress.visibility= View.GONE
        mDescription = findViewById(R.id.descriptionData)
        mHashTags = findViewById(R.id.hashtagName)

        upload = findViewById(R.id.upload)
        select = findViewById(R.id.selectImage)
        imageview = findViewById(R.id.imageHolder)
        mAuth = FirebaseAuth.getInstance()

        val actionbar = supportActionBar
        actionbar!!.title = "New Post"
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)
        mDatabase = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()

        select.setOnClickListener() {

            when {
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) -> {
                    if (ContextCompat.checkSelfPermission(this@NewPost, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), mPERMISSION_REQUEST_CODE)
                    } else {
                        selectImage()
                    }
                }

                else -> selectImage()
            }


        }
        upload.setOnClickListener() {

            val post_desc = mDescription.text.toString().trim()
            val post_hash = mHashTags.text.toString().trim()

            if (post_hash.isEmpty()) {
                Toast.makeText(this@NewPost, "Enter HashTag", Toast.LENGTH_LONG).show()
            } else {
                savePost(post_desc, post_hash)
            }
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            mPERMISSION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this@NewPost, " Permission Granted!!", Toast.LENGTH_SHORT).show()

                else
                    selectImage()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == mPICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {

            filePath = data.data

            try {

                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)

                imageview.setImageBitmap(bitmap)

            } catch (e: IOException) {

                e.printStackTrace()
            }

        }
    }

    private fun selectImage() {

        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), mPICK_IMAGE_REQUEST)

    }

    private fun savePost(post_desc: String?, post_hash: String?) {
        mProgress.visibility= View.VISIBLE
        val fileRef = storage.reference.child("Posts/").child(filePath.lastPathSegment)

        var uploadTask = fileRef.putFile(filePath)

        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation fileRef.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                check_hashtag(post_desc, post_hash, downloadUri)
                mProgress.visibility= View.GONE
            }
        }

    }

    private fun check_hashtag(post_desc: String?, post_hash: String?, downloadUri: Uri?) {
        database = FirebaseDatabase.getInstance()
        reference = database.getReference("Posts")
        reference.orderByChild("post_hash").equalTo(post_hash).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    addtoPersonnal(downloadUri.toString(),post_desc.toString(),post_hash.toString())
                } else {
                    savehashtag(post_desc,post_hash,downloadUri)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun savehashtag(post_desc: String?, post_hash: String?, downloadUri: Uri?) {
        val model = UploadHash.create()
        model.post_hash = post_hash
        val add = mDatabase.child(Statisctics.FIREBASE_POSTS).push()
        model.id = add.key
        add.setValue(model)
        addtoPersonnal(downloadUri.toString(),post_desc.toString(),post_hash.toString())
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    private fun addtoPersonnal(url: String, de: String, ha: String) {
        val mUser = mAuth!!.currentUser
        val model = PostModel.create()
        model.post_image = url
        model.post_desc = de
        model.post_hash = ha
        val add = mDatabase.child("Personnal/"+mUser!!.uid).push()
        model.id = add.key
        add.setValue(model)
        addtoHash(url,de,ha)

    }

    private fun addtoHash(url: String, de: String, ha: String) {
        val model = PostModel.create()
        model.post_image = url
        model.post_desc =de
        model.post_hash = ha
        val add = mDatabase.child("Hashtags/"+ha).push()
        model.id = add.key
        add.setValue(model)
        Toast.makeText(this@NewPost, "Upload Successful", Toast.LENGTH_LONG).show()
        clearFields()
    }


    private fun clearFields() {
        mDescription.text.clear()
        mHashTags.text.clear()
        imageview.setImageResource(android.R.color.transparent)
    }
}

