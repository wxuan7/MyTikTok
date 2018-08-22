# 从零开始写一个抖音app————基本架构与MVPs
**本文首发于简书——何时夕，搬运转载请注明出处，否则将追究版权责任。交流qq群:859640274**

# 连载文章
- [1.从零开始写一个抖音app——开始](https://www.jianshu.com/p/e92bd896ac35)
- [3.从零开始写一个抖音App——Apt代码生成技术、gradle插件开发与protocol协议](https://www.jianshu.com/p/f71cd4c91df8)
# [本项目的 github 地址：MyTikTok](https://github.com/whenSunSet/MyTikTok)

>大家好久不见，距离上次发博客已经三个星期过去了，很惭愧没有达到两周更一次的目标。但在中间的一周我还是收获挺大的，所以在文章的开始与大家分享两个问题。

## 一、程序员的打字速度与Vim
### 1.程序员是否应该有比较快的打字速度？
>这一节，不感兴趣的同学可以跳过不看。这个问题是我某天逛知乎看见的，[打字速度对编程的影响大吗？](https://www.zhihu.com/question/30325795)。我想就像哈姆雷特一样，每个人对这个问题的想法都不一样。

**正方：程序员应该有比较快的打字速度**
- 1.厉害的程序员的思想非常快，需要有一个能更跟得上思想的手速
- 2.作为一个程序员打字速度不快，怎么装逼呢？
- 3.I was trying to figure out which is the most important computer science course a CS student could ever take, and eventuallly realized it's Typing 101. ———— Steve Yegge

**反方：程序员打字速度快不快没啥关系**
- 1.我有个同事打字速度不快也很牛逼
- 2.编程靠的是思想，不是手速

我的话还是同意正方的观点的，所以我抛弃了使用多年的**双手二指禅**，花了一周的业余时间练习标准打字指法。现在大家看见的这篇文章就是用标准指法打出来的。速度的话这一周时间我的打字速度已经恢复到之前的水平了。[指法练习](https://www.typingclub.com/sportal/program-3.game)，这个网站是我这周练习的网站。有兴趣的同学可以玩玩，我一直推崇的一句话是：**人不能呆在舒适区中。**这也是我下决定花时间改变我的指法的一个原因，大家也可以尝试改变一下，可能可以收获一些东西。

### 2.程序员是否应该去学习Vim
>这个争论也已经很久了，我今天就说说我自己决定用时间去学习Vim的原因吧。
- 1.我在学习我们公司底层的音视频源码的时候，因为工程是android和ios公用的，光一个Android Studio是运行不起来demo的，所以需要运行各种脚本和使用各种命令行。在这期间就因为Vim和shell用的不熟花了比较多的时间。我想要深入Android底层，这两个工具是必不可少的吧。
- 2.在各种IDE之间切换的时候，总是会碰见快捷键之间不统一的问题。JetBrains家族的IDE还好，但是有时也会用VS Code，再加上VisualStudio之类的。这样就没有一个统一的规范，所以我想着是不是能用Vim来统一一下。

## 二、MyTikTok基本架构
>这一节开始言归正传，在这一节中我会把整个app未来的moudle组成，以及配置结构讲一下。

### 1.项目
这是本项目的github地址：[MyTikTok](https://github.com/whenSunSet/MyTikTok)，**下面有很多代码实现都是项目中的实现，所以建议大家结合项目食用博客！另外项目求star、求fork！**

### 2.项目结构

![MyTikTok架构图.png](https://upload-images.jianshu.io/upload_images/2911038-89e8398f54502bee.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
上面这个是项目的架构图，下面我们自底向上来讲解一下。

- 1.最底层是sdk层，这一层分为两部分。一部分是第三方的sdk这一部分是一些基础库。另一部分是自制的sdk，像视频、深度学习、聊天等等这些库会用到c++的代码，所以到时候会另外创建几个项目来专门写这些东西，然后作为sdk集成到主项目中。以供APPFramework和APPExternal使用。
- 2.第二层分为两块：这一层最终会供给AppPlugin层使用
    + 1.AppFramework：这一部分顾名思义，表示是用java写的app的基础层。比如Router表示路由组件、比如Http表示网络组件、再比如Video表示对底层视频sdk的再封装。
    + 2.AppExternal：这一部分是外部的一些需要封装的sdk的封装层。比如bugly需要再封装一下。
- 3.第三层是AppPlugin层，这一层在第二层的基础上写各种业务，各种业务以插件形式解耦。最终会使用插件化技术被插入到各个不同的app版本中去。理论上来说每一个插件都能独立运行，每个插件之间是用路由的形式互相调起activity。
- 4.最顶层是App层，这一层表示各种不同版本的App。比如我在小米应用市场可以发布带有聊天功能的App，而在华为应用市场则发布没有这个功能的App。

### 3.项目配置
>gradle配置在大型项目中的使用一直是我不熟悉的，所以我学习了我司的一些配置方式，这一节就来讲一讲。

![version.png](https://upload-images.jianshu.io/upload_images/2911038-cffdee8cdd4b05f8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
version.gradle这个文件用于储存所有用到的第三方sdk的名字与版本，这样一来其他moudule在使用三方sdk之后需要升级的时候就可以只改动version.gradle里面的版本就行了。

![library.png](https://upload-images.jianshu.io/upload_images/2911038-05e3da702af3eb9d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
library.gradle这个文件用于把各个android library moudule中重复定义的东西放在一起，其他moudule只需要引入这个文件就行了

![widget.png](https://upload-images.jianshu.io/upload_images/2911038-0970ab7125fbf85d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
widget.gradle文件就是widget模块的gradle文件，第一行就是引入library,gradle文件来解决重复的配置。下面几行则是引入在version.gradle里面定义的Rxjava这个库。

## 三、MVPs

### 1.MVPs概述
![MVPs.png](https://upload-images.jianshu.io/upload_images/2911038-23f665aed29c8c26.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
上面是MVPs的架构图，接下来我会一个个进行解释

- 1.各个层级的关系：顶层是Activity，Activity管辖着多个Fragment，每个Fragment中会有一个Presenter，来管理Fragment的Root View。接下来根据View的层级，会创建每一层View对应的Presenter，这个Presenter用于管理View的状态和数据。就像图中一样，每个Presenter都可以有子Presenter。
- 2.Presenter的约束：
    + 1.Presenter对使用者来说需要是透明的，也就是说每个Presenter的子类除了 Presenter 接口中定义的方法,不允许有public的方法和变量和有参数构造函数，对使用者来说每个Persenter都可以以使用接口的方式使用。**我看见很多人对这个约束有质疑,所以在这里解释一下:**
        - 1.首先这里的约束是对于开发者来说的,并不是语言上的约束,也就是说这是一个开发契约.在多人合作开发的时候,各个成员需要遵守契约,不可在 presenter 的子类上面增加 public 的方法,变量和有参构造函数
        - 2.为什么要有这个契约,那是为了让 presenter 的 所有子类的行为与 presenter一致,这样在后面的业务变化与重构的时候能实现 presenter 的可插拔,减少耦合度.
        - 3.那么有些情况下 presenter 需要有 public 方法怎么办呢?这个问题我会在后面遇见真实例子的时候讲解.简单说一下就是增加一个 Helper 类来放置这种逻辑.
    + 2.各个层级的Presenter是平等的，同一个Fragment下的所有Presenter包括Fragment本身都可以使用Publisher的方式通信，不同Fragment/Activity的Presenter可以使用XXBus的方式通信。
    + 3.Presenter需要的外部参数可以通过注入的方式实现（**类似Butter knife**），具体的实现方式在后面讲。
    + 4.Presenter需要的View通过**Butter knife**注入
    + 5.组合优于继承，所以Presenter不支持继承
- 3.Presenter的生命周期：图中把Fragment的生命周期简化了
    + 1.init方法只能调用一次，可以在Fragment的view初始化完毕之后调用，将各个Presenter需要的view注入进去。这里的调用是递归的，也就是说子Presenter的init方法也会被调。
    + 2.bind方法可以调用多次，除了在Fragment初始化完毕的时候调用，还可以在Fragment每次重新启动的时候调用，每次调用会重新设置外部注入的非view参数，要求bind方法是**可重入的**，也就是说**多次调用bind方法不会造成内存泄漏**。这里的调用是递归的，也就是说子Presenter的bind方法也会被调。
    + 3.destroy方法在fragment销毁的时候调用，用于回收内存，此外这里的调用是递归的，也就是说子Presenter的destroy方法也会被调用。

根据上面说的这些东西，一个Presenter接口就可以被设计出来了
![presenter接口.png](https://upload-images.jianshu.io/upload_images/2911038-1156c84b81dfd8fe.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
当然项目中有一个实现了Presenter接口的基类，由于篇幅限制就不一一讲解了，有兴趣的同学可以去项目中找BasePresenter这个类来看看，有问题可以加我QQ进行交流。


### 2.如何为Presenter注入参数
>我们在上一节中说到了Presenter中的参数都是由外部注入的，这一节我们来讲讲如何使用APT来自动生成代码，从而减少编写模板代码。

首先我先写一个在项目中写一个demo，当然现在这个demo是不可运行的，目前Presenter的整个流程还没走通，但是并不妨碍大家的理解。
![MainActivity.png](https://upload-images.jianshu.io/upload_images/2911038-90658fff7d618558.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
- 1.首先我们在MainActivity中定义一个变量mTextString在上面添加我们定义的注解@Field并传入一个string作为id。
- 2.接下来我们定义一个TestPresenter继承于我们的基类BasePresenter
![TestPresenter.png](https://upload-images.jianshu.io/upload_images/2911038-f3c42121f6db9bc1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
- 3.这个Presenter很简单，只有一个mTextString和其上面我们定义的注解@Inject，这里也要传入和MainActivity中同名的string作为id。
- 4.再回到MainActivity中， 我在onCreate方法中创建了TestPresenter。先调用init来传入root view来为Presenter绑定view，当然现在Presenter中并没有view。然后调用bind方法来将MainActivity中的mTextString注入到TestPresenter的mTextString中。
- 5.以上就是整个Presenter参数注入的使用流程，当业务复杂了之后这样的注入能减少很多模板代码的编写。简单来说就是通过@Field和@Inject这两个注解，将MainActivity的同id参数注入到TestPresenter中。当然这里的MainActivity可以换成任何内部有同id被@Field注解了的参数的对象。


### 3.为Presenter注入参数的原理
>对于一个技术不知道原理是不行的，所以这一节就来讲讲为Presenter注入参数的原理

- 1.首先我们进入到上一节中说到的TestPresenter的基类BasePresenter的bind方法中。如下图。
![BasePresenter_bind.png](https://upload-images.jianshu.io/upload_images/2911038-eabac4339a06baf7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
- 2.我们可以看见，这里为当前Presenter注入参数的任务主要交给了mInjector的inject方法。这里mInjector在Presenter里面的存在方式是Injector接口。如下图。
![Injector.png](https://upload-images.jianshu.io/upload_images/2911038-11a5db1a24c48321.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
- 3.Injector接口中，inject负责为Presenter注入参数，allNames用于获取Presenter中需要注入的参数的名字，allTypes用于获取Presenter中需要注入的参数的class对象，reset用于在重新调用Presenter的bind方法的时候为需要注入的参数置空。
- 4.那么TestPresenter的mInjector的实现在哪里呢？其实这里就会通过APT在编译期为TestPresenter创建一个TestPresenterInjector类，来执行这个逻辑。我们的项目中在编译之后就有这个类，大家可以搜索这个文件。如下图。
![TestPresenterInjector.png](https://upload-images.jianshu.io/upload_images/2911038-328d7b67760f01e1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
- 5.我们主要关注，TestPresenterInjector的inject方法，可以看见其是先通过ProviderHolder.fetch方法将MainActivity中的mTextString这个参数获取到，然后直接给TestPresenter的mTextString赋值的。**这里我们可以看出无论是目标参数还是被注入参数都不可为private**
- 6.我们再看ProviderHolder.fetch方法是怎么获取到MainActivity的mTextString参数的。如下图。
![ProviderHolder.png](https://upload-images.jianshu.io/upload_images/2911038-0f5938b20d56d727.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
- 7.再进入ObjectProvider的实现类ObjectProviderImpl.fetch中。如下图。
![ObjectProviderImpl.png](https://upload-images.jianshu.io/upload_images/2911038-5b54cae338855a2a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
- 8.可以看见最终是交给了这一行代码：Fetchers.fetcherNonNull(obj.getClass()).get(obj, fieldName)，这里获取到了一个Fetcher的对象之后，再调用其get方法。我们再进入Fetcher看看，如下图。
![Fetcher.png](https://upload-images.jianshu.io/upload_images/2911038-76c416d20ec3d8cf.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
- 9.这里Fetcher也是一个接口，我想到了这里大家应该猜到了，这个Fetcher的实现也是用APT生成的，同样编译期间，APT会为MainActivity生成一个MainActivityFetcher类，来执行获取参数的逻辑。我们搜索这个文件可以看见，如下图。
![MainActivityFetcher.png](https://upload-images.jianshu.io/upload_images/2911038-fc1888042839ce16.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
- 10.这里的实现就简单粗暴了，直接将target变成了MainActivity然后根据id返回相应的参数。
- 11.以上就是整个参数注入的内部原理，**当然有同学会说还不过瘾，我还要了解这几个类到底是怎么生成的，那么就请期待下一篇博文吧！**当然大家也可以直接去看本项目的源码，里面的整个代码生成的实现都已经完成了。

## 四、尾巴
>这是**从零开始写一个抖音app**系列的第二篇博客，这几个星期在我司学到的东西很多。不仅仅是上面博客中写的这些项目架构的东西，这三个星期不断的学习音视频的核心代码也同样让我受益匪浅。**未来当这些知识成体系之后一样会以博客的形式分享给大家，希望大家能持续关注这个系列的博客，也算对我的一个监督，嘿嘿！**






























