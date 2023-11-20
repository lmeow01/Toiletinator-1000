package hk.hku.cs.toiletinator1000

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    private fun isValidSignupDetails(firstName: String, lastName: String, email: String, password: String): Boolean {
        // Perform basic validation for all sign-up details
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                && password.isNotEmpty()
                && firstName.isNotEmpty()
                && lastName.isNotEmpty()
    }

    private fun performSignUp(firstName: String, lastName: String, email: String, password: String) {
        Toast.makeText(this, "Sign-up successful for $email", Toast.LENGTH_SHORT).show()

        // Redirect to the main screen after successful sign-up
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    //login here text is clicked at the bottom
    private fun redirectToLogin(){
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        val signUpButton: Button = findViewById(R.id.signupbtn)
        signUpButton.setOnClickListener {
            val firstNameEditText: EditText = findViewById(R.id.firstName)
            val lastNameEditText: EditText = findViewById(R.id.lastName)
            val emailEditText: EditText = findViewById(R.id.emailSignup)
            val passwordEditText: EditText = findViewById(R.id.pwSignup)

            val firstName = firstNameEditText.text.toString()
            val lastName = lastNameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (isValidSignupDetails(firstName, lastName, email, password)) {
                // Call function to perform sign-up
                performSignUp(firstName, lastName, email, password)
            } else {
                Toast.makeText(this, "Invalid sign-up details", Toast.LENGTH_SHORT).show()
            }
        }

        val loginText: TextView = findViewById(R.id.loginHere)
        loginText.setOnClickListener {
            redirectToLogin();
        }
    }
}