package it.superiaprivata.app.multimedia

import android.content.Intent
import.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Renderforest capability hub. Generation and editing are performed by the
 * official online service; SuperIA does not impersonate or clone its engines.
 */
@Composable
fun RenderforestPanel() {
    val context = LocalContext.current
    val home = "https://www.renderforest.com/it/"
    val video = "https://www.renderforest.com/it/ai-video-generator"
    val tools = "https://www.renderforest.com/it/branding-tools"

    fun open(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Renderforest · Studio creativo AI", style = MaterialTheme.typography.titleLarge)
            Text(
                "Area di accesso alle funzioni Renderforest. Per generare ed esportare contenuti serve una connessione Internet e, secondo lo strumento, un account, crediti o un piano Renderforest.",
                style = MaterialTheme.typography.bodySmall
            )
            Button(onClick = { open(home) }, modifier = Modifier.fillMaxWidth()) {
                Text("Apri Renderforest")
            }
            Text("VIDEO E ANIMAZIONI", style = MaterialTheme.typography.titleMedium)
            Text("• Video da testo, script, immagini e audio")
            Text("• Video brevi e progetti multi-scena con storyboard")
            Text("• Video promozionali, esplicativi, educativi e commerciali")
            Text("• Reels, Shorts, TikTok, intro, outro e animazioni logo")
            Text("• Cartoni, anime, video musicali e visualizer")
            Text("• Avatar parlanti, sincronizzazione labiale e clonazione vocale")
            Text("• Effetti AI, modelli e librerie di scene")
            OutlinedButton(onClick = { open(video) }, modifier = Modifier.fillMaxWidth()) {
                Text("Apri generatore video AI")
            }

            Text("IMMAGINI E GRAFICA", style = MaterialTheme.typography.titleMedium)
            Text("• Generazione immagini da testo e trasformazione immagini")
            Text("• Immagini di prodotto, sfondi, miniature e visual per campagne")
            Text("• Loghi, identità visiva, grafiche social, poster e presentazioni")
            Text("• Mockup per prodotti, confezioni, abbigliamento e materiali promozionali")

            Text("AUDIO E VOCE", style = MaterialTheme.typography.titleMedium)
            Text("• Sintesi vocale e voice-over in oltre 20 lingue")
            Text("• Libreria di voci, cambio voce e strumenti di doppiaggio")
            Text("• Musica di sottofondo e tracce per video")
            Text("• Sottotitoli e narrazione sincronizzata, dove supportati dallo strumento")

            Text("BRAND, SITI E CONTENUTI", style = MaterialTheme.typography.titleMedium)
            Text("• Creazione di loghi e materiali coordinati")
            Text("• Costruttore di siti web AI e modelli web")
            Text("• Presentazioni, slideshow e contenuti aziendali")
            Text("• Idee e nomi per canali YouTube e attività")

            Text("EDITOR E PUBBLICAZIONE", style = MaterialTheme.typography.titleMedium)
            Text("• Montaggio di scene, taglio, riordino e livelli su timeline")
            Text("• Sostituzione o rigenerazione di singole scene")
            Text("• Aggiunta di logo, colori del marchio, testi e sottotitoli")
            Text("• Ridimensionamento per YouTube, TikTok, Reels e Shorts")
            Text("• Anteprima, cronologia delle generazioni, download e condivisione")

            OutlinedButton(onClick = { open(tools) }, modifier = Modifier.fillMaxWidth()) {
                Text("Apri tutti gli strumenti Renderforest")
            }
            Text(
                "Nota: questa sezione raccoglie e organizza gli accessi alle funzioni ufficiali. Non include ancora un collegamento API diretto, né rende disponibili offline i motori Renderforest. Disponibilità, modelli, limiti, prezzi, crediti e watermark dipendono dal servizio e dal piano attivo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
