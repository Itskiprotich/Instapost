package a.mak.instapost.each

import a.mak.instapost.model.Users
import a.mak.instapost.R
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class EachUser : AppCompatActivity() {
    private lateinit var arrayList: MutableList<Users>
    private lateinit var recyclerView: RecyclerView
    private var progressBar: ProgressBar? = null
    private lateinit var recyclerAdapter: RecyclerAdapter
    private lateinit var reference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var context: Context
    private var mAuth: FirebaseAuth? = null
    private val writeexternalstoragecode = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eachuser)

        val user_id = intent.getStringExtra("user_id")
        val user_name = intent.getStringExtra("user_name")
        val actionbar = supportActionBar
        actionbar!!.title = user_name
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)
        arrayList = ArrayList()
        context = this@EachUser

        mAuth = FirebaseAuth.getInstance()
        progressBar = findViewById(R.id.loadprogressBar)
        progressBar!!.visibility = View.GONE
        recyclerView = findViewById(R.id.recyclerView)
        recyclerAdapter = RecyclerAdapter(arrayList, context)
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = recyclerAdapter
        addData(user_id)

    }


    private fun addData(user_id: String) {
        progressBar!!.visibility = View.VISIBLE
        database = FirebaseDatabase.getInstance()
        reference = database.getReference("Personnal/" + user_id)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                arrayList.clear()
                progressBar!!.visibility = View.GONE

                for (messageSnapshot in dataSnapshot.children) {
                    val user_name = messageSnapshot.child("post_image").value as String?
                    val user_email = messageSnapshot.child("post_desc").value as String?
                    val user_nic = messageSnapshot.child("post_hash").value as String?

                    val resultData = Users()
                    resultData.user_name = user_name.toString()
                    resultData.user_email = user_email.toString()
                    resultData.user_nic = user_nic.toString()
                    arrayList.add(resultData)
                    recyclerAdapter.notifyDataSetChanged()

                }

            }

            override fun onCancelled(error: DatabaseError) {

                Log.w("Hello", "Failed to read value.", error.toException())
            }
        })

    }

    inner class RecyclerAdapter(internal var arrayList: List<Users>, internal var context: Context) :
        RecyclerView.Adapter<MyHoder>() {

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyHoder {
            return MyHoder(LayoutInflater.from(this.context).inflate(R.layout.posts_down, viewGroup, false))
        }

        override fun onBindViewHolder(myHoder: MyHoder, i: Int) {
            val resultData = arrayList[i]
            var image = resultData.user_name
            Picasso.get().load(image)
                .fit()
                .placeholder(R.drawable.hot)
                .centerCrop()
                .into(myHoder.Name)
            myHoder.Nic.text = "#" + resultData.user_nic
            myHoder.Email.text = resultData.user_email
            myHoder.Download.setOnClickListener() {
                downloadImage(image)

            }

        }

        override fun getItemCount(): Int {
            var arr = 0
            try {
                if (arrayList.size == 0) {
                    arr = 0
                } else {
                    arr = arrayList.size
                }
            } catch (e: Exception) {
            }

            return arr
        }
    }

    inner class imageDownload(context: Context) : AsyncTask<String, Unit, Unit>() {
        private var mContext: WeakReference<Context> = WeakReference(context)
        override fun doInBackground(vararg params: String?) {
            val url = params[0]
            val request = RequestOptions()
                .override(100)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)

            mContext.get()?.let {
                val bitmap = Glide.with(it)
                    .asBitmap()
                    .load(url)
                    .apply(request)
                    .submit()
                    .get()

                try {
                    val timeStamp =
                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())

                    val f = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Instapost")
                    if (!f.exists()) {
                        f.mkdirs()
                    }
                    val imageName = "$timeStamp.PNG"
                    val file = File(f, imageName)
                    val out = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                    out.close()




                } catch (e: Exception) {
                    Log.d("Error", "Failed to save")
                }
            }
        }

        override fun onPostExecute(result: Unit?) {
            super.onPostExecute(result)
            Toast.makeText(context, "Image saved ", Toast.LENGTH_SHORT).show()
        }

    }

    private fun downloadImage(image: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                requestPermissions(permission, writeexternalstoragecode)
            } else {

                imageDownload(context).execute(image)

            }
        } else {

            imageDownload(context).execute(image)
        }

    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    inner class MyHoder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var Name: ImageView
        var Email: TextView
        var Nic: TextView
        var Download: ImageView


        init {
            Name = itemView.findViewById(R.id.imageView)
            Email = itemView.findViewById(R.id.textView)
            Nic = itemView.findViewById(R.id.textView2)
            Download = itemView.findViewById(R.id.mDownload)

        }
    }

}
