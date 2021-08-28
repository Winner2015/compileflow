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
package com.alibaba.compileflow.engine.process.preruntime.generator.impl.container;

import com.alibaba.compileflow.engine.definition.common.EndElement;
import com.alibaba.compileflow.engine.definition.common.GatewayElement;
import com.alibaba.compileflow.engine.definition.common.NodeContainer;
import com.alibaba.compileflow.engine.definition.common.TransitionNode;
import com.alibaba.compileflow.engine.process.preruntime.generator.Generator;
import com.alibaba.compileflow.engine.process.preruntime.generator.code.CodeTargetSupport;
import com.alibaba.compileflow.engine.runtime.impl.AbstractProcessRuntime;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * ContainerGenerator与NodeGenerator的区别在于，
 * ContainerGenerator处理的是完整的节点链路
 * ContainerGenerator处理的是一个节点
 */
public class ContainerGenerator extends AbstractContainerGenerator {

    public ContainerGenerator(AbstractProcessRuntime runtime,
                              NodeContainer nodeContainer) {
        super(runtime, nodeContainer);
    }

    @Override
    public void generateCode(CodeTargetSupport codeTargetSupport) {
        TransitionNode startNode = (TransitionNode)nodeContainer.getStartNode();
        //拿到起始节点，触发代码生成
        generateCode(startNode, codeTargetSupport);
    }

    //递归方法，沿着节点链路，生成一个个节点的代码
    private void generateCode(TransitionNode flowNode, CodeTargetSupport codeTargetSupport) {
        if (flowNode instanceof EndElement) { //遇到结束节点，返回
            return;
        }

        Generator generator = getGenerator(flowNode);
        generator.generateCode(codeTargetSupport);
        if (flowNode instanceof GatewayElement) {
            return; //一旦遇到决策节点，后续节点的代码会交由决策节点来触发生成
        }

        List<TransitionNode> outgoingNodes = getOutingNodes(flowNode);
        if (CollectionUtils.isNotEmpty(outgoingNodes)) {
            //继续处理下游节点
            for (TransitionNode outgoingNode : outgoingNodes) {
                generateCode(outgoingNode, codeTargetSupport);
            }
        }
    }

    private List<TransitionNode> getOutingNodes(TransitionNode flowNode) {
        return flowNode.getOutgoingNodes();
    }

}
