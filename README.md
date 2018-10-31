# Android threat intelligence module

## src_0

The first version was inspired by https://github.com/pangliang/android-anti-debug
一个android native工程反调试例子:
check /proc/self/status tracerpid to see if itself was attached by a debugger, then kill. it was launched in a thread.

## src_d20151001

Added features:
    - anti patch  ( so library signature, anti rewrapping )
    - ptrace check   ( deny debugger attachment )
    - java call trace check  ( anti xposed )
    - jni native hook check   ( anti xposed )
    - gcc call trace check   ( anti frida  )
    - classes.dex file signature   ( anti apk rewrapping )
    - tracerpid check
    - thread check  (Issue: unable to exit normally)
    - ld_preload check   
    - android device id

Ovreall strategy:  delayed response shown in g_status


## src_d20151209.zip
 
Added android id module
加入了 root checker 模块，包括: sucheck, port scan, ssh scan, anti-emulator, dex opt, and func pointer validation(重复).


TODO:
  -  merge anti_debug_linux.c  ( proc fork )
  -  201308.c


