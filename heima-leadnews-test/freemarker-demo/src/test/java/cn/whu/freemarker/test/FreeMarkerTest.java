package cn.whu.freemarker.test;

import cn.whu.freemarker.FreemarkerDemoApplication;
import cn.whu.freemarker.entity.Student;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@SpringBootTest(classes = FreemarkerDemoApplication.class)
@RunWith(SpringRunner.class)
public class FreeMarkerTest {

    @Autowired // 可能不行，会有重复
    private Configuration configuration; // 注意是: freemarker.template.Configuration;

    @Test
    public void test() throws IOException, TemplateException {
        // 1. 读取模板
        Template template = configuration.getTemplate("02-list.ftl");

        // 2. 填充数据 -》 并将生成的静态html写到磁盘
        /**
         * 合成方法
         *
         * 两个参数
         * 第一个参数：模型数据
         * 第二个参数：输出流
         */
        template.process(getData(),new FileWriter("E:/prodata/leader-news/list.html"));
    }


    private Map getData() {
        Map<String, Object> map = new HashMap<>();

        //小强对象模型数据
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

        //向map中存放List集合数据
        map.put("stus", stus);


        //创建Map数据
        HashMap<String, Student> stuMap = new HashMap<>();
        stuMap.put("stu1", stu1);
        stuMap.put("stu2", stu2);
        //向map中存放Map数据
        map.put("stuMap", stuMap);

        //返回Map
        return map;
    }
}
