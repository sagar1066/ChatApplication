package com.example.chatapplication.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapplication.R
import com.example.chatapplication.RetrofitInstance
import com.example.chatapplication.adapter.ChatAdapter
import com.example.chatapplication.adapter.UserAdapter
import com.example.chatapplication.model.Chat
import com.example.chatapplication.model.NotificationData
import com.example.chatapplication.model.PushNotification
import com.example.chatapplication.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.Gson
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import java.lang.Exception
import java.util.ArrayList


class ChatActivity : AppCompatActivity() {

    var firebaseUser:FirebaseUser ?= null
    var reference:DatabaseReference ?= null

    var chatList = ArrayList<Chat>()

    var topic = ""

    private lateinit var imgProfile: CircleImageView
    private lateinit var tvUserName:TextView
    private lateinit var imgBack:ImageView
    private lateinit var btnSendMessage:ImageButton
    private lateinit var edtMessage:EditText
    private lateinit var chatRecyclerView:RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        imgProfile = findViewById(R.id.imgProfile)
        tvUserName = findViewById(R.id.tvUserName)
        imgBack = findViewById(R.id.imgBack)
        btnSendMessage = findViewById(R.id.btnSendMessage)
        edtMessage = findViewById(R.id.edtMessage)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)

        var intent = intent
        var userId = intent.getStringExtra("userId")
        var userName = intent.getStringExtra("userName")

        chatRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayout.VERTICAL,false)

        imgBack.setOnClickListener {
            onBackPressed()
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)

        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val user = snapshot.getValue(User::class.java)

                tvUserName.text = user!!.userName

                if (user.profileImage == ""){
                    imgProfile.setImageResource(R.mipmap.ic_launcher)
                }else{
                    Glide.with(this@ChatActivity).load(user!!.profileImage).into(imgProfile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        btnSendMessage.setOnClickListener {
            var message:String = edtMessage.text.toString()

            if (message.isEmpty()){
                Toast.makeText(this,"Message is empty.",Toast.LENGTH_LONG).show()
                edtMessage.setText("")
            }else{
                sendMessage(firebaseUser!!.uid,userId,message)
                edtMessage.setText("")
                topic = "/topics/$userId"
                PushNotification(NotificationData(userName!!,message),
                topic).also {
                    sendNotification(it)
                }
            }
        }

        readMessage(firebaseUser!!.uid,userId)

    }

    private fun sendMessage(senderId:String,receiverId:String,message:String){
        var reference:DatabaseReference ?= FirebaseDatabase.getInstance().getReference()

        var hashMap:HashMap<String,String> = HashMap()
        hashMap.put("senderId",senderId)
        hashMap.put("receiverId",receiverId)
        hashMap.put("message",message)

        reference!!.child("Chat").push().setValue(hashMap)


    }

    fun readMessage(senderId: String,receiverId: String){
        val databaseReference : DatabaseReference = FirebaseDatabase.getInstance().getReference("Chat")

        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (dataSnapShot:DataSnapshot in snapshot.children){
                    val chat = dataSnapShot.getValue(Chat::class.java)

                    if (chat!!.senderId.equals(senderId) && chat!!.receiverId.equals(receiverId) ||
                        chat!!.senderId.equals(receiverId) && chat!!.receiverId.equals(senderId)){
                        chatList.add(chat)
                    }
                }

                val chatAdapter = ChatAdapter(this@ChatActivity,chatList)

                chatRecyclerView.adapter = chatAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful){
                Toast.makeText(this@ChatActivity,"Response ${Gson().toJson(response)}",Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this@ChatActivity,response.errorBody().toString(),Toast.LENGTH_LONG).show()
            }
        }catch (e:Exception){
            Toast.makeText(this@ChatActivity,e.message,Toast.LENGTH_LONG).show()
        }
    }

}