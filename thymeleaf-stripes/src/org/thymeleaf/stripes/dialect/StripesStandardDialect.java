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
package org.thymeleaf.stripes.dialect;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Standards;
import org.thymeleaf.doctype.DocTypeIdentifier;
import org.thymeleaf.doctype.resolution.ClassLoaderDocTypeResolutionEntry;
import org.thymeleaf.doctype.resolution.IDocTypeResolutionEntry;
import org.thymeleaf.doctype.translation.DocTypeTranslation;
import org.thymeleaf.doctype.translation.IDocTypeTranslation;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.stripes.expression.StripesLinkExpression;
import org.thymeleaf.stripes.expression.StripesOgnlVariableExpressionEvaluator;
import org.thymeleaf.stripes.processor.element.StripesUseActionBeanElementProcessor;
import org.thymeleaf.util.ClassLoaderUtils;

public class StripesStandardDialect extends StandardDialect {

    private static final Logger logger = LoggerFactory.getLogger(StripesStandardDialect.class);

    private static boolean linkExpressionFixApplied = false;
    
    public static final DocTypeIdentifier XHTML1_STRICT_THYMELEAFSPRING3_1_SYSTEMID = 
        DocTypeIdentifier.forValue("http://www.thymeleaf.org/dtd/xhtml1-strict-thymeleaf-spring3-1.dtd");
    public static final DocTypeIdentifier XHTML1_TRANSITIONAL_THYMELEAFSPRING3_1_SYSTEMID = 
        DocTypeIdentifier.forValue("http://www.thymeleaf.org/dtd/xhtml1-transitional-thymeleaf-spring3-1.dtd");
    public static final DocTypeIdentifier XHTML1_FRAMESET_THYMELEAFSPRING3_1_SYSTEMID = 
        DocTypeIdentifier.forValue("http://www.thymeleaf.org/dtd/xhtml1-frameset-thymeleaf-spring3-1.dtd");
    public static final DocTypeIdentifier XHTML11_THYMELEAFSPRING3_1_SYSTEMID = 
        DocTypeIdentifier.forValue("http://www.thymeleaf.org/dtd/xhtml11-thymeleaf-spring3-1.dtd");

    public static final DocTypeIdentifier XHTML1_STRICT_THYMELEAFSPRING3_2_SYSTEMID = 
        DocTypeIdentifier.forValue("http://www.thymeleaf.org/dtd/xhtml1-strict-thymeleaf-spring3-2.dtd");
    public static final DocTypeIdentifier XHTML1_TRANSITIONAL_THYMELEAFSPRING3_2_SYSTEMID = 
        DocTypeIdentifier.forValue("http://www.thymeleaf.org/dtd/xhtml1-transitional-thymeleaf-spring3-2.dtd");
    public static final DocTypeIdentifier XHTML1_FRAMESET_THYMELEAFSPRING3_2_SYSTEMID = 
        DocTypeIdentifier.forValue("http://www.thymeleaf.org/dtd/xhtml1-frameset-thymeleaf-spring3-2.dtd");
    public static final DocTypeIdentifier XHTML11_THYMELEAFSPRING3_2_SYSTEMID = 
        DocTypeIdentifier.forValue("http://www.thymeleaf.org/dtd/xhtml11-thymeleaf-spring3-2.dtd");

    public static final DocTypeIdentifier XHTML1_STRICT_THYMELEAFSPRING3_3_SYSTEMID =
        DocTypeIdentifier.forValue("http://www.thymeleaf.org/dtd/xhtml1-strict-thymeleaf-spring3-3.dtd");
    public static final DocTypeIdentifier XHTML1_TRANSITIONAL_THYMELEAFSPRING3_3_SYSTEMID =
        DocTypeIdentifier.forValue("http://www.thymeleaf.org/dtd/xhtml1-transitional-thymeleaf-spring3-3.dtd");
    public static final DocTypeIdentifier XHTML1_FRAMESET_THYMELEAFSPRING3_3_SYSTEMID =
        DocTypeIdentifier.forValue("http://www.thymeleaf.org/dtd/xhtml1-frameset-thymeleaf-spring3-3.dtd");
    public static final DocTypeIdentifier XHTML11_THYMELEAFSPRING3_3_SYSTEMID =
        DocTypeIdentifier.forValue("http://www.thymeleaf.org/dtd/xhtml11-thymeleaf-spring3-3.dtd");

