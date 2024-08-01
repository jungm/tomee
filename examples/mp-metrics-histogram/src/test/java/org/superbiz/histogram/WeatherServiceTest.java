/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.histogram;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class WeatherServiceTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(WeatherService.class)
                .addAsWebInfResource(new StringAsset("<beans/>"),
                        "beans.xml");
        return webArchive;
    }

    @ArquillianResource
    private URL base;

    private Client client;

    @Before
    public void before() {
        this.client = ClientBuilder.newClient();
    }

    @After
    public void after() {
        this.client.close();
    }

    @Test
    public void testHistogramMetric() {
        WebTarget webTarget = this.client.target(this.base.toExternalForm());
        final String message = webTarget.path("/weather/histogram").request().get(String.class);

        final String metric = webTarget.path("/metrics")
                .queryParam("scope", "application")
                .request(MediaType.TEXT_PLAIN).get(String.class);

        assertEquals("""
                # HELP temperatures A histogram metrics example.
                # TYPE temperatures summary
                temperatures{mp_scope="application",quantile="0.5",} 45.015625
                temperatures{mp_scope="application",quantile="0.75",} 48.015625
                temperatures{mp_scope="application",quantile="0.95",} 55.015625
                temperatures{mp_scope="application",quantile="0.98",} 55.015625
                temperatures{mp_scope="application",quantile="0.99",} 55.015625
                temperatures{mp_scope="application",quantile="0.999",} 55.015625
                temperatures_count{mp_scope="application",} 15.0
                temperatures_sum{mp_scope="application",} 666.0
                # HELP temperatures_max A histogram metrics example.
                # TYPE temperatures_max gauge
                temperatures_max{mp_scope="application",} 55.0
                """, metric);
    }
}
