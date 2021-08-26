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
package com.alibaba.compileflow.engine;

import java.util.Map;

/**
 * @description 流程引擎的顶层接口，抽象出了引擎的基本功能，比如：执行流程、加载配置文件、代码编译等
 * @author chenlongfei
*/
public interface ProcessEngine<T extends FlowModel> {

    /**
     * @description 执行指定流程
     * @param code 流程唯一code
     * @param context 上下文参数
     * @return 执行结果
     * @author chenlongfei
    */
    Map<String, Object> execute(String code, Map<String, Object> context);

    @Deprecated
    Map<String, Object> start(String code, Map<String, Object> context); //已废弃
    void preCompile(String... codes); //已废弃，无调用
    void reload(String code);//已废弃，无调用

    /**
     * @description 加载、解析流程的配置文件，并将其转换为流程的数据模型
     * @param code 流程唯一code
     * @return 流程的数据模型
     * @author chenlongfei
    */
    T load(String code);

    String getJavaCode(String code);

    String getTestCode(String code);

}
