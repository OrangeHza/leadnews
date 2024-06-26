package cn.whu.kafka.sample2;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * 生产者
 */
public class ProducerQuickStart {

    public static void main(String[] args) {
        // 1. kafka连接配置信息
        Properties properties = new Properties();
        //kafka的连接地址
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"192.168.141.102:9092");
        //发送失败，失败的重试次数
        properties.put(ProducerConfig.RETRIES_CONFIG,5);
        //消息key的序列化器 死代码 两个序列化器(第二个参数)是一样的
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        //消息value的序列化器  死代码
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");

        //2. 创建kafka生产者对象
        KafkaProducer<String,String> producer = new KafkaProducer<String,String>(properties);

        //3. 发送消息
        /**
         * 第一个参数: topic (消息分类)  :string
         * 第二个参数：消息的key         :string
         * 第三个参数：消息的value       :string
         */
        ProducerRecord<String, String> kvProducerRecord = new ProducerRecord<String, String>(
                "topic-first","key-001","hello kafka");
        producer.send(kvProducerRecord);

        //4. 关闭消息通道 -- 必须要关闭，否则消息发送不成功
        producer.close();

    }
}