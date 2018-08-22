# 从零开始写一个抖音App——Apt代码生成流程、gradle插件开发与protocol协议
**本文首发于简书——何时夕，搬运转载请注明出处，否则将追究版权责任。交流qq群：859640274**

# [本项目的 github 地址：MyTikTok](https://github.com/whenSunSet/MyTikTok)

>大家好，两周不见技术有没有增长呢？本周的文章主要讨论下面几个问题，大家可以按需跳章查看以节省大家宝贵的时间，**本文预计阅读时间10分钟**。

- 1.讨论——总结前两周评论中有意义的讨论并给予我的解答
- 2.mvps代码生成原理——将上周的 mvps 架构的代码生成原理进行解析
- 3.开发一款gradle插件——从 mvps 的代码引出 gradle 插件的开发
- 4.高效的跨语言数据协议protocol——protocol 数据协议在 android 项目中的使用以及优势

## 一、讨论
>在放上讨论之前我需要重申**本项目的意义、初衷和前提**：

- 1.本项目希望复刻大厂开发大 app 的流程和模式，这样不仅对我自己是一个提升，对广大读者来说也是一个了解大厂的好机会。
- 2.既然是复刻大厂的流程，大家就应该以**一人分饰多角**的角度来看待项目的结构、架构与开发过程，比如
    + 1.写业务的同学应该想的是怎么让业务代码高效高可复用。
    + 2.写架构的同学应该想的是怎么在对业务同学透明的前提下减少模板代码，为框架添加一些**”工具糖“**让业务同学写起代码来更爽，使用规则的限制让业务同学**”带着镣铐码代码“**使得项目代码不会随着时间推移而**”膨胀腐化“**。
    + 3.写底层算法的同学应该想的是怎样让算法更高效，让底层算法对业务同学透明，同时让他们用起算法来更方便。
    + 4.**在学习本项目的过程中，如果能随时切换上面的三个视角，那么一些困惑就会迎刃而解。**
- 3.对于大项目来说**”麻烦就是方便“**，一些目前看起来麻烦的操作都是为了以后项目的可控性。
- 4.**最后也希望大家在讨论一个问题的时候：有异议就拿出自己的想法和论据来进行讨论，我对于这种评论都会认真回复。如果是毫无论据的攻讦我会用同样的方式回怼。**

**讨论1：为啥不用viewmode/mvvm框架？**

- 1.mvp 现在还是主流的 app 开发架构。
- 2.光 livedata 和 viewmodel 如果不用 databinding 的话并不能成为 mvvm 架构。
- 3.如果用了 databinding，databinding 不成熟也是一个坑。
- 4.谷歌的架构组件也可以加入到mvp里面，我在第一篇文章中叙述过。

**讨论2：架构感觉不是很简洁？**

- 1.参考**本项目的意义**

**讨论3：apt 的这套操作，只是为了 Presenter 的初始化传参，如果使用注解通过 apt 生成 build 对象，是否简单很多？**

- 1.参考**本项目的意义中的一人分饰多角**

**讨论4：基本的接口定义都瞎写…不允许有public…可怕!**

- 1.首先这里的约束是对于开发者来说的，并不是语言上的约束，也就是说这是一个开发契约。在多人合作开发的时候，各个成员需要遵守契约，不可在 presenter 子类上面增加 public 的方法，变量和有参构造函数。
- 2.为什么要有这个契约？那是为了让 presenter 的所有子类的行为与 presenter 一致，这样在后面的业务变化与重构的时候能实现 presenter 的可插拔，减少耦合度。
- 3.**参考本项目意义中的”带着镣铐码代码“**

**讨论5：因为是按模块划分的，所有模块都使用mvp模式开发是否够稳妥，我觉得每个模块应该根据具体业务场景，来选择适合的架构模式，有些适用mvc，有些适用mvvm，有些适用mvp。**

