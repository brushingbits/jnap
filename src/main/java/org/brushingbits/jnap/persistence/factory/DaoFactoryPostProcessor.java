/*
 * DaoFactoryPostProcessor.java created on 2001-02-01
 *
 * Created by Brushing Bits Labs
 * http://www.brushingbits.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.brushingbits.jnap.persistence.factory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.brushingbits.jnap.bean.model.IndexedModel;
import org.brushingbits.jnap.bean.model.PersistentModel;
import org.brushingbits.jnap.persistence.hibernate.Dao;
import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.Assert;


/**
 * @author Daniel Rochetti
 * @since 1.0
 */
public class DaoFactoryPostProcessor implements BeanDefinitionRegistryPostProcessor, PriorityOrdered  {

	private int order = Ordered.LOWEST_PRECEDENCE - 1;

	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		Assert.isAssignable(DefaultListableBeanFactory.class, registry.getClass(),
				"The DaoFactoryPostProcessor only works within a DefaultListableBeanFactory capable" +
				"BeanFactory, your BeanDefinitionRegistry is " + registry.getClass());
		final DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) registry;

		// get already defined beans of type Dao
		final Set<Class<? extends PersistentModel>> alreadyDefinedDaos = new HashSet<Class<? extends PersistentModel>>();
		for (String beanName : beanFactory.getBeanNamesForType(Dao.class, true, false)) {
			Dao<? extends PersistentModel> dao = beanFactory.getBean(beanName, Dao.class);
			alreadyDefinedDaos.add(dao.getEntityClass());
		}

		for (String sessionFactoryName : beanFactory.getBeanNamesForType(SessionFactory.class)) {
			final SessionFactory sessionFactory = beanFactory.getBean(sessionFactoryName, SessionFactory.class);
			Map<String, ClassMetadata> entitiesMetadata = sessionFactory.getAllClassMetadata();
			CollectionUtils.forAllDo(entitiesMetadata.values(), new Closure() {
				public void execute(Object input) {
					Assert.isAssignable(ClassMetadata.class, input.getClass());
					Class<? extends PersistentModel> entityClass = ((ClassMetadata) input).getMappedClass(EntityMode.POJO);
					if (entityClass != null && !alreadyDefinedDaos.contains(entityClass)) {
						String daoName = entityClass.getSimpleName() + "Dao";
						daoName = Character.toLowerCase(daoName.charAt(0)) + daoName.substring(1);
						beanFactory.registerBeanDefinition(daoName,
								createDaoDefinition(entityClass, sessionFactory));
					}
				}
			});
		}
	}

	protected BeanDefinition createDaoDefinition(
			Class<? extends PersistentModel> entityClass,
			SessionFactory sessionFactory) {
		Class daoClass = IndexedModel.class.isAssignableFrom(entityClass)
				? GenericFullTextDao.class
				: GenericDao.class;
		return BeanDefinitionBuilder.genericBeanDefinition(daoClass)
				.addConstructorArgValue(entityClass)
				.addPropertyValue("sessionFactory", sessionFactory)
				.setScope(BeanDefinition.SCOPE_SINGLETON).getBeanDefinition();
	}

	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
	}

	public int getOrder() {
		return order;
	}

}
