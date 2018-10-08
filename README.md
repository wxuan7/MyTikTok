# MyTikTok
>因为抄袭的人太多了,所以在这里正式声明三个平台的我本人的账号:

[简书：何时夕](https://www.jianshu.com/u/45661204c0d6)

[掘金：何时夕](https://juejin.im/user/5a74437bf265da4e896aa1ed/posts)

[今日头条：何时夕阳](https://www.toutiao.com/c/user/84868379568/)

**文章列表**：
- [1.从零开始仿写一个抖音App——开始](https://www.jianshu.com/p/e92bd896ac35)
- [2.从零开始仿写一个抖音App——基本架构图与MVPs](https://www.jianshu.com/p/3867f6cf4e82)
- [3.从零开始仿写一个抖音App——Apt代码生成技术、gradle插件开发与protocol协议](https://www.jianshu.com/p/f71cd4c91df8)
- [4.从零开始仿写一个抖音App——日志和埋点以及后端初步架构](https://www.jianshu.com/p/a957098fe9ea)
- [5.从零开始仿写一个抖音App——app架构更新与网络层定制](https://www.jianshu.com/p/c47eff0c57a7)

**有人说我标题党，也怪我开始决定写的时候没有注意标题，也没想到会有这么多阅读量，的确会生出一些是非出来。那么从现在开始标题改为 从零开始仿写一个抖音App 系列。希望技术讨论能够多一点，没看文章就喷的人能够少一点。我会坚持写下去，好好提升自己的能力。**

# 如何开始编译运行项目

![图24：根 build.gradle.png](https://user-gold-cdn.xitu.io/2018/8/28/1657fdafc9ccbffc?w=1222&h=598&f=png&s=99405)
![图25：app module build.gradle.png](https://user-gold-cdn.xitu.io/2018/8/28/1657fdafc8e816c2?w=842&h=554&f=png&s=99803)
![图26：invoker gradle properties.png](https://upload-images.jianshu.io/upload_images/2911038-e688d06e21d998b7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![图27：invoker gradle文件.png](https://upload-images.jianshu.io/upload_images/2911038-214fa35eedeefae3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![图28：maven目录.png](https://upload-images.jianshu.io/upload_images/2911038-6d59b8bbf590891e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![图29：运行 upload.png](https://upload-images.jianshu.io/upload_images/2911038-e8a7a7775d65146e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 1.关掉 instant run
- 2.使用插件有两个步骤：
    + 1.在根目录的 build.gradle 文件里面引入插件的代码库。这里可以先注释掉，等本地 maven库建好之后再引用。如图1
    + 2.在需要使用插件的 module 中引入插件。现在可以先注释掉app module 插件中的引用，等待插件上传成功的之后再引用。如图2
- 3.可能会有人奇怪了，我运行了项目之后报错了啊！说是找不到这个插件。这里我们应该了解一下关于 Maven 的一些知识。
    + 1.Maven 是一种构建源代码的工具，他会将某些源代码以某种格式(Project Object Model)进行打包，这样我们就能很方便的引用某个别人开源的代码库了。
    + 2.gradle 中能够使用 Maven 包，使用的方法就是大家在 dependencies 块里面的引用方式。
    + 3.在图1中我们可以看见 repositories 块里面写着好几行代码，每一行都表示一个 Maven 库。有 google 的、有 jcenter 的、最后一个是我本地的 Maven 库。当我们引用一个包的时候，gradle 就会去这些库里面找相应的 Maven 包然后下载下来供项目使用。
- 4.怎么新建一个本地 Maven 库呢？很简单:
    + 1.将本地某个**空目录路径**设置为仓库根目录，比如我在 mac 下的库根目录就如图1所示。
    + 2.比如我们的项目需要将 generate-code/invoker 这个 module 上传到本地库中，那么就在该 module 中新建一个 gradle.properties 文件，如图3，这样在本 module 的 gradle 脚本中就能读取里面的配置
    + 3.注意 gradle.properties 以及 build.gradle 文件里面引用的路径需要是**你自己设置的本地路径。**
    + 4.我们需要再在 module 的 gradle 文件里面添加一个 maven 插件，然后写一个上传方法 uploadArchives。如图4.
    + 5.在**Gradle project窗口运行 uploadArchives任务，这样就上传了 invoker 插件，如图6。**
- 5.最后将第二步中说到的插件的引用和代码库的引用的注释去掉，重新 clean build 一下就可以在app module 里面使用插件了。


