package com.example.demo.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * @author zln
 * @date 2021/08/29
 */
public class CheckService implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) {
        System.out.println("对于请求参数进行check: \n 申请人:"+delegateExecution.getVariable("userId")
                +"申请金额:"+delegateExecution.getVariable("money"));
    }
}
