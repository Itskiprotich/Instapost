package a.mak.instapost.data

import a.mak.instapost.each.EachUser
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

class AllUserNames : AppCompatActivity() {
    private lateinit var arrayList: MutableList<Users>
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private  lateinit var recyclerAdapter: RecyclerAdapter
    private lateinit var reference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var context: Context

    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_allusernames)
        arrayList = ArrayList()
        context = this@AllUserNames
        mAuth = FirebaseAuth.getInstance()
        progressBar = findViewById(R.id.loadprogressBar)
        progressBar.visibility = View.GONE
        recyclerView = findViewById(R.id.recyclerView)
        recyclerAdapter = RecyclerAdapter(arrayList, context)
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        recyclerView.itemAnimator = DefaultItemAnimator()
        val actionbar = supportActionBar
        actionbar!!.title = "Users"
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)
        recyclerView.adapter = recyclerAdapter
        addData()
        val fab = findViewById<View>(R.id.fab) as FloatingActionButton

        fab.setOnClickListener {
            addpost()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun addpost() {
        val intent = Intent(this, NewPost::class.java)
        startActivity(intent)
    }

    private fun addData() {
        progressBar.visibility = View.VISIBLE
        database = FirebaseDatabase.getInstance()
        reference = database.getReference("Users")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                arrayList.clear()
                progressBar.visibility = View.GONE

                for (messageSnapshot in dataSnapshot.children) {
                    val user_name = messageSnapshot.child("user_name").value as String?
                    val user_email = messageSnapshot.child("user_email").value as String?
                    val user_nic = messageSnapshot.child("user_nic").value as String?
                    val user_id = messageSnapshot.child("user_id").value as String?

                    val resultData = Users()
                    resultData.user_name = user_name.toString()
                    resultData.user_email = user_email.toString()
                    resultData.user_nic = user_nic.toString()
                    resultData.user_id = user_id.toString()

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
            return MyHoder(LayoutInflater.from(this.context).inflate(R.layout.users, viewGroup, false))
        }

        override fun onBindViewHolder(myHoder: MyHoder, i: Int) {
            val resultData = arrayList[i]
            val user_id=resultData.user_id
            val user_name=resultData.user_name
            myHoder.Name.text = user_name
            myHoder.Nic.text = resultData.user_nic
            myHoder.Email.text = resultData.user_email
            myHoder.Email.setOnClickListener{
                viewUser(user_id,user_name)
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

    private fun viewUser(user_id: String,user_name: String) {
        val intent = Intent(this@AllUserNames, EachUser::class.java)
        val bundle = Bundle()
        bundle.putString("user_id", user_id)
        bundle.putString("user_name", user_name)
        intent.putExtras(bundle)
        startActivity(intent)

    }

    inner class MyHoder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         var Name: TextView
         var Email: TextView
         var Nic: TextView

        init {
            Name = itemView.findViewById(R.id.name)
            Email = itemView.findViewById(R.id.email)
            Nic = itemView.findViewById(R.id.nicname)

        }
    }
}
