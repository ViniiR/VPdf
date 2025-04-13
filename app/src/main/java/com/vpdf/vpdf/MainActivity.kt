package com.vpdf.vpdf

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import androidx.compose.runtime.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.vpdf.vpdf.ui.theme.VpdfTheme
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VpdfTheme {
                Scaffold(modifier = Modifier) { innerPadding ->
                    Home(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .background(Color.White),
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Home(modifier: Modifier = Modifier) {
    val buttonWidth = 500.dp
    val context = LocalContext.current

    var selectedAmount by remember { mutableIntStateOf(0) }
    var fileUriList by remember { mutableStateOf(emptyList<Uri>()) }

    var pdfName = "${LocalDateTime.now()}"
    var pdfFile: ByteArray? by remember { mutableStateOf(null) }
    var enableSavePdf by remember { mutableStateOf(true) }
    var savedPdfUri by remember { mutableStateOf(Uri.EMPTY) }

    val filePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
            selectedAmount = uris.size
            if (uris.isNotEmpty()) {
                fileUriList = uris
            }
        }

    fun openPrinterApp(context: Context, fileUri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val shareIntent = Intent.createChooser(intent, null)

        context.startActivity(shareIntent, null)
    }


    Column (
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (selectedAmount == 1) { "1 Arquivo selecionado" }
            else if (selectedAmount > 1) { "$selectedAmount Arquivos selecionados" }
            else { "nenhum Arquivo selecionado" },
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedAmount > 0) {
                    Color.Red
                } else {
                    Color.Blue
                },
            ),
            modifier = Modifier
                .padding(20.dp)
                .width(buttonWidth),
            onClick = {
                // launches file picker allowing multiple files of any kind to be selected
                enableSavePdf = true
                pdfFile = null
                pdfName = "${LocalDateTime.now()}"
                filePickerLauncher.launch("*/*")
            }
        ) {
            Text(
                text = if (selectedAmount == 1) {
                    "Trocar Arquivo"
                } else if (selectedAmount > 1){
                    "Trocar Arquivos"
                } else {
                    "Selecionar Arquivos"
                },
                color = Color.White,
                fontSize = 26.sp
            )
        }
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Blue,
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.LightGray,
            ),
            modifier = Modifier
                .padding(20.dp)
                .width(buttonWidth),
            enabled = pdfFile == null && fileUriList.isNotEmpty(),
            onClick = {
                // TODO notify user
                pdfFile = makePdfFile(
                    context = context,
                    uriList = fileUriList,
                )
            }
        ) {
            Text(
                text = "Converter para PDF",
                color = Color.White,
                fontSize = 26.sp
            )

        }
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Blue,
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.LightGray,
            ),
            modifier = Modifier
                .padding(20.dp)
                .width(buttonWidth),
            enabled = pdfFile != null && enableSavePdf,
            onClick = {
                // TODO notify user
                if (pdfFile != null) {
                    enableSavePdf = false
                    savedPdfUri = savePdfFile(context = context, byteArray = pdfFile!!, fileName = pdfName)
                }
            }
        ) {
            Text(
                text = "Salvar PDF",
                color = Color.White,
                fontSize = 26.sp
            )
        }
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Blue,
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.LightGray,
            ),
            modifier = Modifier
                .padding(20.dp)
                .width(buttonWidth),
            enabled = pdfFile != null && savedPdfUri != Uri.EMPTY,
            onClick = {
                // TODO pop up request to confirm not to save PDFs before print
                if ( pdfFile != null) {
                    openPrinterApp(context = context, fileUri = savedPdfUri)
                }
            }
        ) {
            Text(
                text = "Imprimir",
                color = Color.White,
                fontSize = 26.sp
            )
        }
    }
}

private fun makePdfFile(context: Context, uriList: List<Uri>): ByteArray {
    val pdfDocument = PdfDocument()

    uriList.forEachIndexed {index, uri ->
        val mimeType = context.contentResolver.getType(uri) ?: ""

        val bitmap = when {
            mimeType.startsWith("image/") -> {
                val stream = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(stream)
                stream?.close()
                bmp
            }
            else -> {
                // render text
                TODO()
            }
        }

        // create one PDF page for each file
        bitmap?.let {
            val pageInfo = PdfDocument.PageInfo.Builder(it.width, it.height, index + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            page.canvas.drawBitmap(it, 0f, 0f, null)
            pdfDocument.finishPage(page)
        }
    }

    val outputStream = ByteArrayOutputStream()
    pdfDocument.writeTo(outputStream)
    pdfDocument.close()

    return outputStream.toByteArray()
}

private fun savePdfFile(context: Context, byteArray: ByteArray, fileName: String): Uri {
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
        put(MediaStore.MediaColumns.IS_PENDING, 1)
    }
    val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        ?: throw IOException("Failed to create MediaStore entry")
    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
        outputStream.write(byteArray)
    } ?: throw IOException("Failed to open OutputStream")

    contentValues.clear()

    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
    context.contentResolver.update(uri, contentValues, null, null)
    
    return uri
}

private fun printFile(context: Context, file: File) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
    val jobName = file.name

    printManager.print(jobName, object : PrintDocumentAdapter() {
        override fun onLayout(
            oldAttributes: PrintAttributes,
            newAttributes: PrintAttributes,
            cancellationSignal: CancellationSignal,
            callback: LayoutResultCallback,
            extras: Bundle?
        ) {
            if (cancellationSignal.isCanceled) {
                callback.onLayoutCancelled()
                return
            }
            val builder = PrintDocumentInfo.Builder(file.name)
            builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)

            callback.onLayoutFinished(builder.build(), true)
        }
        override fun onWrite(
            pages: Array<PageRange>,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal,
            callback: WriteResultCallback
        ) {
            try {
                file.inputStream().use { input ->
                    FileOutputStream(destination.fileDescriptor).use { output ->
                        input.copyTo(output)
                    }
                }
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            } catch (e: Exception) {
                callback.onWriteFailed(e.message)
            }
        }
    },null)
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomePreview() {
    VpdfTheme {
        Scaffold(modifier = Modifier) { innerPadding ->
            Home(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color.White),
            )
        }
    }
}