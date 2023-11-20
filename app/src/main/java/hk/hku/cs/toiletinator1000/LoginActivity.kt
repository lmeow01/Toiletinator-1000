package hk.hku.cs.toiletinator1000

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class LoginActivity : AppCompatActivity() {

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

        val login: Button = findViewById(R.id.login)
        login.setOnClickListener {
            // Perform basic validation and authentication
            val emailEditText: EditText = findViewById(R.id.editTextTextEmailAddress)
            val passwordEditText: EditText = findViewById(R.id.editTextTextPassword)

            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (isValidCredentials(email, password)) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Login failed. Invalid credentials.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        val signUpText: TextView = findViewById(R.id.signup)
        signUpText.setOnClickListener {
            redirectToSignUp()
        }
    }
}
