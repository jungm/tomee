/**
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

import java.net.URL;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class WeatherServiceTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(WeatherService.class)
                .addAsWebInfResource(new StringAsset("<beans/>"), "beans.xml");
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
    public void testTimedMetric() {
        WebTarget webTarget = client.target(base.toExternalForm());

        webTarget.path("/weather/day/status").request().get();

        String prometheusMetric = webTarget.path("/metrics").request().get(String.class);

        String[] expected = {
                "# TYPE weather_day_status_seconds summary",
                "# HELP weather_day_status_seconds This metric shows the weather status of the day.",
                "weather_day_status_seconds{mp_scope=\"weather\",quantile=\"0.5\",}",
                "weather_day_status_seconds{mp_scope=\"weather\",quantile=\"0.75\",}",
                "weather_day_status_seconds{mp_scope=\"weather\",quantile=\"0.95\",}",
                "weather_day_status_seconds{mp_scope=\"weather\",quantile=\"0.98\",}",
                "weather_day_status_seconds{mp_scope=\"weather\",quantile=\"0.99\",}",
                "weather_day_status_seconds{mp_scope=\"weather\",quantile=\"0.999\",}",
                "weather_day_status_seconds_count{mp_scope=\"weather\",}",
                "weather_day_status_seconds_sum{mp_scope=\"weather\",}",
                "# TYPE weather_day_status_seconds_max gauge",
                "# HELP weather_day_status_seconds_max This metric shows the weather status of the day.",
                "weather_day_status_seconds_max{mp_scope=\"weather\",}"
        };

        Stream.of(expected)
                .forEach(text -> assertTrue("Expected: " + text + " to be present in " + prometheusMetric, prometheusMetric.contains(text)));
    }
}
