package com.example.demo;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zln
 * @date 2021/08/29
 */
@RestController
@RequestMapping("flowable-money")
public class MoneyController {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ProcessEngine processEngine;


    /**
     * 创建流程
     *
     * @param userId
     * @param money
     * @return
     */
    @GetMapping("add")
    public String addExpense(String userId, String money) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("money", money);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("MoneyProcess", map);
        return "提交成功,流程ID为：" + processInstance.getId();
    }

    /**
     * 获取指定用户组流程任务列表
     *
     * @param group
     * @return
     */
    @GetMapping("list")
    public Object list(String group,String userId) {
        List<Task> tasks = null;
        if (StringUtils.isNotBlank(group)){
            tasks = taskService.createTaskQuery().taskCandidateGroup(group).list();
        }else if(StringUtils.isNotBlank(userId)){
             tasks = taskService.createTaskQuery().taskAssignee(userId).orderByTaskCreateTime().desc().list();
        }
        return tasks.toString();
    }

    /**
     * 通过/拒绝任务
     *
     * @param taskId
     * @param approved 1 ：true  2：false
     * @return
     */
    @GetMapping("apply")
    public String apply(String taskId, String approved,String money) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return "流程不存在";
        }
        Map<String, Object> variables = new HashMap<>();
        if (StringUtils.isNotBlank(approved)){
            Boolean apply = approved.equals("1") ? true : false;
            variables.put("approved", apply);
        }
        if (StringUtils.isNotBlank(money)){
            variables.put("money", money);
        }
        taskService.complete(taskId, variables);
        return "审批是否通过：" + approved+"  "+money;

    }


    /**
     * 查询流程图
     *
     * @param httpServletResponse
     * @param processId
     * @throws Exception
     */
    @RequestMapping(value = "processDiagram")
    public void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) throws Exception {
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();

        //流程走完的不显示图
        if (pi == null) {
            return;
        }
        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        //使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
        String InstanceId = task.getProcessInstanceId();
        List<Execution> executions = runtimeService
                .createExecutionQuery()
                .processInstanceId(InstanceId)
                .list();

        //得到正在执行的Activity的Id
        List<String> activityIds = new ArrayList<>();
        List<String> flows = new ArrayList<>();
        for (Execution exe : executions) {
            List<String> ids = runtimeService.getActiveActivityIds(exe.getId());
            activityIds.addAll(ids);
        }

        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", activityIds, flows, engconf.getActivityFontName(), engconf.getLabelFontName(), engconf.getAnnotationFontName(), engconf.getClassLoader(), 1.0);
        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            out = httpServletResponse.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

}