    public static final DocTypeIdentifier XHTML1_STRICT_THYMELEAFSPRING3_4_SYSTEMID =
        DocTypeIdentifier.forValue("http://www.thymeleaf.org/dtd/xhtml1-strict-thymeleaf-spring3-4.dtd");
    public static final DocTypeIdentifier XHTML1_TRANSITIONAL_THYMELEAFSPRING3_4_SYSTEMID =
        DocTypeIdentifier.forValue("http://www.thymeleaf.org/dtd/xhtml1-transitional-thymeleaf-spring3-4.dtd");
    public static final DocTypeIdentifier XHTML1_FRAMESET_THYMELEAFSPRING3_4_SYSTEMID =
        DocTypeIdentifier.forValue("http://www.thymeleaf.org/dtd/xhtml1-frameset-thymeleaf-spring3-4.dtd");
    public static final DocTypeIdentifier XHTML11_THYMELEAFSPRING3_4_SYSTEMID =
        DocTypeIdentifier.forValue("http://www.thymeleaf.org/dtd/xhtml11-thymeleaf-spring3-4.dtd");

    
    public static final IDocTypeResolutionEntry XHTML1_STRICT_THYMELEAFSPRING3_1_DOC_TYPE_RESOLUTION_ENTRY =
        new ClassLoaderDocTypeResolutionEntry(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_STRICT_THYMELEAFSPRING3_1_SYSTEMID, // SYSTEMID
                "org/thymeleaf/dtd/thymeleaf-spring3/xhtml1-strict-thymeleaf-spring3-1.dtd"); // CLASS-LOADER-RESOLVABLE RESOURCE 
    
    public static final IDocTypeResolutionEntry XHTML1_TRANSITIONAL_THYMELEAFSPRING3_1_DOC_TYPE_RESOLUTION_ENTRY =
        new ClassLoaderDocTypeResolutionEntry(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_TRANSITIONAL_THYMELEAFSPRING3_1_SYSTEMID, // SYSTEMID
                "org/thymeleaf/dtd/thymeleaf-spring3/xhtml1-transitional-thymeleaf-spring3-1.dtd"); // CLASS-LOADER-RESOLVABLE RESOURCE 
    
    public static final IDocTypeResolutionEntry XHTML1_FRAMESET_THYMELEAFSPRING3_1_DOC_TYPE_RESOLUTION_ENTRY =
        new ClassLoaderDocTypeResolutionEntry(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_FRAMESET_THYMELEAFSPRING3_1_SYSTEMID, // SYSTEMID
                "org/thymeleaf/dtd/thymeleaf-spring3/xhtml1-frameset-thymeleaf-spring3-1.dtd"); // CLASS-LOADER-RESOLVABLE RESOURCE 

    public static final IDocTypeResolutionEntry XHTML11_THYMELEAFSPRING3_1_DOC_TYPE_RESOLUTION_ENTRY =
        new ClassLoaderDocTypeResolutionEntry(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML11_THYMELEAFSPRING3_1_SYSTEMID, // SYSTEMID
                "org/thymeleaf/dtd/thymeleaf-spring3/xhtml11-thymeleaf-spring3-1.dtd"); // CLASS-LOADER-RESOLVABLE RESOURCE 


    
    public static final IDocTypeResolutionEntry XHTML1_STRICT_THYMELEAFSPRING3_2_DOC_TYPE_RESOLUTION_ENTRY =
        new ClassLoaderDocTypeResolutionEntry(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_STRICT_THYMELEAFSPRING3_2_SYSTEMID, // SYSTEMID
                "org/thymeleaf/dtd/thymeleaf-spring3/xhtml1-strict-thymeleaf-spring3-2.dtd"); // CLASS-LOADER-RESOLVABLE RESOURCE 
    
