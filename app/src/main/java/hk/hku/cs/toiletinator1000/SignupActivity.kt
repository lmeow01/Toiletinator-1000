package hk.hku.cs.toiletinator1000

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

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

        auth = FirebaseAuth.getInstance()

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


            // Cloud function from FirebaseAuth to create user
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful){

                    // save the current user UID to User collection -> document ID
                    val user = auth.currentUser
                    user?.let {
                        val currentUserUID = it.uid

                        //create a firestore document for user under 'User' collection
                        val userDocumentRef = db.collection("User").document(currentUserUID)

                        //create a Fav empty array
                        val userData = hashMapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "Fav" to emptyList<String>()
                        )

                        // Set the 'Fav' field in the user's document
                        userDocumentRef.set(userData, SetOptions.merge())
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Sign-up successful for $email",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Redirect to the main screen after successful sign-up
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Failed to update Fav field: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    Toast.makeText(this, task.exception?.message.toString(), Toast.LENGTH_SHORT).show()
                }
            }

        }

        val loginText: TextView = findViewById(R.id.loginHere)
        loginText.setOnClickListener {
            redirectToLogin();
        }
    }
}