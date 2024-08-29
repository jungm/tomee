/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.jee;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.AroundInvoke$JAXB.readAroundInvoke;
import static org.apache.openejb.jee.AroundInvoke$JAXB.writeAroundInvoke;
import static org.apache.openejb.jee.AroundTimeout$JAXB.readAroundTimeout;
import static org.apache.openejb.jee.AroundTimeout$JAXB.writeAroundTimeout;
import static org.apache.openejb.jee.AsyncMethod$JAXB.readAsyncMethod;
import static org.apache.openejb.jee.AsyncMethod$JAXB.writeAsyncMethod;
import static org.apache.openejb.jee.ConcurrencyManagementType$JAXB.parseConcurrencyManagementType;
import static org.apache.openejb.jee.ConcurrencyManagementType$JAXB.toStringConcurrencyManagementType;
import static org.apache.openejb.jee.ConcurrentMethod$JAXB.readConcurrentMethod;
import static org.apache.openejb.jee.ConcurrentMethod$JAXB.writeConcurrentMethod;
import static org.apache.openejb.jee.ContextService$JAXB.readContextService;
import static org.apache.openejb.jee.ContextService$JAXB.writeContextService;
import static org.apache.openejb.jee.DataSource$JAXB.readDataSource;
import static org.apache.openejb.jee.DataSource$JAXB.writeDataSource;
import static org.apache.openejb.jee.EjbLocalRef$JAXB.readEjbLocalRef;
import static org.apache.openejb.jee.EjbLocalRef$JAXB.writeEjbLocalRef;
import static org.apache.openejb.jee.EjbRef$JAXB.readEjbRef;
import static org.apache.openejb.jee.EjbRef$JAXB.writeEjbRef;
import static org.apache.openejb.jee.Empty$JAXB.readEmpty;
import static org.apache.openejb.jee.Empty$JAXB.writeEmpty;
import static org.apache.openejb.jee.EnvEntry$JAXB.readEnvEntry;
import static org.apache.openejb.jee.EnvEntry$JAXB.writeEnvEntry;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.InitMethod$JAXB.readInitMethod;
import static org.apache.openejb.jee.InitMethod$JAXB.writeInitMethod;
import static org.apache.openejb.jee.JMSConnectionFactory$JAXB.readJMSConnectionFactory;
import static org.apache.openejb.jee.JMSConnectionFactory$JAXB.writeJMSConnectionFactory;
import static org.apache.openejb.jee.JMSDestination$JAXB.readJMSDestination;
import static org.apache.openejb.jee.JMSDestination$JAXB.writeJMSDestination;
import static org.apache.openejb.jee.LifecycleCallback$JAXB.readLifecycleCallback;
import static org.apache.openejb.jee.LifecycleCallback$JAXB.writeLifecycleCallback;
import static org.apache.openejb.jee.MessageDestinationRef$JAXB.readMessageDestinationRef;
import static org.apache.openejb.jee.MessageDestinationRef$JAXB.writeMessageDestinationRef;
import static org.apache.openejb.jee.NamedMethod$JAXB.readNamedMethod;
import static org.apache.openejb.jee.NamedMethod$JAXB.writeNamedMethod;
import static org.apache.openejb.jee.PersistenceContextRef$JAXB.readPersistenceContextRef;
import static org.apache.openejb.jee.PersistenceContextRef$JAXB.writePersistenceContextRef;
import static org.apache.openejb.jee.PersistenceUnitRef$JAXB.readPersistenceUnitRef;
import static org.apache.openejb.jee.PersistenceUnitRef$JAXB.writePersistenceUnitRef;
import static org.apache.openejb.jee.RemoveMethod$JAXB.readRemoveMethod;
import static org.apache.openejb.jee.RemoveMethod$JAXB.writeRemoveMethod;
import static org.apache.openejb.jee.ResourceEnvRef$JAXB.readResourceEnvRef;
import static org.apache.openejb.jee.ResourceEnvRef$JAXB.writeResourceEnvRef;
import static org.apache.openejb.jee.ResourceRef$JAXB.readResourceRef;
import static org.apache.openejb.jee.ResourceRef$JAXB.writeResourceRef;
import static org.apache.openejb.jee.SecurityIdentity$JAXB.readSecurityIdentity;
import static org.apache.openejb.jee.SecurityIdentity$JAXB.writeSecurityIdentity;
import static org.apache.openejb.jee.SecurityRoleRef$JAXB.readSecurityRoleRef;
import static org.apache.openejb.jee.SecurityRoleRef$JAXB.writeSecurityRoleRef;
import static org.apache.openejb.jee.ServiceRef$JAXB.readServiceRef;
import static org.apache.openejb.jee.ServiceRef$JAXB.writeServiceRef;
import static org.apache.openejb.jee.SessionType$JAXB.parseSessionType;
import static org.apache.openejb.jee.SessionType$JAXB.toStringSessionType;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;
import static org.apache.openejb.jee.Timeout$JAXB.readTimeout;
import static org.apache.openejb.jee.Timeout$JAXB.writeTimeout;
import static org.apache.openejb.jee.Timer$JAXB.readTimer;
import static org.apache.openejb.jee.Timer$JAXB.writeTimer;
import static org.apache.openejb.jee.TransactionType$JAXB.parseTransactionType;
import static org.apache.openejb.jee.TransactionType$JAXB.toStringTransactionType;

