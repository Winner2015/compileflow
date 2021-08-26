/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.compileflow.engine.process.preruntime.converter.impl.parser.support;

import com.alibaba.compileflow.engine.definition.common.Element;
import com.alibaba.compileflow.engine.process.preruntime.converter.impl.parser.FlowElementParser;
import com.alibaba.compileflow.engine.process.preruntime.converter.impl.parser.model.ParseContext;
import com.alibaba.compileflow.engine.process.preruntime.converter.impl.parser.provider.ParserProviderSupport;
import com.alibaba.compileflow.engine.process.preruntime.converter.impl.parser.provider.support.AbstractFlowElementParserProvider;
import com.alibaba.compileflow.engine.process.preruntime.converter.impl.parser.model.XMLSource;

/**
 * @description XML元素解析器的抽象类，最重要的作用是完成了两种接口的合流：
 * 1、FlowElementParser：XML元素——》流程节点数据模型
 * 2、ParserProviderSupport：获取ParserProvider，ParserProvider则汇集了全部的FlowElementParser
 * 由于节点可能有多级子节点，解析器会一直递归地向下解析，途中就可能会遇到不同的节点类型，
 * ParserProviderSupport的意义就在于，使得每种解析器随时可以获取全部FlowElementParser，去解析它的子节点
 * @author chenlongfei
 */
public abstract class AbstractFlowElementParser<E extends Element> implements FlowElementParser<E>,
    ParserProviderSupport<AbstractFlowElementParserProvider> {

    @Override
    public E parse(XMLSource xmlSource, ParseContext parseContext) throws Exception {
        E element = doParse(xmlSource, parseContext); //解析当前节点
        parseContext.setParent(element);
        parseChildElements(xmlSource, element, parseContext); //解析当前节点的子节点
        return element;
    }

    protected abstract E doParse(XMLSource xmlSource, ParseContext parseContext) throws Exception;

    //本方法是一个递归方法，从XML的根节点开始，直到最后一个元素
    protected void parseChildElements(XMLSource xmlSource, E element, ParseContext parseContext) throws Exception {
        while (xmlSource.hasNext()) {
            String elementName = xmlSource.nextElementName();
            if (elementName != null) {
                Element childElement = getParserProvider().getParser(elementName)
                    .parse(xmlSource, parseContext); //解析成不同的节点模型

                //处理子节点与父节点的附属关系
                if (!attachPlatformChildElement(childElement, element, parseContext)) {
                    attachChildElement(childElement, element, parseContext);
                }
            }
        }
    }

    /**
     * @description 是否将子节点，附加到父节点
     * @author chenlongfei
    */
    protected abstract boolean attachPlatformChildElement(Element childElement, E element, ParseContext parseContext);

    /**
     * @description 将子节点附加到父节点
     * @author chenlongfei
     */
    protected abstract void attachChildElement(Element childElement, E element, ParseContext parseContext);

}
