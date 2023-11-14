package com.example.loginpage



import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.loginpage.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var username: EditText
    lateinit var password: EditText
    lateinit var loginButton: Button
    lateinit var signUpButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        username = binding.email
        password = binding.password
        loginButton = binding.loginButton
        signUpButton = binding.signUpButton

        val gestureDetector = GestureDetector(this, object : SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 != null) {
                    if (e2.y - e1.y > 50) {
                        hideKeyboard()
                        return true
                    }
                }
                return false
            }
        })

        binding.root.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }

        loginButton.setOnClickListener {
            val email = username.text.toString()
            val password = password.text.toString()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity3::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        signUpButton.setOnClickListener {
            val intent1 = Intent(this, MainActivity2::class.java)
            startActivity(intent1)
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}
