package cn.whu.minio.test;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;

import java.io.FileInputStream;

public class MinIOTest {

    /**
     * 把list.html文件上传到minio中，并且可以在浏览器中访问
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            // 0. 先读取前面生成的静态文件，以便上传
            FileInputStream fis = new FileInputStream("E:/prodata/leader-news/list.html");

            // 1. 获取minio的连接信息，创建一个minio客户端
            MinioClient minioClient = MinioClient.builder()
                    .credentials("minio", "minio123")//用户名和密码
                    .endpoint("http://192.168.141.102:9000")//minio服务器地址
                    .build();

            // 2. 上传
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object("list.html") //文件名称
                    .contentType("text/html") //文件类型
                    .bucket("leadnews") //桶名称 与minio管理界面创建的桶一致即可
                    .stream(fis, fis.available(), -1) // 输出流 流大小 上传多少(-1表示全部)
                    .build();
            minioClient.putObject(putObjectArgs);

            // 3. 访问路径
            System.out.println("访问路径: minio服务地址+桶名称+文件名称");
            System.out.println("http://192.168.141.102:9000/leadnews/list.html");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}
