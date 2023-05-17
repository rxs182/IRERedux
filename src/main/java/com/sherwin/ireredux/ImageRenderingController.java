package com.sherwin.ireredux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.sherwin.ireredux.ImageRenderingConstants.*;


@RestController
@Slf4j
public class ImageRenderingController {
    @GetMapping(value = "**", produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] getImage(@RequestParam Map<String, String> params) throws IOException {
        byte[] payload = new byte[0];

        String baseImageLocation = params.get(BASE_IMAGE_LOCATION_PARAMETER);
        int lastIndex = baseImageLocation.lastIndexOf("/");
        String sourceDir = baseImageLocation.substring( 0, lastIndex );
        String baseFileName = baseImageLocation.substring( lastIndex + 1 );

        File sourceFile = new File( sourceDir + "/" + baseFileName + ".jpg" );

        String responseType = params.get(RESPONSE_TYPE) != null ? params.get(RESPONSE_TYPE) : "";

        int width = params.get(RENDERED_IMAGE_WIDTH) != null ? Integer.parseInt(params.get(RENDERED_IMAGE_WIDTH)) : 0;
        int height = params.get(RENDERED_IMAGE_HEIGHT) != null ? Integer.parseInt(params.get(RENDERED_IMAGE_HEIGHT)) : 0;


        try {
            long imageTime = System.currentTimeMillis();
            BufferedImage baseImage = ImageIO.read( sourceFile );
            if (log.isDebugEnabled()) {
                log.debug("Time to read image: " + (System.currentTimeMillis() - imageTime) + " ms");
            }

            File xmlFile = new File( baseImageLocation + ".xml" );
            Map<String, String> surfaceMaskMap = SourceXmlFileParser.parse(xmlFile);

            // resize if we have at least one dimension
            if (width > 0 || height > 0) {
                int baseImageWidth = baseImage.getWidth();
                int baseImageHeight = baseImage.getHeight();
                baseImage = ImageUtil.resizeImage( baseImage, width, height );
            }
            // do we actually do this? -RS
            // apply surface masking data to the image
            SurfaceMasking.maskSurfaceData( baseImage, surfaceMaskMap, params );

            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            ImageIO.write(baseImage, "jpg", bytesOut);

            payload = bytesOut.toByteArray();

        } catch (Exception e) {
            log.error("Could not get image: " + e.getMessage());
        }

        return payload;
    }
}
