package com.example.chatapplication.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.chatapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    private lateinit var btnLogin: Button
    private lateinit var btnSignUp:Button

    private lateinit var edtName:EditText
    private lateinit var edtEmail:EditText
    private lateinit var edtPassword:EditText
    private lateinit var edtConfirmPassword:EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        btnLogin = findViewById(R.id.btnLogin)
        btnSignUp = findViewById(R.id.btnSignUp)

        edtName = findViewById(R.id.edtName)
        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword)

        auth = FirebaseAuth.getInstance()

        btnSignUp.setOnClickListener {
            val userName = edtName.text.toString()
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()
            val confirmPassword = edtConfirmPassword.text.toString()

            if (TextUtils.isEmpty(userName)){
                Toast.makeText(this,"UserName Is Required.",Toast.LENGTH_LONG).show()
            }
            if (TextUtils.isEmpty(email)){
                Toast.makeText(this,"Email Is Required.",Toast.LENGTH_LONG).show()
            }
            if (TextUtils.isEmpty(password)){
                Toast.makeText(this,"Password Is Required.",Toast.LENGTH_LONG).show()
            }
            if (TextUtils.isEmpty(confirmPassword)){
                Toast.makeText(this,"ConfirmPassword Is Required.",Toast.LENGTH_LONG).show()
            }

            if (password != confirmPassword){
                Toast.makeText(this,"Password Not Match.",Toast.LENGTH_LONG).show()
            }

            registerUser(userName,email,password)
        }

        btnLogin.setOnClickListener {
            val intent:Intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun registerUser(userName:String,email:String,password:String){
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful){
                    val user : FirebaseUser? = auth.currentUser
                    val userId : String = user!!.uid

                    databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

                    val hashMap : HashMap<String,String> = HashMap()
                    hashMap.put("userId",userId)
                    hashMap.put("userName",userName)
                    hashMap.put("profileImage","")

                    databaseReference.setValue(hashMap).addOnCompleteListener(this) {
                        if (it.isSuccessful){
                            edtName.setText("")
                            edtEmail.setText("")
                            edtPassword.setText("")
                            edtConfirmPassword.setText("")
                            //open Home Activity
                            val intent:Intent = Intent(this, UsersActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
    }
}