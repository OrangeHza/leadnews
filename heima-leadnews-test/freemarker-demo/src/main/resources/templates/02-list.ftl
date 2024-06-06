<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Hello World!</title>
</head>
<body>

<#-- list 数据的展示 -->
<b>展示list中的stu数据:</b>
<br>
<br>
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>钱包</td>
    </tr>
    <#list stus as stu>
    <#-- 这里=和==效果一样 -->
        <#if stu.name='小红'>
            <tr style="color: red">
                <td>${stu_index+1}</td>
                <td>${stu.name}</td>
                <td>${stu.age}</td>
                <td>${stu.money}</td>
            </tr>
        <#else >
            <tr>
                <td>${stu_index+1}</td>
                <td>${stu.name}</td>
                <td>${stu.age}</td>
                <td>${stu.money}</td>
            </tr>
        </#if>



    </#list>
</table>
<hr>

<#-- Map 数据的展示 -->
<b>map数据的展示：</b>
<br/><br/>
<a href="###">方式一：通过map['keyname'].property</a><br/>
输出stu1的学生信息：<br/>
姓名：${stuMap['stu1'].name}<br/>
年龄：${stuMap['stu1'].age}<br/>
<br/>
<a href="###">方式二：通过map.keyname.property</a><br/>
输出stu2的学生信息：<br/>
姓名：${stuMap.stu2.name}<br/>
年龄：${stuMap.stu2.age}<br/>

<br/>
<a href="###">遍历map中两个学生信息：</a><br/>
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>钱包</td>
    </tr>
    <#-- 注意map的遍历写法，比较特殊 -->
    <#list stuMap?keys as key>
        <tr>
            <td>${key_index+1}</td>
            <td>${stuMap[key].name}</td>
            <td>${stuMap[key].age}</td>
            <td>${stuMap[key].money}</td>
        </tr>
    </#list>
</table>
<br/>
<br/>
<br/>
<hr>

<br>
<a href="###">空值处理和默认值：</a><br/>
<table>
   <#if (stuMap.stu1)??>
       输出stuMap.stu1的学生信息：<br/>
       姓名：${stuMap.stu1.name}<br/>
       年龄：${stuMap.stu1.age}<br/>
       学校: ${(stuMap.stu1.school)!'武汉大学'}<br/><#-- 缺省默认值 -->
   </#if>
    <#if stuMap.stu10??>
        输出stuMap.stu10的学生信息：<br/>
        姓名：${stuMap.stu10.name}<br/>
        年龄：${stuMap.stu10.age}<br/>
    </#if>
</table>
<hr>


</body>
</html>