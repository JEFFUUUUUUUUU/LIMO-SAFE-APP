package limosafe.com

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import limosafe.com.ui.theme.LIMOSAFETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LIMOSAFETheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().padding(innerPadding)
                    ) {
                        MainContent()
                    }
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Greeting(name = "LIMO SAFE")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val intent = Intent(context, UsernamePasswordActivity::class.java)
            context.startActivity(intent)
        }) {
            Text(text = "Enter")
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "$name",
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
}

class UsernamePasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LIMOSAFETheme {
                UsernamePasswordScreen()
            }
        }
    }
}

@Composable
fun UsernamePasswordScreen() {
    val context = LocalContext.current
    var username by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showExitDialog by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Log In",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        )

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            isError = username.text.isEmpty()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            isError = password.text.isEmpty()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                val isUsernameValid = username.text == "Admin" // Default username (**NOT SECURE**)
                val isPasswordValid = password.text == "AdM1n123" // Default password (**NOT SECURE**)

                when {
                    !isUsernameValid && !isPasswordValid -> {
                        errorMessage = "Both Username and Password are Incorrect, Please Try Again"
                        showErrorDialog = true
                    }
                    !isUsernameValid -> {
                        errorMessage = "Username Incorrect, Please Try again"
                        showErrorDialog = true
                    }
                    !isPasswordValid -> {
                        errorMessage = "Password Incorrect, Please Try again"
                        showErrorDialog = true
                    }
                    else -> {
                        val intent = Intent(context, LogsPanelActivity::class.java)
                        context.startActivity(intent)
                    }
                }
            }) {
                Text(text = "Log In")
            }

            Button(onClick = {
                val intent = Intent(context, RegisterActivity::class.java)
                context.startActivity(intent)
            }) {
                Text(text = "Register")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { showExitDialog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Exit")
        }

        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                confirmButton = {
                    Button(onClick = { showErrorDialog = false }) {
                        Text("OK")
                    }
                },
                title = { Text("Error") },
                text = { Text(errorMessage) }
            )
        }

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Exit App") },
                text = { Text("Do you really want to exit LIMO SAFE APP?") },
                confirmButton = {
                    Button(onClick = {
                        showExitDialog = false
                        (context as? ComponentActivity)?.finishAffinity()
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    Button(onClick = { showExitDialog = false }) {
                        Text("No")
                    }
                }
            )
        }
    }
}

class LogsPanelActivity : ComponentActivity() {
    private val sharedPreferences by lazy {
        getSharedPreferences(
            "app_prefs",
            Context.MODE_PRIVATE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionDenied = sharedPreferences.getBoolean("permission_denied", false)

        if (!permissionDenied) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            } else {
                initializeFlashlightControl()
            }
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun initializeFlashlightControl() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]

        setContent {
            LIMOSAFETheme {
                LogsPanelScreen { flashes ->
                    controlFlashlight(cameraManager, cameraId, flashes)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeFlashlightControl()
                sharedPreferences.edit().putBoolean("permission_denied", false).apply()
            } else {
                if (permissionRequestCount < MAX_ATTEMPTS) {
                    Toast.makeText(
                        this,
                        "Camera permission denied. Flashlight functionality will be unavailable. Please grant permission from Settings.",
                        Toast.LENGTH_SHORT
                    ).show()
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_CODE
                    )
                    permissionRequestCount++
                } else {
                    Toast.makeText(
                        this,
                        "Permission request limit reached. Flashlight functionality will be unavailable.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val MAX_ATTEMPTS = 10
        private var permissionRequestCount = 5
    }
 @Composable
    fun LogsPanelScreen(onFlashControl: (Int) -> Unit) {
        val context = LocalContext.current
        var otp by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LIMO SAFE APP",
                    fontSize = 30.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Logs",
                    fontSize = 24.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(200.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Logs Table (Placeholder)")
                }
            }

            Spacer(Modifier.height(32.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BasicTextField(
                    value = otp,
                    onValueChange = { otp = it },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(8.dp)
                        .background(Color.Gray)
                        .padding(16.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            if (otp.isNotEmpty()) {
                                val flashes = otp.toIntOrNull() ?: 0
                                onFlashControl(flashes)
                            }
                        }
                    ) {
                        Text(text = "Request OTP")
                    }

                    Button(
                        onClick = {
                            // Handle "Enter OTP" button click
                        }
                    ) {
                        Text(text = "Enter OTP")
                    }
                }
                val context = LocalContext.current
                Button(
                    onClick = {
                        (context as? LogsPanelActivity)?.finish()
                    },
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .padding(vertical = 16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "Back")
                }
            }
        }
    }

    fun controlFlashlight(cameraManager: CameraManager, cameraId: String, flashes: Int) {
        try {
            for (i in 1..flashes) {
                cameraManager.setTorchMode(cameraId, true)
                Thread.sleep(300)
                cameraManager.setTorchMode(cameraId, false)
                Thread.sleep(300)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
}

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegisterScreen()
        }
    }
}

@Composable
fun RegisterScreen() {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Register",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
            },
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .padding(vertical = 16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Register")
        }
        val context = LocalContext.current
        Button(
            onClick = {
                (context as? RegisterActivity)?.finish()
            },
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .padding(vertical = 16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Back")
        }
        }
    }

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    LIMOSAFETheme {
        MainContent()
    }
}

@Preview(showBackground = true)
@Composable
fun UsernamePasswordScreenPreview() {
    LIMOSAFETheme {
        UsernamePasswordScreen()
    }
}