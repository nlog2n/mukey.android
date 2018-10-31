package com.nlog2n.mukey;


// 通常在对apk进行反编译的时候用到的最多的两个工具就是apk-tool和dex2jar。
// 利用这两个工具将apk首先反编译成classes.dex然后再将classes.dex反编译成
// jar文件或者将apk直接反编译成jar文件；得到jar文件以后就可以利用JD-GUI将
// 得到的jar文件打开就可以直接查看apk的java源码了。


// 对抗dex2jar反编译工具
// 通常是在分析dex2jar源码后，来找到漏洞，在java源码中加入会触发dex2jar异常的代码，从而使dex2jar不能正常使用。
// 这里使用的是dex2jar版本0.0.7.8，
// http://blog.csdn.net/jltxgcy/article/details/50633490

// 参考代码源码地址https://github.com/jltxgcy/AntiCrack/tree/master/Antidex2jar
// 这个工程是通过在java源码中加入下面函数导致dex2jar不能正常使用的。
// 其实来源于java open source code: BitArray.java:
/*
    private int position(int idx) { // bits big-endian in each unit
        return 1 << (BITS_PER_UNIT - 1 - (idx % BITS_PER_UNIT));        
    }
*/



// 对抗JD-GUI查看源码原理
// https://m.oschina.net/blog/403621
// http://itfish.net/article/39622.html

// 我们在用JD-GUI查看源码时有时有些函数的根本看不到直接提示error错误，我们就利用这点来保护我们的apk。
// 原来JD-GUI在将经过混淆处理的jar里面的class字节码文件转成java文件时，遇到函数中根本走不到的分支的
// 特殊实现时就会提示函数error。这时我们只要查看这些提示error的文件或者函数对应的源码是有什么语句引起的，
// 将这些语句加到我们的源码中就可以防止利用JD-GUI去查看我们的apk源码了。

/*
        举例: 在要保护函数里面加上不可能的特殊分支语句
        switch(0)
        {
        case 1:
        // do something, which will never happen
        break;
        }

        将apk转成jar文件，然后用JD-GUI打开会看到提示error错误。
*/


public class AntiDecompile {


    private static int  internal_var = 0;   // 只是冗余代码

    private static final int BITS_PER_UNIT = 8;

    public void antiDex2Jar() {
        int i = position(internal_var);    // 调用该函数并使用其返回值
        internal_var = i;
    }
    
    private int position(int idx) { // bits big-endian in each unit
        return 1 << (BITS_PER_UNIT - 1 - (idx % BITS_PER_UNIT));        
    }




    public void antiJdGui()
    {
        switch(0)
        {
            case 1:
                // do something, which will never happen
                internal_var += 1;
                break;
        }

    }

    
}