- 1.大的工程，不可能使用多种架构混合的，这样不同的开发人员使用的架构不同，不具备思想一致性，开发起来会非常困难，**参考本项目的意义**.
- 2.mvc 只适用于小的项目的架构，我想很多事实已经证明 mvc 会使 Activity/Fragment 的代码 膨胀，就算是按现在分模块，到了后面一个 Activity 的代码也会膨胀到几千行，这个问题在 mvc 下面是无解的。
- 3.**mvvm 的缺点参考第一个讨论中的回答**

## 二、mvps代码生成原理
>上篇博客对于 mvps 我只介绍了在 app 运行时的整个流程，但是对于编译时的整个代码生成的流程却因为时间限制没有写完，本章节就会将整个流程走一遍，同时介绍一下 apt 下 debug 的技巧。

### 1.例子

![图1：MainActivity.png](https://upload-images.jianshu.io/upload_images/2911038-6a3b77a9ecea72ee.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![图2：LinearPresenter.png](https://upload-images.jianshu.io/upload_images/2911038-874ea05e08fc5194.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![图3：TextPresenter.png](https://upload-images.jianshu.io/upload_images/2911038-617fa574f1f6b6dd.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![图4：ImagePresenter.png](https://upload-images.jianshu.io/upload_images/2911038-100aba74cedccddd.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 1.先上一个完整的例子吧，如图1、2、3、4：MainActivity 包含了一个 LinearPresenter，LinearPresenter 包含了一个 TextPresenter 和一个 ImagePresenter。这里的层级结构和 xml 文件中是一致的。
- 2.我们的目的是将 MainActivity 中的两个字段设置到 TextView 和 ImageView 中，这里我们需要创建一个 LinearPresenter 然后用 create() 注入 view，用 bind()注入参数，最终在各个 子Presenter 中进行相应的操作。
- 3.有人会说：**这么简单的操作，我直接在 MainActivity 里面一下就做好了，还需要这么麻烦？**但是大家可以仔细想想这这里的结构会发现，这种结构有下面这些优点：
  + 1.我们其实是将 MainActivity 里面的业务代码和 ui 操作分散到各个不同的 presenter 中去了，而且仔细比较起来代码量其实增加的不多。大多数模板代码都被 apt 自动生成了。
  + 2.在这里 MainActivity 就只需要管数据的生产以及生命周期的管理。这样在未来业务复杂起来的时候所有的代码都会被均摊到各个 Presenter 中，而不会让 Activity 膨胀。
  + 3.除此之外，因为我们的在前面说过 Presenter 的子类是不允许有 **public 的方法和变量、有参构造函数**，这样一来任何的 Presenter 都不会与外界进行耦合，我们未来想怎么重构界面就怎么重构。
  + 4.**当然现在这样还是有一些问题的：第一个问题是：每个 Presenter 都和固定的 Viewid 绑定了，如果我一个界面上有多个相同的 View 难道要定义多个不同的 Presenter 吗？第二个问题是：View 之间如果有相互调用或者 Presenter 之间有相互调用怎么办？**
  + 5.**4中的问题由于篇幅限制本篇文章暂时不会说明解决方案，如果想知道答案的话，一定要关注本系列接下来的文章哟！**

### 2.代码生成流程
>我们在上篇文章里面讲解了 Presenter 的整个运行流程，这一节我们就来讲讲使用 apt 生成模板代码的流程。**建议结合项目源码食用！**

- 1.有些无关紧要的代码我就不一一截图了，大家可以结合项目源码来走整个流程。
- 2.首先我们需要定义几个注解：看看我们第一节中的例子吧，我定义了 @Field 和 @Inject 这两个注解。这两个注解在项目的 Annotation module 里面。**需要注意的是该 module 是 java library 的原因是因为要使用到 javax 这个包**
- 3.有了注解了就相当于有了一个标识，这样 APT 在编译的时候就能获取到注解注释的字段的信息。有了信息之后我们就可以根据这些信息来生成代码了。
- 4.那么生成代码的代码在哪里呢？细心的同学会发现有一个 Annotation-Processing module，很显然这就是生成代码的代码。我们就拿其中的一个类进行分析好了。**就决定是你了：FieldProcessor**

![图5：FieldProcessor1.png](https://upload-images.jianshu.io/upload_images/2911038-0fd710678dc45a8c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![图6：AutoService生成的文件.png](https://upload-images.jianshu.io/upload_images/2911038-f44290e82142a51a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 5.我就贴一些核心代码，图5中主要有用的就是最上面的两个注解，@AutoService：注解处理器是Google开发的，用来生成 META-INF/services/javax.annotation.processing.Processor 文件如图6，而这个文件就是让 APT 知道 FieldProcessor是用于生成代码的。大家可以在项目中找到这个文件看看里面写的是啥。
- 6.图5中第二个注解，@SupportedAnnotationTypes：用于标识 FieldProcessor 需要处理的注解，我们这里需要处理的就是 @Field 所标注的字段。

![图7：FieldProcessor2.png](https://upload-images.jianshu.io/upload_images/2911038-1df7af57051206c3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 7.图7中有两个方法：init 和 process。这两个方法会按顺序被在 APT 调用一次。

![图8：FetcherHelper.png](https://upload-images.jianshu.io/upload_images/2911038-d7b082466f32d03a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![图9：MainActivityFetcher.png](https://upload-images.jianshu.io/upload_images/2911038-8e1a790a73d5fc96.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![图10：app module 配置文件.png](https://upload-images.jianshu.io/upload_images/2911038-14d968993d87299d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 8.我贴一下生成后的代码，这样更方便讲解。
- 9.图7中我们先进入 init 方法，首先这里获取了一个 fullName。这里似乎是获取了哪里的配置？是哪里呢？大家可以看图10或者打开 App module 的 gradle 文件。可以看见给 providerInterfaceName 配置了一个 "value" 。这里的 ”value“ 就是我们获取到的 fullName。
- 10.图7中接下来几行很简单就是分割一下，获取一下 package 和 className。
- 11.然后我们进入到图7中的 process 方法里面，这里用到了 squareup.javapoet 这个库的 api 我这里不细讲，就讲讲含义，有兴趣的同学可以去百度用法。
    + 1.我们先直接去 FieldProcessor 的96行。这里的意思很明显，就是生成一个名为 FetcherHelper 的类，类的内容被包含在 fetcherInitClass 里面。
    + 2.再看95行，fetcherInitClass 添加了一个名为 init 的方法。
    + 3.我们再向前看 fetcherInitClass 的定义处，发现这个 FetcherHelper 被定义成了 public final 的形式。
    + 4.再看 init 的定义处，这个方法是 public static final 的，并且这个方法被 invokeBy 这个 Annotation 给注释了。
    + 5.最后我们回头看看图8的 FetcherHelper 类的代码，发现生成的代码和我们想象中差不多，就是 init 方法里面还有一些代码，这些代码我们在后面讲。

![图11：FieldProcessor3.png](https://upload-images.jianshu.io/upload_images/2911038-dfb08e6dc1f501b6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 12.我们继续解析图7，进入 init 方法的88-94行，这里是一个循环，循环的 map 的 key 表示某个含有 @Field 注解的 class 对象的信息结构，value 表示该类中所有被 @Field 注解的字段的 class 对象的信息结构的集合。那么 generateForClass 方法的用处也就呼之欲出：就是为每个含有 @Field 注解的 class 对象生成一个 XXXFetcher 类，这个类的实现了 Fetcher 接口。具体例子可以看图1的 MainActivity 对应生成的 Fetcher 类就是图9中的 MainActivityFetcher。接下来我们进入 generateForClass 方法来看个究竟，如图11：
    + 1.106行老样子获取需要生成的类的名字。
    + 2.177-123行，构造一个 class，这个 class 是 public final 的，并含有 Set mAccessibleNames、Set mAccessibleTypes、Fetcher mSuperFetche 这几个 private final 的字段。
    + 3.124-127行，构造一个 构造函数，然后初始化 mAccessibleTypes 和 mAccessibleNames 这两个变量。
    + 4.接下来就是依葫芦画瓢，不断的按照 Fetcher 接口的定义生成方法。
    + 5.**我们可以发现使用 squareup.javapoet 库生成代码就类似搭积木一样，给一个个方法添加一个个节点，然后让一个个方法组成一个类。下面的更多代码就交给读者去解析了，我想这应该不是很难的事情。**

## 三、开发一款gradle插件
>上一节我们讲了如何使用 APT 生成模板代码，可能有同学会想如果我想向已经有的代码里面插入一些模板代码怎么办呢？这一件事 APT 是办不到的，但是我们可以开发一款 gradle 插件来满足我们这种需求。

### 1.背景知识

- 1.Android apk 的构建流程是由一系列 gradle task 来实现的。比如说生成 R 文件，比如 将 java 文件编译成 class 文件最终生成 dex 文件，比如将所有资源打包成一个 apk。
- 2.我们可以通过定义 gradle 插件来将自己的 task 插入到 Android apk 的构建流程之中，这样就能实现批量修改和插入代码，减少重复的劳动。
- 3.gradle 插件是使用 groovy 语言来开发的，是一种脚本语言比较简单，我就不详细介绍了，百度上都有。

### 2.开发插件
![图12：invoker 项目结构.png](https://upload-images.jianshu.io/upload_images/2911038-bcb9e4e2074f1f48.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![图13：invoker gradle文件.png](https://upload-images.jianshu.io/upload_images/2911038-214fa35eedeefae3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![图14：invoker maven 配置.png](https://upload-images.jianshu.io/upload_images/2911038-dc99cf0b5d6f0185.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 1.我就非常简单的介绍一下 gradle 插件开发的开始流程吧！
    + 1.先看图12的项目结构，main 目录里面就是改了一下开发目录的名字，然后就是需要在 resource 目录里面加一个配置文件。
    + 2.然后就是 groovy 和 java 可以混编(圆形的是 java 文件，方形的是 groovy 文件)，所以图13中的 gradle 配置里面添加了 groovy 和 java 插件来分别编译两种文件。还有就是加了一个 maven 插件用于将插件传到本地 maven 库中，方便在主项目中引用。
    + 3.最后就是定义一些图14中插件的 maven 配置，方便在 gradle 文件中引用。


![图15：FetcherHelper.png](https://upload-images.jianshu.io/upload_images/2911038-902af318b3be54ef.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![图16：Fetchers.png](https://upload-images.jianshu.io/upload_images/2911038-57390ceda1bf2ac1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![图17：app gradle.png](https://upload-images.jianshu.io/upload_images/2911038-e38e87ef78924278.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![图18：invoker_info.png](https://upload-images.jianshu.io/upload_images/2911038-36ed72d5a3788fe4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 2.突然想起还没说这个插件的目的是什么，所以现在先来说一下这款插件的目的。
    + 1.我们先看看图15中的代码，细心的同学会发现这个类是我们前面 APT 生成的代码，是用于初始化 MainActivityFetcher 类的。我们可以看见这里用了一个静态方法 init 来初始化，如果一个 module 里面有多个不同的类中含有 @Field 注解的话，那么 init 里面就会初始化多个对应的 XXXFetcher 对象。
    + 2.这个时候有同学就会发现了，如果我们有很多个 module 都使用了 @Field 的话，那么就会有很多个 FetcherHelper 类在等着初始化，此时我们是每增加一个 module 就手动增加代码吗？如果我又有其他注解也是类似这样的模式的话比如 @Inject 那还是手动增加吗？
    + 3.让我们切换到**框架组**的视角，会发现这种事情是光靠人肉添加来保证是不可控制的，鬼知道某个**业务同学**会不会写了一个 module 之后就忘记某些必须操作，最后上线就爆炸了。**当然可以通过某些检测和测试来保证正确性，但是那样就会消耗人力资源，我们还是的从根源解决这个问题。**
    + 4.人不可靠，但是代码是可靠的。所以我们能不能想一个办法让所有的 FetcherHelper 的初始化代码每次编译都自动在某个地方注入代码，然后被调用呢？答案就是：**用 gradle 插件在编译的时候将所有 FetcherHelper 的初始化代码插入到图16 Fetchers 的 init 方法里面，最后我们只需要调用 Fetchers.init 就能初始化所有的 FetcherHelper 了。**
    + 5.整个方案分三步：
        * 1.还是使用 APT，我们定义两个注解：@ForInvoker 和 @InvokeBy，就像图15和图16里面那样，需要被调用的方法就用 @InvokeBy 注解。需要调用别的方法的方法就用 @ForInvoker 注解。
        * 2.使用和前面生成代码类似的流程，定义一个 InvokerProcessor，然后像图17中那样传入一个文件路径，最后将上一步注解中的信息写入文件中。最终的结果如图18所示，大家可以在项目中查看代码和相应的文件，这里就不重复介绍了。
        * 3.最后在我们的插件中读取上一步中储存在文件中的信息，然后用 javassist 这个库在相应的位置注入代码。
    + 6.是不是很简单？**简单个屁啊！**肯定有一大波人又要吐槽了。**这么麻烦的方式亏你想的出来?、这就是一个伪需求!。。。**当然我要辩解一番：
        * 1.第一个原因也是最重要的一个：站在**框架组**的角度，为了保证项目可控，这是一个对**业务同学**透明的好的解决方式。
        * 2.其实有了这个插件，我们不仅仅是解决这一个问题，一批类似的问题我们都可以用这两个注解来解决，算是一劳永逸。
        * 3.有了这一个插件的经验，我们可以定制更多插件，这就极大的增加了我们对 android apk 打包流程的控制程度。

![图19：invoker properties.png](https://upload-images.jianshu.io/upload_images/2911038-dcce47595c3dcedf.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![图20：invoker plugin.png](https://upload-images.jianshu.io/upload_images/2911038-8521871cb074f7ca.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![图21：invoker transform.png](https://upload-images.jianshu.io/upload_images/2911038-eec125531e7bc751.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![图22：jar scanner.png](https://upload-images.jianshu.io/upload_images/2911038-7c008a1263078bcb.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![图23：jar modifier.png](https://upload-images.jianshu.io/upload_images/2911038-b82507c0cbf9c1ad.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 3.到了这里我们就可以开始正式的插件的解析了。
    + 1.首先任何程序的运行都会有一个入口，gradle 插件也不例外。还记得我们前面在 resource 目录里面定义的文件吗？如图19这就是我们定义的插件的入口，当 gradle 运行的时候回去读取这个文件然后运行图20中我们定义的 InvokerPlugin 的 apply 方法。
    + 2.我们看 apply 方法的内部，这里传入的是一个 project 对象，这对象储存着整个工程的信息对应着根目录下面的 build.gradle 脚本。
    + 3.然后我们仅仅在当前的 module 是一个 android app 的时候才让插件进行编译替换，这样能减少无谓的编译。
    + 4.然后我们注册了一个自定义的 Transform 对象，Transform是Android官方提供给开发者在项目构建阶段即由class到dex转换期间修改class文件的一套api。目前比较经典的应用是字节码插桩、代码注入技术。要了解更多这个类的详情戳这里：[Transform](https://blog.csdn.net/tscyds/article/details/78082861)，我就不详细讲了。
    + 5.我们运行的时候图21的 transform 方法就会被 gralde 调用。
        * 1.先看25-53，这里是遍历被汇聚在 android app module 的 jar 和 源码文件，然后复制到输出目录也就是 build 目录下面。这里在遍历的过程中将两种文件的信息存成了集合，以供后面使用。
        * 2.然后我们定义了一个 JarScanner 用来在后面扫描源码文件和 jar 文件。这里我们存了一个路径，我想大家应该还记得，这个路径就是前面我们定义的 invoker_info 文件的相对路径。
        * 3.59-69行我们就开始遍历前面存起来的 jar 和源码文件了。这里用的是我们定义的 JarScanner 来遍历的。我们进入图22的 scan 方法来看一看。
            - 1.先去 scanMetaInfo 里，我们发现这方法是读取每个 jar 的 meta 文件数据，**这里我们要注意的是，比如 module1 被集成到 module2里面的时候，到了当前这一步，module1的所有源码文件都被打包成了 jar 的形式，然后我们之前定义的 invoke_info 文件里的数据就被存在 jar 的 meta 数据中。**
            - 2.这里读取了每个 jar 中的 invoker_info 数据之后，我们就有了 **要被调用的方法 --》要调用被调用方法的方法** 这样的键值对集合。这样先存入 mUnmatchedClasses。等后面一一验证一下这里的键值对是否是正确的。
            - 3.然后我们进入了 scanClasses 方法里面，这里只要每验证了 mUnmatchedClasses 中的一个键值对是正确的，那么就将其从 mUnmatchedClasses 中移除，然后将信息加入到 mInfos 里面。
        * 4.出了 scan 方法，addToPath 遍历源码文件也是一样的行为。最后 mInfos 里面就有了经过验证确实存在的 **要被调用的方法 --》要调用被调用方法的方法** 集合。
        * 5.最后我们看72行，这创建了一个 JarModifier 对象用于class字节码进行修改。
        * 6.看图23的 modify 方法，这里先将传入的信息整合成 mFile2InvocationMap<File, Map<String, Set<Invocation>>> 对象，这个对象的意义就是：某个 File 中的某个 String 名字的方法中需要调用对应的 Set 中的全部方法。
        * 7.然后遍历 mFile2InvocationMap，这里每次遍历的主要逻辑在 modifyClass 里面，这里就是用 javassist 的 api 进行代码注入。

### 3.上传插件到本地Maven库
>到这里为止我们的插件已经开发完成了，但是我们该如何使用这个插件呢？其实在任何项目中我们都在使用着 gradle 插件。

![图24：根 build.gradle.png](https://upload-images.jianshu.io/upload_images/2911038-74ed8844a36f4bc4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![图25：app module build.gradle .png](https://upload-images.jianshu.io/upload_images/2911038-5a89e3bc4252f0d4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![图26：invoker gradle properties.png](https://upload-images.jianshu.io/upload_images/2911038-e688d06e21d998b7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![图27：invoker gradle文件.png](https://upload-images.jianshu.io/upload_images/2911038-214fa35eedeefae3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![图28：maven目录.png](https://upload-images.jianshu.io/upload_images/2911038-6d59b8bbf590891e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 1.使用插件有两个步骤：
    + 1.在根目录的 build.gradle 文件里面引入插件的代码库。如图24
    + 2.在需要使用插件的 module 中引入插件。如图25
- 2.可能会有人奇怪了，我运行了项目之后报错了啊！说是找不到这个插件。这里我们应该了解一下关于 Maven 的一些知识。
    + 1.Maven 是一种构建源代码的工具，他会将某些源代码以某种格式(Project Object Model)进行打包，这样我们就能很方便的引用某个别人开源的代码库了。
    + 2.gradle 中能够使用 Maven 包，使用的方法就是大家在 dependencies 块里面的引用方式。
    + 3.在图24中我们可以看见 repositories 块里面写着好几行代码，每一行都表示一个 Maven 库。有 google 的、有 jcenter 的、最后一个是我本地的 Maven 库。当我们引用一个包的时候，gradle 就会去这些库里面找相应的 Maven 包然后下载下来供项目使用。
- 3.怎么新建一个本地 Maven 库呢？很简单:
    + 1.将本地某个路径设置为仓库根目录，比如我在 mac 下的库根目录就如图24所示。
    + 2.比如我们需要将 Invoker 这个 module 上传到本地库中，那么就在 module 中新建一个 gradle.properties 文件，这样在本 module 的 gradle 脚本中就能读取里面的配置
    + 3.我们需要再在 module 的 gradle 文件里面添加一个 maven 插件，然后写一个上传方法 uploadArchives。如图27.
    + 4.最后运行 **./gradlew -p invoker clean build uploadArchives --info** 也就是运行上传插件的方法。这样你就会看见在你前面定义的仓库目录中多了一些文件。如图28。
- 4.当然你也可以在私有或者公有云上创建一个 Maven 库然后修改一下依赖和上传路径这样也能顺利的运行起来。


## 四、高效的跨语言数据协议protocol

- 1.protocol 是一款和 json、xml 类似的跨语言数据传输协议，是 google 开发的。
- 2.他由三部分构成：
    + 1.proto 定义文件：相当于一种新的语言，用于定义某种数据结构，比如 java 中的 Person Bean。定义完成了之后，google 提供了各种转化程序，可以直接将这种数据结构转化成相应语言的类文件。现在支持java、c++、python、go 等等。我们用 proto 语言定义好了数据结构转化为相应语言之后，可以集成到相应语言的项目中。
    + 2.proto 库：这个是对应语言的代码库，对应到 java 就是 jar 包。这个库有很多 proto 相关的工具，比如说1中生成的类文件中就会依赖 proto 库中的代码。
    + 3.proto 数据：当我们要用 proto 在 c++ 项目和 java 项目之间传输数据的时候应该怎么做呢？
        * 1.定义好 proto 定义文件
        * 2.将 proto 定义文件转化为 jar 和 c++库文件，然后与 proto 库一起集成到两个项目中。
        * 3.在 java 项目中初始化 proto 定义文件中定义的对象，然后用 proto 库的 api 将对象数据写入到文件中。
        * 4.将文件传给 c++ 项目然后用 proto 库来读取文件中的数据，最后恢复成 c++ 的对象。
        * 5.上面我们传输的数据就是 proto 数据。
- 3.我们为什么要使用 proto，简单来讲他有下面这些优势：
    + 1.他简单快速，需要注意到的是， **proto 数据中并不会包含任何的对象的类信息，里面有的只是对象字段的值**。仅这一点他占用的空间就比 json 和 xml 小上一半多。
    + 2.proto 数据序列化后所生成的二进制消息非常紧凑，他利用了**Varint**来非常紧凑的表示数字。比如说不是所有的 int 都占4个字节，小的数可能只占1个字节。**这种技术和哈夫曼编码很像。**
    + 3.proto 定义文件生成的类的 api 非常完善，有各种最佳实践的代码，省去了我们在解析 json 和 xml 的时候写的大量模板判断代码。
- 4.protocol 在 android 项目中的使用，这里的话我就直接上一个链接了：[protocol 在 android 中的使用](https://www.jianshu.com/p/e8712962f0e9)，感谢这位博主的博客，我们的项目中已经集成了 protocol。
- 5.**今天这里只是对 protocol 进行一个简单的介绍，后面的话我会针对 protocol 在项目中的实际应用专门开一篇博客进行讲解，希望大家能持续关注我的博客！**

## 五、尾巴
>本篇文章是**从零开始写一个抖音App**系列文章的第三篇，篇幅比较长能看到这里的同学非常感谢你们对我的认可。给一个看到这里的同学的小福利吧：**在未来我会开放本项目在 github 上权限，只要对本项目了解比较深的同学都能参与项目的开发，看到这句话的同学我会优先考虑，但是只限前5名，记得加QQ群然后在群里小窗我。**

## 连载文章
- [1.从零开始写一个抖音app——开始](https://www.jianshu.com/p/e92bd896ac35)
- [2.从零开始写一个抖音App——基本架构与MVPs](https://www.jianshu.com/p/3867f6cf4e82)


































