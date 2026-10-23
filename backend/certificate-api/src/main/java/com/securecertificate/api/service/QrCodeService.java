package com.securecertificate.api.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class QrCodeService {

    public byte[] generateQrCode(
            String content,
            int width,
            int height) {

        try {

            BitMatrix matrix = new MultiFormatWriter().encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    width,
                    height,
                    Map.of(
                            EncodeHintType.MARGIN,
                            1));

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(
                    matrix,
                    "PNG",
                    output);

            return output.toByteArray();

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to generate QR code",
                    exception);
        }
    }
}