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
import com.alibaba.compileflow.engine.definition.common.action.HasAction;
import com.alibaba.compileflow.engine.definition.common.action.IAction;
import com.alibaba.compileflow.engine.process.preruntime.generator.Generator;
import com.alibaba.compileflow.engine.process.preruntime.generator.code.CodeTargetSupport;
import com.alibaba.compileflow.engine.process.preruntime.generator.factory.GeneratorFactory;
import com.alibaba.compileflow.engine.process.preruntime.generator.impl.action.ActionMethodGenerator;
import com.alibaba.compileflow.engine.runtime.impl.AbstractProcessRuntime;

/**
 * 流程节点的动作——》代码
 * 服务对象是<action>元素
 *
 *
 * Generator的继承关系与XML的节点层次相呼应：
 * <bpm ...> --AbstractRuntimeGenerator，持有ProcessRuntime模型
 *     <autoTask ...> --AbstractNodeGenerator 持有FlowNode模型
 *         <transition .../>
 *         <action type="java"> --AbstractActionNodeGenerator
 *             <actionHandle clazz="" method=""> --AbstractActionGenerator 持有ActionHandle模型
 *                 <var name="p1" .../>
 *             </actionHandle>
 *         </action>
 *     </autoTask>
 *  </bpm>
 */
public abstract class AbstractActionNodeGenerator<N extends Node>
    extends AbstractNodeGenerator<N> {

    public AbstractActionNodeGenerator(AbstractProcessRuntime runtime, N flowNode) {
        super(runtime, flowNode);
    }

    @Override
    public void generateCode(CodeTargetSupport codeTargetSupport) {
        generateNodeComment(codeTargetSupport);
        HasAction hasAction = (HasAction)flowNode; //归属的父节点
        IAction action = hasAction.getAction();
        generateActionCode(codeTargetSupport, action);
    }

    protected void generateActionCode(CodeTargetSupport codeTargetSupport, IAction action) {
        if (action != null) {
            //获取方法调用的代码生成器，依据<action type="">的type属性，生成Java、QL表达式等不同的代码
            Generator actionGenerator = GeneratorFactory.getInstance().getActionGenerator(action, runtime);
            actionGenerator.generateCode(codeTargetSupport); //生成动作代码
        }
    }

    protected void generateActionMethodCode(CodeTargetSupport codeTargetSupport, IAction action) {
        if (action != null) {

            //第一步：生成方法体，插入到目标类当中
            ActionMethodGenerator actionMethodGenerator = GeneratorFactory.getInstance()
                .getActionGenerator(action, runtime);
            actionMethodGenerator.generateActionMethodCode(codeTargetSupport);

            //第二步：生成方法调用的语句，调用的的就是上面生成的方法，然后插入到流程执行的方法体中
            codeTargetSupport.addBodyLine(
                actionMethodGenerator.generateActionMethodName(codeTargetSupport) + "();");

            /**
             * 第一步生成了一个方法A，方法显然要依托于类而存在，所以要插入到代表流程的那个"类"当中
             * 第二步是生成了调用方法A的代码行，方法调用显然要依托于另一个方法而存在，所以要插入到流程执行的那个"方法"当中
             * 还注意到，方法调用时，没有入参，也没有返回值，实际上入参与返回值都是用类变量来存储的，为什么这样做呢？
             * 简化参数传递，后面的代码使用该方法的调用结果，可以直接从类变量获取
            */

        }
    }

}
