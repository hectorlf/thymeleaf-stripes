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

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IProcessingContext;

/**
 * <p>
 *   Utility class containing methods for creating utility
 *   objects that will be included into expression evaluation contexts.
 * </p>
 * 
 * @author Daniel Fern&aacute;ndez
 * 
 * @since 1.1
 *
 */
public final class JodaExpressionEvaluatorObjects {

    public static final String JODA_DATES_EVALUATION_VARIABLE_NAME = "joda";

    private static final ConcurrentHashMap<Locale, Map<String,Object>> BASE_OBJECTS_BY_LOCALE_CACHE =
            new ConcurrentHashMap<Locale, Map<String, Object>>(1, 1.0f, 1);
    
    
    private JodaExpressionEvaluatorObjects() {
        super();
    }

    
    
    public static Map<String,Object> computeEvaluationObjects(final IProcessingContext processingContext) {
        final IContext context = processingContext.getContext();
        final Map<String,Object> variables = new HashMap<String, Object>(1);
        variables.putAll(computeBaseObjectsByLocale(context.getLocale()));
        return variables;
    }

    private static Map<String,Object> computeBaseObjectsByLocale(final Locale locale) {
    	if (locale == null) return Collections.emptyMap();
    	
        Map<String,Object> objects = BASE_OBJECTS_BY_LOCALE_CACHE.get(locale);
        if (objects != null) return objects; 

        objects = new HashMap<String, Object>(1);
        objects.put(JODA_DATES_EVALUATION_VARIABLE_NAME, new JodaDates(locale));
        BASE_OBJECTS_BY_LOCALE_CACHE.put(locale, objects);
        return objects;
    }

}