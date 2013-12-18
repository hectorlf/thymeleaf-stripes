package org.thymeleaf.stripes.messageresolver;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Arguments;
import org.thymeleaf.exceptions.ConfigurationException;
import org.thymeleaf.messageresolver.AbstractMessageResolver;
import org.thymeleaf.messageresolver.MessageResolution;
import org.thymeleaf.util.Validate;

public final class PropertyResourceBundleMessageResolver extends AbstractMessageResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(PropertyResourceBundleMessageResolver.class);
    
    private List<String> configuringBundles;
    private String[] bundleNames;

    public PropertyResourceBundleMessageResolver() {
        super();
        configuringBundles = new LinkedList<String>();
        bundleNames = new String[0];
        // Set order to be the first
        setOrder(1);
    }
       
    @Override
    protected final void initializeSpecific() {
    	if (configuringBundles.size() == 0) throw new ConfigurationException("You need to add at least one resource bundle name before initializing the engine to be able to use this message resolver.");
    	bundleNames = configuringBundles.toArray(bundleNames);
    }
    
    public void addBundleFromProperties(String fileNames) {
    	if (fileNames == null || fileNames.trim().length() == 0) return;
    	String[] names = fileNames.trim().split(",");
    	for (int i = 0; i < names.length; i++) {
    		if (names[i] == null || names[i].trim().length() == 0) continue;
   			configuringBundles.add(names[i].trim());
   			// Pre-initialization sanity check
   			try {
   				ResourceBundle.getBundle(names[i].trim());
   			} catch(Exception e) {
   				throw new ConfigurationException("Could not create ResourceBundle from name ["+names[i].trim()+"], maybe nonexistent properties file");
   			}
    	}
    }

    public MessageResolution resolveMessage(final Arguments arguments, final String key, final Object[] messageParameters) {
    	logger.debug("Looking up key [{}] in application-configured ResourceBundles.", key);
        Validate.notNull(arguments.getContext().getLocale(), "Locale in context cannot be null");
        Validate.notNull(key, "Message key cannot be null");

        String message = null;
        // Look up in all the configured bundles until key is found
        for (int i = 0; i < bundleNames.length; i++) {
        	try {
            	message = ResourceBundle.getBundle(bundleNames[i], arguments.getContext().getLocale()).getString(key);
            	break;
            } catch (MissingResourceException mre) {
                // Nothing to catch, key may exist in another bundle
            }
        }
        // A key not found should not throw an exception
        if (message == null) return null;
        // If message has parameters, format it
        if (messageParameters != null && messageParameters.length > 0) return new MessageResolution(MessageFormat.format(message, messageParameters));
        else return new MessageResolution(message);
    }

}