    public static final IDocTypeResolutionEntry XHTML1_TRANSITIONAL_THYMELEAFSPRING3_2_DOC_TYPE_RESOLUTION_ENTRY =
        new ClassLoaderDocTypeResolutionEntry(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_TRANSITIONAL_THYMELEAFSPRING3_2_SYSTEMID, // SYSTEMID
                "org/thymeleaf/dtd/thymeleaf-spring3/xhtml1-transitional-thymeleaf-spring3-2.dtd"); // CLASS-LOADER-RESOLVABLE RESOURCE 
    
    public static final IDocTypeResolutionEntry XHTML1_FRAMESET_THYMELEAFSPRING3_2_DOC_TYPE_RESOLUTION_ENTRY =
        new ClassLoaderDocTypeResolutionEntry(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_FRAMESET_THYMELEAFSPRING3_2_SYSTEMID, // SYSTEMID
                "org/thymeleaf/dtd/thymeleaf-spring3/xhtml1-frameset-thymeleaf-spring3-2.dtd"); // CLASS-LOADER-RESOLVABLE RESOURCE 

    public static final IDocTypeResolutionEntry XHTML11_THYMELEAFSPRING3_2_DOC_TYPE_RESOLUTION_ENTRY =
        new ClassLoaderDocTypeResolutionEntry(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML11_THYMELEAFSPRING3_2_SYSTEMID, // SYSTEMID
                "org/thymeleaf/dtd/thymeleaf-spring3/xhtml11-thymeleaf-spring3-2.dtd"); // CLASS-LOADER-RESOLVABLE RESOURCE 


    
    public static final IDocTypeResolutionEntry XHTML1_STRICT_THYMELEAFSPRING3_3_DOC_TYPE_RESOLUTION_ENTRY =
        new ClassLoaderDocTypeResolutionEntry(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_STRICT_THYMELEAFSPRING3_3_SYSTEMID, // SYSTEMID
                "org/thymeleaf/dtd/thymeleaf-spring3/xhtml1-strict-thymeleaf-spring3-3.dtd"); // CLASS-LOADER-RESOLVABLE RESOURCE 
    
    public static final IDocTypeResolutionEntry XHTML1_TRANSITIONAL_THYMELEAFSPRING3_3_DOC_TYPE_RESOLUTION_ENTRY =
        new ClassLoaderDocTypeResolutionEntry(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_TRANSITIONAL_THYMELEAFSPRING3_3_SYSTEMID, // SYSTEMID
                "org/thymeleaf/dtd/thymeleaf-spring3/xhtml1-transitional-thymeleaf-spring3-3.dtd"); // CLASS-LOADER-RESOLVABLE RESOURCE 
    
    public static final IDocTypeResolutionEntry XHTML1_FRAMESET_THYMELEAFSPRING3_3_DOC_TYPE_RESOLUTION_ENTRY =
        new ClassLoaderDocTypeResolutionEntry(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_FRAMESET_THYMELEAFSPRING3_3_SYSTEMID, // SYSTEMID
                "org/thymeleaf/dtd/thymeleaf-spring3/xhtml1-frameset-thymeleaf-spring3-3.dtd"); // CLASS-LOADER-RESOLVABLE RESOURCE 

    public static final IDocTypeResolutionEntry XHTML11_THYMELEAFSPRING3_3_DOC_TYPE_RESOLUTION_ENTRY =
        new ClassLoaderDocTypeResolutionEntry(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML11_THYMELEAFSPRING3_3_SYSTEMID, // SYSTEMID
                "org/thymeleaf/dtd/thymeleaf-spring3/xhtml11-thymeleaf-spring3-3.dtd"); // CLASS-LOADER-RESOLVABLE RESOURCE 



