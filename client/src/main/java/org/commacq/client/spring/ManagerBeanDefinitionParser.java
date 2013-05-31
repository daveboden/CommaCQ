package org.commacq.client.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import org.commacq.client.BeanCacheUpdater;
import org.commacq.client.Manager;

public class ManagerBeanDefinitionParser extends AbstractBeanDefinitionParser {

	Logger logger = LoggerFactory.getLogger(ManagerBeanDefinitionParser.class);
	
	private static String ATTRIBUTE_entityName = "entityName";
	private static String ATTRIBUTE_beanCacheUpdaterFactory = "beanCacheUpdaterFactory";
	private static String ATTRIBUTE_beanType = "beanType";
	private static String ATTRIBUTE_managerType = "managerType";
	private static String ATTRIBUTE_managerFactory = "managerFactory";
	
	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		final String entityName = element.getAttribute(ATTRIBUTE_entityName); //Required attribute

		final String beanCacheUpdaterFactory = element.getAttribute(ATTRIBUTE_beanCacheUpdaterFactory);
		
		//TODO allow this to be overridden by an attribute
		
		BeanDefinitionBuilder beanCacheUpdaterBuilder = BeanDefinitionBuilder.genericBeanDefinition(BeanCacheUpdater.class);
		AbstractBeanDefinition beanCacheUpdaterBuilderRaw = beanCacheUpdaterBuilder.getRawBeanDefinition();
		beanCacheUpdaterBuilderRaw.setFactoryBeanName(beanCacheUpdaterFactory);
		beanCacheUpdaterBuilderRaw.setFactoryMethodName("createBeanCacheUpdater");
		beanCacheUpdaterBuilder.addConstructorArgValue(entityName);
		
		final String beanTypeOverride = element.getAttribute(ATTRIBUTE_beanType);
		if(StringUtils.hasText(beanTypeOverride)) {
			beanCacheUpdaterBuilder.addConstructorArgValue(beanTypeOverride);
			logger.debug("Calling the (entityName, beanType) factory method");
		} else {
			logger.debug("Calling the (entityName) factory method and letting the BeanCacheUpdater decide the bean type");
		}
		
		BeanDefinition beanCacheUpdaterBeanDefinition = beanCacheUpdaterBuilder.getBeanDefinition();
		
		//Register as a spring bean so that, if required, the beanCacheUpdater can benefit from resource injection.
		//Must be registered with a unique name so that we can define multiple managers for the same entityName
		final String beanCacheUpdaterId = BeanDefinitionReaderUtils.generateBeanName(beanCacheUpdaterBeanDefinition, parserContext.getRegistry());
		logger.debug("Registering a beanCacheUpdater in Spring as: {}", beanCacheUpdaterId);
		BeanDefinitionHolder beanCacheUpdaterHolder = new BeanDefinitionHolder(beanCacheUpdaterBuilder.getBeanDefinition(), beanCacheUpdaterId);		
		registerBeanDefinition(beanCacheUpdaterHolder, parserContext.getRegistry());

		
		BeanDefinitionBuilder managerBuilder = BeanDefinitionBuilder.genericBeanDefinition(Manager.class);
		AbstractBeanDefinition managerBuilderRaw = managerBuilder.getRawBeanDefinition();
		
		//Has a defaulted value in the xsd
		final String managerFactoryBeanId = element.getAttribute(ATTRIBUTE_managerFactory);
		
		managerBuilderRaw.setFactoryBeanName(managerFactoryBeanId);
		managerBuilderRaw.setFactoryMethodName("createManager");
		
		managerBuilder.addConstructorArgReference(beanCacheUpdaterId);
		
		final String managerTypeOverride = element.getAttribute(ATTRIBUTE_managerType);
		if(StringUtils.hasText(managerTypeOverride)) {
			logger.debug("Manager type has been specified as: {}", managerTypeOverride);
			managerBuilder.addConstructorArgValue(managerTypeOverride);
			logger.debug("Calling the managerFactory's (beanCacheUpdater, managerType) factory method");			
		} else {
			logger.debug("Calling the managerFactory's (beanCacheUpdater) factory method");
		}
		
		return managerBuilder.getBeanDefinition();
	}
	
	/**
	 * Default the id of the customer entityName to customerManager
	 * Use the id if specified.
	 */
	@Override
	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext)
			throws BeanDefinitionStoreException {
		
		String id = element.getAttribute(ID_ATTRIBUTE);
		if (!StringUtils.hasText(id) && shouldGenerateIdAsFallback()) {
			id = element.getAttribute(ATTRIBUTE_entityName) + "Manager";
		}
		return id;
	}
	
	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}
}
