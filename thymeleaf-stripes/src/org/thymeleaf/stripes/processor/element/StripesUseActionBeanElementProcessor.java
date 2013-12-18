package org.thymeleaf.stripes.processor.element;

import java.util.MissingResourceException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.ActionResolver;
import net.sourceforge.stripes.controller.DispatcherHelper;
import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.controller.StripesFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.processor.ProcessorResult;
import org.thymeleaf.processor.element.AbstractElementProcessor;
import org.thymeleaf.stripes.context.StripesWebContext;

/**
 * Mimics Stripes' useActionBean tag
 */
public final class StripesUseActionBeanElementProcessor extends AbstractElementProcessor {

	private final static Logger logger = LoggerFactory.getLogger(StripesUseActionBeanElementProcessor.class);

    public static final int ATTR_PRECEDENCE = 100000;
    public static final String ELEMENT_NAME = "use-action-bean";


    public StripesUseActionBeanElementProcessor() {
        super(ELEMENT_NAME);
    }

    
    @Override
    public int getPrecedence() {
        return ATTR_PRECEDENCE;
    }

	@Override
	protected ProcessorResult processElement(Arguments arguments, Element element) {
		final String beanclass = element.getAttributeValue("class");
		final String event = element.getAttributeValue("event");
		final String var = element.getAttributeValue("var");
		final boolean validate = Boolean.valueOf(element.getAttributeValue("validate"));
		final boolean alwaysExecuteEvent = Boolean.valueOf(element.getAttributeValue("alwaysExecuteEvent"));
		final boolean executeResolution = Boolean.valueOf(element.getAttributeValue("executeResolution"));

		logger.debug("Processing useActionBean on class {} and event {}", beanclass, event);

		final StripesWebContext sctx = (StripesWebContext) arguments.getContext();
    	// Get the urlBinding from the class
    	final String binding;
    	try {
    		binding = sctx.resolveActionBeanUrlBinding(beanclass);
    		if (binding == null) throw new MissingResourceException("Resource not found", beanclass, null);
    	} catch(Exception e) {
    		throw new TemplateProcessingException("Could not resolve ActionBean class [" + beanclass + "]. Exception: " + e.getClass().getName() + " : " + e.getMessage());
    	}

        final Configuration config = StripesFilter.getConfiguration();
        final ActionResolver resolver = StripesFilter.getConfiguration().getActionResolver();
        final HttpServletRequest request = sctx.getHttpServletRequest();
        final HttpServletResponse response = sctx.getHttpServletResponse();
        Resolution resolution = null;
        ExecutionContext ctx = new ExecutionContext();
        // Check to see if the action bean already exists
        ActionBean actionBean = (ActionBean) sctx.getVariables().get(binding);
        boolean beanNotPresent = actionBean == null;
        try {
        	// Lookup the ActionBean if we don't already have it
            if (beanNotPresent) {
                ActionBeanContext tempContext = config.getActionBeanContextFactory().getContextInstance(request, response);
                tempContext.setServletContext(sctx.getServletContext());
                ctx.setLifecycleStage(LifecycleStage.ActionBeanResolution);
                ctx.setActionBeanContext(tempContext);

                // Run action bean resolution
                ctx.setInterceptors(config.getInterceptors(LifecycleStage.ActionBeanResolution));
                resolution = ctx.wrap( new Interceptor() {
                    public Resolution intercept(ExecutionContext ec) throws Exception {
                        ActionBean bean = resolver.getActionBean(ec.getActionBeanContext(), binding);
                        ec.setActionBean(bean);
                        return null;
                    }
                });
            } else {
                ctx.setActionBean(actionBean);
                ctx.setActionBeanContext(actionBean.getContext());
            }

            // Then, if and only if an event was specified, run handler resolution
            if (resolution == null && event != null && (beanNotPresent || alwaysExecuteEvent)) {
                ctx.setLifecycleStage(LifecycleStage.HandlerResolution);
                ctx.setInterceptors(config.getInterceptors(LifecycleStage.HandlerResolution));
                resolution = ctx.wrap( new Interceptor() {
                    public Resolution intercept(ExecutionContext ec) throws Exception {
                        ec.setHandler(resolver.getHandler(ec.getActionBean().getClass(), event));
                        ec.getActionBeanContext().setEventName(event);
                        return null;
                    }
                });
            }

            // Bind applicable request parameters to the ActionBean
            if (resolution == null && (beanNotPresent || validate)) {
                resolution = DispatcherHelper.doBindingAndValidation(ctx, validate);
            }

            // Run custom validations if we're validating
            if (resolution == null && validate) {
                String temp =  config.getBootstrapPropertyResolver().getProperty(DispatcherServlet.RUN_CUSTOM_VALIDATION_WHEN_ERRORS);
                boolean validateWhenErrors = temp != null && Boolean.valueOf(temp);

                resolution = DispatcherHelper.doCustomValidation(ctx, validateWhenErrors);
            }

            // Fill in any validation errors if they exist
            if (resolution == null && validate) {
                resolution = DispatcherHelper.handleValidationErrors(ctx);
            }

            // And (again) if an event was supplied, then run the handler
            if (resolution == null && event != null && (beanNotPresent || alwaysExecuteEvent)) {
                resolution = DispatcherHelper.invokeEventHandler(ctx);
            }

            DispatcherHelper.fillInValidationErrors(ctx);  // just in case!

            if (resolution != null && executeResolution) {
                DispatcherHelper.executeResolution(ctx, resolution);
            }
            
            // If a name was specified, bind the ActionBean into context variables
            if (var != null) {
            	logger.debug("Setting action bean [{}] to context variable [{}]", ctx.getActionBean(), var);
            	sctx.setVariable(var, ctx.getActionBean());
            }
        }
        catch(Exception e) {
    		throw new TemplateProcessingException("Could not execute ActionBean [" + beanclass + "]. Exception: " + e.getClass().getName() + " : " + e.getMessage());
        }
		// Delete the tag
		element.getParent().extractChild(element);
      	return ProcessorResult.OK;
	}

}