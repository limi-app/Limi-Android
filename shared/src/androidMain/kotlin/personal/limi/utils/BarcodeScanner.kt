package personal.limi.utils

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.tasks.await

suspend fun startPlayBarcodeScanner(context: Context): List<String> {
    val options = GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .enableAutoZoom().build()
    val scanner = GmsBarcodeScanning.getClient(context, options)
    val result = mutableListOf<String>()
    val barcode = scanner.startScan().await()
    val rawValue = barcode.rawValue
    if (rawValue != null) result.add(rawValue)
    return if (result.isEmpty()) emptyList() else result.toList()
}

suspend fun scanQRCodeFromImageUri(context: Context, imageUri: Uri): List<String> {
    val options = BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
    val scanner = BarcodeScanning.getClient(options)
    val image = InputImage.fromFilePath(context, imageUri)
    val result = mutableListOf<String>()
    val barcodes = scanner.process(image).await()
    barcodes.forEach { barcode ->
        val rawValue = barcode.rawValue
        if (rawValue != null) result.add(rawValue)
    }
    return if (result.isEmpty()) emptyList() else result.toList()
}