package cn.whu.common.aliyun;

import com.alibaba.fastjson.JSON;
import com.aliyun.imageaudit20191230.models.ScanImageAdvanceRequest;
import com.aliyun.imageaudit20191230.models.ScanImageResponse;
import com.aliyun.imageaudit20191230.models.ScanImageResponseBody;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "aliyun") // 读取配置 aliyun开头的配置 下面3个属性自动赋值
public class GreenImageScan {

    private String accessKeyId;
    private String secret;
    private String scenes;

    public Map imageScan(List<byte[]> imageList) throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(secret);
        // 访问的域名
        config.endpoint = "imageaudit.cn-shanghai.aliyuncs.com";

        com.aliyun.imageaudit20191230.Client client = new com.aliyun.imageaudit20191230.Client(config);

        List<ScanImageAdvanceRequest.ScanImageAdvanceRequestTask> taskList = new ArrayList<>();

        for (byte[] bytes : imageList) {
            ByteInputStream byteInputStream = new ByteInputStream(bytes, bytes.length);
            ScanImageAdvanceRequest.ScanImageAdvanceRequestTask task = new ScanImageAdvanceRequest.ScanImageAdvanceRequestTask();
            task.setImageURLObject(byteInputStream);
            task.setDataId(UUID.randomUUID().toString());
            task.setImageTimeMillisecond(1L);
            task.setInterval(1);
            task.setMaxFrames(1);
            taskList.add(task);
        }


        //场景
        List<String> sceneList = new ArrayList<>();
        sceneList.add(scenes);
        sceneList.add("logo");
        sceneList.add("porn");
        ScanImageAdvanceRequest scanImageAdvanceRequest = new ScanImageAdvanceRequest().setTask(taskList).setScene(sceneList);
        //com.aliyun.imageaudit20191230.models.ScanImageRequest scanImageRequest = new com.aliyun.imageaudit20191230.models.ScanImageRequest()
        //        .setTask(taskList)
        //        .setScene(sceneList);
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        try {
            ScanImageResponse scanImageResponse = client.scanImageAdvance(scanImageAdvanceRequest, runtime);
            //ScanImageResponse scanImageResponse = client.scanImageWithOptions(scanImageRequest, runtime);
            Map<String, String> resultMap = new HashMap<>();

            if (scanImageResponse.getStatusCode() == 200) {

                List<ScanImageResponseBody.ScanImageResponseBodyDataResultsSubResults> subResults = scanImageResponse.body.data.results.get(0).getSubResults();

                ListIterator<ScanImageResponseBody.ScanImageResponseBodyDataResultsSubResults> listIterator = subResults.listIterator();
                while (listIterator.hasNext()) {
                    ScanImageResponseBody.ScanImageResponseBodyDataResultsSubResults item = listIterator.next();
                    System.out.println("scene = [" + item.scene + "]");
                    System.out.println("suggestion = [" + item.suggestion + "]");
                    System.out.println("label = [" + item.label + "]");

                    if (!item.suggestion.equals("pass")) {
                        resultMap.put("suggestion", item.suggestion);
                        resultMap.put("label", item.label);
                        return resultMap;
                    }
                }
                resultMap.put("suggestion", "pass");

            } else {
                /*   *
                 * 表明请求整体处理失败，原因视具体的情况详细分析
                 */
                System.out.println("the whole image scan request failed. response:" + JSON.toJSONString(scanImageResponse));
                return null;
            }


        } catch (com.aliyun.tea.TeaException teaException) {
            // 获取整体报错信息
            System.out.println(com.aliyun.teautil.Common.toJSONString(teaException));
            // 获取单个字段
            System.out.println(teaException.getCode());
        }
        return null;

      /*  Map<String, String> resultMap = new HashMap<>();
        resultMap.put("suggestion", "pass");
        return resultMap;*/
    }
}