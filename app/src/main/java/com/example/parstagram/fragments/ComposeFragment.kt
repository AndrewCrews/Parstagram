package com.example.parstagram.fragments

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.parstagram.LoginActivity
import com.example.parstagram.MainActivity
import com.example.parstagram.Post
import com.example.parstagram.R
import com.parse.ParseFile
import com.parse.ParseUser
import java.io.File

private const val ARG_PARAM2 = "param2"

class ComposeFragment : Fragment() {

    val photoFileName = "photo.jpg"
    var photoFile: File? = null
    val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034

    lateinit var ivImage: ImageView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //set onclickListeners and setup logic

        ivImage = view.findViewById(R.id.ivImage)

        fun submitPost(description: String, user: ParseUser, file: File) {
            //create post object
            val post = Post()
            post.setDescription(description)
            post.setUser(user)
            post.setImage(ParseFile(file))
            post.saveInBackground{  exception ->
                if (exception != null) {
                    Log.e(MainActivity.TAG, "Error while saving post")
                    exception.printStackTrace()
                } else {
                    Log.i(MainActivity.TAG, "Successfully saved post")
                    post.setDescription("")

                }
            }
        }


        view.findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            val pb = view.findViewById<View>(R.id.pbLoading) as ProgressBar
            val description = view.findViewById<EditText>(R.id.description).text.toString()
            val user = ParseUser.getCurrentUser()
            pb.visibility = ProgressBar.VISIBLE;
            pb.visibility = ProgressBar.INVISIBLE;
            if (photoFile != null) {
                submitPost(description, user, photoFile!!)
            } else {
                Log.e(MainActivity.TAG, "No photo")
            }
        }
        fun goToLoginActivity() {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<Button>(R.id.button3).setOnClickListener {
            ParseUser.logOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            val currentUser = ParseUser.getCurrentUser() // this will now be null
            goToLoginActivity()
        }

        fun getPhotoFileUri(fileName: String): File {
            // Get safe storage directory for photos
            // Use `getExternalFilesDir` on Context to access package-specific directories.
            // This way, we don't need to request external read/write runtime permissions.
            val mediaStorageDir =
                File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), MainActivity.TAG)

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.d(MainActivity.TAG, "failed to create directory")
            }

            // Return the file target for the photo based on filename
            return File(mediaStorageDir.path + File.separator + fileName)
        }

        fun onLaunchCamera() {
            // create Intent to take a picture and return control to the calling application
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            // Create a File reference for future access
            photoFile = getPhotoFileUri(photoFileName)

            // wrap File object into a content provider
            // required for API >= 24
            // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
            if (photoFile != null) {
                val fileProvider: Uri =
                    FileProvider.getUriForFile(requireContext(), "com.codepath.fileprovider", photoFile!!)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

                // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
                // So as long as the result is not null, it's safe to use the intent.

                // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
                // So as long as the result is not null, it's safe to use the intent.
                if (intent.resolveActivity(requireContext().packageManager) != null) {
                    // Start the image capture intent to take photo
                    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
                }
            }
        }

        view.findViewById<Button>(R.id.btnTakePicture).setOnClickListener {
            onLaunchCamera()
            //launch camera to let user take picture
        }


        //Sen a Post to our parse server





        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    val takenImage = BitmapFactory.decodeFile(photoFile!!.absolutePath)
                    val ivPreview: ImageView = view.findViewById(R.id.ivImage)
                    ivPreview.setImageBitmap(takenImage)
                } else {
                    Toast.makeText(requireContext(), "picture was not taken", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}