
# opencv-android studio集成opencv-sdk

最近看了一些opencv的相关内容，这里做一下记录以及学习中的体会，和大家分享一下，本人在学习的时候也是网上查阅很多人的博客。 

现在都是开源时代，opencv也是一个开源库，也有官网和社区，所以简介什么的大家去[官网](https://opencv.org/)看看，了解什么是opencv，用来干什么的，这里就不罗嗦了。 

## 1、下载 

[各个版本下载地址](https://opencv.org/releases.html)，

可以根据自己的工作平台下载对应的资源，本人实在android的领域下使用opencv，所以下载的是**3.4.5 Android pack**，后面的内容也是**针对opencv在android开发中的使用。**

![opencv skd 版本](https://wangyt-imgs.oss-cn-beijing.aliyuncs.com/blog/opencv-sdk-android-studio%E9%9B%86%E6%88%90/opencv-sdk-release.png)


## 2、sdk结构 

下载解压后的文件结构如下

![opencv sdk 文件结构](https://wangyt-imgs.oss-cn-beijing.aliyuncs.com/blog/opencv-sdk-android-studio%E9%9B%86%E6%88%90/opencv-sdk-%E6%96%87%E4%BB%B6%E7%BB%93%E6%9E%84.png)

* apk：这个包下面是opencv-manager安装包，具体作用后面在细说。
* samples：是opencv官方提供的几个demo工程，有工程源代码，也有打包好的apk。
* sdk：这个是重点，以后开发的时候也是用的这里面的东西。 
    * etc：识别相关的级联分类器之类的，目前我也不太懂，前期学习也用不到。 
    * java：这是opencv官方提供的一个opencv的android库工程，提供了完整的opencv能力，因为opencv底层是用c/c++写的，但是现在编程语言很多，java、python等等，所以官方就针对不同的语言平台，对底层库进行了二次封装，使用的时候将该该工程直接作为库导入即可，后面会细说。
    * native：一些native层的库 
        * 3rdparty：第三方的一些库 
        * jni：一些cmake编译脚本和动态库的头文件 
        * libs：官方根据不同平台架构打好的.so动态库，提供完整的opencv能力，体积稍大，单个架构对应的.so文件体积在10M以上 
        * staticlibs：将不同的功能分别做成.a静态库，可以根据使用到的opencv能力，选择加载相应的.a静态库，有利于降低应用体积。

 ## 3、开发机制
 
opencv底层是c/c++写的，语言门槛高，另外涉及的数学、图形学等知识也加大了学习难度，所以官方针对不同水平和应用方向的人员提供了不同的开发方式，以造福广大的opencv能力需要者。

**1、要不要在项目中配置sdk中native库？**

在使用opencv能力之前，需要加载opencv提供的native库，opencv提供了两种方式：

* 从应用本地包下面加载，要求相关的native层的库、头文件等要拷贝到项目工程中，并配置好，应用运行时就会按照配置从应用本地进行加载。
* 设备上提前安装 opencv-manager，是一个apk安装包，以aidl的方式向其他应用提供opencv能力，所以开发应用时就不需要在拷贝native库什么的到项目中，因为app运行时可以通过aidl方式，从opencv-manager中进行加载。 

**为什么opencv要提供这两种方式呢？**

原因就在于上面说过opencv的native库文件体积比较大。

如果是从本地包进行加载，设备上安装的应用很多都使用opencv的话，每个应用下都要拷贝一份native库文件，浪费存储空间，另外项目中配置native库也相对繁琐。

虽然设备上提前安装 opencv-manager的方式节省了存储空间，但是一个硬伤就是不能保证用户设备上实现安装了opencv-manager，再说了，用户也不会为了你的一个app而提前安装另外一个app，用户才不会听你的。 

所以，自己学习或者开发demo的时候可以用提前安装 opencv-manager的方式，省去了拷贝、配置native库的操作，也节省了手机空间。但是如果开发正式的app还是老老实实的从本地包加载为好。 

**2、涉不涉及ndk开发？**

android开发目前来说java仍是主流，kotlin虽然越来越流行，但是对java是全兼容的。 opencv底层是c/c++实现的。 所以android中使用opencv能力就需要解决两种语言通信的问题，使用的方案当然是jni啦，opencvsdk的java工程对jni调用opencv方法进行了封装，开发时直接调用封装好的java方法即可实现opencv能力的调用，很爽吧，不用和c/c++打交道。

但是毕竟opencv底层是c/c++实现的，想要100%使用opencv的能力，或者sdk的java库中提供的方法不能满足需求，还是需要自己使用c/c++去实现一部分功能，自行封装成jni方法使用。再说了现在都是团队开发，可以有专门的小伙伴实现底层的工作，对吧，这就需要拷贝、配置相关的native库，使用ndk进行开发。

当然了，更厉害的大神可以下载源码进行修改，自行编译，不仅可以得到最新的sdk，而且能够根据需要，自行实现底层相关算法之类的。 

这里对不同的开发机制的适用场景进行简单的总结：

1. java库+opencv-manager.apk：适用于个人学习或者做demo，在设备上事先安装opencv-manager.apk，然后项目中导入java库工程，即可进行后续开发。
2. java库+native库：对opencv能力没有特殊要求的情况下，开发线上应用。
3. java库+native库+自行封装实现的jni方法：开发线上应用，但是java库提供的opencv能力不能满足业务需求时使用这种方式。
## 4、开发实战

经过上面的讨论，对android下opencv的开发机制有了大概了解，下面的分别对几种开发方式进行简单的实现。

**1、java库+opencv-manager.apk**

**准备**

需要准备的东西是sdk的java库工程以及设备平台对应的opencv-manager.apk

**配置**

1. 在设备上安装opencv-manager.apk
2. 在android studio中新建一个工程，已经有的话直接打开
3. 导入sdk的java库工程：`File-New-Import module`，定位到java库工程，导入后库工程的module name 为openCVLibrary343
4. 配置主module依赖openCVLibrary343：在主modlue的guild.gradle文件的dependencies节点下添加`implementation project(':openCVLibrary343')`，然后同步代码即可。

**开发**

在 Activity 的 onResume 方法中初始化opencv

```java
        //使用OpenCV Engine service，需要运行设备事先安装OpenCV Manager
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                if (status == LoaderCallbackInterface.SUCCESS) {
                    Log.d(TAG, "onManagerConnected: success");
                } else {
                    super.onManagerConnected(status);
                }
            }
        });
```

对图片进行均值模糊

```java
//本地加载图片
Bitmap originImg = BitmapFactory.decodeFile("filePath");
//初始化Mat
Mat src = new Mat(originImg.getHeight(), originImg.getWidth(), CvType.CV_8UC4);
//bitmap转mat
Utils.bitmapToMat(originImg, src);
//均值模糊
Imgproc.blur(src, src, new Size(3, 3));
//初始化处理后bitmap
Bitmap processedImg = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
//mat转bitmap
Utils.matToBitmap(src, processedImg);
```

**2、java库+native库**

和上种方式的区别在于：

* 不需要事先安装opencv-manager.apk
* 需要在项目中配置native库

**准备**

需要准备的东西是sdk的java库工程、native库（sdk-native-libs）

**配置**

1. 导入java库工程，并配置依赖，具体步骤在【java库+opencv-manager.apk】开发方式中有描述
2. 将libs文件夹复制拷贝到项目工程的src-main-jniLibs路径下，如果jniLis文件夹不存在就自己新建一个
3. 打开module的build.gradle文件，在 android 节点下添加如下代码，然后同步代码即可

```gradle
    sourceSets {
        main {
            jniLibs.srcDirs = ['src/main/jniLibs/libs']
        }
    }
```

**开发**

和【java库+opencv-manager.apk】开发方式一样。

**3、java库+native库+自行封装实现的jni方法**

这种方式是在【java库+native库】开发方式的基础上使用ndk进行jni开发。

**准备**

需要准备的东西是sdk的java库工程、native库（sdk-native-libs）、native库头文件（sdk-native-jni-include)

**配置**

1. 参考博客【[音视频开发01–AS3.x NDK开发环境搭建](https://www.jianshu.com/p/f9e244211293)】建立支持ndk开发的项目。
2. 完成【java库+native库】开发方式中的相关配置。
3. 将native库头文件（sdk-native-jni-include）拷贝到src-main-cpp目录下（如果没有该目录，请先按照【[音视频开发01–AS3.x NDK开发环境搭建](https://www.jianshu.com/p/f9e244211293)】的过程进行操作）
4. 编辑 CMakeLists.txt 文件内容（如果没有该文件，请先按照【音视频开发01–AS3.x NDK开发环境搭建】的过程进行操作）
5. 配置比较繁琐，如果出现问题，请积极搜索，学习android studio cmake ndk开发的相关知识。

```
cmake_minimum_required(VERSION 3.4.1)

include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/include)

add_library(libopencv_java3 SHARED IMPORTED)
set_target_properties(libopencv_java3 PROPERTIES IMPORTED_LOCATION
             ${CMAKE_SOURCE_DIR}/src/main/jniLibs/libs/${ANDROID_ABI}/libopencv_java3.so)

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp )

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

target_link_libraries( # Specifies the target library.
                       native-lib libopencv_java3

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )
```

**开发**

看demo吧，就是jni开发那一套。

## 总结

android中使用opencv进行开发时，针对不同的开发和应用场景选择开发方式，总的来说涉及的东西挺多的，android知识、ndk/jni知识、c/c++知识、opencv知识、数学知识等。

学习的时候以android 开发为基础，通过本文搭建起开发框架，在写demo的过程中学习opencv api和相关的理论知识，同时也学习了ndk/jni和c/c++。