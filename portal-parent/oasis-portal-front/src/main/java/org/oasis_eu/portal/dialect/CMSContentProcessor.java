package org.oasis_eu.portal.dialect;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.oasis_eu.portal.core.services.cms.ContentRenderingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.dom.Text;
import org.thymeleaf.processor.attr.AbstractChildrenModifierAttrProcessor;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;

/**
 * 
 * @author mkalamalami
 *
 */
@Component
public class CMSContentProcessor extends AbstractChildrenModifierAttrProcessor {

    @Autowired
    private ContentRenderingService contentRenderingService;

    @Autowired
    private HttpServletRequest request;

    public CMSContentProcessor() {
        super("content");
    }

    public int getPrecedence() {
        return 10000;
    }

    @Override
    protected final List<Node> getModifiedChildren(final Arguments arguments, final Element element,
            final String attributeName) {
        // (Adapted from AbstractTextChildModifierAttrProcessor)
        
        final String text = getText(arguments, element, attributeName);
        final Text newNode = new Text(text != null ? text : "", false);
        newNode.setProcessable(false);
        return Collections.singletonList(newNode);
    }

    protected String getText(final Arguments arguments, final Element element, final String attributeName) {
        // (Adapted from AbstractStandardTextChildModifierAttrProcessor)
        
        // Fetch and evaluate attribute
        final String attributeExpression = element.getAttributeValue(attributeName);
        final Configuration configuration = arguments.getConfiguration();
        final IStandardExpressionParser expressionParser = StandardExpressions.getExpressionParser(configuration);
        final IStandardExpression expression = expressionParser.parseExpression(configuration, arguments, attributeExpression);
        final String contentId = (String) expression.execute(configuration, arguments);

        // Render template
        final Locale locale = RequestContextUtils.getLocale(request);
        return contentRenderingService.render(contentId, locale);
    }

}