package com.example.loginpage

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.loginpage.databinding.ActivityMain2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class MainActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityMain2Binding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.email
        binding.password
        binding.confirmPassword

        auth = FirebaseAuth.getInstance()
        // Set up a gesture detector
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                // Check if the swipe is towards the left
                if (e1 != null && e2 != null && e2.x - e1.x > 50) {
                    // Swipe towards the left detected, finish the activity (go back)
                    finish()
                    return true
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })

        // Set up the onTouchListener for the root view
        binding.root.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)

        }
        fun saveUserDataToDatabase(userId: String?, email: String, password: String) {
            if (userId != null) {
                val databaseReference = FirebaseDatabase.getInstance().getReference("users")
                val user = User(userId, email ,password) // Add other properties as needed
                databaseReference.child(userId).setValue(user)
            }
        }

        // Set up click listener for SignUp button
        binding.signUpButton.setOnClickListener {
            val emailInput = binding.email.text.toString()
            val passwordInput = binding.password.text.toString()
            val confirmPasswordInput = binding.confirmPassword.text.toString()

            if (emailInput.isEmpty() || passwordInput.isEmpty() || confirmPasswordInput.isEmpty()) {
                // Handle empty fields error
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordInput != confirmPasswordInput) {
                // Handle password mismatch error
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign-up success
                        val user = auth.currentUser

                        // Save additional user data to Firebase Realtime Database
                        saveUserDataToDatabase(user?.uid, emailInput , passwordInput)

                        Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show()
                        // You can do additional actions here, like navigating to the next activity.
                        val intent = Intent(this, MainActivity3::class.java)
                        startActivity(intent)
                    } else {
                        // Sign-up failed
                        // You can handle errors here.
                        Toast.makeText(this, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }

        }

        // Function to save user data to Firebase Realtime Database






    }
}


