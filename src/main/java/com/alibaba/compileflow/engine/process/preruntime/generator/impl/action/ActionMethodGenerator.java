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
package com.alibaba.compileflow.engine.process.preruntime.generator.impl.action;

import com.alibaba.compileflow.engine.process.preruntime.generator.code.CodeTargetSupport;

/**
 * 流程动作的触发，在代码层面表现为一次方法调用
 */
public interface ActionMethodGenerator {

    /**
     * @description 生成方法名
     * @param codeTargetSupport
     * @return
     * @author chenlongfei
    */
    String generateActionMethodName(CodeTargetSupport codeTargetSupport);

    /**
     * @description 生成方法体
     * @param codeTargetSupport 方法肯定是挂在类当中的，所以该参数肯定是一个类ClassTarget
     * @return
     * @author chenlongfei
    */
    void generateActionMethodCode(CodeTargetSupport codeTargetSupport);

}
