/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.weld.tests.interceptors.defaultmethod;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 * @see WELD-2093
 */
@RunWith(Arquillian.class)
public class InterfaceDefaultMethodInterceptedTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InterfaceDefaultMethodInterceptedTest.class))
                .addPackage(InterfaceDefaultMethodInterceptedTest.class.getPackage());
    }

    @Test
    public void testInterception(Foo foo) {
        MissileInterceptor.INTERCEPTED.set(false);
        assertEquals(1, foo.ping());
        assertTrue(MissileInterceptor.INTERCEPTED.get());
        MissileInterceptor.INTERCEPTED.set(false);
        assertEquals(5, foo.pong());
        assertTrue(MissileInterceptor.INTERCEPTED.get());
    }

}
