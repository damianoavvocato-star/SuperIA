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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun AssistantChatPanel() {
    val context = LocalContext.current
    val historyStore = remember(context) { ChatHistoryStore(context) }
    val initialHistory = remember(historyStore) { runCatching { historyStore.load() } }
    val messages = remember(initialHistory) {
        mutableStateListOf<ChatLine>().apply {
            addAll(initialHistory.getOrDefault(emptyList()))
        }
    }
    var draft by remember { mutableStateOf("") }
    var storageError by remember { mutableStateOf(initialHistory.exceptionOrNull()?.message) }

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
                OutlinedButton(
                    onClick = {
                        runCatching { historyStore.clear() }
                            .onSuccess {
                                messages.clear()
                                storageError = null
                            }
                            .onFailure { storageError = it.message ?: "Errore nella cancellazione" }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancella cronologia") }
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
                            runCatching { historyStore.save(messages.toList()) }
                                .onSuccess { storageError = null }
                                .onFailure { storageError = it.message ?: "Errore nel salvataggio" }
                        }
                    }
                ) { Text("Invia") }
            }
            storageError?.let {
                Text(
                    "Problema con la cronologia protetta: $it",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                "Cronologia locale cifrata con Android Keystore. L'assistente AI non è ancora collegato: l'invio salva la richiesta ma non la esegue.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
