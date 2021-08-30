package com.example.demo.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
/**
 * @author zln
 * @date 2021/08/29
 */
public class SuccessService implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) {
        System.out.println("请求被通过check: \n 申请人:"+delegateExecution.getVariable("username")
                +"申请金额:"+delegateExecution.getVariable("money"));
    }
}


