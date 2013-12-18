package org.thymeleaf.stripes.messageresolver;

import java.text.MessageFormat;
import java.util.MissingResourceException;

import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.localization.LocalizationBundleFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Arguments;
import org.thymeleaf.messageresolver.AbstractMessageResolver;
import org.thymeleaf.messageresolver.MessageResolution;
import org.thymeleaf.util.Validate;

public final class StripesMessageResolver extends AbstractMessageResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(StripesMessageResolver.class);

    public StripesMessageResolver() {
        super();
        // Order this resolver as last in line
        setOrder(1000);
    }
       
    @Override
    protected final void initializeSpecific() {
    }

    public MessageResolution resolveMessage(final Arguments arguments, final String key, final Object[] messageParameters) {
    	logger.debug("Looking up key [{}] in Stripes' field and error bundles.", key);
        Validate.notNull(arguments.getContext().getLocale(), "Locale in context cannot be null");
        Validate.notNull(key, "Message key cannot be null");

        LocalizationBundleFactory bundleFactory = StripesFilter.getConfiguration().getLocalizationBundleFactory();
        // First, look up the fields bundle
        try {
        	String message = bundleFactory.getFormFieldBundle(arguments.getContext().getLocale()).getString(key);
            if (messageParameters != null && messageParameters.length > 0) return new MessageResolution(MessageFormat.format(message, messageParameters));
            else return new MessageResolution(message);
        } catch (MissingResourceException mre) {
            // Nothing to catch, key may not be a field but an error message
        }
        // If no match found in fields, check in errors bundle
        try {
        	String message = bundleFactory.getErrorMessageBundle(arguments.getContext().getLocale()).getString(key);
            if (messageParameters != null && messageParameters.length > 0) return new MessageResolution(MessageFormat.format(message, messageParameters));
            else return new MessageResolution(message);
        } catch (MissingResourceException mre) {
        	// Nothing to catch, we don't raise an exception for a missing key
        }
        // Not found in either of the bundles
        return null;
    }

}