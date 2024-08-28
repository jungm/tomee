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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;

@SuppressWarnings({
    "StringEquality"
})
public class FacesConfigFlowDefinitionInitializer$JAXB
    extends JAXBObject<FacesConfigFlowDefinitionInitializer>
{


    public FacesConfigFlowDefinitionInitializer$JAXB() {
        super(FacesConfigFlowDefinitionInitializer.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-flow-definition-initializerType".intern()));
    }

    public static FacesConfigFlowDefinitionInitializer readFacesConfigFlowDefinitionInitializer(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesConfigFlowDefinitionInitializer(XoXMLStreamWriter writer, FacesConfigFlowDefinitionInitializer facesConfigFlowDefinitionInitializer, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinitionInitializer, context);
    }

    public void write(XoXMLStreamWriter writer, FacesConfigFlowDefinitionInitializer facesConfigFlowDefinitionInitializer, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinitionInitializer, context);
    }

    public static final FacesConfigFlowDefinitionInitializer _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesConfigFlowDefinitionInitializer facesConfigFlowDefinitionInitializer = new FacesConfigFlowDefinitionInitializer();
        context.beforeUnmarshal(facesConfigFlowDefinitionInitializer, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-flow-definition-initializerType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesConfigFlowDefinitionInitializer.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesConfigFlowDefinitionInitializer);
                facesConfigFlowDefinitionInitializer.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // VALUE: value
        String valueRaw = reader.getElementText();

        String value = null;
        boolean valueConverted;
        try {
            value = Adapters.collapsedStringAdapterAdapter.unmarshal(valueRaw);
            valueConverted = true;
        } catch (Exception e) {
            context.xmlAdapterError(reader, CollapsedStringAdapter.class, String.class, String.class, e);
            valueConverted = false;
        }

        if (valueConverted) {
            facesConfigFlowDefinitionInitializer.value = value;
        }

        context.afterUnmarshal(facesConfigFlowDefinitionInitializer, LifecycleCallback.NONE);

        return facesConfigFlowDefinitionInitializer;
    }

    public final FacesConfigFlowDefinitionInitializer read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesConfigFlowDefinitionInitializer facesConfigFlowDefinitionInitializer, RuntimeContext context)
        throws Exception
    {
        if (facesConfigFlowDefinitionInitializer == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (FacesConfigFlowDefinitionInitializer.class!= facesConfigFlowDefinitionInitializer.getClass()) {
            context.unexpectedSubclass(writer, facesConfigFlowDefinitionInitializer, FacesConfigFlowDefinitionInitializer.class);
            return ;
        }

        context.beforeMarshal(facesConfigFlowDefinitionInitializer, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesConfigFlowDefinitionInitializer.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesConfigFlowDefinitionInitializer, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // VALUE: value
        String valueRaw = facesConfigFlowDefinitionInitializer.value;
        String value = null;
        try {
            value = Adapters.collapsedStringAdapterAdapter.marshal(valueRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesConfigFlowDefinitionInitializer, "value", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        writer.writeCharacters(value);

        context.afterMarshal(facesConfigFlowDefinitionInitializer, LifecycleCallback.NONE);
    }

}
