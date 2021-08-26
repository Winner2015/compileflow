package com.alibaba.compileflow.engine.runtime;

import java.util.Map;

/**
 * @description 流程运行时模型
 * @author chenlongfei
*/
public interface ProcessRuntime {

    Map<String, Object> start(Map<String, Object> context);

}
