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

import com.alibaba.compileflow.engine.common.CompileFlowException;
import com.alibaba.compileflow.engine.common.utils.ClassLoaderUtils;
import com.alibaba.compileflow.engine.definition.common.Element;
import com.alibaba.compileflow.engine.definition.common.var.IVar;
import com.alibaba.compileflow.engine.process.preruntime.converter.impl.parser.FlowStreamParser;
import com.alibaba.compileflow.engine.process.preruntime.converter.impl.parser.model.ParseConfig;
import com.alibaba.compileflow.engine.process.preruntime.converter.impl.parser.model.ParseContext;
import com.alibaba.compileflow.engine.process.preruntime.converter.impl.parser.provider.support.AbstractFlowElementParserProvider;
import com.alibaba.compileflow.engine.process.preruntime.converter.impl.parser.model.FlowStreamSource;
import com.alibaba.compileflow.engine.process.preruntime.converter.impl.parser.model.XMLSource;
import com.alibaba.compileflow.engine.process.preruntime.converter.impl.parser.model.XMLStreamReaderSource;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description 文件流——》流程数据模型
 * @author chenlongfei
 */
public abstract class AbstractFlowStreamParser<R> implements FlowStreamParser<R> {

    @Override
    public R parse(FlowStreamSource source) {
        return parse(source, new ParseConfig());
    }

    @Override
    public R parse(FlowStreamSource source, ParseConfig parseConfig) {

        //使用javax.xml工具包，解析配置文件
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        try {
            if (parseConfig.isValidateSchema()) { //使用.xsd文件，校验XML
                validateSchema(source.getFlow());
            }

            InputStreamReader inputStreamReader = new InputStreamReader(source.getFlow(), parseConfig.getEncoding());
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStreamReader);
            xmlStreamReader = xmlInputFactory.createFilteredReader(
                xmlStreamReader, reader -> XMLStreamConstants.START_DOCUMENT == reader.getEventType()
                    || XMLStreamConstants.END_DOCUMENT == reader.getEventType()
                    || XMLStreamConstants.START_ELEMENT == reader.getEventType()
                    || XMLStreamConstants.END_ELEMENT == reader.getEventType());

            return parseFlowModel(XMLStreamReaderSource.of(xmlStreamReader));
        } catch (UnsupportedEncodingException e) {
            throw new CompileFlowException("Unsupported encoding " + parseConfig.getEncoding(), e);
        } catch (CompileFlowException e) {
            throw e;
        } catch (Exception e) {
            throw new CompileFlowException("Failed to parse flow", e);
        }
    }

    protected R parseFlowModel(XMLSource xmlSource) throws Exception {
        ParseContext parseContext = new ParseContext();
        while (xmlSource.hasNext()) { //实际上只会循环一次，因为根节点只有一个
            String elementName = xmlSource.nextElementName();
            if (elementName != null) {
                //拿到对应的元素解析器，
                getFlowElementParserProvider().getParser(elementName).parse(xmlSource, parseContext);
            }
        }
        return convertToFlowModel(parseContext.getTop());
    }

    //获取XML解析器工厂类
    protected abstract AbstractFlowElementParserProvider getFlowElementParserProvider();

    protected abstract R convertToFlowModel(Element top);

    private void validateSchema(InputStream flowStream) throws Exception {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(ClassLoaderUtils.getResource(getXSD()));

        if (schema == null) {
            throw new CompileFlowException("XSD could not be found");
        }

        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(flowStream));
    }

    protected abstract String getXSD();

    protected List<IVar> buildTypeVars(List<IVar> vars, String varType) {
        return vars.stream().filter(var -> varType.equals(var.getInOutType())).collect(Collectors.toList());
    }

}
