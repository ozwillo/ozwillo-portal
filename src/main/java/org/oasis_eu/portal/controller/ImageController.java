package org.oasis_eu.portal.controller;

import org.oasis_eu.portal.model.images.Image;
import org.oasis_eu.portal.services.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RestController
public class ImageController {

    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * serves image
     */
    @GetMapping("/media/{id}/{name}")
    public void getIcon(@PathVariable String id, @RequestHeader(required = false, value = "If-None-Match") String hash, HttpServletResponse response) throws IOException {
        if (hash != null) {
            // we have an etag!
            String storedHash = imageService.getHash(id);
            if (storedHash != null && storedHash.equals(hash)) {
                response.setStatus(HttpStatus.NOT_MODIFIED.value());
                response.setHeader("Cache-Control", "public, max-age=31536000");
            } else {
                writeOutput(response, getIconBody(id));
            }
        } else {
            writeOutput(response, getIconBody(id));
        }
    }

    private void writeOutput(HttpServletResponse response, ResponseEntity<byte[]> entity) throws IOException {
        entity.getHeaders().forEach((key, values) -> values.forEach(value -> {
            if (value != null) response.setHeader(key, value);
        }));
        ServletOutputStream outputStream = response.getOutputStream();
        StreamUtils.copy(new ByteArrayInputStream(entity.getBody()), outputStream);
        outputStream.close();
    }

    private ResponseEntity<byte[]> getIconBody(String id) {
        return imageService.getImage(id)
                .map(this::toIconBodyResponse)
                .orElseThrow(IconNotFound::new);
    }

    private ResponseEntity<byte[]> toIconBodyResponse(Image image) {
        HttpHeaders headers = new HttpHeaders();
        headers.setETag(image.getHash());
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(image.getBytes().length);
        headers.setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());
        return new ResponseEntity<>(image.getBytes(), headers, HttpStatus.OK);
    }

    @ExceptionHandler(IconNotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFound() {}

    @ExceptionHandler(EmptyUploadException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void emptyUpload() {}


    static class IconNotFound extends RuntimeException {

        private static final long serialVersionUID = 9096153080333575467L;
    }

    static class UploadException extends RuntimeException {
        private static final long serialVersionUID = -2019828404927362881L;

        public UploadException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    static class EmptyUploadException extends UploadException {
        private static final long serialVersionUID = 7550351339973155116L;

        public EmptyUploadException() {
            super("Uploaded file is empty", null);
        }
    }

    /**
     * @param objectId whose icon we're POSTing
     * @param iconFile has also filename, size etc.
     */
    @PostMapping(value = "/media/" + ImageService.OBJECTICONIMAGE_PATHELEMENT + "/{objectId}")
    @ResponseBody
    public String handleFileUpload(@PathVariable String objectId, @RequestParam("iconFile") MultipartFile iconFile) {
        if (iconFile.isEmpty())
            throw new EmptyUploadException();

        try {
            byte[] bytes = iconFile.getBytes();
            Image imageToStore = new Image();
            imageToStore.setBytes(bytes);
            imageToStore.setFilename(iconFile.getOriginalFilename());
            imageToStore = imageService.storeImageForObjectId(objectId, imageToStore);
            return imageService.buildImageServedUrl(imageToStore); // ex. http://localhost:8080/media/$id/icon.png
        } catch (IOException ex) {
            throw new UploadException("IO exception while getting uploaded content of file "
                + iconFile.getOriginalFilename(), ex); // TODO problem with upload
        }
    }
}
