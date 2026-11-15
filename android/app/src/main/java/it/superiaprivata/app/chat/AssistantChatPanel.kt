package it.superiaprivata.app.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class ChatLine(val id: Long, val text: String)

@Composable
fun AssistantChatPanel() {
    var draft by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatLine>() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Parla con SuperIA", style = MaterialTheme.typography.titleLarge)
            Text(
                "Scrivi una richiesta con parole tue. In seguito potrai anche allegare file, immagini, video e link e usare la voce.",
                style = MaterialTheme.typography.bodyMedium
            )
            if (messages.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { line ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Tu", style = MaterialTheme.typography.labelMedium)
                                Text(line.text, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Scrivi cosa vuoi che faccia…") },
                placeholder = { Text("Es. Riassumi questo documento") },
                minLines = 2,
                maxLines = 5
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    enabled = draft.isNotBlank(),
                    onClick = {
                        val request = draft.trim()
                        if (request.isNotEmpty()) {
                            messages.add(ChatLine(System.nanoTime(), request))
                            draft = ""
                        }
                    }
                ) { Text("Invia") }
            }
            Text(
                "Anteprima dell'interfaccia: il motore AI non è ancora collegato, quindi l'invio mostra la richiesta ma non la esegue. Cronologia temporanea, non ancora salvata.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