    public static final IDocTypeResolutionEntry XHTML1_STRICT_THYMELEAFSPRING3_4_DOC_TYPE_RESOLUTION_ENTRY =
        new ClassLoaderDocTypeResolutionEntry(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_STRICT_THYMELEAFSPRING3_4_SYSTEMID, // SYSTEMID
                "org/thymeleaf/dtd/thymeleaf-spring3/xhtml1-strict-thymeleaf-spring3-4.dtd"); // CLASS-LOADER-RESOLVABLE RESOURCE

    public static final IDocTypeResolutionEntry XHTML1_TRANSITIONAL_THYMELEAFSPRING3_4_DOC_TYPE_RESOLUTION_ENTRY =
        new ClassLoaderDocTypeResolutionEntry(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_TRANSITIONAL_THYMELEAFSPRING3_4_SYSTEMID, // SYSTEMID
                "org/thymeleaf/dtd/thymeleaf-spring3/xhtml1-transitional-thymeleaf-spring3-4.dtd"); // CLASS-LOADER-RESOLVABLE RESOURCE

    public static final IDocTypeResolutionEntry XHTML1_FRAMESET_THYMELEAFSPRING3_4_DOC_TYPE_RESOLUTION_ENTRY =
        new ClassLoaderDocTypeResolutionEntry(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_FRAMESET_THYMELEAFSPRING3_4_SYSTEMID, // SYSTEMID
                "org/thymeleaf/dtd/thymeleaf-spring3/xhtml1-frameset-thymeleaf-spring3-4.dtd"); // CLASS-LOADER-RESOLVABLE RESOURCE

    public static final IDocTypeResolutionEntry XHTML11_THYMELEAFSPRING3_4_DOC_TYPE_RESOLUTION_ENTRY =
        new ClassLoaderDocTypeResolutionEntry(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML11_THYMELEAFSPRING3_4_SYSTEMID, // SYSTEMID
                "org/thymeleaf/dtd/thymeleaf-spring3/xhtml11-thymeleaf-spring3-4.dtd"); // CLASS-LOADER-RESOLVABLE RESOURCE

    
    
    
    public static final Set<IDocTypeResolutionEntry> SPRING3_DOC_TYPE_RESOLUTION_ENTRIES;
    
    
    
    public static final IDocTypeTranslation SPRING3_XHTML1_STRICT_THYMELEAF_1_DOC_TYPE_TRANSLATION = 
        new DocTypeTranslation(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_STRICT_THYMELEAFSPRING3_1_SYSTEMID, // SYSTEMID
                Standards.XHTML_1_STRICT_PUBLICID, 
                Standards.XHTML_1_STRICT_SYSTEMID);
    
    public static final IDocTypeTranslation SPRING3_XHTML1_TRANSITIONAL_THYMELEAF_1_DOC_TYPE_TRANSLATION = 
        new DocTypeTranslation(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_TRANSITIONAL_THYMELEAFSPRING3_1_SYSTEMID, // SYSTEMID
                Standards.XHTML_1_TRANSITIONAL_PUBLICID, 
                Standards.XHTML_1_TRANSITIONAL_SYSTEMID);
    
    public static final IDocTypeTranslation SPRING3_XHTML1_FRAMESET_THYMELEAF_1_DOC_TYPE_TRANSLATION = 
        new DocTypeTranslation(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_FRAMESET_THYMELEAFSPRING3_1_SYSTEMID, // SYSTEMID
                Standards.XHTML_1_FRAMESET_PUBLICID, 
                Standards.XHTML_1_FRAMESET_SYSTEMID);
    
    public static final IDocTypeTranslation SPRING3_XHTML11_THYMELEAF_1_DOC_TYPE_TRANSLATION = 
        new DocTypeTranslation(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML11_THYMELEAFSPRING3_1_SYSTEMID, // SYSTEMID
                Standards.XHTML_11_PUBLICID, 
                Standards.XHTML_11_SYSTEMID);

    
    
