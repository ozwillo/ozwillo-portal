package org.oasis_eu.portal.front.icon;

import com.google.common.io.ByteStreams;
import org.oasis_eu.portal.core.mongo.model.icons.Icon;
import org.oasis_eu.portal.core.services.icons.IconService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping(method = RequestMethod.GET, value = "/icon")
public class IconController {

    private static final Logger logger = LoggerFactory.getLogger(IconController.class);

    @Autowired
    private IconService iconService;

    @RequestMapping("/{id}/{name}")
    public void getIcon(@PathVariable String id, @RequestHeader(required = false, value = "If-None-Match") String hash, HttpServletResponse response) throws IOException {
        if (hash != null) {
            // we have an etag!
            String storedHash = iconService.getHash(id);
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
        for (String header: entity.getHeaders().keySet()) {
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
        Icon icon = iconService.getIcon(id);
        if (icon != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("ETag", icon.getHash());
            headers.add("Content-Type", "image/png");
            headers.add("Content-Length", Integer.toString(icon.getBytes().length));
            headers.put("Cache-Control", Arrays.asList("public, max-age=31536000")); // one year
            ResponseEntity<byte[]> res = new ResponseEntity<byte[]>(icon.getBytes(), headers, HttpStatus.OK);
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

    }
}
