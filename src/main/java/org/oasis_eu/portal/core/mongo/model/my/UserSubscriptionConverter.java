package org.oasis_eu.portal.core.mongo.model.my;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class UserSubscriptionConverter implements Converter<String, UserSubscription> {

    @Override
    public UserSubscription convert(String source) {
        UserSubscription us = new UserSubscription();
        us.setId(source);
        return us;
    }
}