    public static final IDocTypeTranslation SPRING3_XHTML1_STRICT_THYMELEAF_2_DOC_TYPE_TRANSLATION = 
        new DocTypeTranslation(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_STRICT_THYMELEAFSPRING3_2_SYSTEMID, // SYSTEMID
                Standards.XHTML_1_STRICT_PUBLICID, 
                Standards.XHTML_1_STRICT_SYSTEMID);
    
    public static final IDocTypeTranslation SPRING3_XHTML1_TRANSITIONAL_THYMELEAF_2_DOC_TYPE_TRANSLATION = 
        new DocTypeTranslation(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_TRANSITIONAL_THYMELEAFSPRING3_2_SYSTEMID, // SYSTEMID
                Standards.XHTML_1_TRANSITIONAL_PUBLICID, 
                Standards.XHTML_1_TRANSITIONAL_SYSTEMID);
    
    public static final IDocTypeTranslation SPRING3_XHTML1_FRAMESET_THYMELEAF_2_DOC_TYPE_TRANSLATION = 
        new DocTypeTranslation(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_FRAMESET_THYMELEAFSPRING3_2_SYSTEMID, // SYSTEMID
                Standards.XHTML_1_FRAMESET_PUBLICID, 
                Standards.XHTML_1_FRAMESET_SYSTEMID);
    
    public static final IDocTypeTranslation SPRING3_XHTML11_THYMELEAF_2_DOC_TYPE_TRANSLATION = 
        new DocTypeTranslation(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML11_THYMELEAFSPRING3_2_SYSTEMID, // SYSTEMID
                Standards.XHTML_11_PUBLICID, 
                Standards.XHTML_11_SYSTEMID);

    
    
    public static final IDocTypeTranslation SPRING3_XHTML1_STRICT_THYMELEAF_3_DOC_TYPE_TRANSLATION = 
        new DocTypeTranslation(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_STRICT_THYMELEAFSPRING3_3_SYSTEMID, // SYSTEMID
                Standards.XHTML_1_STRICT_PUBLICID, 
                Standards.XHTML_1_STRICT_SYSTEMID);
    
    public static final IDocTypeTranslation SPRING3_XHTML1_TRANSITIONAL_THYMELEAF_3_DOC_TYPE_TRANSLATION = 
        new DocTypeTranslation(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_TRANSITIONAL_THYMELEAFSPRING3_3_SYSTEMID, // SYSTEMID
                Standards.XHTML_1_TRANSITIONAL_PUBLICID, 
                Standards.XHTML_1_TRANSITIONAL_SYSTEMID);
    
    public static final IDocTypeTranslation SPRING3_XHTML1_FRAMESET_THYMELEAF_3_DOC_TYPE_TRANSLATION = 
        new DocTypeTranslation(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_FRAMESET_THYMELEAFSPRING3_3_SYSTEMID, // SYSTEMID
                Standards.XHTML_1_FRAMESET_PUBLICID, 
                Standards.XHTML_1_FRAMESET_SYSTEMID);
    
    public static final IDocTypeTranslation SPRING3_XHTML11_THYMELEAF_3_DOC_TYPE_TRANSLATION = 
        new DocTypeTranslation(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML11_THYMELEAFSPRING3_3_SYSTEMID, // SYSTEMID
                Standards.XHTML_11_PUBLICID, 
                Standards.XHTML_11_SYSTEMID);



    public static final IDocTypeTranslation SPRING3_XHTML1_STRICT_THYMELEAF_4_DOC_TYPE_TRANSLATION =
        new DocTypeTranslation(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_STRICT_THYMELEAFSPRING3_4_SYSTEMID, // SYSTEMID
                Standards.XHTML_1_STRICT_PUBLICID,
                Standards.XHTML_1_STRICT_SYSTEMID);

