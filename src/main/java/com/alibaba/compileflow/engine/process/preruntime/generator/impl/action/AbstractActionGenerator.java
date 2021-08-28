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

import com.alibaba.compileflow.engine.common.utils.DataType;
import com.alibaba.compileflow.engine.definition.common.action.IAction;
import com.alibaba.compileflow.engine.definition.common.action.IActionHandle;
import com.alibaba.compileflow.engine.definition.common.var.IVar;
import com.alibaba.compileflow.engine.process.preruntime.generator.impl.AbstractRuntimeGenerator;
import com.alibaba.compileflow.engine.process.preruntime.generator.code.CodeTargetSupport;
import com.alibaba.compileflow.engine.runtime.impl.AbstractProcessRuntime;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 节点动作的处理（一次方法调用）——》代码
 * 服务对象是<actionHandle>元素
 *
 * 该类比较特殊的地方是，不仅继承了AbstractRuntimeGenerator，还是实现了两个接口：
 * 1、ActionGenerator：识别动作类型，比如Java，QL表达式等
 * 2、ActionMethodGenerator：
 */
public abstract class AbstractActionGenerator extends AbstractRuntimeGenerator
    implements ActionGenerator, ActionMethodGenerator {

    protected IActionHandle actionHandle;

    public AbstractActionGenerator(AbstractProcessRuntime runtime, IAction action) {
        super(runtime);
        this.actionHandle = action.getActionHandle();
    }

    //生成方法括号之间，代表入参的代码片段
    protected String generateParameterCode(CodeTargetSupport codeTargetSupport) {
        List<IVar> methodParameters = getMethodParameters(); //方法入参类型
        if (CollectionUtils.isNotEmpty(methodParameters)) {
            List<String> params = new ArrayList<>(methodParameters.size());
            for (IVar v : methodParameters) {
                addImportedType(codeTargetSupport, DataType.getJavaClass(v.getDataType()));
                if (v.getContextVarName() != null) { //从上下文中获取入参的值
                    String param = DataType.getVarTransferString(getVarType(v.getContextVarName()),
                        DataType.getJavaClass(v.getDataType()), v.getContextVarName());
                    params.add(param);
                } else { //或者取默认值
                    String param = DataType.getDefaultValueString(DataType.getJavaClass(v.getDataType()),
                        v.getDefaultValue());
                    params.add(param);
                }
            }
            return String.join(", ", params);
        }

        return "";
    }

    @Override
    public void generateActionMethodCode(CodeTargetSupport codeTargetSupport) {
        generateMethodCode(codeTargetSupport, generateActionMethodName(codeTargetSupport), this::generateCode);
    }

    protected List<IVar> getMethodParameters() {
        return actionHandle.getParamVars();
    }

    protected IVar getReturnVar() {
        return actionHandle.getReturnVar();
    }

    protected String getReturnVarCode() {
        IVar returnVar = getReturnVar();
        if (returnVar == null || returnVar.getContextVarName() == null) {
            return "";
        }
        return returnVar.getContextVarName() + " = ";
    }

}
