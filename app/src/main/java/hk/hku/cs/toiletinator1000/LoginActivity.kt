package hk.hku.cs.toiletinator1000

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private fun isValidCredentials(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }

    //clicked on Sign Up Text at the bottom
    private fun redirectToSignUp() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val login: Button = findViewById(R.id.login)
        login.setOnClickListener {
            // Perform basic validation and authentication
            val emailEditText: EditText = findViewById(R.id.editTextTextEmailAddress)
            val passwordEditText: EditText = findViewById(R.id.editTextTextPassword)

            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this, "Login failed. Invalid credentials.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        val signUpText: TextView = findViewById(R.id.signup)
        signUpText.setOnClickListener {
            redirectToSignUp()
        }
    }
}
