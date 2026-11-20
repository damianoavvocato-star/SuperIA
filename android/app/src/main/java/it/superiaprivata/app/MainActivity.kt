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
import androidx.compose.runtime.saveable.rememberSaveable
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
import it.superiaprivata.app.chat.CrashLogStore
import it.superiaprivata.app.documents.DocumentsPanel
import it.superiaprivata.app.multimedia.RenderforestPanel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashLogStore.install(applicationContext)
        val profileReader = DeviceProfileReader(this)
        setContent {
            MaterialTheme {
                var profile by remember { mutableStateOf(profileReader.read()) }
                var pairingQr by remember { mutableStateOf<Bitmap?>(null) }
                var pairingError by remember { mutableStateOf<String?>(null) }
                var documentContext by remember { mutableStateOf<String?>(null) }
                var showMore by rememberSaveable { mutableStateOf(false) }
                val identityStore = remember { DeviceIdentityStore() }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (showMore) "SuperIA · Funzioni" else "SuperIA",
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 1
                                )
                                Text(
                                    if (showMore) "Gestione documenti, dispositivo e strumenti"
                                    else "La tua intelligenza artificiale privata",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            OutlinedButton(onClick = { showMore = !showMore }) {
                                Text(if (showMore) "Torna alla chat" else "Menu")
                            }
                        }

                        if (!showMore) {
                            AssistantChatPanel(
                                documentContext = documentContext,
                                modifier = Modifier.weight(1f).padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    DocumentsPanel(onContextChanged = { documentContext = it })
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
                                            Text("La RAM virtuale non viene conteggiata nei dati RAM fisici.")
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
                                    RenderforestPanel()
                                }
                                item {
                                    ElevatedCard {
                                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("Moduli", style = MaterialTheme.typography.titleLarge)
                                            Text("Assistente AI · Chat online tramite backend HTTPS gratuito")
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
                                        "La chat AI usa il backend online configurato nelle Impostazioni. La cronologia resta cifrata sul dispositivo; il QR è ancora un invito dimostrativo e non crea una connessione tra telefoni.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
