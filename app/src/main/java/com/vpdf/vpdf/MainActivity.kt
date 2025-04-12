package com.vpdf.vpdf

import androidx.compose.runtime.*
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vpdf.vpdf.ui.theme.VpdfTheme

class MainActivity : ComponentActivity() {
    /*
    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            data?.let {
                val count = when {
                    it.clipData != null -> it.clipData!!.itemCount
                    it.data != null -> 1
                    else -> 0
                }
                selectedAmount = count
            }
        }
    }
    private var selectedAmount by mutableStateOf(0);
    */
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
                        //filePickerLauncher = this.filePickerLauncher,
                        //selectedAmount = selectedAmount
                    )
                }
            }
        }
    }
}

@Composable
fun Home(modifier: Modifier = Modifier/*, filePickerLauncher: ActivityResultLauncher<Intent>?, selectedAmount: Int*/) {
    var selectedAmount by remember { mutableIntStateOf(0) }
    val filePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
            selectedAmount = uris.size
            uris.forEach { uri ->

            }
        }
    Column (
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (selectedAmount == 1) {
                "1 Arquivo selecionado"
            } else if (selectedAmount > 1) {
                "$selectedAmount Arquivos selecionados"
            } else {
                "nenhum Arquivo selecionado"
                   },
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Blue
            ),
            modifier = Modifier.padding(20.dp),
            onClick = {
                /*val intent =Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "**"
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
                }*/
                filePickerLauncher.launch("*/*")
            }
        ) {
            Text(
                text = "Selecionar arquivos",
                color = Color.White,
                fontSize = 26.sp
            )

        }
    }
}

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
                //filePickerLauncher = null,
                //selectedAmount = 0
            )
        }
    }
}