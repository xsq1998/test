package com.shop.service;

import java.io.InputStream;
import java.util.List;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;

import com.shop.pojo.Leavebill;

public interface WorkFlowService {
	// 部署流程
	public void deployProcess(InputStream in,String processName);
	// 根据姓名查询某个员工下的所有待办事务
	public List<Task> findTaskListByName(String name);
	// 保存并启动流程实例
	public void saveStartProcess(String name);
	public void saveStartProcess(Long id, String name);
	public Leavebill findLeaveBillByTaskId(String taskId);
	public List<Comment> findCommentList(String taskId);
	public void addSubmitTask(Long id, String taskId, String username,String comment);
	public ProcessDefinition findProcessDefinationByTaskId();


}
