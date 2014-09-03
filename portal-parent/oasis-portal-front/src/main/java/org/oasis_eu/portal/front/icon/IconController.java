package org.oasis_eu.portal.front.icon;

import org.oasis_eu.portal.core.mongo.model.icons.Icon;
import org.oasis_eu.portal.core.services.icons.IconService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * User: schambon
 * Date: 9/2/14
 */
@Controller
@RequestMapping(method = RequestMethod.GET, value = "/icon")
public class IconController {

    @Autowired
    private IconService iconService;

    @RequestMapping("/{id}/{name}")
    public ResponseEntity<byte[]> getIcon(@PathVariable String id, @RequestHeader(required = false, value = "If-None-Match") String hash) {
        if (hash != null) {
            // we have an etag!
            String storedHash = iconService.getHash(id);
            if (storedHash != null && storedHash.equals(hash)) {
                return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
            } else {
                return getIconBody(id);
            }
        } else {
            return getIconBody(id);
        }
    }

    private ResponseEntity<byte[]> getIconBody(String id) {
        Icon icon = iconService.getIcon(id);
        if (icon != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("ETag", icon.getHash());
            headers.add("Content-Type", "image/png");
            headers.add("Content-Length", Integer.toString(icon.getBytes().length));
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
