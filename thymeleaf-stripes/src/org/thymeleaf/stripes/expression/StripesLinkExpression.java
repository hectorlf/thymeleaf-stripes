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
package org.thymeleaf.stripes.expression;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.standard.expression.Assignation;
import org.thymeleaf.standard.expression.AssignationSequence;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.LinkExpression;
import org.thymeleaf.standard.expression.LiteralValue;
import org.thymeleaf.standard.expression.SimpleExpression;
import org.thymeleaf.standard.expression.StandardExpressionExecutionContext;
import org.thymeleaf.stripes.context.StripesWebContext;
import org.thymeleaf.util.StringUtils;

public final class StripesLinkExpression extends SimpleExpression {
    
	private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(StripesLinkExpression.class);

    private static final String URL_PARAM_NO_VALUE = "%%%__NO_VALUE__%%%";



	static Object executeLink(final Configuration configuration, final IProcessingContext processingContext, final LinkExpression expression, final StandardExpressionExecutionContext expContext) {
        if (logger.isTraceEnabled()) {
            logger.trace("[THYMELEAF][{}] Evaluating link: \"{}\"", TemplateEngine.threadIndex(), expression.getStringRepresentation());
        }
        if (!(processingContext.getContext() instanceof StripesWebContext)) {
        	throw new TemplateProcessingException("Stripes link resolver needs a " + StripesWebContext.class.getName() + " object (context is of class: " + processingContext.getContext().getClass().getName() + ")");
        }

        final IStandardExpression baseExpression = expression.getBase();
        Object base = baseExpression.execute(configuration, processingContext, expContext);

        base = LiteralValue.unwrap(base);
        if (base != null && !(base instanceof String)) {
            base = base.toString();
        }
        if (base == null || StringUtils.isEmptyOrWhitespace((String) base)) {
            base = "";
        }

        String linkBase = (String) base;
        
        @SuppressWarnings("unchecked")
        final Map<String,List<Object>> parameters =
            (expression.hasParameters()?
                    resolveParameters(configuration, processingContext, expression, expContext) :
                    (Map<String,List<Object>>) Collections.EMPTY_MAP);
        
        /*
         * Detect URL fragments (selectors after '#') so that they can be output at the end of the URL, after parameters.
         */
        final int hashPosition = linkBase.indexOf('#');
        String urlFragment = "";
        // If hash position == 0 we will not consider it as marking an
        // URL fragment.
        if (hashPosition > 0) {
            // URL fragment String will include the # sign
            urlFragment = linkBase.substring(hashPosition);
            linkBase = linkBase.substring(0, hashPosition);
        }
        
        /*
         * At this point, we have all the info we need in case it's a Stripes-class link
         */
        if (linkLooksLikeStripesClass(linkBase)) {
        	if (logger.isDebugEnabled()) {
                logger.debug("[STRIPES THYMELEAF PLUGIN][{}] Evaluating a Stripes link candidate: \"{}\"", TemplateEngine.threadIndex(), expression.getStringRepresentation());
            }
        	final StripesWebContext sctx = (StripesWebContext) processingContext.getContext();
        	String url = null;
        	try {
        		// We need to traslate ActionBean class to a url
        		// ActionBean urls always have the context prepended
        		url = sctx.resolveActionBeanUrl(linkBase, parameters, urlFragment);
        	} catch(Exception e) {
        		// Exceptions here usually mean a good class name, but not an ActionBean or badly configured
        		throw new TemplateProcessingException("Could not resolve ActionBean url [" + linkBase + "]. Exception: " + e.getClass().getName() + " : " + e.getMessage());
        	}
        	// Url may be null if linkBase looked like a class but couldn't be found
        	// If not null, resolved url is already complete
        	if (url != null) return sctx.getHttpServletResponse().encodeURL(url);
        }
        
        /*
         * Check for the existence of a question mark symbol in the link base itself
         */
        final int questionMarkPosition = linkBase.indexOf('?');
        
        final StringBuilder parametersBuilder = new StringBuilder();
        
        for (final Map.Entry<String,List<Object>> parameterEntry : parameters.entrySet()) {
            final String parameterName = parameterEntry.getKey();
            final List<Object> parameterValues = parameterEntry.getValue();
            
            for (final Object parameterObjectValue : parameterValues) {
                // Insert a separator with the previous parameter, if needed
                if (parametersBuilder.length() == 0) {
                    if (questionMarkPosition == -1) {
                        parametersBuilder.append("?");
                    } else {
                        parametersBuilder.append("&");
                    }
                } else {
                    parametersBuilder.append("&");
                }

                final String parameterValue = (parameterObjectValue == null? "" : parameterObjectValue.toString());

                if (URL_PARAM_NO_VALUE.equals(parameterValue)) {
                    // This is a parameter without a value and even without an "=" symbol
                    parametersBuilder.append(parameterName);
                } else {
                    try {
                        parametersBuilder.append(parameterName).append("=").append(URLEncoder.encode(parameterValue, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        throw new TemplateProcessingException("Exception while processing link parameters", e);
                    }
                }
            }
        }

        /*
         * Link resolution and encoding
         */
        final IWebContext webContext = (IWebContext) processingContext.getContext();
        
        final HttpServletRequest request = webContext.getHttpServletRequest();
        final HttpServletResponse response = webContext.getHttpServletResponse();

        String url = null;
        
        if (isLinkBaseContextRelative(linkBase)) {
            url = request.getContextPath() + linkBase + parametersBuilder + urlFragment;
        } else if (isLinkBaseServerRelative(linkBase)) {
            // remove the "~" from the link base
            url = linkBase.substring(1) + parametersBuilder + urlFragment;
        } else if (isLinkBaseAbsolute(linkBase)) {
            url = linkBase + parametersBuilder + urlFragment;
        } else {
            // Link base is current-URL-relative
            url = linkBase + parametersBuilder + urlFragment;
        }

        return (response != null? response.encodeURL(url) : url);
    }
    
    

    private static boolean linkLooksLikeStripesClass(final String linkBase) {
        return (!linkBase.contains("/") && !linkBase.contains(":") && !linkBase.contains("?") && !linkBase.endsWith(".html") && !linkBase.endsWith(".jsp"));
    }

    
    private static boolean isLinkBaseAbsolute(final String linkBase) {
        return (linkBase.contains("://") ||
                linkBase.toLowerCase().startsWith("mailto:") || // Email URLs
                linkBase.startsWith("//")); // protocol-relative URLs
    }

    private static boolean isLinkBaseContextRelative(final String linkBase) {
        return linkBase.startsWith("/") && !linkBase.startsWith("//");
    }

    private static boolean isLinkBaseServerRelative(final String linkBase) {
        return linkBase.startsWith("~/");
    }
    
    private static Map<String,List<Object>> resolveParameters(
            final Configuration configuration, final IProcessingContext processingContext, 
            final LinkExpression expression, final StandardExpressionExecutionContext expContext) {

        final AssignationSequence assignationValues = expression.getParameters();

        final Map<String,List<Object>> parameters = new LinkedHashMap<String,List<Object>>(assignationValues.size() + 1, 1.0f);
        for (final Assignation assignationValue : assignationValues) {
            
            final IStandardExpression parameterNameExpr = assignationValue.getLeft();
            final IStandardExpression parameterValueExpr = assignationValue.getRight();

            // We know parameterNameExpr cannot be null (the Assignation class would not allow it)
            final Object parameterNameValue = parameterNameExpr.execute(configuration, processingContext, expContext);
            final String parameterName = (parameterNameValue == null? null : parameterNameValue.toString());

            if (StringUtils.isEmptyOrWhitespace(parameterName)) {
                throw new TemplateProcessingException(
                        "Parameters in link expression \"" + expression.getStringRepresentation() + "\" are " +
                        "incorrect: parameter name expression \"" + parameterNameExpr.getStringRepresentation() +
                        "\" evaluated as null or empty string.");
            }

            List<Object> currentParameterValues = parameters.get(parameterName);
            if (currentParameterValues == null) {
                currentParameterValues = new ArrayList<Object>(4);
                parameters.put(parameterName, currentParameterValues);
            }
            
            if (parameterValueExpr == null) {
                // If this is null, it means we want to render the parameter without a value and
                // also without an equals sign.
                currentParameterValues.add(URL_PARAM_NO_VALUE);
            } else {
                final Object value =
                        parameterValueExpr.execute(configuration, processingContext, expContext);
                if (value == null) {
                    // Not the same as not specifying a value!
                    currentParameterValues.add("");
                } else {
                    currentParameterValues.addAll(convertParameterValueToList(LiteralValue.unwrap(value)));
                }
            }
            
        }
        return parameters;
        
    }

    private static List<Object> convertParameterValueToList(final Object parameterValue) {
        
        if (parameterValue instanceof Iterable<?>) {
            final List<Object> result = new ArrayList<Object>(4);
            for (final Object obj : (Iterable<?>) parameterValue) {
                result.add(obj);
            }
            return result;
        } else if (parameterValue.getClass().isArray()){
            final List<Object> result = new ArrayList<Object>(4);
            if (parameterValue instanceof byte[]) {
                for (final byte obj : (byte[]) parameterValue) {
                    result.add(Byte.valueOf(obj));
                }
            } else if (parameterValue instanceof short[]) {
                for (final short obj : (short[]) parameterValue) {
                    result.add(Short.valueOf(obj));
                }
            } else if (parameterValue instanceof int[]) {
                for (final int obj : (int[]) parameterValue) {
                    result.add(Integer.valueOf(obj));
                }
            } else if (parameterValue instanceof long[]) {
                for (final long obj : (long[]) parameterValue) {
                    result.add(Long.valueOf(obj));
                }
            } else if (parameterValue instanceof float[]) {
                for (final float obj : (float[]) parameterValue) {
                    result.add(Float.valueOf(obj));
                }
            } else if (parameterValue instanceof double[]) {
                for (final double obj : (double[]) parameterValue) {
                    result.add(Double.valueOf(obj));
                }
            } else if (parameterValue instanceof boolean[]) {
                for (final boolean obj : (boolean[]) parameterValue) {
                    result.add(Boolean.valueOf(obj));
                }
            } else if (parameterValue instanceof char[]) {
                for (final char obj : (char[]) parameterValue) {
                    result.add(Character.valueOf(obj));
                }
            } else {
                final Object[] objParameterValue = (Object[]) parameterValue;
                Collections.addAll(result, objParameterValue);
            }
            return result;
        } else{
            return Collections.singletonList(parameterValue);
        }
        
    }




	@Override
	public String getStringRepresentation() {
		return null;
	}
    
}
