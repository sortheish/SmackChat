package com.example.smackchat.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.smackchat.R
import com.example.smackchat.services.AuthService
import com.example.smackchat.utilities.BROADCAST_USER_DATA_CHANGE
import com.example.smackchat.utilities.EMAIL_PATTERN
import com.example.smackchat.utilities.PASSWORD_PATTERN
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class CreateUserActivity : AppCompatActivity() {
    private var userAvatar = (R.drawable.profiledefault).toString()
    private var avatarColor: String = "[0.5, 0.5 ,0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        createSpinner.visibility = View.INVISIBLE
    }

    fun generateUserAvatarClick(view: View) {
        if(view.id == R.id.createAvatarImageView) {
            val random = Random()
            val color = random.nextInt(2)
            val avatar = random.nextInt(28)

            userAvatar = if (color == 0) {
                "light$avatar"
            } else {
                "dark$avatar"
            }

            val resourceId = resources.getIdentifier(userAvatar, "drawable", packageName)
            createAvatarImageView.setImageResource(resourceId)
        }
    }

    fun generateBackgroundColorClick(view: View) {
        if(view.id == R.id.btnBackgroundColor) {
            val random = Random()
            val r = random.nextInt(255)
            val g = random.nextInt(255)
            val b = random.nextInt(255)

            createAvatarImageView.setBackgroundColor(Color.rgb(r, g, b))

            val saveR = r.toDouble() / 255
            val saveG = g.toDouble() / 255
            val saveB = b.toDouble() / 255

            avatarColor = "[$saveR, $saveG, $saveB, 1]"
        }
    }

    fun createNewUserClick(view: View) {
        if(view.id == R.id.btnCreateUser) {
            enableSpinner(true)
            val userName = createUserNameText.text.toString()
            val email = createEmailText.text.toString()
            val password: String = createPasswordText.text.toString()

            if (userName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                if (Utils.isValidEmail(email)) {
                    if (Utils.isValidPassword(password)) {
                        createUser(userName, email, password)
                        //sendEmail(email)
                    } else {
                        Toast.makeText(
                            this,
                            "Password must contain min one capital letter,one number, one symbol ",
                            Toast.LENGTH_LONG
                        ).show()
                        enableSpinner(false)
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Make sure user email address is in correct format",
                        Toast.LENGTH_LONG
                    ).show()
                    enableSpinner(false)
                }
            } else {
                Toast.makeText(
                    this,
                    "Make sure user name, email, password are filled in",
                    Toast.LENGTH_LONG
                ).show()
                enableSpinner(false)
            }
        }
    }

    private fun createUser(userName: String, email: String, password: String) {
        AuthService.registerUser(email, password) { registerSuccess ->
            if (registerSuccess) {
                AuthService.loginUser(email, password) { loginSuccess ->
                    if (loginSuccess) {
                        AuthService.crateUser(
                            userName,
                            email,
                            userAvatar,
                            avatarColor
                        ) { crateSuccess ->
                            if (crateSuccess) {
                                val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                                LocalBroadcastManager.getInstance(this)
                                    .sendBroadcast(userDataChange)
                                enableSpinner(false)
                                finish()
                            } else {
                                errorToast()
                            }
                        }
                    } else {
                        errorToast()
                    }
                }
            } else {
                errorToast()
            }
        }
    }

    private fun errorToast() {
        Toast.makeText(this, "Something went wrong, please try again", Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    private fun enableSpinner(enable: Boolean) {
        if (enable) {
            createSpinner.visibility = View.VISIBLE
        } else {
            createSpinner.visibility = View.INVISIBLE
        }
        btnCreateUser.isEnabled = !enable
        btnBackgroundColor.isEnabled = !enable
        createAvatarImageView.isEnabled = !enable
    }
    object Utils{
        fun isValidEmail(email_value: String): Boolean {
            val matcher: Matcher = Pattern.compile(EMAIL_PATTERN).matcher(email_value)
            return matcher.matches()
        }

         fun isValidPassword(password: String?): Boolean {
            password?.let {
                val passwordMatcher = Regex(PASSWORD_PATTERN)
                return passwordMatcher.find(password) != null
            } ?: return false
        }
    }
}
