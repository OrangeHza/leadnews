package cn.whu.common.constants;

public class WemediaConstants {

    public static final Short COLLECT_MATERIAL = 1;//收藏

    public static final Short CANCEL_COLLECT_MATERIAL = 0;//取消收藏

    public static final String WM_NEWS_TYPE_IMAGE = "image";

    public static final Short WM_NEWS_NONE_IMAGE = 0; // 无图·封面
    public static final Short WM_NEWS_SINGLE_IMAGE = 1; // 单图封面
    public static final Short WM_NEWS_MANY_IMAGE = 3; // 多图封面
    public static final Short WM_NEWS_TYPE_AUTO = -1; // 自动封面类型

    public static final Short WM_CONTENT_REFERENCE = 0; //图片的引用类型: 0内容引用  引用为内容图片
    public static final Short WM_COVER_REFERENCE = 1; //图片的引用类型: 1主图引用  引用为封面图片
}