package mx.tec.authandextrasdemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.facebook.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var auth: FirebaseAuth
    lateinit var callbackManager: CallbackManager
    val RC_SIGN_IN: Int = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContentView(R.layout.activity_main)
        loginButton.setOnClickListener(this)
        registerButton.setOnClickListener(this)
        googleLoginButton.setOnClickListener(this)
        callbackManager = CallbackManager.Factory.create()
        facebookLoginButton.setOnClickListener{
            LoginManager.getInstance().logInWithReadPermissions(this,listOf("public_profile","email"))
        }
        try {
            LoginManager.getInstance()
                .registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
                    override fun onSuccess(result: LoginResult?) {
                        Log.d("Facebook Login", "Succesfully logged in")
                        handleFacebookAccessToken(result!!.accessToken)
                    }

                    override fun onCancel() {
                        Toast.makeText(this@MainActivity, "Login cancelled", Toast.LENGTH_SHORT)
                            .show()
                    }

                    override fun onError(error: FacebookException) {
                        Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_SHORT).show()
                    }
                })
        }catch(e: Exception){
            Log.d("Facebook login","Error setting callback")
        }
    }

    override fun onStart() {
        super.onStart()
        //auto login user
        /*val currentUser = auth.currentUser
        updateUI(currentUser)*/
    }
    override fun onClick(p0: View?) {
        when(p0){
            loginButton->{
                auth.signInWithEmailAndPassword(userEditText.text.toString(), passwordEditText.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Login in success, update UI with the signed-in user's information
                            Log.d("login", "loginUserWithEmail:success")
                            val user = auth.currentUser
                            updateUI(user)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("login", "loginUserWithEmail:failure", task.exception)
                            Toast.makeText(baseContext, "Login failed.",
                                Toast.LENGTH_SHORT).show()
                            updateUI(null)
                        }
                    }
            }
            registerButton->{
                Log.d("registerButton","clicked")
                auth.createUserWithEmailAndPassword(userEditText.text.toString(),passwordEditText.text.toString())
                    .addOnCompleteListener(this){ task->
                        if(task.isSuccessful){
                            Log.d("register","createUserWithEmail:success")
                            val user = auth.currentUser
                            updateUI(user)
                        } else {
                            Log.w("register","createUserwithEmail:faied")
                            Toast.makeText(baseContext,"Register failed",Toast.LENGTH_SHORT).show()
                            updateUI(null)
                        }
                    }
            }
            googleLoginButton->{
                Log.d("Google Login","button pressed")
                val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
                val signInClient = GoogleSignIn.getClient(this,signInOptions)
                val loginIntent: Intent = signInClient.signInIntent
                startActivityForResult(loginIntent,RC_SIGN_IN)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //pass the activity result back to the facebook sdk
        callbackManager.onActivityResult(requestCode,resultCode,data)
        if(requestCode == RC_SIGN_IN){
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val account =  task.getResult(ApiException::class.java)
                if(account!=null){
                    // Google Sign In was successful, authenticate with Firebase
                    firebaseAuthWithGoogle(account.idToken!!)
                    Toast.makeText(baseContext,"Google account ${account.email} successfully signed in",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(baseContext,"Error in google sign in",Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception){
                Toast.makeText(baseContext,e.message,Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("Facebook Login", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Facebook Login", "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Facebook Login", "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }

                // ...
            }
    }
    private fun updateUI(user: FirebaseUser?) {
        if(user!=null)
            startActivity(Intent(this,MainScreen::class.java))
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Google Login", "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Google Login", "signInWithCredential:failure", task.exception)
                    // ...
                    Toast.makeText(baseContext, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }

                // ...
            }
    }
}