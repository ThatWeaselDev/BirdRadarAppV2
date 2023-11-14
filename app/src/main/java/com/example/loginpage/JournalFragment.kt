
package com.example.loginpage
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.firebase.database.FirebaseDatabase

//import com.azure.storage.blob.BlobServiceClientBuilder
//import com.azure.storage.blob.specialized.BlockBlobClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JournalFragment : Fragment() {

    private lateinit var editBirdName: EditText
    private lateinit var editBirdDescription: EditText
    private lateinit var editBirdLatitude: EditText
    private lateinit var editBirdLongitude: EditText
    private lateinit var btnAddBird: Button

    val database = FirebaseDatabase.getInstance()
    val myDB = database.getReference("message")

   // myDB.setValue("Database connected")

    private val birdList = mutableListOf<Bird>()

   // val blobContainerName = "opensource7312" // Set your Azure Blob Storage container name
    //val connectionString = "DefaultEndpointsProtocol=https;AccountName=opensource7312;AccountKey=r/+IftxJviDVRnMUT3IWEUlQANJE3GWKX/WRC4clYllL63eeBsfD9rJP+RIud+NyhhO9wPzK2Lw++AStWXbttw==;EndpointSuffix=core.windows.net" // Blob storage connection string

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


                // Get a reference to the "birds" node in your Firebase database
                val birdsRef = database.getReference("birds")

                // Push the bird data to the database
                val newBirdRef = birdsRef.push()
                newBirdRef.setValue(bird)


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

