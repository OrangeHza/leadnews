package cn.whu.kafka.sample;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * 消费者
 */
public class ConsumerQuickStart {

    public static void main(String[] args) {

        // 1. kafka的配置信息
        Properties properties = new Properties();
        //kafka的连接地址
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.141.102:9092");
        //消息的反序列化器
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        //消费者组 ★
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "group2");

        // 手动提交偏移量
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // 2. 创建消费者对象
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);

        // 3. 订阅主题
        consumer.subscribe(Collections.singletonList("topic-first"));

        // 4. 拉取消息
        /*while (true) {
            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(1000));//每s拉取一次
            //System.out.println("isEmpty: " + consumerRecords.isEmpty());
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                System.out.println(consumerRecord.key());
                System.out.println(consumerRecord.value());
                System.out.println(consumerRecord.partition());// 当前消息存储在哪个分区 打印分区号(目前就一个kafka服务器)
                System.out.println(consumerRecord.offset());

                // 手动写代码提交，也有3种手动方式
                // 1) 同步提交偏移量
                *//*try {
                    consumer.commitSync();
                } catch (CommitFailedException e) {
                    System.out.println("记录提交失败的异常: " + e);
                }*//*
            }

            // 2) 异步方式提交偏移量
            consumer.commitAsync(new OffsetCommitCallback() {
                @Override
                public void onComplete(Map<TopicPartition, OffsetAndMetadata> map, Exception e) {
                    if (e != null) {
                        System.out.println("记录错误的提交偏移量: " + map + ",异常信息为:" + e);
                    }
                }
            });
        }*/

        // 3) 手动，同步和异步方式提交偏移量
        try {
            while (true) {
                ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(1000));//每s拉取一次
                //System.out.println("isEmpty: " + consumerRecords.isEmpty());
                for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                    System.out.println(consumerRecord.key());
                    System.out.println(consumerRecord.value());
                    System.out.println(consumerRecord.partition());// 当前消息存储在哪个分区 打印分区号(目前就一个kafka服务器)
                    System.out.println(consumerRecord.offset());
                    // a、先异步提交偏移量
                    consumer.commitAsync();
                }
            }
        } catch (Exception e) {
            System.out.println("记录错误的信息: " + e);
        } finally {
            // b、异步失败，再用同步提交方式去重试提交
            consumer.commitSync();
        }

    }

}
