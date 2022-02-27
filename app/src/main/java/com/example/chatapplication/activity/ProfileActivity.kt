package com.example.chatapplication.activity

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.example.chatapplication.R
import com.example.chatapplication.model.User
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var databaseReference: DatabaseReference

    private lateinit var imgBack: ImageView
    private lateinit var imgProfile: CircleImageView
    private lateinit var userImage:CircleImageView
    private lateinit var btnSave:Button
    private lateinit var edtuserName:EditText

    private var filepath:Uri ?= null
    private val PICK_IMAGE_REQUEST:Int = 2020

    private lateinit var storage:FirebaseStorage
    private lateinit var storageRef:StorageReference

    private lateinit var progressBar:ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)

        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)

                edtuserName.setText(user!!.userName)

                if (user!!.profileImage == ""){
                    userImage.setImageResource(R.mipmap.ic_launcher)
                }else{
                    Glide.with(this@ProfileActivity).load(user.profileImage).into(userImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity,error.message,Toast.LENGTH_LONG).show()
            }

        })

        imgBack = findViewById(R.id.imgBack)
        imgProfile = findViewById(R.id.imgProfile)
        userImage = findViewById(R.id.userImage)
        btnSave =  findViewById(R.id.btnSave)
        progressBar = findViewById(R.id.progressBar)
        edtuserName = findViewById(R.id.edtuserName)

        imgBack.setOnClickListener {
            onBackPressed()
        }

        userImage.setOnClickListener {
            chooseImage()
        }

        btnSave.setOnClickListener {
            uploadImage()
            progressBar.visibility = View.VISIBLE
        }

    }

    private fun chooseImage(){
        val intent:Intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent,"Select Image"),PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode != null){
            filepath = data!!.data
            try {
                var bitmap:Bitmap = MediaStore.Images.Media.getBitmap(contentResolver,filepath)
                userImage.setImageBitmap(bitmap)
                btnSave.visibility = View.VISIBLE
            }catch (e:IOException){
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage(){
        if (filepath != null){

            var ref:StorageReference = storageRef.child("image/"+UUID.randomUUID().toString())
            ref.putFile(filepath!!)
                .addOnSuccessListener {

                        val hashMap : HashMap<String,String> = HashMap()
                        hashMap.put("userName",edtuserName.text.toString())
                        hashMap.put("profileImage",filepath.toString())

                    databaseReference.updateChildren(hashMap as Map<String,Any>)

                        progressBar.visibility = View.GONE
                        Toast.makeText(this,"Uploaded.",Toast.LENGTH_LONG).show()
                        btnSave.visibility = View.GONE
                }
                .addOnFailureListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this,"Failed.",Toast.LENGTH_LONG).show()
                }
        }
    }
}