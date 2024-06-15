package cn.whu.common.aliyun;

import com.aliyun.imageaudit20191230.*;
import com.aliyun.imageaudit20191230.models.ScanTextRequest;
import com.aliyun.imageaudit20191230.models.ScanTextResponse;
import com.aliyun.imageaudit20191230.models.ScanTextResponseBody;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "aliyun") // 读取xml配置李 aliyun打头的配置  为两个属性自动赋值
                                            // 动态的参数都在xml里了，工具类直接拿来用，多好哇
                                            // wemedia要用，就在wemedia的nacos动态xml中配置
public class GreenTextScan {


    private String accessKeyId;
    private String secret;

    public Map greenTextScan(String content) throws Exception {
        /*
          初始化配置对象com.aliyun.teaopenapi.models.Config
          Config对象存放 AccessKeyId、AccessKeySecret、endpoint等配置
         */
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(secret);
        // 访问的域名
        config.endpoint = "imageaudit.cn-shanghai.aliyuncs.com";
        Client client = new Client(config);
        ScanTextRequest.ScanTextRequestTasks tasks = new ScanTextRequest.ScanTextRequestTasks()
                .setContent(content);  //审核内容
        ScanTextRequest.ScanTextRequestLabels labels = new ScanTextRequest.ScanTextRequestLabels()
                .setLabel("abuse");  //设置审核类型
        ScanTextRequest scanTextRequest = new ScanTextRequest()
                .setLabels(java.util.Arrays.asList(
                        labels
                ))
                .setTasks(java.util.Arrays.asList(
                        tasks
                ));
        RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        Map<String, String> resultMap = new HashMap<>();
        try {
            // 复制代码运行请自行打印API的返回值
            ScanTextResponse response = client.scanTextWithOptions(scanTextRequest, runtime);

            //System.out.println(com.aliyun.teautil.Common.toJSONString(TeaModel.buildMap(response)));
            if (response.getStatusCode() == 200) {

                ScanTextResponseBody.ScanTextResponseBodyDataElementsResults elementsResults = response.body.getData().elements.get(0).results.get(0);
                if (!elementsResults.suggestion.equals("pass")) {
                    resultMap.put("suggestion", elementsResults.suggestion);
                    resultMap.put("label", elementsResults.label);
                    return resultMap;
                }
                resultMap.put("suggestion", "pass");
                return resultMap;
            } else {
                return null;
            }


        } catch (TeaException error) {
            // 获取整体报错信息
            System.out.println(com.aliyun.teautil.Common.toJSONString(error));
            // 获取单个字段
            System.out.println(error.getCode());
            error.printStackTrace();
        }
        return null;
    }
}