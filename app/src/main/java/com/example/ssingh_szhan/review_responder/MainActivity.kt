package com.example.ssingh_szhan.review_responder

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.nl.smartreply.SmartReply
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult
import com.google.mlkit.nl.smartreply.TextMessage
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition


class MainActivity : AppCompatActivity() {
    val REQUEST_CODE = 100;


    val conversation : MutableList<TextMessage> = mutableListOf()

    private lateinit var imageView: ImageView
    private lateinit var editText: EditText
    private lateinit var editTextIntro: EditText
    private lateinit var checkBox: CheckBox
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButton1: RadioButton
    private lateinit var radioButton2: RadioButton
    private lateinit var radioButton3: RadioButton
    private lateinit var radioButton4: RadioButton
    lateinit var clipboardManager: ClipboardManager
    lateinit var clipData: ClipData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById((R.id.editText))
        editTextIntro  = findViewById(R.id.editTextIntro)
        checkBox = findViewById(R.id.checkBox)
        radioGroup = findViewById(R.id.radioGroup)
        radioButton1 = findViewById(R.id.radioButton1)
        radioButton2 = findViewById(R.id.radioButton2)
        radioButton3 = findViewById(R.id.radioButton3)
        radioButton4 = findViewById(R.id.radioButton4)
        imageView = findViewById(R.id.uploadImageView)

    }


    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE){
            imageView.setImageURI(data?.data) // handle chosen image
//            var uriString : String? = data?.data.toString();
//            println("DEBUGGER:" + uriString)
            var myBitmap: Bitmap? = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data?.data);
            println("DEBUGGER:" + myBitmap)
            lateinit var notNullBitMap : Bitmap;
            if(myBitmap != null){
                notNullBitMap = myBitmap;
            }
            val image = InputImage.fromBitmap(notNullBitMap, 0)
            val recognizer = TextRecognition.getClient()
            val result = recognizer.process(image)
                .addOnSuccessListener { visionText -> // Task completed successfully
                    // Display the text found in the textView
                    editText.setText(visionText.text)
                }
                .addOnFailureListener { // Task failed with an exception
                    // ...
                    editText.setText("recongenizer task failed")
                }
        }
    }


    fun onChecked(view: View){
        if(checkBox.isChecked){
            editTextIntro.visibility = View.VISIBLE;
        }
        else{
            editTextIntro.visibility = View.GONE;
        }

    }

    fun onSubmit(view: View){
        conversation.clear()
        conversation.add(
            TextMessage.createForRemoteUser(
                editText.text.toString(),
                System.currentTimeMillis(),
                "user"
            )
        )
        radioGroup.visibility = View.VISIBLE

        val smartReply = SmartReply.getClient()
        smartReply.suggestReplies(conversation)
                .addOnSuccessListener { result ->
                    if (result.status == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                        Toast.makeText(
                            this,
                            "text may be missing or non-english",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (result.status == SmartReplySuggestionResult.STATUS_SUCCESS) {
                        radioButton1.text = result.suggestions[0].text
                        radioButton2.text = result.suggestions[1].text
                        radioButton3.text = result.suggestions[2].text
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "listener failed", Toast.LENGTH_LONG).show()
                }

    }

    fun doCopy(text: String){
        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipData = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
    }

    fun showReply(view: View){
        var replyText = ""
        if (checkBox.isChecked){
            replyText = editTextIntro.text.toString()
        }
        if (view.id != R.id.radioButton4){
            replyText = replyText + " " + (view as RadioButton).text.toString()
        }
        AlertDialog.Builder(this)
                .setTitle("Composed Reply")
                .setMessage(replyText)
                .setPositiveButton("copy") { dialog, which -> doCopy(replyText) }
                .setNegativeButton("close", null)
                .show()
    }

    fun imgOnClick(view: View) {
        openGalleryForImage();
    }

    fun imgBtnClick(view: View) {
        openGalleryForImage();
    }


}