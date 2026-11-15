package it.superiaprivata.app

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import it.superiaprivata.app.device.DeviceProfileReader
import it.superiaprivata.app.pairing.DeviceIdentityStore
import it.superiaprivata.app.pairing.PairingInvite
import it.superiaprivata.app.chat.AssistantChatPanel
import it.superiaprivata.app.documents.DocumentsPanel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val profileReader = DeviceProfileReader(this)
        setContent {
            MaterialTheme {
                var profile by remember { mutableStateOf(profileReader.read()) }
                var pairingQr by remember { mutableStateOf<Bitmap?>(null) }
                var pairingError by remember { mutableStateOf<String?>(null) }
                var documentContext by remember { mutableStateOf<String?>(null) }
                val identityStore = remember { DeviceIdentityStore() }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text("SuperIA Privata", style = MaterialTheme.typography.headlineMedium)
                            Text("La tua piattaforma AI personale · Android")
                        }
                        item { DocumentsPanel(onContextChanged = { documentContext = it }) }
                        item { AssistantChatPanel(documentContext = documentContext) }
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
                                    Text("Collegamento tra telefoni", style = MaterialTheme.typography.titleLarge)
                                    Text("Genera un invito QR temporaneo da mostrare all'altro telefono.")
                                    Button(onClick = {
                                        runCatching {
                                            val invite = PairingInvite.create(
                                                deviceLabel = android.os.Build.MODEL ?: "Dispositivo Android",
                                                identityStore = identityStore
                                            )
                                            val hints = mapOf(
                                                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                                                EncodeHintType.MARGIN to 2
                                            )
                                            val matrix = QRCodeWriter().encode(
                                                invite.toQrPayload(),
                                                BarcodeFormat.QR_CODE,
                                                640,
                                                640,
                                                hints
                                            )
                                            Bitmap.createBitmap(640, 640, Bitmap.Config.ARGB_8888).also { bitmap ->
                                                for (x in 0 until 640) {
                                                    for (y in 0 until 640) {
                                                        bitmap.setPixel(
                                                            x, y,
                                                            if (matrix[x, y]) android.graphics.Color.BLACK
                                                            else android.graphics.Color.WHITE
                                                        )
                                                    }
                                                }
                                            }.also {
                                                pairingQr = it
                                                pairingError = null
                                            }
                                        }.onFailure {
                                            pairingQr = null
                                            pairingError = it.message ?: "Impossibile creare l'invito"
                                        }
                                    }) { Text("Genera codice QR") }
                                    pairingQr?.let { qr ->
                                        Image(
                                            bitmap = qr.asImageBitmap(),
                                            contentDescription = "Invito QR per collegare un dispositivo",
                                            modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp)
                                        )
                                        Text("Invito valido per 5 minuti.")
                                        Text(
                                            "Il QR identifica il dispositivo ma non concede fiducia né avvia ancora una connessione.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    pairingError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                        item {
                            ElevatedCard {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Moduli", style = MaterialTheme.typography.titleLarge)
                                    Text("Assistente AI · Chat locale LiteRT-LM (richiede un modello installato)")
                                    Text("Documenti · Importazione ed estrazione locale PDF/DOCX/TXT/CSV")
                                    Text("Immagini e video · In preparazione")
                                    Text("Audio e voce · In preparazione")
                                    Text("GitHub e coding · In preparazione")
                                    Text("Collegamento tra telefoni · Invito QR iniziale disponibile")
                                }
                            }
                        }
                        item {
                            Text(
                                "La chat AI locale richiede l'importazione di un modello compatibile e l'accettazione della relativa licenza. Il QR è ancora un invito dimostrativo: non crea una connessione tra telefoni.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