@SuppressWarnings({
    "StringEquality"
})
public class StatefulBean$JAXB
    extends JAXBObject<StatefulBean>
{


    public StatefulBean$JAXB() {
        super(StatefulBean.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "statefulBean".intern()), Text$JAXB.class, Icon$JAXB.class, Empty$JAXB.class, SessionType$JAXB.class, Timeout$JAXB.class, NamedMethod$JAXB.class, Timer$JAXB.class, ConcurrencyManagementType$JAXB.class, ConcurrentMethod$JAXB.class, InitMethod$JAXB.class, RemoveMethod$JAXB.class, AsyncMethod$JAXB.class, TransactionType$JAXB.class, AroundInvoke$JAXB.class, AroundTimeout$JAXB.class, EnvEntry$JAXB.class, EjbRef$JAXB.class, EjbLocalRef$JAXB.class, ServiceRef$JAXB.class, ResourceRef$JAXB.class, ResourceEnvRef$JAXB.class, MessageDestinationRef$JAXB.class, PersistenceContextRef$JAXB.class, PersistenceUnitRef$JAXB.class, LifecycleCallback$JAXB.class, DataSource$JAXB.class, JMSConnectionFactory$JAXB.class, JMSDestination$JAXB.class, SecurityRoleRef$JAXB.class, SecurityIdentity$JAXB.class, ContextService$JAXB.class);
    }

    public static StatefulBean readStatefulBean(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeStatefulBean(XoXMLStreamWriter writer, StatefulBean statefulBean, RuntimeContext context)
        throws Exception
    {
        _write(writer, statefulBean, context);
    }

    public void write(XoXMLStreamWriter writer, StatefulBean statefulBean, RuntimeContext context)
        throws Exception
    {
        _write(writer, statefulBean, context);
    }

    public static final StatefulBean _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        StatefulBean statefulBean = new StatefulBean();
        context.beforeUnmarshal(statefulBean, org.metatype.sxc.jaxb.LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        LinkedHashSet<String> businessLocal = null;
        LinkedHashSet<String> businessRemote = null;
        List<Timer> timer = null;
        List<ConcurrentMethod> concurrentMethod = null;
        List<InitMethod> initMethod = null;
        List<RemoveMethod> removeMethod = null;
        List<AsyncMethod> asyncMethod = null;
        List<AroundInvoke> aroundInvoke = null;
        List<AroundTimeout> aroundTimeout = null;
        KeyedCollection<String, EnvEntry> envEntry = null;
        KeyedCollection<String, EjbRef> ejbRef = null;
        KeyedCollection<String, EjbLocalRef> ejbLocalRef = null;
        KeyedCollection<String, ServiceRef> serviceRef = null;
        KeyedCollection<String, ResourceRef> resourceRef = null;
        KeyedCollection<String, ResourceEnvRef> resourceEnvRef = null;
        KeyedCollection<String, MessageDestinationRef> messageDestinationRef = null;
        KeyedCollection<String, PersistenceContextRef> persistenceContextRef = null;
        KeyedCollection<String, PersistenceUnitRef> persistenceUnitRef = null;
        List<org.apache.openejb.jee.LifecycleCallback> postConstruct = null;
        List<org.apache.openejb.jee.LifecycleCallback> preDestroy = null;
        KeyedCollection<String, DataSource> dataSource = null;
        KeyedCollection<String, JMSConnectionFactory> jmsConnectionFactories = null;
        KeyedCollection<String, JMSDestination> jmsDestinations = null;
        List<org.apache.openejb.jee.LifecycleCallback> postActivate = null;
        List<org.apache.openejb.jee.LifecycleCallback> prePassivate = null;
        List<SecurityRoleRef> securityRoleRef = null;
        KeyedCollection<String, ContextService> contextService = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("statefulBean"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, StatefulBean.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, statefulBean);
                statefulBean.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("description" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<>();
                }
                descriptions.add(descriptionsItem);
            } else if (("display-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: displayNames
                Text displayNamesItem = readText(elementReader, context);
                if (displayNames == null) {
                    displayNames = new ArrayList<>();
                }
                displayNames.add(displayNamesItem);
            } else if (("icon" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: icon
                Icon iconItem = readIcon(elementReader, context);
                if (icon == null) {
                    icon = statefulBean.icon;
                    if (icon!= null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<>();
                    }
                }
                icon.add(iconItem);
            } else if (("ejb-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbName
                String ejbNameRaw = elementReader.getElementText();

                String ejbName;
                try {
                    ejbName = Adapters.collapsedStringAdapterAdapter.unmarshal(ejbNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                statefulBean.ejbName = ejbName;
            } else if (("mapped-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: mappedName
                String mappedNameRaw = elementReader.getElementText();

                String mappedName;
                try {
                    mappedName = Adapters.collapsedStringAdapterAdapter.unmarshal(mappedNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                statefulBean.mappedName = mappedName;
            } else if (("home" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: home
                String homeRaw = elementReader.getElementText();

                String home;
                try {
                    home = Adapters.collapsedStringAdapterAdapter.unmarshal(homeRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                statefulBean.home = home;
            } else if (("remote" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: remote
                String remoteRaw = elementReader.getElementText();

                String remote;
                try {
                    remote = Adapters.collapsedStringAdapterAdapter.unmarshal(remoteRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                statefulBean.remote = remote;
            } else if (("local-home" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: localHome
                String localHomeRaw = elementReader.getElementText();

                String localHome;
                try {
                    localHome = Adapters.collapsedStringAdapterAdapter.unmarshal(localHomeRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                statefulBean.localHome = localHome;
            } else if (("local" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: local
                String localRaw = elementReader.getElementText();

                String local;
                try {
                    local = Adapters.collapsedStringAdapterAdapter.unmarshal(localRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                statefulBean.local = local;
            } else if (("business-local" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: businessLocal
                String businessLocalItemRaw = elementReader.getElementText();

                String businessLocalItem;
                try {
                    businessLocalItem = Adapters.collapsedStringAdapterAdapter.unmarshal(businessLocalItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (businessLocal == null) {
                    businessLocal = statefulBean.businessLocal;
                    if (businessLocal!= null) {
                        businessLocal.clear();
                    } else {
                        businessLocal = new LinkedHashSet<>();
                    }
                }
                businessLocal.add(businessLocalItem);
            } else if (("business-remote" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: businessRemote
                String businessRemoteItemRaw = elementReader.getElementText();

                String businessRemoteItem;
                try {
                    businessRemoteItem = Adapters.collapsedStringAdapterAdapter.unmarshal(businessRemoteItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (businessRemote == null) {
                    businessRemote = statefulBean.businessRemote;
                    if (businessRemote!= null) {
                        businessRemote.clear();
                    } else {
                        businessRemote = new LinkedHashSet<>();
                    }
                }
                businessRemote.add(businessRemoteItem);
            } else if (("local-bean" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: localBean
                Empty localBean = readEmpty(elementReader, context);
                statefulBean.localBean = localBean;
            } else if (("service-endpoint" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: serviceEndpoint
                String serviceEndpointRaw = elementReader.getElementText();

                String serviceEndpoint;
                try {
                    serviceEndpoint = Adapters.collapsedStringAdapterAdapter.unmarshal(serviceEndpointRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                statefulBean.serviceEndpoint = serviceEndpoint;
            } else if (("ejb-class" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbClass
                String ejbClassRaw = elementReader.getElementText();

                String ejbClass;
                try {
                    ejbClass = Adapters.collapsedStringAdapterAdapter.unmarshal(ejbClassRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                statefulBean.ejbClass = ejbClass;
            } else if (("session-type" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: sessionType
                SessionType sessionType = parseSessionType(elementReader, context, elementReader.getElementText());
                if (sessionType!= null) {
                    statefulBean.sessionType = sessionType;
                }
            } else if (("stateful-timeout" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: statefulTimeout
                Timeout statefulTimeout = readTimeout(elementReader, context);
                statefulBean.statefulTimeout = statefulTimeout;
            } else if (("timeout-method" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: timeoutMethod
                NamedMethod timeoutMethod = readNamedMethod(elementReader, context);
                statefulBean.timeoutMethod = timeoutMethod;
            } else if (("timer" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: timer
                Timer timerItem = readTimer(elementReader, context);
                if (timer == null) {
                    timer = statefulBean.timer;
                    if (timer!= null) {
                        timer.clear();
                    } else {
                        timer = new ArrayList<>();
                    }
                }
                timer.add(timerItem);
            } else if (("init-on-startup" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: initOnStartup
                Boolean initOnStartup = ("1".equals(elementReader.getElementText())||"true".equals(elementReader.getElementText()));
                statefulBean.initOnStartup = initOnStartup;
            } else if (("concurrency-management-type" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: concurrencyManagementType
                ConcurrencyManagementType concurrencyManagementType = parseConcurrencyManagementType(elementReader, context, elementReader.getElementText());
                if (concurrencyManagementType!= null) {
                    statefulBean.concurrencyManagementType = concurrencyManagementType;
                }
            } else if (("concurrent-method" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: concurrentMethod
                ConcurrentMethod concurrentMethodItem = readConcurrentMethod(elementReader, context);
                if (concurrentMethod == null) {
                    concurrentMethod = statefulBean.concurrentMethod;
                    if (concurrentMethod!= null) {
                        concurrentMethod.clear();
                    } else {
                        concurrentMethod = new ArrayList<>();
                    }
                }
                concurrentMethod.add(concurrentMethodItem);
            } else if (("depends-on" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT WRAPPER: dependsOn
                _readDependsOn(elementReader, context, statefulBean);
            } else if (("init-method" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: initMethod
                InitMethod initMethodItem = readInitMethod(elementReader, context);
                if (initMethod == null) {
                    initMethod = statefulBean.initMethod;
                    if (initMethod!= null) {
                        initMethod.clear();
                    } else {
                        initMethod = new ArrayList<>();
                    }
                }
                initMethod.add(initMethodItem);
            } else if (("remove-method" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: removeMethod
                RemoveMethod removeMethodItem = readRemoveMethod(elementReader, context);
                if (removeMethod == null) {
                    removeMethod = statefulBean.removeMethod;
                    if (removeMethod!= null) {
                        removeMethod.clear();
                    } else {
                        removeMethod = new ArrayList<>();
                    }
                }
                removeMethod.add(removeMethodItem);
            } else if (("async-method" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: asyncMethod
                AsyncMethod asyncMethodItem = readAsyncMethod(elementReader, context);
                if (asyncMethod == null) {
                    asyncMethod = statefulBean.asyncMethod;
                    if (asyncMethod!= null) {
                        asyncMethod.clear();
                    } else {
                        asyncMethod = new ArrayList<>();
                    }
                }
                asyncMethod.add(asyncMethodItem);
            } else if (("transaction-type" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: transactionType
                TransactionType transactionType = parseTransactionType(elementReader, context, elementReader.getElementText());
                if (transactionType!= null) {
                    statefulBean.transactionType = transactionType;
                }
            } else if (("after-begin-method" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: afterBeginMethod
                NamedMethod afterBeginMethod = readNamedMethod(elementReader, context);
                try {
                    statefulBean.setAfterBeginMethod(afterBeginMethod);
                } catch (Exception e) {
                    context.setterError(reader, SessionBean.class, "setAfterBeginMethod", NamedMethod.class, e);
                }
            } else if (("before-completion-method" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: beforeCompletionMethod
                NamedMethod beforeCompletionMethod = readNamedMethod(elementReader, context);
                try {
                    statefulBean.setBeforeCompletionMethod(beforeCompletionMethod);
                } catch (Exception e) {
                    context.setterError(reader, SessionBean.class, "setBeforeCompletionMethod", NamedMethod.class, e);
                }
            } else if (("after-completion-method" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: afterCompletionMethod
                NamedMethod afterCompletionMethod = readNamedMethod(elementReader, context);
                try {
                    statefulBean.setAfterCompletionMethod(afterCompletionMethod);
                } catch (Exception e) {
                    context.setterError(reader, SessionBean.class, "setAfterCompletionMethod", NamedMethod.class, e);
                }
            } else if (("around-invoke" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: aroundInvoke
                AroundInvoke aroundInvokeItem = readAroundInvoke(elementReader, context);
                if (aroundInvoke == null) {
                    aroundInvoke = statefulBean.aroundInvoke;
                    if (aroundInvoke!= null) {
                        aroundInvoke.clear();
                    } else {
                        aroundInvoke = new ArrayList<>();
                    }
                }
                aroundInvoke.add(aroundInvokeItem);
            } else if (("around-timeout" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: aroundTimeout
                AroundTimeout aroundTimeoutItem = readAroundTimeout(elementReader, context);
                if (aroundTimeout == null) {
                    aroundTimeout = statefulBean.aroundTimeout;
                    if (aroundTimeout!= null) {
                        aroundTimeout.clear();
                    } else {
                        aroundTimeout = new ArrayList<>();
                    }
                }
                aroundTimeout.add(aroundTimeoutItem);
            } else if (("env-entry" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: envEntry
                EnvEntry envEntryItem = readEnvEntry(elementReader, context);
                if (envEntry == null) {
                    envEntry = statefulBean.envEntry;
                    if (envEntry!= null) {
                        envEntry.clear();
                    } else {
                        envEntry = new KeyedCollection<>();
                    }
                }
                envEntry.add(envEntryItem);
            } else if (("ejb-ref" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbRef
                EjbRef ejbRefItem = readEjbRef(elementReader, context);
                if (ejbRef == null) {
                    ejbRef = statefulBean.ejbRef;
                    if (ejbRef!= null) {
                        ejbRef.clear();
                    } else {
                        ejbRef = new KeyedCollection<>();
                    }
                }
                ejbRef.add(ejbRefItem);
            } else if (("ejb-local-ref" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbLocalRef
                EjbLocalRef ejbLocalRefItem = readEjbLocalRef(elementReader, context);
                if (ejbLocalRef == null) {
                    ejbLocalRef = statefulBean.ejbLocalRef;
                    if (ejbLocalRef!= null) {
                        ejbLocalRef.clear();
                    } else {
                        ejbLocalRef = new KeyedCollection<>();
                    }
                }
                ejbLocalRef.add(ejbLocalRefItem);
            } else if (("service-ref" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: serviceRef
                ServiceRef serviceRefItem = readServiceRef(elementReader, context);
                if (serviceRef == null) {
                    serviceRef = statefulBean.serviceRef;
                    if (serviceRef!= null) {
                        serviceRef.clear();
                    } else {
                        serviceRef = new KeyedCollection<>();
                    }
                }
                serviceRef.add(serviceRefItem);
            } else if (("resource-ref" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resourceRef
                ResourceRef resourceRefItem = readResourceRef(elementReader, context);
                if (resourceRef == null) {
                    resourceRef = statefulBean.resourceRef;
                    if (resourceRef!= null) {
                        resourceRef.clear();
                    } else {
                        resourceRef = new KeyedCollection<>();
                    }
                }
                resourceRef.add(resourceRefItem);
            } else if (("resource-env-ref" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resourceEnvRef
                ResourceEnvRef resourceEnvRefItem = readResourceEnvRef(elementReader, context);
                if (resourceEnvRef == null) {
                    resourceEnvRef = statefulBean.resourceEnvRef;
                    if (resourceEnvRef!= null) {
                        resourceEnvRef.clear();
                    } else {
                        resourceEnvRef = new KeyedCollection<>();
                    }
                }
                resourceEnvRef.add(resourceEnvRefItem);
            } else if (("message-destination-ref" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageDestinationRef
                MessageDestinationRef messageDestinationRefItem = readMessageDestinationRef(elementReader, context);
                if (messageDestinationRef == null) {
                    messageDestinationRef = statefulBean.messageDestinationRef;
                    if (messageDestinationRef!= null) {
                        messageDestinationRef.clear();
                    } else {
                        messageDestinationRef = new KeyedCollection<>();
                    }
                }
                messageDestinationRef.add(messageDestinationRefItem);
            } else if (("persistence-context-ref" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: persistenceContextRef
                PersistenceContextRef persistenceContextRefItem = readPersistenceContextRef(elementReader, context);
                if (persistenceContextRef == null) {
                    persistenceContextRef = statefulBean.persistenceContextRef;
                    if (persistenceContextRef!= null) {
                        persistenceContextRef.clear();
                    } else {
                        persistenceContextRef = new KeyedCollection<>();
                    }
                }
                persistenceContextRef.add(persistenceContextRefItem);
            } else if (("persistence-unit-ref" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: persistenceUnitRef
                PersistenceUnitRef persistenceUnitRefItem = readPersistenceUnitRef(elementReader, context);
                if (persistenceUnitRef == null) {
                    persistenceUnitRef = statefulBean.persistenceUnitRef;
                    if (persistenceUnitRef!= null) {
                        persistenceUnitRef.clear();
                    } else {
                        persistenceUnitRef = new KeyedCollection<>();
                    }
                }
                persistenceUnitRef.add(persistenceUnitRefItem);
            } else if (("post-construct" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: postConstruct
                org.apache.openejb.jee.LifecycleCallback postConstructItem = readLifecycleCallback(elementReader, context);
                if (postConstruct == null) {
                    postConstruct = statefulBean.postConstruct;
                    if (postConstruct!= null) {
                        postConstruct.clear();
                    } else {
                        postConstruct = new ArrayList<>();
                    }
                }
                postConstruct.add(postConstructItem);
            } else if (("pre-destroy" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: preDestroy
                org.apache.openejb.jee.LifecycleCallback preDestroyItem = readLifecycleCallback(elementReader, context);
                if (preDestroy == null) {
                    preDestroy = statefulBean.preDestroy;
                    if (preDestroy!= null) {
                        preDestroy.clear();
                    } else {
                        preDestroy = new ArrayList<>();
                    }
                }
                preDestroy.add(preDestroyItem);
            } else if (("data-source" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: dataSource
                DataSource dataSourceItem = readDataSource(elementReader, context);
                if (dataSource == null) {
                    dataSource = statefulBean.dataSource;
                    if (dataSource!= null) {
                        dataSource.clear();
                    } else {
                        dataSource = new KeyedCollection<>();
                    }
                }
                dataSource.add(dataSourceItem);
            } else if (("jms-connection-factory" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: jmsConnectionFactories
                JMSConnectionFactory jmsConnectionFactoriesItem = readJMSConnectionFactory(elementReader, context);
                if (jmsConnectionFactories == null) {
                    jmsConnectionFactories = statefulBean.jmsConnectionFactories;
                    if (jmsConnectionFactories!= null) {
                        jmsConnectionFactories.clear();
                    } else {
                        jmsConnectionFactories = new KeyedCollection<>();
                    }
                }
                jmsConnectionFactories.add(jmsConnectionFactoriesItem);
            } else if (("jms-destination" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: jmsDestinations
                JMSDestination jmsDestinationsItem = readJMSDestination(elementReader, context);
                if (jmsDestinations == null) {
                    jmsDestinations = statefulBean.jmsDestinations;
                    if (jmsDestinations!= null) {
                        jmsDestinations.clear();
                    } else {
                        jmsDestinations = new KeyedCollection<>();
                    }
                }
                jmsDestinations.add(jmsDestinationsItem);
            } else if (("post-activate" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: postActivate
                org.apache.openejb.jee.LifecycleCallback postActivateItem = readLifecycleCallback(elementReader, context);
                if (postActivate == null) {
                    postActivate = statefulBean.postActivate;
                    if (postActivate!= null) {
                        postActivate.clear();
                    } else {
                        postActivate = new ArrayList<>();
                    }
                }
                postActivate.add(postActivateItem);
            } else if (("pre-passivate" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: prePassivate
                org.apache.openejb.jee.LifecycleCallback prePassivateItem = readLifecycleCallback(elementReader, context);
                if (prePassivate == null) {
                    prePassivate = statefulBean.prePassivate;
                    if (prePassivate!= null) {
                        prePassivate.clear();
                    } else {
                        prePassivate = new ArrayList<>();
                    }
                }
                prePassivate.add(prePassivateItem);
            } else if (("security-role-ref" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: securityRoleRef
                SecurityRoleRef securityRoleRefItem = readSecurityRoleRef(elementReader, context);
                if (securityRoleRef == null) {
                    securityRoleRef = statefulBean.securityRoleRef;
                    if (securityRoleRef!= null) {
                        securityRoleRef.clear();
                    } else {
                        securityRoleRef = new ArrayList<>();
                    }
                }
                securityRoleRef.add(securityRoleRefItem);
            } else if (("security-identity" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: securityIdentity
                SecurityIdentity securityIdentity = readSecurityIdentity(elementReader, context);
                statefulBean.securityIdentity = securityIdentity;
            } else if (("passivation-capable" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: passivationCapable
                Boolean passivationCapable = ("1".equals(elementReader.getElementText())||"true".equals(elementReader.getElementText()));
                statefulBean.passivationCapable = passivationCapable;
            } else if (("context-service" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: contextService
                ContextService contextServiceItem = readContextService(elementReader, context);
                if (contextService == null) {
                    contextService = statefulBean.contextService;
                    if (contextService!= null) {
                        contextService.clear();
                    } else {
                        contextService = new KeyedCollection<>();
                    }
                }
                contextService.add(contextServiceItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-name"), new QName("http://java.sun.com/xml/ns/javaee", "mapped-name"), new QName("http://java.sun.com/xml/ns/javaee", "home"), new QName("http://java.sun.com/xml/ns/javaee", "remote"), new QName("http://java.sun.com/xml/ns/javaee", "local-home"), new QName("http://java.sun.com/xml/ns/javaee", "local"), new QName("http://java.sun.com/xml/ns/javaee", "business-local"), new QName("http://java.sun.com/xml/ns/javaee", "business-remote"), new QName("http://java.sun.com/xml/ns/javaee", "local-bean"), new QName("http://java.sun.com/xml/ns/javaee", "service-endpoint"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-class"), new QName("http://java.sun.com/xml/ns/javaee", "session-type"), new QName("http://java.sun.com/xml/ns/javaee", "stateful-timeout"), new QName("http://java.sun.com/xml/ns/javaee", "timeout-method"), new QName("http://java.sun.com/xml/ns/javaee", "timer"), new QName("http://java.sun.com/xml/ns/javaee", "init-on-startup"), new QName("http://java.sun.com/xml/ns/javaee", "concurrency-management-type"), new QName("http://java.sun.com/xml/ns/javaee", "concurrent-method"), new QName("http://java.sun.com/xml/ns/javaee", "depends-on"), new QName("http://java.sun.com/xml/ns/javaee", "init-method"), new QName("http://java.sun.com/xml/ns/javaee", "remove-method"), new QName("http://java.sun.com/xml/ns/javaee", "async-method"), new QName("http://java.sun.com/xml/ns/javaee", "transaction-type"), new QName("http://java.sun.com/xml/ns/javaee", "after-begin-method"), new QName("http://java.sun.com/xml/ns/javaee", "before-completion-method"), new QName("http://java.sun.com/xml/ns/javaee", "after-completion-method"), new QName("http://java.sun.com/xml/ns/javaee", "around-invoke"), new QName("http://java.sun.com/xml/ns/javaee", "around-timeout"), new QName("http://java.sun.com/xml/ns/javaee", "env-entry"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-ref"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-local-ref"), new QName("http://java.sun.com/xml/ns/javaee", "service-ref"), new QName("http://java.sun.com/xml/ns/javaee", "resource-ref"), new QName("http://java.sun.com/xml/ns/javaee", "resource-env-ref"), new QName("http://java.sun.com/xml/ns/javaee", "message-destination-ref"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-context-ref"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-unit-ref"), new QName("http://java.sun.com/xml/ns/javaee", "post-construct"), new QName("http://java.sun.com/xml/ns/javaee", "pre-destroy"), new QName("http://java.sun.com/xml/ns/javaee", "data-source"), new QName("http://java.sun.com/xml/ns/javaee", "jms-connection-factory"), new QName("http://java.sun.com/xml/ns/javaee", "jms-destination"), new QName("http://java.sun.com/xml/ns/javaee", "post-activate"), new QName("http://java.sun.com/xml/ns/javaee", "pre-passivate"), new QName("http://java.sun.com/xml/ns/javaee", "security-role-ref"), new QName("http://java.sun.com/xml/ns/javaee", "security-identity"), new QName("http://java.sun.com/xml/ns/javaee", "passivation-capable"), new QName("http://java.sun.com/xml/ns/javaee", "context-service"));
            }
        }
        if (descriptions!= null) {
            try {
                statefulBean.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, SessionBean.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames!= null) {
            try {
                statefulBean.setDisplayNames(displayNames.toArray(new Text[displayNames.size()] ));
            } catch (Exception e) {
                context.setterError(reader, SessionBean.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon!= null) {
            statefulBean.icon = icon;
        }
        if (businessLocal!= null) {
            statefulBean.businessLocal = businessLocal;
        }
        if (businessRemote!= null) {
            statefulBean.businessRemote = businessRemote;
        }
        if (timer!= null) {
            statefulBean.timer = timer;
        }
        if (concurrentMethod!= null) {
            statefulBean.concurrentMethod = concurrentMethod;
        }
        if (initMethod!= null) {
            statefulBean.initMethod = initMethod;
        }
        if (removeMethod!= null) {
            statefulBean.removeMethod = removeMethod;
        }
        if (asyncMethod!= null) {
            statefulBean.asyncMethod = asyncMethod;
        }
        if (aroundInvoke!= null) {
            statefulBean.aroundInvoke = aroundInvoke;
        }
        if (aroundTimeout!= null) {
            statefulBean.aroundTimeout = aroundTimeout;
        }
        if (envEntry!= null) {
            statefulBean.envEntry = envEntry;
        }
        if (ejbRef!= null) {
            statefulBean.ejbRef = ejbRef;
        }
        if (ejbLocalRef!= null) {
            statefulBean.ejbLocalRef = ejbLocalRef;
        }
        if (serviceRef!= null) {
            statefulBean.serviceRef = serviceRef;
        }
        if (resourceRef!= null) {
            statefulBean.resourceRef = resourceRef;
        }
        if (resourceEnvRef!= null) {
            statefulBean.resourceEnvRef = resourceEnvRef;
        }
        if (messageDestinationRef!= null) {
            statefulBean.messageDestinationRef = messageDestinationRef;
        }
        if (persistenceContextRef!= null) {
            statefulBean.persistenceContextRef = persistenceContextRef;
        }
        if (persistenceUnitRef!= null) {
            statefulBean.persistenceUnitRef = persistenceUnitRef;
        }
        if (postConstruct!= null) {
            statefulBean.postConstruct = postConstruct;
        }
        if (preDestroy!= null) {
            statefulBean.preDestroy = preDestroy;
        }
        if (dataSource!= null) {
            statefulBean.dataSource = dataSource;
        }
        if (jmsConnectionFactories!= null) {
            statefulBean.jmsConnectionFactories = jmsConnectionFactories;
        }
        if (jmsDestinations!= null) {
            statefulBean.jmsDestinations = jmsDestinations;
        }
        if (postActivate!= null) {
            statefulBean.postActivate = postActivate;
        }
        if (prePassivate!= null) {
            statefulBean.prePassivate = prePassivate;
        }
        if (securityRoleRef!= null) {
            statefulBean.securityRoleRef = securityRoleRef;
        }
        if (contextService!= null) {
            statefulBean.contextService = contextService;
        }

        context.afterUnmarshal(statefulBean, org.metatype.sxc.jaxb.LifecycleCallback.NONE);

        return statefulBean;
    }

    public final StatefulBean read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _readDependsOn(XoXMLStreamReader reader, RuntimeContext context, StatefulBean statefulBean)
        throws Exception
    {
        List<String> dependsOn = null;

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("ejb-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: dependsOn
                String dependsOnItemRaw = elementReader.getElementText();

                String dependsOnItem;
                try {
                    dependsOnItem = Adapters.collapsedStringAdapterAdapter.unmarshal(dependsOnItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (dependsOn == null) {
                    dependsOn = statefulBean.dependsOn;
                    if (dependsOn!= null) {
                        dependsOn.clear();
                    } else {
                        dependsOn = new ArrayList<>();
                    }
                }
                dependsOn.add(dependsOnItem);
            }
        }
        if (dependsOn!= null) {
            statefulBean.dependsOn = dependsOn;
        }
    }

    public static final void _write(XoXMLStreamWriter writer, StatefulBean statefulBean, RuntimeContext context)
        throws Exception
    {
        if (statefulBean == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (StatefulBean.class!= statefulBean.getClass()) {
            context.unexpectedSubclass(writer, statefulBean, StatefulBean.class);
            return ;
        }

        context.beforeMarshal(statefulBean, org.metatype.sxc.jaxb.LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = statefulBean.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(statefulBean, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = statefulBean.getDescriptions();
        } catch (Exception e) {
            context.getterError(statefulBean, "descriptions", SessionBean.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(statefulBean, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = statefulBean.getDisplayNames();
        } catch (Exception e) {
            context.getterError(statefulBean, "displayNames", SessionBean.class, "getDisplayNames", e);
        }
        if (displayNames!= null) {
            for (Text displayNamesItem: displayNames) {
                if (displayNamesItem!= null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(statefulBean, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        LocalCollection<Icon> icon = statefulBean.icon;
        if (icon!= null) {
            for (Icon iconItem: icon) {
                if (iconItem!= null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(statefulBean, "icon");
                }
            }
        }

        // ELEMENT: ejbName
        String ejbNameRaw = statefulBean.ejbName;
        String ejbName = null;
        try {
            ejbName = Adapters.collapsedStringAdapterAdapter.marshal(ejbNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(statefulBean, "ejbName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (ejbName!= null) {
            writer.writeStartElement(prefix, "ejb-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(ejbName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(statefulBean, "ejbName");
        }

        // ELEMENT: mappedName
        String mappedNameRaw = statefulBean.mappedName;
        String mappedName = null;
        try {
            mappedName = Adapters.collapsedStringAdapterAdapter.marshal(mappedNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(statefulBean, "mappedName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (mappedName!= null) {
            writer.writeStartElement(prefix, "mapped-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(mappedName);
            writer.writeEndElement();
        }

        // ELEMENT: home
        String homeRaw = statefulBean.home;
        String home = null;
        try {
            home = Adapters.collapsedStringAdapterAdapter.marshal(homeRaw);
        } catch (Exception e) {
            context.xmlAdapterError(statefulBean, "home", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (home!= null) {
            writer.writeStartElement(prefix, "home", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(home);
            writer.writeEndElement();
        }

        // ELEMENT: remote
        String remoteRaw = statefulBean.remote;
        String remote = null;
        try {
            remote = Adapters.collapsedStringAdapterAdapter.marshal(remoteRaw);
        } catch (Exception e) {
            context.xmlAdapterError(statefulBean, "remote", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (remote!= null) {
            writer.writeStartElement(prefix, "remote", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(remote);
            writer.writeEndElement();
        }

        // ELEMENT: localHome
        String localHomeRaw = statefulBean.localHome;
        String localHome = null;
        try {
            localHome = Adapters.collapsedStringAdapterAdapter.marshal(localHomeRaw);
        } catch (Exception e) {
            context.xmlAdapterError(statefulBean, "localHome", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (localHome!= null) {
            writer.writeStartElement(prefix, "local-home", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(localHome);
            writer.writeEndElement();
        }

        // ELEMENT: local
        String localRaw = statefulBean.local;
        String local = null;
        try {
            local = Adapters.collapsedStringAdapterAdapter.marshal(localRaw);
        } catch (Exception e) {
            context.xmlAdapterError(statefulBean, "local", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (local!= null) {
            writer.writeStartElement(prefix, "local", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(local);
            writer.writeEndElement();
        }

        // ELEMENT: businessLocal
        LinkedHashSet<String> businessLocalRaw = statefulBean.businessLocal;
        if (businessLocalRaw!= null) {
            for (String businessLocalItem: businessLocalRaw) {
                String businessLocal = null;
                try {
                    businessLocal = Adapters.collapsedStringAdapterAdapter.marshal(businessLocalItem);
                } catch (Exception e) {
                    context.xmlAdapterError(statefulBean, "businessLocal", CollapsedStringAdapter.class, LinkedHashSet.class, LinkedHashSet.class, e);
                }
                if (businessLocal!= null) {
                    writer.writeStartElement(prefix, "business-local", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(businessLocal);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: businessRemote
        LinkedHashSet<String> businessRemoteRaw = statefulBean.businessRemote;
        if (businessRemoteRaw!= null) {
            for (String businessRemoteItem: businessRemoteRaw) {
                String businessRemote = null;
                try {
                    businessRemote = Adapters.collapsedStringAdapterAdapter.marshal(businessRemoteItem);
                } catch (Exception e) {
                    context.xmlAdapterError(statefulBean, "businessRemote", CollapsedStringAdapter.class, LinkedHashSet.class, LinkedHashSet.class, e);
                }
                if (businessRemote!= null) {
                    writer.writeStartElement(prefix, "business-remote", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(businessRemote);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: localBean
        Empty localBean = statefulBean.localBean;
        if (localBean!= null) {
            writer.writeStartElement(prefix, "local-bean", "http://java.sun.com/xml/ns/javaee");
            writeEmpty(writer, localBean, context);
            writer.writeEndElement();
        }

        // ELEMENT: serviceEndpoint
        String serviceEndpointRaw = statefulBean.serviceEndpoint;
        String serviceEndpoint = null;
        try {
            serviceEndpoint = Adapters.collapsedStringAdapterAdapter.marshal(serviceEndpointRaw);
        } catch (Exception e) {
            context.xmlAdapterError(statefulBean, "serviceEndpoint", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (serviceEndpoint!= null) {
            writer.writeStartElement(prefix, "service-endpoint", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(serviceEndpoint);
            writer.writeEndElement();
        }

        // ELEMENT: ejbClass
        String ejbClassRaw = statefulBean.ejbClass;
        String ejbClass = null;
        try {
            ejbClass = Adapters.collapsedStringAdapterAdapter.marshal(ejbClassRaw);
        } catch (Exception e) {
            context.xmlAdapterError(statefulBean, "ejbClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (ejbClass!= null) {
            writer.writeStartElement(prefix, "ejb-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(ejbClass);
            writer.writeEndElement();
        }

        // ELEMENT: sessionType
        SessionType sessionType = statefulBean.sessionType;
        if (sessionType!= null) {
            writer.writeStartElement(prefix, "session-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringSessionType(statefulBean, null, context, sessionType));
            writer.writeEndElement();
        }

        // ELEMENT: statefulTimeout
        Timeout statefulTimeout = statefulBean.statefulTimeout;
        if (statefulTimeout!= null) {
            writer.writeStartElement(prefix, "stateful-timeout", "http://java.sun.com/xml/ns/javaee");
            writeTimeout(writer, statefulTimeout, context);
            writer.writeEndElement();
        }

        // ELEMENT: timeoutMethod
        NamedMethod timeoutMethod = statefulBean.timeoutMethod;
        if (timeoutMethod!= null) {
            writer.writeStartElement(prefix, "timeout-method", "http://java.sun.com/xml/ns/javaee");
            writeNamedMethod(writer, timeoutMethod, context);
            writer.writeEndElement();
        }

        // ELEMENT: timer
        List<Timer> timer = statefulBean.timer;
        if (timer!= null) {
            for (Timer timerItem: timer) {
                if (timerItem!= null) {
                    writer.writeStartElement(prefix, "timer", "http://java.sun.com/xml/ns/javaee");
                    writeTimer(writer, timerItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: initOnStartup
        Boolean initOnStartup = statefulBean.initOnStartup;
        if (initOnStartup!= null) {
            writer.writeStartElement(prefix, "init-on-startup", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(initOnStartup));
            writer.writeEndElement();
        }

        // ELEMENT: concurrencyManagementType
        ConcurrencyManagementType concurrencyManagementType = statefulBean.concurrencyManagementType;
        if (concurrencyManagementType!= null) {
            writer.writeStartElement(prefix, "concurrency-management-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringConcurrencyManagementType(statefulBean, null, context, concurrencyManagementType));
            writer.writeEndElement();
        }

        // ELEMENT: concurrentMethod
        List<ConcurrentMethod> concurrentMethod = statefulBean.concurrentMethod;
        if (concurrentMethod!= null) {
            for (ConcurrentMethod concurrentMethodItem: concurrentMethod) {
                if (concurrentMethodItem!= null) {
                    writer.writeStartElement(prefix, "concurrent-method", "http://java.sun.com/xml/ns/javaee");
                    writeConcurrentMethod(writer, concurrentMethodItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: dependsOn
        List<String> dependsOnRaw = statefulBean.dependsOn;
        if (dependsOnRaw!= null) {
            writer.writeStartElement(prefix, "depends-on", "http://java.sun.com/xml/ns/javaee");
            for (String dependsOnItem: dependsOnRaw) {
                String dependsOn = null;
                try {
                    dependsOn = Adapters.collapsedStringAdapterAdapter.marshal(dependsOnItem);
                } catch (Exception e) {
                    context.xmlAdapterError(statefulBean, "dependsOn", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (dependsOn!= null) {
                    writer.writeStartElement(prefix, "ejb-name", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(dependsOn);
                    writer.writeEndElement();
                }
            }
            writer.writeEndElement();
        }

        // ELEMENT: initMethod
        List<InitMethod> initMethod = statefulBean.initMethod;
        if (initMethod!= null) {
            for (InitMethod initMethodItem: initMethod) {
                if (initMethodItem!= null) {
                    writer.writeStartElement(prefix, "init-method", "http://java.sun.com/xml/ns/javaee");
                    writeInitMethod(writer, initMethodItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: removeMethod
        List<RemoveMethod> removeMethod = statefulBean.removeMethod;
        if (removeMethod!= null) {
            for (RemoveMethod removeMethodItem: removeMethod) {
                if (removeMethodItem!= null) {
                    writer.writeStartElement(prefix, "remove-method", "http://java.sun.com/xml/ns/javaee");
                    writeRemoveMethod(writer, removeMethodItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: asyncMethod
        List<AsyncMethod> asyncMethod = statefulBean.asyncMethod;
        if (asyncMethod!= null) {
            for (AsyncMethod asyncMethodItem: asyncMethod) {
                if (asyncMethodItem!= null) {
                    writer.writeStartElement(prefix, "async-method", "http://java.sun.com/xml/ns/javaee");
                    writeAsyncMethod(writer, asyncMethodItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: transactionType
        TransactionType transactionType = statefulBean.transactionType;
        if (transactionType!= null) {
            writer.writeStartElement(prefix, "transaction-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringTransactionType(statefulBean, null, context, transactionType));
            writer.writeEndElement();
        }

        // ELEMENT: afterBeginMethod
        NamedMethod afterBeginMethod = null;
        try {
            afterBeginMethod = statefulBean.getAfterBeginMethod();
        } catch (Exception e) {
            context.getterError(statefulBean, "afterBeginMethod", SessionBean.class, "getAfterBeginMethod", e);
        }
        if (afterBeginMethod!= null) {
            writer.writeStartElement(prefix, "after-begin-method", "http://java.sun.com/xml/ns/javaee");
            writeNamedMethod(writer, afterBeginMethod, context);
            writer.writeEndElement();
        }

        // ELEMENT: beforeCompletionMethod
        NamedMethod beforeCompletionMethod = null;
        try {
            beforeCompletionMethod = statefulBean.getBeforeCompletionMethod();
        } catch (Exception e) {
            context.getterError(statefulBean, "beforeCompletionMethod", SessionBean.class, "getBeforeCompletionMethod", e);
        }
        if (beforeCompletionMethod!= null) {
            writer.writeStartElement(prefix, "before-completion-method", "http://java.sun.com/xml/ns/javaee");
            writeNamedMethod(writer, beforeCompletionMethod, context);
            writer.writeEndElement();
        }

        // ELEMENT: afterCompletionMethod
        NamedMethod afterCompletionMethod = null;
        try {
            afterCompletionMethod = statefulBean.getAfterCompletionMethod();
        } catch (Exception e) {
            context.getterError(statefulBean, "afterCompletionMethod", SessionBean.class, "getAfterCompletionMethod", e);
        }
        if (afterCompletionMethod!= null) {
            writer.writeStartElement(prefix, "after-completion-method", "http://java.sun.com/xml/ns/javaee");
            writeNamedMethod(writer, afterCompletionMethod, context);
            writer.writeEndElement();
        }

        // ELEMENT: aroundInvoke
        List<AroundInvoke> aroundInvoke = statefulBean.aroundInvoke;
        if (aroundInvoke!= null) {
            for (AroundInvoke aroundInvokeItem: aroundInvoke) {
                if (aroundInvokeItem!= null) {
                    writer.writeStartElement(prefix, "around-invoke", "http://java.sun.com/xml/ns/javaee");
                    writeAroundInvoke(writer, aroundInvokeItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(statefulBean, "aroundInvoke");
                }
            }
        }

        // ELEMENT: aroundTimeout
        List<AroundTimeout> aroundTimeout = statefulBean.aroundTimeout;
        if (aroundTimeout!= null) {
            for (AroundTimeout aroundTimeoutItem: aroundTimeout) {
                if (aroundTimeoutItem!= null) {
                    writer.writeStartElement(prefix, "around-timeout", "http://java.sun.com/xml/ns/javaee");
                    writeAroundTimeout(writer, aroundTimeoutItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: envEntry
        KeyedCollection<String, EnvEntry> envEntry = statefulBean.envEntry;
        if (envEntry!= null) {
            for (EnvEntry envEntryItem: envEntry) {
                if (envEntryItem!= null) {
                    writer.writeStartElement(prefix, "env-entry", "http://java.sun.com/xml/ns/javaee");
                    writeEnvEntry(writer, envEntryItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(statefulBean, "envEntry");
                }
            }
        }

        // ELEMENT: ejbRef
        KeyedCollection<String, EjbRef> ejbRef = statefulBean.ejbRef;
        if (ejbRef!= null) {
            for (EjbRef ejbRefItem: ejbRef) {
                if (ejbRefItem!= null) {
                    writer.writeStartElement(prefix, "ejb-ref", "http://java.sun.com/xml/ns/javaee");
                    writeEjbRef(writer, ejbRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(statefulBean, "ejbRef");
                }
            }
        }

        // ELEMENT: ejbLocalRef
        KeyedCollection<String, EjbLocalRef> ejbLocalRef = statefulBean.ejbLocalRef;
        if (ejbLocalRef!= null) {
            for (EjbLocalRef ejbLocalRefItem: ejbLocalRef) {
                if (ejbLocalRefItem!= null) {
                    writer.writeStartElement(prefix, "ejb-local-ref", "http://java.sun.com/xml/ns/javaee");
                    writeEjbLocalRef(writer, ejbLocalRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(statefulBean, "ejbLocalRef");
                }
            }
        }

        // ELEMENT: serviceRef
        KeyedCollection<String, ServiceRef> serviceRef = statefulBean.serviceRef;
        if (serviceRef!= null) {
            for (ServiceRef serviceRefItem: serviceRef) {
                if (serviceRefItem!= null) {
                    writer.writeStartElement(prefix, "service-ref", "http://java.sun.com/xml/ns/javaee");
                    writeServiceRef(writer, serviceRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(statefulBean, "serviceRef");
                }
            }
        }

        // ELEMENT: resourceRef
        KeyedCollection<String, ResourceRef> resourceRef = statefulBean.resourceRef;
        if (resourceRef!= null) {
            for (ResourceRef resourceRefItem: resourceRef) {
                if (resourceRefItem!= null) {
                    writer.writeStartElement(prefix, "resource-ref", "http://java.sun.com/xml/ns/javaee");
                    writeResourceRef(writer, resourceRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(statefulBean, "resourceRef");
                }
            }
        }

        // ELEMENT: resourceEnvRef
        KeyedCollection<String, ResourceEnvRef> resourceEnvRef = statefulBean.resourceEnvRef;
        if (resourceEnvRef!= null) {
            for (ResourceEnvRef resourceEnvRefItem: resourceEnvRef) {
                if (resourceEnvRefItem!= null) {
                    writer.writeStartElement(prefix, "resource-env-ref", "http://java.sun.com/xml/ns/javaee");
                    writeResourceEnvRef(writer, resourceEnvRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(statefulBean, "resourceEnvRef");
                }
            }
        }

        // ELEMENT: messageDestinationRef
        KeyedCollection<String, MessageDestinationRef> messageDestinationRef = statefulBean.messageDestinationRef;
        if (messageDestinationRef!= null) {
            for (MessageDestinationRef messageDestinationRefItem: messageDestinationRef) {
                if (messageDestinationRefItem!= null) {
                    writer.writeStartElement(prefix, "message-destination-ref", "http://java.sun.com/xml/ns/javaee");
                    writeMessageDestinationRef(writer, messageDestinationRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(statefulBean, "messageDestinationRef");
                }
            }
        }

        // ELEMENT: persistenceContextRef
        KeyedCollection<String, PersistenceContextRef> persistenceContextRef = statefulBean.persistenceContextRef;
        if (persistenceContextRef!= null) {
            for (PersistenceContextRef persistenceContextRefItem: persistenceContextRef) {
                if (persistenceContextRefItem!= null) {
                    writer.writeStartElement(prefix, "persistence-context-ref", "http://java.sun.com/xml/ns/javaee");
                    writePersistenceContextRef(writer, persistenceContextRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(statefulBean, "persistenceContextRef");
                }
            }
        }

        // ELEMENT: persistenceUnitRef
        KeyedCollection<String, PersistenceUnitRef> persistenceUnitRef = statefulBean.persistenceUnitRef;
        if (persistenceUnitRef!= null) {
            for (PersistenceUnitRef persistenceUnitRefItem: persistenceUnitRef) {
                if (persistenceUnitRefItem!= null) {
                    writer.writeStartElement(prefix, "persistence-unit-ref", "http://java.sun.com/xml/ns/javaee");
                    writePersistenceUnitRef(writer, persistenceUnitRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(statefulBean, "persistenceUnitRef");
                }
            }
        }

        // ELEMENT: postConstruct
        List<org.apache.openejb.jee.LifecycleCallback> postConstruct = statefulBean.postConstruct;
        if (postConstruct!= null) {
            for (org.apache.openejb.jee.LifecycleCallback postConstructItem: postConstruct) {
                if (postConstructItem!= null) {
                    writer.writeStartElement(prefix, "post-construct", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, postConstructItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(statefulBean, "postConstruct");
                }
            }
        }

        // ELEMENT: preDestroy
        List<org.apache.openejb.jee.LifecycleCallback> preDestroy = statefulBean.preDestroy;
        if (preDestroy!= null) {
            for (org.apache.openejb.jee.LifecycleCallback preDestroyItem: preDestroy) {
                if (preDestroyItem!= null) {
                    writer.writeStartElement(prefix, "pre-destroy", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, preDestroyItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(statefulBean, "preDestroy");
                }
            }
        }

        // ELEMENT: dataSource
        KeyedCollection<String, DataSource> dataSource = statefulBean.dataSource;
        if (dataSource!= null) {
            for (DataSource dataSourceItem: dataSource) {
                if (dataSourceItem!= null) {
                    writer.writeStartElement(prefix, "data-source", "http://java.sun.com/xml/ns/javaee");
                    writeDataSource(writer, dataSourceItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: jmsConnectionFactories
        KeyedCollection<String, JMSConnectionFactory> jmsConnectionFactories = statefulBean.jmsConnectionFactories;
        if (jmsConnectionFactories!= null) {
            for (JMSConnectionFactory jmsConnectionFactoriesItem: jmsConnectionFactories) {
                if (jmsConnectionFactoriesItem!= null) {
                    writer.writeStartElement(prefix, "jms-connection-factory", "http://java.sun.com/xml/ns/javaee");
                    writeJMSConnectionFactory(writer, jmsConnectionFactoriesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(statefulBean, "jmsConnectionFactories");
                }
            }
        }

        // ELEMENT: jmsDestinations
        KeyedCollection<String, JMSDestination> jmsDestinations = statefulBean.jmsDestinations;
        if (jmsDestinations!= null) {
            for (JMSDestination jmsDestinationsItem: jmsDestinations) {
                if (jmsDestinationsItem!= null) {
                    writer.writeStartElement(prefix, "jms-destination", "http://java.sun.com/xml/ns/javaee");
                    writeJMSDestination(writer, jmsDestinationsItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: postActivate
        List<org.apache.openejb.jee.LifecycleCallback> postActivate = statefulBean.postActivate;
        if (postActivate!= null) {
            for (org.apache.openejb.jee.LifecycleCallback postActivateItem: postActivate) {
                if (postActivateItem!= null) {
                    writer.writeStartElement(prefix, "post-activate", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, postActivateItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(statefulBean, "postActivate");
                }
            }
        }

        // ELEMENT: prePassivate
        List<org.apache.openejb.jee.LifecycleCallback> prePassivate = statefulBean.prePassivate;
        if (prePassivate!= null) {
            for (org.apache.openejb.jee.LifecycleCallback prePassivateItem: prePassivate) {
                if (prePassivateItem!= null) {
                    writer.writeStartElement(prefix, "pre-passivate", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, prePassivateItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(statefulBean, "prePassivate");
                }
            }
        }

        // ELEMENT: securityRoleRef
        List<SecurityRoleRef> securityRoleRef = statefulBean.securityRoleRef;
        if (securityRoleRef!= null) {
            for (SecurityRoleRef securityRoleRefItem: securityRoleRef) {
                if (securityRoleRefItem!= null) {
                    writer.writeStartElement(prefix, "security-role-ref", "http://java.sun.com/xml/ns/javaee");
                    writeSecurityRoleRef(writer, securityRoleRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(statefulBean, "securityRoleRef");
                }
            }
        }

        // ELEMENT: securityIdentity
        SecurityIdentity securityIdentity = statefulBean.securityIdentity;
        if (securityIdentity!= null) {
            writer.writeStartElement(prefix, "security-identity", "http://java.sun.com/xml/ns/javaee");
            writeSecurityIdentity(writer, securityIdentity, context);
            writer.writeEndElement();
        }

        // ELEMENT: passivationCapable
        Boolean passivationCapable = statefulBean.passivationCapable;
        if (passivationCapable!= null) {
            writer.writeStartElement(prefix, "passivation-capable", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(passivationCapable));
            writer.writeEndElement();
        }

        // ELEMENT: contextService
        KeyedCollection<String, ContextService> contextService = statefulBean.contextService;
        if (contextService!= null) {
            for (ContextService contextServiceItem: contextService) {
                if (contextServiceItem!= null) {
                    writer.writeStartElement(prefix, "context-service", "http://java.sun.com/xml/ns/javaee");
                    writeContextService(writer, contextServiceItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(statefulBean, org.metatype.sxc.jaxb.LifecycleCallback.NONE);
    }

}
