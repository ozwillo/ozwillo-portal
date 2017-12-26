package org.oasis_eu.portal.core.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * User: schambon
 * Date: 1/8/15
 */
public class XmlParserConverter extends AbstractHttpMessageConverter<Object> {

    private XmlMapper xmlMapper = new XmlMapper();

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Arrays.asList(MediaType.APPLICATION_XML);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return xmlMapper.readValue(inputMessage.getBody(), clazz);
    }

    @Override
    protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws HttpMessageNotWritableException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
