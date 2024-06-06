package cn.whu.freemarker.controller;

import cn.whu.freemarker.entity.Student;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller // @ResponseBody 不能加，这里就是返回springMVC默认返回的视图，而不是json数据
@Slf4j
public class HelloController {

    @RequestMapping("basic")
    public String hello(Model model) { // spring内置类
        // 设置数据
        model.addAttribute("name","慕容紫英");
        Student stu = new Student();
        stu.setName("百里屠苏");
        stu.setAge(18);
        model.addAttribute("stu",stu);
        // 返回置顶视图 model数据也会被传递过去
        return "01-basic"; // templates下找01-basic.ftl页面，application.yml里面都配置好了
    }

    @RequestMapping("list")
    public String list(Model model){

        //------------------------------------
        Student stu1 = new Student();
        stu1.setName("小强");
        stu1.setAge(18);
        stu1.setMoney(1000.86f);
        stu1.setBirthday(new Date());

        //小红对象模型数据
        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMoney(200.1f);
        stu2.setAge(19);

        //将两个对象模型数据存放到List集合中
        List<Student> stus = new ArrayList<>();
        stus.add(stu1);
        stus.add(stu2);

        //向model中存放List集合数据
        model.addAttribute("stus",stus);

        //------------------------------------

        //创建Map数据
        HashMap<String,Student> stuMap = new HashMap<>();
        stuMap.put("stu1",stu1);
        stuMap.put("stu2",stu2);
        // 3.1 向model中存放Map数据
        model.addAttribute("stuMap", stuMap);

        return "02-list";
    }

    @RequestMapping("sign")
    public String sign(Model model){
        model.addAttribute("date1",new Date(1714640257l));
        model.addAttribute("date2",new Date(1714641257l));
        return "03-sign";
    }

    @GetMapping("innerFunc")
    public String testInnerFunc(Model model) {
        //1.1 小强对象模型数据
        Student stu1 = new Student();
        stu1.setName("小强");
        stu1.setAge(18);
        stu1.setMoney(1000.86f);
        stu1.setBirthday(new Date());
        //1.2 小红对象模型数据
        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMoney(200.1f);
        stu2.setAge(19);
        //1.3 将两个对象模型数据存放到List集合中
        List<Student> stus = new ArrayList<>();
        stus.add(stu1);
        stus.add(stu2);
        model.addAttribute("stus", stus);
        // 2.1 添加日期
        Date date = new Date();
        model.addAttribute("today", date);
        // 3.1 添加(长)数值
        model.addAttribute("point", 2024789632145l);
        return "04-innerFunc";
    }

}
