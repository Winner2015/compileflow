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
package com.alibaba.compileflow.engine.runtime.impl;

import com.alibaba.compileflow.engine.common.ClassWrapper;
import com.alibaba.compileflow.engine.common.constants.ProcessType;
import com.alibaba.compileflow.engine.definition.common.*;
import com.alibaba.compileflow.engine.process.preruntime.validator.ValidateMessage;
import com.alibaba.compileflow.engine.runtime.instance.ProcessInstance;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wuxiang
 * @author yusu
 */
public abstract class AbstractStatelessProcessRuntime<T extends AbstractFlowModel> extends AbstractProcessRuntime<T> {

    public AbstractStatelessProcessRuntime(T flowModel) {
        super(flowModel);
    }

    @Override
    public void init() {
        super.init();
        initGatewayGraph(); //分析网关节点的图结构
    }

    @Override
    public String generateJavaCode() {
        classTarget.addSuperInterface(ClassWrapper.of(ProcessInstance.class));
        generateFlowMethod("execute", this::generateExecuteMethodBody);
        return classTarget.generateCode();
    }

    @Override
    public ProcessType getProcessType() {
        return ProcessType.STATELESS;
    }

    @Override
    protected boolean isStateless() {
        return true;
    }

    @Override
    protected List<Class<?>> getExtImportedTypes() {
        return Collections.singletonList(ProcessInstance.class);
    }

    @Override
    protected List<ValidateMessage> validateFlowModel() {
        return super.validateFlowModel();
    }

    //初始化与网关节点相关的图形结构
    private void initGatewayGraph() {
        buildGatewayGraph(flowModel);
    }

    private void buildGatewayGraph(NodeContainer<Node> nodeContainer) {
        List<TransitionNode> nodes = nodeContainer.getAllNodes()
            .stream()
            .filter(node -> node instanceof TransitionNode)
            .map(e -> (TransitionNode) e)
            .collect(Collectors.toList());

        nodes.forEach(this::buildFollowingNodes);
        nodes.stream()
            .filter(flowNode -> flowNode instanceof GatewayElement)
            .forEach(gatewayNode -> {
                gatewayNode.getOutgoingNodes().forEach(outgoingNode -> {
                    List<TransitionNode> branchNodes = buildBranchNodes(outgoingNode).stream()
                        .filter(node -> !followingGraph.get(gatewayNode.getId()).contains(node)) //过滤掉outgoingNode自己
                        .collect(Collectors.toList());

                    branchGraph.put(outgoingNode.getId(), branchNodes); //outgoingNode——》该分支链路下的全部节点
                });

                if (CollectionUtils.isNotEmpty(gatewayNode.getIncomingNodes())
                    && gatewayNode.getIncomingNodes().stream()
                    .allMatch(incomingNode -> isContainedByIncomingNode(gatewayNode, incomingNode))) {
                    //如果网关节点的全部分支，是其上游节点（也是网关节点）的分支子集，该网关节点的followingNode置空（防止节点被重复编译）
                    followingGraph.put(gatewayNode.getId(), Collections.emptyList());
                }
            });

        nodes.stream()
            .filter(flowNode -> flowNode instanceof NodeContainer)
            .map(e -> (NodeContainer) e)
            .forEach(this::buildGatewayGraph);
    }

    //获取指定节点的直接下游节点
    private List<TransitionNode> buildFollowingNodes(TransitionNode flowNode) {
        if (followingGraph.containsKey(flowNode.getId())) {
            return followingGraph.get(flowNode.getId());
        }

        List<TransitionNode> followingNodes;
        //EndElement、BreakElement无下游
        //GatewayElement有多个下游
        //其余节点只有一个下游
        if (flowNode instanceof EndElement) {
            followingNodes = Collections.emptyList();
        } else if (flowNode instanceof BreakElement) {
            followingNodes = Collections.emptyList();
        } else if (flowNode instanceof GatewayElement) {
            followingNodes = buildGatewayFollowingNodes(flowNode);
        } else {
            followingNodes = new ArrayList<>();
            TransitionNode theOnlyOutgoingNode = getTheOnlyOutgoingNode(flowNode);
            if (theOnlyOutgoingNode != null) {
                followingNodes.add(theOnlyOutgoingNode);
                followingNodes.addAll(buildFollowingNodes(theOnlyOutgoingNode));
            }
        }

        followingGraph.put(flowNode.getId(), followingNodes);
        return followingNodes;
    }

