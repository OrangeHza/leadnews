# leadnews
黑马头条项目

自己从0开始搭建环境做一遍，自己写业务的代码量固然重要，但是多学习牛人代码，自己才会有实质性的增长

C503 新建第一个其他分支，做最初的开发

远程仓库地址：https://github.com/OrangeHza/leadnews.git

-----
查看端口占用：

netstat -ano | findstr 端口号
netstat -ano | findstr :51701

结束进程

taskkill /F /PID 进程PID


----
gitHub总是上传失败： https://blog.csdn.net/weixin_46389691/article/details/132032845

我的问题：

fatal: unable to access 'https://github.com/xxxxx.git/': Failed to connect to github.com port 443 after 21126 ms: Timed out；

解决方法:

取消代理设置。

```git
git config --global --unset http.proxy
git config --global --unset https.proxy
```
-------
2024年06月08日 22:55:32
当前旁支合并到主分支成功：模拟项目上线成功


2024年06月27日 00:37:21
当前旁支合并到主分支成功：模拟项目上线成功