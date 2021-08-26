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
package com.alibaba.compileflow.engine.process.preruntime.generator.code;

import java.util.List;

/**
 * @description 抽象了生成代码的子操作，比如添加一行代码体、添加大括号、添加缩进等；
 * generateCode()方法会基于这些子操作生成最终的代码
 * 共有四个子类：
 * 1、ClassTarget：生成类的代码
 * 2、MethodTarget：生成类的方法体
 * 3、FieldTarget：生成类中的变量
 * 4、ParamTarget：生成方法的入参
 * @author chenlongfei
*/
public interface CodeTargetSupport {

    void addBodyLine(String line);

    void addBodyLines(List<String> lines);

    void appendLine(String line);

    void addNewLine();

    void addSemicolon();

    void addSpace();

    void addOpenBrace();

    void addCloseBrace();

    void addOpenParen();

    void addCloseParen();

    void addIndent();

    void addIndent(int indent);

    String getName();

    /**
     * @description 代码生成器有多种实现，分别用于生成类、方法、字段、方法变量，
     * 而方法、字段等代码片段是不能独立存在的，必然要关联某个类的代码体，将其添加进去，
     * 该方法就用于获取这个目标类
     * @author chenlongfei
     */
    CodeTargetSupport getClassTarget();

    /**
     * @description 生成代码的核心入口
     * @author chenlongfei
    */
    String generateCode();

}
