package com.example.all_in

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.sign), contentDescription = "Login Image", modifier = Modifier.size(200.dp))
        Text(text = "Hey Again", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("login to your account")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), // Set ImeAction to Done
            keyboardActions = KeyboardActions(
                //onDone = { focusManager.clearFocus() } // Clear focus when Done is pressed
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), // Set ImeAction to Done
            keyboardActions = KeyboardActions(
                //onDone = { focusManager.clearFocus() } // Clear focus when Done is pressed
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(modifier = Modifier.fillMaxWidth().height(50.dp),
            onClick = { viewModel.login(email, password) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)

        ) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Don't have an account? Sign Up", modifier = Modifier.clickable(onClick = {
            onNavigateToRegister()
        }))
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}
