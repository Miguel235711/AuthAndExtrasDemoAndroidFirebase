package mx.tec.authandextrasdemo

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Sampler
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main_screen.*
import kotlinx.android.synthetic.main.dialog_add.*
import java.util.*

class MainScreen : AppCompatActivity() {
    data class Post(val title: String,val content: String) {
        companion object{
            fun from (map:Map<String,String>) = object{
                val title by map
                val content by map
                val data = Post(title,content)
            }.data
        }
        /*@PropertyName("title")
        val title: String? = "",
        @PropertyName("content")
        val content: String? = ""*/
    }
    val database = Firebase.database.reference

    private fun writeNewPost(title: String,content: String): Post{
        val post = Post(title,content)
        return post
    }

    override fun onStart() {
        super.onStart()
    }
    fun getPostObject(snapshot: DataSnapshot): List<Post>{
        val posts = snapshot.value as Map<String,Map<String,String>>
        return posts.map{Post.from(it.value)}
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val childEventListener = object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val postsTr = getPostObject(snapshot)
                postsTr.forEach{
                    Log.i("child event listener ","onChildAdded title: ${it.title} content: ${it.content}")
                }
                val listAdapter = ArrayAdapter(this@MainScreen,android.R.layout.simple_list_item_1,postsTr.map{it.toString()})
                listView.adapter=listAdapter
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val postsTr = getPostObject(snapshot)
                postsTr.forEach{
                    Log.i("child event listener ","onChildChanged title: ${it.title} content: ${it.content}")
                }
                val listAdapter = ArrayAdapter(this@MainScreen,android.R.layout.simple_list_item_1,postsTr.map{it.toString()})
                listView.adapter=listAdapter
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.i("child event listener","onChildRemoved "+snapshot.toString())
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.i("child event listener","onChildMoved"+snapshot.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("child event listener","onCancelled"+error.message)
            }

        }
        database.addChildEventListener(childEventListener)
        setContentView(R.layout.activity_main_screen)
        floatingAddButton.setOnClickListener{
            val mView = this@MainScreen.layoutInflater.inflate(R.layout.dialog_add,null)
            val etTitle = mView.findViewById<EditText>(R.id.titleEditText)
            val etContent = mView.findViewById<EditText>(R.id.multilineEditText)
            AlertDialog.Builder(this@MainScreen)
                .setTitle("Add Post")
                .setView(mView)
                .setNegativeButton(R.string.add_button) { dialog, button ->
                    val post = writeNewPost(etTitle.text.toString(),etContent.text.toString())
                    database.child("posts").child(UUID.randomUUID().toString()).setValue(post)
                }
                .setPositiveButton(R.string.cancel_button) { dialog, button ->
                    dialog.dismiss()
                }.show()
        }

    }
}
