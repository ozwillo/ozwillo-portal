package org.oasis_eu.portal.dialect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.processor.IProcessor;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author mkalamalami
 *
 */
@Component
public class CMSDialect extends AbstractDialect {

    @Autowired
    CMSContentProcessor cmsContentProcessor;

    public String getPrefix() {
        return "cms";
    }

    @Override
    public Set<IProcessor> getProcessors() {
        final Set<IProcessor> processors = new HashSet<IProcessor>();
        processors.add(cmsContentProcessor);
        return processors;
    }

}