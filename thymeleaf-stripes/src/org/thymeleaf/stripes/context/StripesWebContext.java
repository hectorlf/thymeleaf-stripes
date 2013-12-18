/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2013, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package org.thymeleaf.stripes.context;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.util.ReflectUtil;
import net.sourceforge.stripes.util.UrlBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.WebContext;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class StripesWebContext extends WebContext implements IWebContext {
	
	private static final Logger logger = LoggerFactory.getLogger(StripesWebContext.class);
	
	private static final Cache<String,String> cache = CacheBuilder.newBuilder().maximumSize(100).build();

	private final net.sourceforge.stripes.config.Configuration stripesConfig;

    public StripesWebContext(final HttpServletRequest request, final HttpServletResponse response, final ServletContext servletContext, final Locale locale) {
    	super(request, response, servletContext, locale);
    	this.stripesConfig = StripesFilter.getConfiguration();
    }

    
    
    
    /**
     * @throws ClassCastException, if classname was found by the classloader but it wasn't an ActionBean
     */
    public String resolveActionBeanUrl(String className, Map<String,List<Object>> parameters, String anchor) throws ClassCastException {
    	// Nonexistent class names may not be an error
    	String baseUrl = resolveActionBeanUrlBinding(className);
    	if (baseUrl == null) return null;
    	// If found, build the final url
    	UrlBuilder builder = new UrlBuilder(getLocale(), baseUrl, false);
    	builder.addParameters(parameters);
    	builder.setAnchor(anchor);
    	String url = builder.toString();
    	// Prepend the context path
        String contextPath = getHttpServletRequest().getContextPath();
        if (!url.startsWith("/")) url = "/" + url;
        if (contextPath.length() > 1) url = contextPath + url;
        // Done
        return url;
    }

    public String resolveActionBeanUrlBinding(String className) throws ClassCastException {
    	// Get binding from cache instead of resolving
    	String binding = cache.getIfPresent(className);
    	if (binding != null) return binding;
    	// Not on cache, try to resolve bean class for name
    	// It may be null, if it's not an actionbean
    	Class<ActionBean> beanType = resolveActionBeanClass(className);
    	if (beanType == null) return null;
    	// Resolve url binding and cache it
    	binding = stripesConfig.getActionResolver().getUrlBinding(beanType);
    	cache.put(className, binding);
    	return binding;
    }
    @SuppressWarnings("unchecked")
	private Class<ActionBean> resolveActionBeanClass(String className) throws ClassCastException {
    	Class<?> actionBean = null;
    	try {
    		actionBean = ReflectUtil.findClass(className);
        	// Non ActionBean classes ARE an error
        	if (!ActionBean.class.isAssignableFrom(actionBean) || actionBean.isInterface()) {
        		logger.debug("Resolved class for name '{}' but it's not a concrete ActionBean. There's no way to guess the corresponding url.", className);
        		throw new ClassCastException("Cannot cast " + actionBean.getName() + " to " + ActionBean.class.getName());
        	}
    	} catch(ClassNotFoundException cnfe) {
    		// Looked like a class, but wasn't
    		logger.debug("Tried to resolve class for name '{}' but couldn't find it with the current classloader.", className);
    	}
    	return (Class<ActionBean>) actionBean;
    }

}