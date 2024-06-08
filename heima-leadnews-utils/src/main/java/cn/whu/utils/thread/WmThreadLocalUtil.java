package cn.whu.utils.thread;

import cn.whu.model.wemedia.pojos.WmUser;

public class WmThreadLocalUtil {

    // 每new一个ThreadLocal<>()，就是一个map的key-value对  new多个就可以存储多个k-v对
    private final static ThreadLocal<WmUser> WM_USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 添加用户
     *
     * @param wmUser
     */
    public static void setUser(WmUser wmUser) {
        WM_USER_THREAD_LOCAL.set(wmUser);
    }

    /**
     * 获取用户
     */
    public static WmUser getUser() {
        return WM_USER_THREAD_LOCAL.get();
    }

    /**
     * 清理用户
     */
    public static void clear() {
        WM_USER_THREAD_LOCAL.remove();
    }
}