package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MyApplicationTheme { NagarStudioMobile() } }
    }
}

@Composable
private fun NagarStudioMobile() {
    var serverUrl by remember { mutableStateOf("http://10.0.2.2:3000") }
    var text by remember { mutableStateOf("") }
    var voice by remember { mutableStateOf("Kore") }
    var emotion by remember { mutableStateOf("natural") }
    var status by remember { mutableStateOf("Not connected") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Nagar Studio Mobile", style = MaterialTheme.typography.headlineSmall)
            Text("Text-to-Audio companion for the Nagar Studio production engine.")

            OutlinedTextField(serverUrl, { serverUrl = it.trimEnd('/') }, label = { Text("Nagar Studio server") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(enabled = !busy, onClick = {
                    scope.launch {
                        status = "Checking server…"
                        status = if (NagarStudioApi(serverUrl).health()) "Connected" else "Server unavailable"
                    }
                }) { Text("Test") }
                Text(status, modifier = Modifier.padding(top = 10.dp))
            }

            OutlinedTextField(voice, { voice = it }, label = { Text("Voice ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(emotion, { emotion = it }, label = { Text("Style / emotion") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(text, { text = it }, label = { Text("Hindi / Hinglish script") }, modifier = Modifier.fillMaxWidth().weight(1f), minLines = 8)

            Button(
                enabled = text.isNotBlank() && !busy,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scope.launch {
                        busy = true
                        status = "Generating…"
                        try {
                            val result = NagarStudioApi(serverUrl).generate(text, voice, emotion)
                            status = "Generated " + "%.2f".format(result.durationSeconds) + "s • " + result.sampleRate + " Hz"
                        } catch (e: Exception) {
                            status = e.message ?: "Generation failed"
                        } finally { busy = false }
                    }
                }
            ) { Text(if (busy) "Working…" else "Generate Audio") }

            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "The phone is a client. Voice generation remains in Nagar Studio, so the same voice, pronunciation and project rules are shared across desktop and mobile.",
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}
