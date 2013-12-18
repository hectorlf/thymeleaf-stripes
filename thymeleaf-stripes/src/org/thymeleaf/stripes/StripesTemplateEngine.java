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
package org.thymeleaf.stripes;

import java.util.Map;

import org.thymeleaf.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.exceptions.ConfigurationException;
import org.thymeleaf.stripes.dialect.StripesStandardDialect;
import org.thymeleaf.stripes.messageresolver.StripesMessageResolver;

public class StripesTemplateEngine extends TemplateEngine {
    
    private static final StripesStandardDialect STRIPES_STANDARD_DIALECT = new StripesStandardDialect();
    private static final StripesMessageResolver STRIPES_MESSAGE_RESOLVER = new StripesMessageResolver();
    
    public StripesTemplateEngine() {
        super();
        // Add the default StripesStandardDialect
        super.clearDialects();
        super.addDialect(STRIPES_STANDARD_DIALECT);
        // Add the default StripesMessageResolver
        addMessageResolver(STRIPES_MESSAGE_RESOLVER);
    }

    @Override
    protected final void initializeSpecific() {
    	// Make sure there's a StripesStandardDialect configured
        final Configuration configuration = getConfiguration();
        final Map<String,IDialect> dialects = configuration.getDialects();
        boolean hasStripesDialect = false;
        for (final IDialect dialect : dialects.values()) {
            if (dialect instanceof StripesStandardDialect) {
            	hasStripesDialect = true;
                return;
            }
        }
        if (!hasStripesDialect) {
        	throw new ConfigurationException("No StripesStandardDialect found. When using StripesTemplateEngine, you should not clear the dialects because there's a StripesStandardDialect added by default.");
        }
    }

}