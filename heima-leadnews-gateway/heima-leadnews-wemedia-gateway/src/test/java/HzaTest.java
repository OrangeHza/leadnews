public class HzaTest {
    public static void main(String[] args) {
        Integer i1 = new Integer(1);
        Integer i2 = new Integer(1);
        System.out.println((i1>=i2)&&(i1<=i2)&&(!i1.equals(i2)));
        System.out.println((i1>=i2)&&(i1<=i2)&&(i1!=i2));

        Integer i3 = 1;
        Integer i4 = 1;
        System.out.println((i3>=i4)&&(i3<=i4)&&(!i3.equals(i4)));
        System.out.println((i3>=i4)&&(i3<=i4)&&(i3!=i4));

        // java == 和!= 比较的引用   equals往往比较的是内容 Integer类肯定重写了这个方法
    }
}
