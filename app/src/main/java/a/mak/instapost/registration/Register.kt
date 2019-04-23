package a.mak.instapost.registration

import a.mak.instapost.model.UserModel
import a.mak.instapost.R
import a.mak.instapost.stats.Statisctics
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Register : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private var mAuth: FirebaseAuth? = null
    private lateinit var eName: EditText
    private lateinit var eNic: EditText
    private lateinit var eEmail: EditText
    private lateinit var ePass: EditText
    private lateinit var eConPas: EditText
    private lateinit var eReg: Button
    private lateinit var eLog: TextView
    private lateinit var mProgress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        eName = findViewById(R.id.userFullNames)
        eNic = findViewById(R.id.userNickName)
        eEmail = findViewById(R.id.useremailAddress)
        ePass = findViewById(R.id.userPassword)
        eConPas = findViewById(R.id.confirmPassword)
        eReg = findViewById(R.id.registerButton)
        eLog = findViewById(R.id.loginTextview)
        mAuth = FirebaseAuth.getInstance()

        mProgress = findViewById(R.id.mProgress)
        mProgress.visibility= View.GONE
        val actionbar = supportActionBar
        actionbar!!.title = "Account Creation"
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)
        database = FirebaseDatabase.getInstance().reference
        eLog.setOnClickListener() {
            finish()
        }
        eReg.setOnClickListener() {
            val username = eName.text.toString()
            val usernic = eNic.text.toString()
            val useremail = eEmail.text.toString()
            val pass = ePass.text.toString()
            val confirmpass = eConPas.text.toString()
            if (username.isEmpty() || usernic.isEmpty() || useremail.isEmpty() || pass.isEmpty() || confirmpass.isEmpty()) {
                Toast.makeText(this@Register, "Enter all Fields", Toast.LENGTH_LONG).show()
            } else {
                if (pass.length <= 6) {
                    Toast.makeText(this@Register, "Password should be more than 6 characters", Toast.LENGTH_LONG).show()
                } else {
                    if (pass.equals(confirmpass)) {
                        login(username, usernic, useremail, pass)
                    } else {
                        Toast.makeText(this@Register, "Password does not match..!!", Toast.LENGTH_LONG).show()
                    }
                }
            }

        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun login(user_name: String, user_nic: String, user_email: String, user_pass: String) {
        mProgress.visibility= View.VISIBLE
        mAuth!!.createUserWithEmailAndPassword(user_email, user_pass)
            .addOnCompleteListener(this) { task ->
                mProgress.visibility= View.VISIBLE
                if (task.isSuccessful) {
                    val mUser = mAuth!!.currentUser
                    val model = UserModel.create()
                    model.user_name = user_name
                    model.user_nic = user_nic
                    model.user_email = user_email
                    model.user_id = mUser!!.uid

                    val add = database.child(Statisctics.FIREBASE_TASK).push()
                    model.id = add.key

                    add.setValue(model)
                    Toast.makeText(this@Register, "Account creation successful", Toast.LENGTH_LONG).show()
                    finish()


                } else {

                    Toast.makeText(
                        this@Register, "Authentication failed.", Toast.LENGTH_SHORT
                    ).show()

                }
            }

    }
}
