package nl.openschoolcloud.calendar.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Generates QR code bitmaps from URLs.
 * Used for booking links so teachers can share with parents
 * (e.g., at parent-teacher conferences / ouderavonden).
 */
object QrCodeGenerator {

    /**
     * Generate a QR code bitmap for the given content.
     *
     * @param content The text/URL to encode
     * @param size Width and height of the QR code in pixels
     * @return Bitmap or null if generation fails
     */
    fun generate(content: String, size: Int = 512): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.MARGIN to 1,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            val bitMatrix = QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )

            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(
                        x, y,
                        if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                    )
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
