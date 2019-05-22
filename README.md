# fpsviewer [![](https://jitpack.io/v/silencedut/fpsviewer.svg)](https://jitpack.io/#silencedut/fpsviewer)
A Real-time Fps Tool for Android,

一个能实时显示fps,一段时间的平均帧率，以及帧率范围占比，并能获取卡顿堆栈的可视化工具。侵入性低，通过在异步线程采样获取堆栈，无代码侵入，性能消耗可忽略，对性能监控项的异常数据进行采集和分析，整理输出展示相应的堆栈，从而帮助开发者开发出更高质量的应用。

## 常见分析、定位卡顿的方案

#### 系统工具

**1. TraceView**

目前一般是用现在AndroidStudio里的cpu-profile工具或者`TraceCompat.beginSection()`生成trace日志，
准确性高，这种分析方式只适合定性分析，因为工具很消耗cpu，有很多假jank，非常影响性能，显示耗时和实际耗时偏差很大，平常开发过程中也不易用，不可能实时开着，没法查看fps。

**2. [Systrace](https://developer.android.com/studio/profile/systrace/command-line)**
Systrace用来检测android系统各个组件随着时间的运行状态，并能提示该如何有效地修复问题，主要偏向于分析一段时间整个系统所处的状态，无法定位到具体代码。

**3. 命令行adb shell dumpsys SurfaceFlinger --latency com...包名**
用来计算一段时间的帧率，无法获取卡顿栈，只能一段时间

以上几种系统提供的方案一般只能在比较短的时间进行分析，平常开发过程中也很不方便。

#### 第三方库方案

* [Matrix-TraceCanary](https://github.com/Tencent/matrix)
微信的卡顿检测方案，采用的ASM插桩的方式，支持fps和堆栈获取的定位，但是需要自己根据asm插桩的方法id来自己分析堆栈，定位精确度高，性能消耗小，比较可惜的是目前没有界面展示，对代码有一定的侵入性。如果线上使用可以考虑。

* [BlockCanaryEx](https://github.com/seiginonakama/BlockCanaryEx)
主要原理是**利用loop()中打印的日志**，loop()中打印的日志可以看鸿洋的这篇博客[Android UI性能优化 检测应用中的UI卡顿](https://blog.csdn.net/lmj623565791/article/details/58626355)，支持方法采样，知道主线程中所有方法的执行时间和执行次数，因为需要获取cpu以及一些系统的状态，性能消耗大，不支持fps展示，尤其检测到卡顿的时候，会让界面卡顿很久。之前我们项目用的就是这个工具。

* [fpsviewer](https://github.com/SilenceDut/fpsviewer/)
利用Choreographer.FrameCallback来监控卡顿和Fps的计算，异步线程进行周期采样，当前的帧耗时超过自定义的阈值时，将帧进行分析保存，不影响正常流程的进行，待需要的时候进行展示，定位。

## fpsviewer 特点

1. 无损FPS实时显示，一段时间的平均帧率和帧率占比
利用Choreographer.FrameCallback的`fun doFrame(frameTimeNanos: Long)`方法回调里获取数据计算每帧消耗的时长，实时性高且不需要额外的数据获取无其他性能消耗，开启和关闭fpsviewer对帧率的影响远小于1帧。支持一段时间的平均帧率和帧率占比显示，可用于性能优化前后的对比。

2. 更详细的堆栈信息，便于定位卡顿源，采样的方式或者堆栈，而不是卡顿发生的瞬间获取堆栈可能造成堆栈偏移从而影响准确性，异步线程在自定义的采样时间(一般>30ms)进行堆栈获取，每次的耗时很小，只有发送卡顿才需要存储分析，也是在异步线程，对主线程无影响，对整体cpu的影响也可忽略不计。一般得到的是多个堆栈信息，按照这段时间的发生次数排序

3. 支持自定义的堆栈标记，类似于` TraceCompat.beginSection()`,方便分析具体业务的卡顿，比如某个列表，某次Activity或者App的启动过程中的卡顿耗时。



## 效果图

**[实时fps]**

![image](http://ww2.sinaimg.cn/large/006tNc79gy1g3ag03fggbj30f007m74c.jpg)

**[功能选择]**

![image](http://ww4.sinaimg.cn/large/006tNc79gy1g3ag122ujdj30f00wiwfw.jpg)

点击分析图标显示：

![image](http://ww3.sinaimg.cn/large/006tNc79gy1g3afwnaze6j30f00wiab1.jpg)

上图折现里点击具体的卡顿点可查看详细的堆栈信息如下：
**[详细堆栈图]**
![image](http://ww1.sinaimg.cn/large/006tNc79gy1g3afze991fj30f02r4jwm.jpg)

首页或者上图点击**bug**图标可展示一段时间的卡顿列表
![image](http://ww4.sinaimg.cn/large/006tNc79gy1g3agjlm2mej30f00wign9.jpg)

可长按删除或者进入详细堆栈界面标记为已解决，点击可进入详细的堆栈信息界面，**TestSection**就是自定义的TAG。

## 引入

**Step1.Add it in your root add build.gradle at the end of repositories:**

```java
allprojects {
	repositories {
		..
		maven { url 'https://jitpack.io' }
	}
}
```


**Step2. Add the dependency:**

```java
dependencies {
    debugImplementation "com.github.silencedut.fpsviewer:fpsviewer:latestVersion"
    releaseImplementation "com.github.silencedut.fpsviewer:fpsviewer-no-op:latestVersion"
}
```

** 初始化，按需添加Section

```java

interface IViewer {
    fun initViewer(application: Application,fpsConfig: FpsConfig? = null)
    fun fpsConfig():FpsConfig
    fun appendSection(sectionName:String)
    fun removeSection(sectionName:String)
}

```

## License


```
Copyright 2018-2019 SilenceDut

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
