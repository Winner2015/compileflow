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
package com.alibaba.compileflow.engine.process.preruntime.generator.impl;

import com.alibaba.compileflow.engine.definition.common.Node;
import com.alibaba.compileflow.engine.process.preruntime.generator.code.CodeTargetSupport;
import com.alibaba.compileflow.engine.runtime.impl.AbstractProcessRuntime;
import org.apache.commons.lang3.StringUtils;

/**
 * 流程节点——》代码
 * 服务对象是<autoTask>、<transition>这样的一个元素
 */
public abstract class AbstractNodeGenerator<N extends Node>
    extends AbstractRuntimeGenerator {

    protected N flowNode; //流程节点模型

    public AbstractNodeGenerator(AbstractProcessRuntime runtime, N flowNode) {
        super(runtime);
        this.flowNode = flowNode;
    }

    protected void generateNodeComment(CodeTargetSupport codeTargetSupport) {
        //加了一行注释，表明该节点对应的数据模型和名字
        String comment = "//" + flowNode.getClass().getSimpleName();
        if (StringUtils.isNotEmpty(flowNode.getName())) {
            comment += ": " + flowNode.getName();
        }
        codeTargetSupport.addBodyLine(comment);
    }

}
