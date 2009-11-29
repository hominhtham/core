/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.environment.se;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.weld.environment.se.beans.InstanceManager;
import org.jboss.weld.environment.se.discovery.SEWeldDeployment;
import org.jboss.weld.environment.se.util.Reflections;
import org.jboss.weld.environment.se.util.WeldManagerUtils;
import org.jboss.weld.manager.api.WeldManager;

/**
 * An alternative means of booting Weld form an arbitrary main method within an
 * SE application, <em>without</em> using the built-in ContainerInitialized event.
 * Typical usage of the API looks like this:
 * <code>
 * Weld weld = new Weld().initialize();
 * weld.instance().select(Foo.class).get();
 * weld.event().select(Bar.class).fire( new Bar() );
 * weld.shutdown();
 * </code>
 *
 * @author Peter Royle
 */
public class Weld
{

   private static final String BOOTSTRAP_IMPL_CLASS_NAME = "org.jboss.weld.bootstrap.WeldBootstrap";
   private final Bootstrap bootstrap;
   private final BeanStore applicationBeanStore;
   private WeldManager manager;
   private InstanceManager instanceManager;

   public Weld()
   {
      try
      {
         bootstrap = Reflections.newInstance(BOOTSTRAP_IMPL_CLASS_NAME, Bootstrap.class);
      } catch (Exception e)
      {
         throw new IllegalStateException("Error loading Weld bootstrap, check that Weld is on the classpath", e);
      }
      this.applicationBeanStore = new ConcurrentHashMapBeanStore();
   }

   public Weld initialize()
   {

      SEWeldDeployment deployment = new SEWeldDeployment()
      {
      };
      bootstrap.startContainer(Environments.SE, deployment, this.applicationBeanStore);
      final BeanDeploymentArchive mainBeanDepArch = deployment.getBeanDeploymentArchives().get(0);
      this.manager = bootstrap.getManager(mainBeanDepArch);
      bootstrap.startInitialization();
      bootstrap.deployBeans();
      WeldManagerUtils.getInstanceByType(manager, ShutdownManager.class).setBootstrap(bootstrap);
      bootstrap.validateBeans();
      bootstrap.endInitialization();

      instanceManager = WeldManagerUtils.getInstanceByType(manager, InstanceManager.class);

      return this;
   }

   public Instance<Object> instance()
   {
      return instanceManager.getInstances();
   }

   public Instance<Event> event()
   {
      return instanceManager.getEvents();
   }

   public BeanManager getBeanManager()
   {
      return manager;
   }

   /**
    * Convenience method for shutting down the container.
    */
   public void shutdown() {
      WeldManagerUtils.getInstanceByType(manager, ShutdownManager.class).shutdown();
   }
}
