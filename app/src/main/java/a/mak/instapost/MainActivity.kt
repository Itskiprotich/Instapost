package a.mak.instapost

import a.mak.instapost.data.AllUserHashTags

import a.mak.instapost.registration.Register
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.AppCompatTextView
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private lateinit var registrationLink: AppCompatTextView
    private lateinit var userEmail: EditText
    private lateinit var userPassword: EditText
    private lateinit var logButton: AppCompatButton
    private lateinit var mProgress: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        userEmail = findViewById(R.id.emailAddress)
        userPassword = findViewById(R.id.passwordInput)
        logButton = findViewById(R.id.appCompatButtonLogin)
        mProgress = findViewById(R.id.mProgress)
        mProgress.visibility= View.GONE
        registrationLink = findViewById(R.id.textViewLinkRegister)
        logButton.setOnClickListener() {
            val emData = userEmail.text.toString()
            val paData = userPassword.text.toString()
            if (emData.isEmpty() || paData.isEmpty()) {
                Toast.makeText(this@MainActivity, "Enter Email and Password", Toast.LENGTH_LONG).show()
            } else {
                logData(emData, paData)
            }
        }
        registrationLink.setOnClickListener() {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }

    private fun logData(em_data: String?, pa_data: String?) {
        mProgress.visibility= View.VISIBLE

        mAuth!!.signInWithEmailAndPassword(em_data!!, pa_data!!)
            .addOnCompleteListener(this) { task ->
        mProgress.visibility= View.GONE
                if (task.isSuccessful) {
                    // Sign in success, update UI with signed-in user's information
                    Log.d("TAG", "signInWithEmail:success")
                    finish()
                    updateUI()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.e("TAG", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        this@MainActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

    }

    private fun updateUI() {
        val intent = Intent(this, AllUserHashTags::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        val mUser = mAuth!!.currentUser
        if (mUser != null) {
            updateUI()
        }
    }
}
