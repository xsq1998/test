package com.shop.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.shop.pojo.Employee;
import com.shop.pojo.Leavebill;
import com.shop.service.LeaveBillService;
import com.shop.service.WorkFlowService;
import com.shop.utils.Constant;

@Controller
public class WorkFlowController {

	@Autowired
	private WorkFlowService workFlowService;
	@Autowired
	private LeaveBillService leaveBillService;

	@RequestMapping(value = "/deployProcess")
	public String deployProcess(MultipartFile fileName, String processName) throws Exception {

		//部署流程
		this.workFlowService.deployProcess(fileName.getInputStream(), processName);
		

		return "index";
	}

	@RequestMapping(value = "/saveStartLeave")
	public String saveStartLeave(Leavebill leavebill, HttpSession httpSession) {
		leavebill.setLeavedate(new Date());
		leavebill.setState(1);//正在运行状态为1，结束就设为2
		//取出session
		Employee e = (Employee) httpSession.getAttribute(Constant.GLOBLE_USER_SESSION);
		leavebill.setUserId(e.getId());
		//保存 请假单
		leaveBillService.saveLeaveBill(leavebill);
		// 启动流程（准备好BUSIINESS_KEY，插入成功后返回leavebill表的id）
		Long id = leavebill.getId();

		System.err.println("请假单id：" + id);
		workFlowService.saveStartProcess(id, e.getName());

		// workFlowService.saveStartProcess(e.getName());
		return "redirect:taskList";
	}

	// 待办事务
	@RequestMapping(value = "/taskList")
	public ModelAndView getTaskList(HttpSession session) {
		ModelAndView mv = new ModelAndView();
		String name = ((Employee) session.getAttribute(Constant.GLOBLE_USER_SESSION)).getName();
		//获得当前登录用户的待办事务列表
		List<Task> list = workFlowService.findTaskListByName(name);
		//把查询出来的待办事务列表存在域中
		mv.addObject("taskList", list);
		//设置视图名称
		mv.setViewName("workflow_task");
		return mv;
	}

	// 审批 查询请假单+批注
	@RequestMapping(value = "/viewTaskForm")
	public ModelAndView viewTaskForm(String taskId) {
		ModelAndView mv = new ModelAndView();
		// 查询请假单
		Leavebill leavebill = workFlowService.findLeaveBillByTaskId(taskId);
		// 查询批注列表
		List<Comment> comments = workFlowService.findCommentList(taskId);
		// 将leaveBill对象存在域中
		mv.addObject("bill", leavebill);
		// 将批注列表存在域中
		mv.addObject("commentList", comments);
		mv.addObject("taskId", taskId);
		
		mv.setViewName("approve_leave");
		return mv;
	}

	// 点击提交审批请假单
	@RequestMapping(value = "/submitTask")
	public String submitTask(Long id, String taskId, String comment, HttpSession session) {
		//获得当前用户名
		String username = ((Employee) session.getAttribute(Constant.GLOBLE_USER_SESSION)).getName();
		//添加批注
		workFlowService.addSubmitTask(id,taskId,username,comment);
		return "redirect:taskList";

	}
	
	@RequestMapping(value="viewCurrentImage")
	public String viewCurrentImage(String taskId,ModelMap modelMap) {
		//通过任务id，获取任务对象，使用任务对象获取流程定义ID，获取流程定义对象
		ProcessDefinition processDefinition=workFlowService.findProcessDefinationByTaskId();
		
		
		return "";
	}
	
	@RequestMapping(value="myBaoxiaoBill")
	public ModelAndView myBaoxiaoBill(HttpSession session) {
		ModelAndView mv=new ModelAndView();
		Long name = ((Employee) session.getAttribute(Constant.GLOBLE_USER_SESSION)).getId();
		return mv;
		
		
	}

}