    private TransitionNode getTheOnlyOutgoingNode(TransitionNode flowNode) {
        if (flowNode.getOutgoingNodes().size() > 0) {
            return flowNode.getOutgoingNodes().get(0);
        }
        return null;
    }

    //递归方法
    private boolean isContainedByIncomingNode(Node decisionNode, TransitionNode incomingNode) {
        if (incomingNode instanceof StartElement) {
            return false;
        }
        if (incomingNode instanceof GatewayElement) {
            List<TransitionNode> decisionFollowingNodes = followingGraph.get(decisionNode.getId());
            List<TransitionNode> incomingFollowingNodes = followingGraph.get(incomingNode.getId());
            if (decisionFollowingNodes.size() == incomingFollowingNodes.size()
                && incomingFollowingNodes.containsAll(decisionFollowingNodes)) {
                return true;
                /**

                                A
                              . . .
                             .  .   .
                            .   .    .
                           .    B     .
                           .   .  .    .
                           .  .     .  .
                           . .        ..
                           C           D
                 A与B都是网关节点
                 集合1：A的直接下游节点：B、C、D
                 集合2：B的直接下游节点：C、D
                 集合2是集合1的子集

                 */
            }
        }
        return CollectionUtils.isNotEmpty(incomingNode.getIncomingNodes())
            && incomingNode.getIncomingNodes().stream()
            .allMatch(node -> isContainedByIncomingNode(decisionNode, node));
    }

    private List<TransitionNode> buildGatewayFollowingNodes(TransitionNode flowNode) {
        List<TransitionNode> outgoingNodes = flowNode.getOutgoingNodes();
        if (outgoingNodes.size() < 2) {
            return Collections.emptyList();
        }
        List<TransitionNode> followingNodes = Collections.emptyList();
        for (int i = 0; i < outgoingNodes.size(); i++) {
            TransitionNode branchNode = outgoingNodes.get(i);
            List<TransitionNode> branchFollowingNodes = buildFollowingNodes(branchNode);

            if (i == 0) {
                followingNodes = new ArrayList<>(branchFollowingNodes);
            } else {
                Iterator<TransitionNode> flowNodeIterator = followingNodes.iterator();
                while (flowNodeIterator.hasNext()) {
                    TransitionNode followingNode = flowNodeIterator.next();
                    if (branchFollowingNodes.stream()
                        .anyMatch(node -> node.getId().equals(followingNode.getId()))) {
                        break;
                    } else {
                        flowNodeIterator.remove();
                    }
                    if (CollectionUtils.isEmpty(followingNodes)) {
                        return followingNodes;
                    }
                }
            }
        }
        return followingNodes;
    }

    /**
     * @description 递归方法，获取网关节点的一个分支链路
     * @param branchNode 网关节点的一个分支节点
     * @return
     * @author chenlongfei
    */
    private List<TransitionNode> buildBranchNodes(TransitionNode branchNode) {
        if (branchGraph.containsKey(branchNode.getId())) {
            return branchGraph.get(branchNode.getId());
        }

        List<TransitionNode> branchNodes = new ArrayList<>();
        branchNodes.add(branchNode);
        //遇到EndElement、BreakElement、GatewayElement三者之一，结束递归。也就是说，得到的是一个单向的、没有任何分支的节点链路
        if (!(branchNode instanceof EndElement) && !(branchNode instanceof BreakElement) && !(branchNode instanceof GatewayElement)) {
            TransitionNode theOnlyOutgoingNode = getTheOnlyOutgoingNode(branchNode);
            if (theOnlyOutgoingNode != null) {
                branchNodes.addAll(buildBranchNodes(theOnlyOutgoingNode));
            }
        }

        branchGraph.put(branchNode.getId(), branchNodes);
        return branchNodes;
    }

}
