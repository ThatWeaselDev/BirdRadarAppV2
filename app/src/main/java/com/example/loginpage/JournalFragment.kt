package com.example.loginpage
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment

class JournalFragment : Fragment() {

    private lateinit var editBirdName: EditText
    private lateinit var editBirdDescription: EditText
    private lateinit var editBirdLatitude: EditText
    private lateinit var editBirdLongitude: EditText
    private lateinit var btnAddBird: Button

    private val birdList = mutableListOf<Bird>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_journal, container, false)

        editBirdName = rootView.findViewById(R.id.editBirdName)
        editBirdDescription = rootView.findViewById(R.id.editBirdDescription)
        editBirdLatitude = rootView.findViewById(R.id.editBirdLatitude)
        editBirdLongitude = rootView.findViewById(R.id.editBirdLongitude)
        btnAddBird = rootView.findViewById(R.id.btnAddBird)

        btnAddBird.setOnClickListener {
            val name = editBirdName.text.toString()
            val description = editBirdDescription.text.toString()
            val latitude = editBirdLatitude.text.toString().toDoubleOrNull()
            val longitude = editBirdLongitude.text.toString().toDoubleOrNull()

            if (name.isNotBlank() && description.isNotBlank() && latitude != null && longitude != null) {
                val bird = Bird(name, description, R.drawable.ic_bird, latitude, longitude)
                birdList.add(bird)

                // Optionally, you can clear the input fields after adding the bird
                clearInputFields()

                // Print or display the added bird data, or save it to a database
                // You can also update the UI to show the added birds in a list, for example.
                // For simplicity, we're just printing the bird data here.
                println("Added Bird: $bird")
            } else {
                // Handle validation errors, e.g., show an error message to the user
            }
        }

        return rootView
    }

    private fun clearInputFields() {
        editBirdName.text.clear()
        editBirdDescription.text.clear()
        editBirdLatitude.text.clear()
        editBirdLongitude.text.clear()
    }
}
data class Bird(
    val name: String,
    val description: String,
    val photoResId: Int,
    val latitude: Double,
    val longitude: Double
)