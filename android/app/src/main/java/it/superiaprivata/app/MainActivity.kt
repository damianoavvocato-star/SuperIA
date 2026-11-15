package it.superiaprivata.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.superiaprivata.app.device.DeviceProfile
import it.superiaprivata.app.device.DeviceProfileReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val profileReader = DeviceProfileReader(this)
        setContent {
            MaterialTheme {
                var profile by remember { mutableStateOf(profileReader.read()) }
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text("SuperIA Privata", style = MaterialTheme.typography.headlineMedium)
                            Text("La tua piattaforma AI personale · Android")
                        }
                        item {
                            ElevatedCard {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Dispositivo in uso", style = MaterialTheme.typography.titleLarge)
                                    Text(profile.modelName, style = MaterialTheme.typography.titleMedium)
                                    Text("Produttore: ${profile.manufacturer}")
                                    Text("Android: ${profile.androidVersion} (API ${profile.apiLevel})")
                                    Text("RAM totale: ${profile.totalRamMb} MB")
                                    Text("RAM disponibile: ${profile.availableRamMb} MB")
                                    Text("Architetture CPU: ${profile.supportedAbis.joinToString()}")
                                    Text("Memoria interna libera: ${profile.freeStorageGb} GB")
                                    Text("Stato termico: ${profile.thermalStatus}")
                                    Text("Nota: i dati RAM di sistema sono rilevati tramite Android; l'espansione RAM virtuale non viene sommata.")
                                    OutlinedButton(onClick = { profile = profileReader.read() }) {
                                        Text("Aggiorna rilevamento")
                                    }
                                }
                            }
                        }
                        item {
                            ElevatedCard {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Moduli", style = MaterialTheme.typography.titleLarge)
                                    Text("Assistente AI · In preparazione")
                                    Text("Documenti · In preparazione")
                                    Text("Immagini e video · In preparazione")
                                    Text("Audio e voce · In preparazione")
                                    Text("GitHub e coding · In preparazione")
                                    Text("Collegamento tra telefoni · In preparazione")
                                }
                            }
                        }
                        item {
                            Text(
                                "Questa è una prima base tecnica. Non esegue ancora modelli AI e non stabilisce automaticamente quale telefono sia più veloce.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
