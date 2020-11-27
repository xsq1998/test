package com.shop.service.Impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shop.mapper.LeavebillMapper;
import com.shop.pojo.Leavebill;
import com.shop.service.WorkFlowService;
import com.shop.utils.Constant;

@Service("workflowService")
public class WorkFlowServiceImpl implements WorkFlowService {
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private FormService formService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private LeavebillMapper leaveBillMapper;

	@Override
	public void deployProcess(InputStream in, String processName) {
		ZipInputStream zipInputStream = new ZipInputStream(in);

		this.repositoryService.createDeployment().addZipInputStream(zipInputStream).name(processName).deploy();

	}

	@Override
	public void saveStartProcess(String name) {
		// 通过表达式方式设置待办人
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId", name);
		this.runtimeService
				// 流程的key
				.startProcessInstanceByKey(Constant.Leave_KEY, map);

	}

	// 待办事务列表
	@Override
	public List<Task> findTaskListByName(String name) {
		List<Task> list = taskService.createTaskQuery().taskAssignee(name).orderByTaskCreateTime().desc().list();
		return list;
	}

	@Override
	public void saveStartProcess(Long id, String name) {
		// 通过表达式方式设置待办人
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId", name);
		//组装bussiness_key  processId+leaveId，点击=办理流程时会先数据所需id
		String business_key=Constant.Leave_KEY+"."+id;
		map.put("business_key", business_key);
		
		this.runtimeService
				// 流程的key,business_key,map
				.startProcessInstanceByKey(Constant.Leave_KEY, business_key, map);

	}

	@Override
	public Leavebill findLeaveBillByTaskId(String taskId) {
		//先得到Task
		Task task=taskService
		.createTaskQuery()
		.taskId(taskId)
		.singleResult();
		
		//得到流程实例id
		String processInstanceId = task.getProcessInstanceId();
		
		//得到流程实例
		ProcessInstance processInstance = runtimeService
		.createProcessInstanceQuery()
		.processInstanceId(processInstanceId)
		.singleResult();
		
		//取出business_key
		String businessKey = processInstance.getBusinessKey();
		
		//切割得到leave Id
		String leaveId="";
		if (businessKey!=null&&!("".equals(businessKey))) {
			int begin=businessKey.lastIndexOf(".");
			leaveId = businessKey.substring(begin+1);
			//leaveId = businessKey.split("\\.")[1];
		}
		System.err.println("leaveId:"+leaveId);
		
		//调用逆向工程的selectByPrimaryKey，去查询leaveBill对象
		Leavebill leavebill = leaveBillMapper.selectByPrimaryKey(Long.parseLong(leaveId));
		
		return leavebill;
	}

	@Override
	public List<Comment> findCommentList(String taskId) {
		//获取task
		Task task = taskService
		.createTaskQuery()
		.taskId(taskId)
		.singleResult();
		//获取流程实例id然后获取
		List<Comment> list = taskService.getProcessInstanceComments(task.getProcessInstanceId());
		return list;
	}

	//添加批注
	@Override
	public void addSubmitTask(Long id, String taskId, String username,String comment) {
		//使用taskId查询任务对象
		Task task = taskService
		.createTaskQuery()
		.taskId(taskId)
		.singleResult();
		
		//获得流程实例id
		String processInstanceId = task.getProcessInstanceId();
		//加当前任务的审核人
		Authentication.setAuthenticatedUserId(username);
		
		taskService.addComment(taskId, processInstanceId,comment);
		//推进当前任务
		taskService.complete(taskId);
		
		//获取流程实例
		ProcessInstance processInstance=runtimeService
				.createProcessInstanceQuery()
				.processInstanceId(processInstanceId)
				.singleResult();
		
		//流程为空就结束
		if (processInstance==null) {
			Leavebill leavebill = leaveBillMapper.selectByPrimaryKey(id);
			//设置业务的状态：审批结束 (2)
			leavebill.setState(2);
			leaveBillMapper.updateByPrimaryKey(leavebill);
		}
		
		
		
		
	}

	@Override
	public ProcessDefinition findProcessDefinationByTaskId() {
		
		return null;
	}

}
