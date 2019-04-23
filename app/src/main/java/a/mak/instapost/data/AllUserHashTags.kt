package a.mak.instapost.data

import a.mak.instapost.each.EachHash
import a.mak.instapost.MainActivity
import a.mak.instapost.model.Users

import a.mak.instapost.newPost.NewPost
import a.mak.instapost.R
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import java.util.ArrayList

class AllUserHashTags : AppCompatActivity() {
    private lateinit var arrayList: MutableList<Users>
    private lateinit var recyclerView: RecyclerView
    private var progressBar: ProgressBar? = null
    private lateinit var recyclerAdapter: RecyclerAdapter
    private lateinit var reference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var context: Context
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_allhashtags)
        arrayList = ArrayList()
        context = this@AllUserHashTags
        mAuth = FirebaseAuth.getInstance()
       progressBar = findViewById(R.id.loadprogressBar)
        progressBar!!.visibility = View.GONE
        recyclerView = findViewById(R.id.recyclerView)
        recyclerAdapter = RecyclerAdapter(arrayList, context)
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = recyclerAdapter
        val actionbar = supportActionBar
        actionbar!!.title = "All Post"
        val fab = findViewById<View>(R.id.fab) as FloatingActionButton

        fab.setOnClickListener {
            addpost()
        }
        addData()
    }


    private fun addpost() {
        val intent = Intent(this, NewPost::class.java)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.textInput) {
            val intent = Intent(this, AllUserNames::class.java)
            startActivity(intent)
            return true
        }

        if (id == R.id.textInputData) {
            mAuth!!.signOut()
            finish()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }
    private fun addData() {
        progressBar!!.visibility = View.VISIBLE
        database = FirebaseDatabase.getInstance()
        reference = database.getReference("Posts")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                arrayList.clear()
                progressBar!!.visibility = View.GONE

                for (messageSnapshot in dataSnapshot.children) {
                    val user_nic = messageSnapshot.child("post_hash").value as String?

                    val resultData = Users()
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

    inner class RecyclerAdapter( var arrayList: List<Users>,  var context: Context) :
        RecyclerView.Adapter<MyHoder>() {

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyHoder {
            return MyHoder(LayoutInflater.from(this.context).inflate(R.layout.hash, viewGroup, false))
        }

        override fun onBindViewHolder(myHoder: MyHoder, i: Int) {
            val resultData = arrayList[i]
            val user_name=resultData.user_nic
            myHoder.Name.text = ("#"+resultData.user_nic)
            myHoder.Name.setOnClickListener{
                viewUser(user_name)
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

    private fun viewUser(user_name: String) {
        val intent = Intent(this@AllUserHashTags, EachHash::class.java)
        val bundle = Bundle()
        bundle.putString("user_name", user_name)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    inner class MyHoder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         var Name: TextView

        init {
            Name = itemView.findViewById(R.id.name)

        }
    }
}