    public static final IDocTypeTranslation SPRING3_XHTML1_TRANSITIONAL_THYMELEAF_4_DOC_TYPE_TRANSLATION =
        new DocTypeTranslation(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_TRANSITIONAL_THYMELEAFSPRING3_4_SYSTEMID, // SYSTEMID
                Standards.XHTML_1_TRANSITIONAL_PUBLICID,
                Standards.XHTML_1_TRANSITIONAL_SYSTEMID);

    public static final IDocTypeTranslation SPRING3_XHTML1_FRAMESET_THYMELEAF_4_DOC_TYPE_TRANSLATION =
        new DocTypeTranslation(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML1_FRAMESET_THYMELEAFSPRING3_4_SYSTEMID, // SYSTEMID
                Standards.XHTML_1_FRAMESET_PUBLICID,
                Standards.XHTML_1_FRAMESET_SYSTEMID);

    public static final IDocTypeTranslation SPRING3_XHTML11_THYMELEAF_4_DOC_TYPE_TRANSLATION =
        new DocTypeTranslation(
                DocTypeIdentifier.NONE, // PUBLICID
                XHTML11_THYMELEAFSPRING3_4_SYSTEMID, // SYSTEMID
                Standards.XHTML_11_PUBLICID,
                Standards.XHTML_11_SYSTEMID);

    

    
    public static final Set<IDocTypeTranslation> SPRING3_DOC_TYPE_TRANSLATIONS =
        Collections.unmodifiableSet(
                new LinkedHashSet<IDocTypeTranslation>(
                        Arrays.asList(new IDocTypeTranslation[] { 
                                SPRING3_XHTML1_STRICT_THYMELEAF_1_DOC_TYPE_TRANSLATION, 
                                SPRING3_XHTML1_TRANSITIONAL_THYMELEAF_1_DOC_TYPE_TRANSLATION, 
                                SPRING3_XHTML1_FRAMESET_THYMELEAF_1_DOC_TYPE_TRANSLATION, 
                                SPRING3_XHTML11_THYMELEAF_1_DOC_TYPE_TRANSLATION,
                                SPRING3_XHTML1_STRICT_THYMELEAF_2_DOC_TYPE_TRANSLATION, 
                                SPRING3_XHTML1_TRANSITIONAL_THYMELEAF_2_DOC_TYPE_TRANSLATION, 
                                SPRING3_XHTML1_FRAMESET_THYMELEAF_2_DOC_TYPE_TRANSLATION, 
                                SPRING3_XHTML11_THYMELEAF_2_DOC_TYPE_TRANSLATION,
                                SPRING3_XHTML1_STRICT_THYMELEAF_3_DOC_TYPE_TRANSLATION, 
                                SPRING3_XHTML1_TRANSITIONAL_THYMELEAF_3_DOC_TYPE_TRANSLATION, 
                                SPRING3_XHTML1_FRAMESET_THYMELEAF_3_DOC_TYPE_TRANSLATION, 
                                SPRING3_XHTML11_THYMELEAF_3_DOC_TYPE_TRANSLATION,
                                SPRING3_XHTML1_STRICT_THYMELEAF_4_DOC_TYPE_TRANSLATION,
                                SPRING3_XHTML1_TRANSITIONAL_THYMELEAF_4_DOC_TYPE_TRANSLATION,
                                SPRING3_XHTML1_FRAMESET_THYMELEAF_4_DOC_TYPE_TRANSLATION,
                                SPRING3_XHTML11_THYMELEAF_4_DOC_TYPE_TRANSLATION
                                })));
    
    

    

    
    
