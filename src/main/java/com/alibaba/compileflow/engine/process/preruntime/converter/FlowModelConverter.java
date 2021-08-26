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
package com.alibaba.compileflow.engine.process.preruntime.converter;

import com.alibaba.compileflow.engine.process.preruntime.converter.impl.parser.model.FlowStreamSource;

import java.io.OutputStream;

/**
 * @description 转换器：bpm文件——》标准数据模型
 * @author chenlongfei
*/
public interface FlowModelConverter<T> {

    /**
     * @description 转换操作
     * @param flowStreamSource 配置文件的IO流
     * @return 标准数据模型
     * @author chenlongfei
    */
    T convertToModel(FlowStreamSource flowStreamSource);

    OutputStream convertToStream(T model);

}
