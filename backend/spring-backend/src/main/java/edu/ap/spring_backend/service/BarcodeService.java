package edu.ap.spring_backend.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Service for generating barcode images.
 * Uses the ZXing library to generate Code 128 barcodes as PNG.
 */
@Service
public class BarcodeService {

    private static final int BARCODE_WIDTH = 800;
    private static final int BARCODE_HEIGHT = 200;

    /**
     * Generates a PNG image of a Code 128 barcode.
     *
     * @param barcodeValue the string to encode (e.g. "COPY-000071")
     * @return the PNG image as a byte array
     * @throws IllegalArgumentException if the barcode value cannot be encoded
     * @throws RuntimeException if the image cannot be created
     */
    public byte[] generateBarcodePng(String barcodeValue) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(barcodeValue, BarcodeFormat.CODE_128, BARCODE_WIDTH, BARCODE_HEIGHT);
            BufferedImage barcodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(barcodeImage, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (WriterException e) {
            throw new IllegalArgumentException("Invalid barcode value: " + barcodeValue, e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create barcode image for: " + barcodeValue, e);
        }
    }
}