    static {
        final Set<IDocTypeResolutionEntry> newDocTypeResolutionEntries = new LinkedHashSet<IDocTypeResolutionEntry>(18, 1.0f);
        newDocTypeResolutionEntries.add(XHTML1_STRICT_THYMELEAFSPRING3_1_DOC_TYPE_RESOLUTION_ENTRY);
        newDocTypeResolutionEntries.add(XHTML1_TRANSITIONAL_THYMELEAFSPRING3_1_DOC_TYPE_RESOLUTION_ENTRY);
        newDocTypeResolutionEntries.add(XHTML1_FRAMESET_THYMELEAFSPRING3_1_DOC_TYPE_RESOLUTION_ENTRY);
        newDocTypeResolutionEntries.add(XHTML11_THYMELEAFSPRING3_1_DOC_TYPE_RESOLUTION_ENTRY);
        newDocTypeResolutionEntries.add(XHTML1_STRICT_THYMELEAFSPRING3_2_DOC_TYPE_RESOLUTION_ENTRY);
        newDocTypeResolutionEntries.add(XHTML1_TRANSITIONAL_THYMELEAFSPRING3_2_DOC_TYPE_RESOLUTION_ENTRY);
        newDocTypeResolutionEntries.add(XHTML1_FRAMESET_THYMELEAFSPRING3_2_DOC_TYPE_RESOLUTION_ENTRY);
        newDocTypeResolutionEntries.add(XHTML11_THYMELEAFSPRING3_2_DOC_TYPE_RESOLUTION_ENTRY);
        newDocTypeResolutionEntries.add(XHTML1_STRICT_THYMELEAFSPRING3_3_DOC_TYPE_RESOLUTION_ENTRY);
        newDocTypeResolutionEntries.add(XHTML1_TRANSITIONAL_THYMELEAFSPRING3_3_DOC_TYPE_RESOLUTION_ENTRY);
        newDocTypeResolutionEntries.add(XHTML1_FRAMESET_THYMELEAFSPRING3_3_DOC_TYPE_RESOLUTION_ENTRY);
        newDocTypeResolutionEntries.add(XHTML11_THYMELEAFSPRING3_3_DOC_TYPE_RESOLUTION_ENTRY);
        newDocTypeResolutionEntries.add(XHTML1_STRICT_THYMELEAFSPRING3_4_DOC_TYPE_RESOLUTION_ENTRY);
        newDocTypeResolutionEntries.add(XHTML1_TRANSITIONAL_THYMELEAFSPRING3_4_DOC_TYPE_RESOLUTION_ENTRY);
        newDocTypeResolutionEntries.add(XHTML1_FRAMESET_THYMELEAFSPRING3_4_DOC_TYPE_RESOLUTION_ENTRY);
        newDocTypeResolutionEntries.add(XHTML11_THYMELEAFSPRING3_4_DOC_TYPE_RESOLUTION_ENTRY);
        SPRING3_DOC_TYPE_RESOLUTION_ENTRIES = Collections.unmodifiableSet(newDocTypeResolutionEntries);
    }
    
    
    
    
    
    
    
    public StripesStandardDialect() {
        super();
        // LinkExpression bytecode modification
        if (!linkExpressionFixApplied) {
        	applyLinkExpressionFix();
        	linkExpressionFixApplied = true;
        }
        // Joda specific expression evaluator
        setVariableExpressionEvaluator(new StripesOgnlVariableExpressionEvaluator());
    }

    


    
    
    @Override
    public Set<IDocTypeTranslation> getDocTypeTranslations() {
        final Set<IDocTypeTranslation> docTypeTranslations = new LinkedHashSet<IDocTypeTranslation>(8, 1.0f);
        docTypeTranslations.addAll(SPRING3_DOC_TYPE_TRANSLATIONS);
        final Set<IDocTypeTranslation> additionalDocTypeTranslations = getAdditionalDocTypeTranslations();
        if (additionalDocTypeTranslations != null) {
            docTypeTranslations.addAll(additionalDocTypeTranslations);
        }
        return Collections.unmodifiableSet(docTypeTranslations);
    }
    
