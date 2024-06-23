package cn.whu.schedule.hzaTest;

public class HzaTest {

    public static void main(String[] args) {
        String futureKey = "future_100_50";
        String topicKey = "topic_" + futureKey.substring("future_".length());
        System.out.println(topicKey);
        // futureKey: future_100_50
        // topicKey: topic_100_50
    }

}
