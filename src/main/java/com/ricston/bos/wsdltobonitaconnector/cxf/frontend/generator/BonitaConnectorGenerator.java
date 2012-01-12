/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Copyright (c) Ricston Ltd.  All rights reserved.  http://www.ricston.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.ricston.bos.wsdltobonitaconnector.cxf.frontend.generator;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.model.*;
import org.apache.cxf.tools.util.ClassCollector;
import org.apache.cxf.tools.util.NameUtil;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.generators.ClientGenerator;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.processor.WSDLToJavaProcessor;
import org.apache.velocity.util.StringUtils;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Map;

public class BonitaConnectorGenerator extends ClientGenerator {

    private static String TEMPLATE_BASE = "com/ricston/bos/wsdltobonitaconnector/cxf/frontend/template";

    public void generate(ToolContext penv) throws ToolException {
        this.env = penv;
        Map<QName, JavaModel> map = CastUtils.cast((Map) penv.get(WSDLToJavaProcessor.MODEL_MAP));
        for (JavaModel javaModel : map.values()) {

            if (javaModel.getServiceClasses().size() == 0) {
                ServiceInfo serviceInfo = (ServiceInfo) env.get(ServiceInfo.class);
                String wsdl = serviceInfo.getDescription().getBaseURI();
                Message msg = new Message("CAN_NOT_GEN_CLIENT", LOG, wsdl);
                if (penv.isVerbose()) {
                    System.out.println(msg.toString());
                }
                return;
            }

            Map<String, JavaInterface> interfaces = javaModel.getInterfaces();
            Iterator it = javaModel.getServiceClasses().values().iterator();
            while (it.hasNext()) {
                JavaServiceClass js = (JavaServiceClass) it.next();
                Iterator i = js.getPorts().iterator();
                while (i.hasNext()) {
                    JavaPort jp = (JavaPort) i.next();
                    String interfaceName = jp.getInterfaceClass();
                    JavaInterface intf = interfaces.get(interfaceName);
                    if (intf == null) {
                        interfaceName = jp.getPortType();
                        intf = interfaces.get(interfaceName);
                    }
                    Iterator<JavaMethod> methodsIterator = intf.getMethods().iterator();
                    while (methodsIterator.hasNext()) {
                        JavaMethod method = methodsIterator.next();
                        String clientClassName = NameUtil.mangleNameToClassName(jp.getPortName()) + StringUtils.capitalizeFirstLetter(method.getName()) + "WS";

                        clientClassName = mapClassName(intf.getPackageName(), clientClassName, penv);
                        clearAttributes();
                        setAttributes("clientClassName", clientClassName);
                        setAttributes("method", method);
                        setAttributes("intf", intf);
                        setAttributes("service", js);
                        setAttributes("port", jp);
                        setAttributes("stringUtils", new StringUtils());

                        setCommonAttributes();

                        doWrite(TEMPLATE_BASE + "/bonita-client.vm", parseOutputName(intf.getPackageName(), clientClassName + "Connector"));
                        doWrite(TEMPLATE_BASE + "/properties.vm", parseOutputName(intf.getPackageName(), clientClassName + "Connector", ".properties"));
                        doWrite(TEMPLATE_BASE + "/xml.vm", parseOutputName(intf.getPackageName(), clientClassName + "Connector", ".xml"));
                    }
                }
            }
        }
    }

    private String mapClassName(String packageName, String name, ToolContext context) {
        ClassCollector collector = context.get(ClassCollector.class);
        int count = 0;
        String checkName = name;
        while (collector.containClientClass(packageName, checkName)) {
            checkName = name + (++count);
        }
        collector.addClientClassName(packageName, checkName,
                packageName + "." + checkName);
        return checkName;
    }
}