    @Override
    protected Set<IDocTypeTranslation> getAdditionalDocTypeTranslations() {
        return null;
    }
    
    

    
    @Override
    public Set<IDocTypeResolutionEntry> getSpecificDocTypeResolutionEntries() {
        final Set<IDocTypeResolutionEntry> docTypeResolutionEntries = new LinkedHashSet<IDocTypeResolutionEntry>(10, 1.0f);
        docTypeResolutionEntries.addAll(SPRING3_DOC_TYPE_RESOLUTION_ENTRIES);
        final Set<IDocTypeResolutionEntry> additionalDocTypeResolutionEntries = getAdditionalDocTypeResolutionEntries();
        if (additionalDocTypeResolutionEntries != null) {
            docTypeResolutionEntries.addAll(additionalDocTypeResolutionEntries);
        }
        return Collections.unmodifiableSet(docTypeResolutionEntries);
    }

    
    @Override
    protected Set<IDocTypeResolutionEntry> getAdditionalDocTypeResolutionEntries() {
        return null;
    }

    
    

    @Override
    public Set<IProcessor> getProcessors() {
        final Set<IProcessor> processors = StandardDialect.createStandardProcessorsSet();
        final Set<IProcessor> dialectAdditionalProcessors = getAdditionalProcessors();

        if (dialectAdditionalProcessors != null) {
            processors.addAll(dialectAdditionalProcessors);
        }
        
        // Add Stripes' custom processors
        processors.add(new StripesUseActionBeanElementProcessor());
        
        return new LinkedHashSet<IProcessor>(processors);
    }


    
    
    /**
     * Utility methods
     */
    
    private static void applyLinkExpressionFix() {
        try {
        	logger.debug("Trying to modify the LinkExpression class.");
            final ClassLoader classLoader = ClassLoaderUtils.getClassLoader(StripesStandardDialect.class);
            final ClassPool pool = new ClassPool(true);
            pool.insertClassPath(new LoaderClassPath(classLoader));

            // We must load by class name here instead of "LinkExpression.class.getName()" because
            // the latter would cause the class to be loaded and therefore it would not be
            // possible to modify it.
            final CtClass linkClass = pool.get("org.thymeleaf.standard.expression.LinkExpression");
            final CtClass fixClass = pool.get(StripesLinkExpression.class.getName());
            
            // Add a helper method for Stripes links
            final CtClass[] stripesHelperMethodParams = new CtClass[] { pool.get(String.class.getName()) };
            final CtMethod stripesHelperMethod = fixClass.getDeclaredMethod("linkLooksLikeStripesClass", stripesHelperMethodParams);
            linkClass.addMethod(CtNewMethod.copy(stripesHelperMethod, linkClass, null));
            
            // Swap the original executeLink method to inject Stripes related behaviour
            final CtClass[] originalMethodParams = new CtClass[] { 
            		pool.get("org.thymeleaf.Configuration"), 
            		pool.get("org.thymeleaf.context.IProcessingContext"), 
            		linkClass, 
            		pool.get("org.thymeleaf.standard.expression.StandardExpressionExecutionContext") 
            };
            final CtMethod originalMethod = linkClass.getDeclaredMethod("executeLink", originalMethodParams);
            final CtMethod fixMethod = fixClass.getDeclaredMethod("executeLink", originalMethodParams);
            originalMethod.setBody(fixMethod, null);
            
            // Pushes the class to the class loader, effectively making it
            // load the modified version instead of the original one. 
            linkClass.toClass(classLoader, null);
            logger.debug("Successful modification to the LinkExpression class.");
        } catch (final Exception e) {
            // Currently, an exception in this process only logs a warn and lets it continue
        	// This would render invalid action urls, no more harm, no less
            logger.warn("Thymeleaf Stripes Dialect was unable to modify the LinkExpression class at runtime, making " +
            		"all action urls in templates incorrect. Probably, LinkExpression class was already loaded or many " +
            		"instances of StripesStandardDialect have been created. Exception raised was {} : {}", e.getClass().getName(), e.getMessage());
        }
    }

}