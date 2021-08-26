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
package com.alibaba.compileflow.engine.process.preruntime.generator;

import com.alibaba.compileflow.engine.process.preruntime.generator.code.CodeTargetSupport;

/**
 * @description 代码生成器
 * 是个包装接口，负责打杂，代码生委托CodeTargetSupport来完成
 * @author chenlongfei
*/
public interface Generator {

    /**
     * @description 生成代码，存于AbstractCodeTargetSupport的StringBuffer
     * @param codeTargetSupport 执行代码生成的真正实体类
     * @return
     * @author chenlongfei
    */
    void generateCode(CodeTargetSupport codeTargetSupport);

}
