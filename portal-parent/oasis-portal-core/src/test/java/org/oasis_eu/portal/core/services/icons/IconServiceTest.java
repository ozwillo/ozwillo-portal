package org.oasis_eu.portal.core.services.icons;

import com.google.common.io.ByteStreams;
import org.junit.Test;
import org.oasis_eu.portal.core.mongo.model.icons.IconFormat;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.*;

public class IconServiceTest {

    @Test
    public void testGetFileName() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        assertEquals("www.whatever.com", doGetFileName("http://www.whatever.com"));
        assertEquals("citizenkin.png", doGetFileName("http://www.citizenkin.com/icon/citizenkin.png"));
        assertEquals("www.plouf.org", doGetFileName("http://www.plouf.org/"));
        assertEquals("citizenkin.png", doGetFileName("http://www.citizenkin.com/icon/citizenkin.png?size=bis"));
        assertEquals("citizenkin.png", doGetFileName("https://www.citizenkin.com/icon/citizenkin.png"));
        assertEquals("file.txt", doGetFileName("file.txt"));
        assertEquals("file.txt", doGetFileName("file.txt?param=true"));
        assertEquals("file.txt", doGetFileName("file.txt/"));
    }

    @Test
    public void testEnsurePNG() throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        assertTrue(doEnsurePNG(load("images/img-16.png")));
        assertFalse(doEnsurePNG(load("images/img-01.svg")));
        assertFalse(doEnsurePNG(load("images/img-test.tiff")));
        assertFalse(doEnsurePNG(new byte[0]));
    }

    @Test
    public void testGetFormat() throws Exception {
        assertEquals(IconFormat.PNG_64BY64, doGetFormat(load("images/64.png")));
        assertEquals(IconFormat.PNG_32BY32, doGetFormat(load("images/32.png")));
        assertEquals(IconFormat.INVALID, doGetFormat(load("images/img-16.png")));

        byte[] b = new byte[1024];
        Random random = new Random();
        random.nextBytes(b);
        assertEquals(IconFormat.INVALID, doGetFormat(b));

        assertEquals(IconFormat.INVALID, doGetFormat(load("images/rectangular.png")));
    }

    @Test
    public void testGetHash() throws Exception {
        byte[] hash = doGetHash(load("images/64.png"));
        assertNotNull(hash);
        assertEquals(32, hash.length);
        // we stringify in order to compare (because String has an equals method while byte[] doesn't)
        String s = new String(hash, "UTF-8");
        // 64-bis is only different from 64 by one pixel
        byte[] hash2 = doGetHash(load("images/64-bis.png"));
        assertNotEquals(s, new String(hash2, "UTF-8"));
        assertEquals(hash.length, hash2.length);
        // 64-ter is really the same as 64
        assertEquals(s, new String(doGetHash(load("images/64-ter.png")), "UTF-8"));
    }


    private String doGetFileName(String arg) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        IconService service = new IconService();
        Method getFileName = service.getClass().getDeclaredMethod("getFileName", String.class);
        getFileName.setAccessible(true);

        return (String) getFileName.invoke(service, arg);
    }

    private boolean doEnsurePNG(byte[] array) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        IconService service = new IconService();
        Method ensurePNG = service.getClass().getDeclaredMethod("ensurePNG", byte[].class);
        ensurePNG.setAccessible(true);

        return (boolean) ensurePNG.invoke(service, array);
    }

    private IconFormat doGetFormat(byte[] array) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        IconService service = new IconService();
        Method getFormat = service.getClass().getDeclaredMethod("getFormat", byte[].class);
        getFormat.setAccessible(true);
        return (IconFormat) getFormat.invoke(service, array);
    }

    private byte[] doGetHash(byte[] array) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        IconService service = new IconService();
        Method getHash = service.getClass().getDeclaredMethod("getHash", byte[].class);
        getHash.setAccessible(true);
        return (byte[]) getHash.invoke(service, array);
    }


    private byte[] load(String name) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
        return ByteStreams.toByteArray(stream);
    }
}