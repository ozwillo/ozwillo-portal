package org.oasis_eu.portal.front.icon;

import com.google.common.io.ByteStreams;
import org.oasis_eu.portal.core.mongo.model.images.Image;
import org.oasis_eu.portal.core.services.icons.ImageService;
import org.oasis_eu.portal.front.generic.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * User: schambon
 * Date: 9/2/14
 */
@Controller
@RequestMapping(method = RequestMethod.GET, value = "/media")
public class ImageController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    @Autowired
    private ImageService imageService;

    /**
     * serves image
     *
     * @param id
     * @param hash
     * @param response
     * @throws IOException
     */
    @RequestMapping("/{id}/{name}")
    public void getIcon(@PathVariable String id, @RequestHeader(required = false, value = "If-None-Match") String hash, HttpServletResponse response) throws IOException {
        if (hash != null) {
            // we have an etag!
            String storedHash = imageService.getHash(id);
            if (storedHash != null && storedHash.equals(hash)) {
                response.setStatus(HttpStatus.NOT_MODIFIED.value());
                response.setHeader("Cache-Control", "public, max-age=31536000");
            } else {
                ResponseEntity<byte[]> entity = getIconBody(id);
                writeOutput(response, entity);
            }
        } else {
            writeOutput(response, getIconBody(id));
        }
    }


    private void writeOutput(HttpServletResponse response, ResponseEntity<byte[]> entity) throws IOException {
        for (String header : entity.getHeaders().keySet()) {
            for (String val : entity.getHeaders().get(header)) {
                logger.debug("Setting header {}: {}", header, val);
                response.setHeader(header, val);
            }
        }
        ServletOutputStream outputStream = response.getOutputStream();
        ByteStreams.copy(new ByteArrayInputStream(entity.getBody()), outputStream);
        outputStream.close();
    }

    private ResponseEntity<byte[]> getIconBody(String id) {
        Image image = imageService.getImage(id);
        return toIconBodyResponse(image);
    }

    private ResponseEntity<byte[]> toIconBodyResponse(Image image) {
        if (image != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("ETag", image.getHash());
            headers.add("Content-Type", "image/png");
            headers.add("Content-Length", Integer.toString(image.getBytes().length));
            headers.put("Cache-Control", Arrays.asList("public, max-age=31536000")); // one year
            ResponseEntity<byte[]> res = new ResponseEntity<>(image.getBytes(), headers, HttpStatus.OK);
            return res;
        } else {
            throw new IconNotFound();
        }
    }

    @ExceptionHandler(IconNotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFound() {

    }

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
     * @param filename (Optional) overrides body file's name. Set it to "icon.png" if the
     *                 image served url is not stored in the business object to that the image url
     *                 can be computed from imageService.buildObjectIconImageVirtualUrlOrNullIfNone()
     * @param iconFile has also filename, size etc.
     * @return
     */
    @RequestMapping(value = "/" + ImageService.OBJECTICONIMAGE_PATHELEMENT + "/{objectId}", method = RequestMethod.POST)
    public
    @ResponseBody
    String serviceHandleFileUploadWithNoFilename(@PathVariable("objectId") String objectId,
        @RequestParam("iconFile") MultipartFile iconFile) {
        return this.handleFileUpload(objectId, iconFile, null);
    }

    @RequestMapping(value = "/" + ImageService.OBJECTICONIMAGE_PATHELEMENT + "/{objectId}/{filename}", method = RequestMethod.POST)
    public
    @ResponseBody
    String serviceHandleFileUpload(@PathVariable("objectId") String objectId,
        @PathVariable("filename") String filename,
        @RequestParam("iconFile") MultipartFile iconFile) {
        return this.handleFileUpload(objectId, iconFile, filename);
    }

    private String handleFileUpload(String objectId, MultipartFile iconFile, String filename) {
        if (!iconFile.isEmpty()) {
            try {
                byte[] bytes = iconFile.getBytes();
                Image imageToStore = new Image();
                imageToStore.setBytes(bytes);
                if (filename != null && filename.trim().length() != 0) {
                    imageToStore.setFilename(filename); // ex. ImageService.ICONIMAGE_NAME i.e. icon.png
                } else {
                    imageToStore.setFilename(iconFile.getOriginalFilename());
                }
                // LATER OPT also iconFile.getContentType()
                imageToStore = imageService.storeImageForObjectId(objectId, imageToStore);
                return imageService.buildImageServedUrl(imageToStore); // ex. http://localhost:8080/media/$id/icon.png
                // NB. alts :
                //return imageService.buildObjectIconImageVirtualUrl(objectId); // ex. http://localhost:8080/media/objectIcon/$objectId/icon.png
                //return imageToStore.getUrl(); // ex. http://localhost:8080/media/objectIcon/$objectId/icon.png
            } catch (IOException ex) {
                throw new UploadException("IO exception while getting uploaded content of file "
                    + iconFile.getOriginalFilename(), ex); // TODO problem with upload
            }
        } else {
            throw new EmptyUploadException(); // TODO no upload
        }
    }

}
