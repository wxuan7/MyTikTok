# 从零开始仿写一个抖音App——app架构更新与网络层定制
**本文首发于简书——何时夕，搬运转载请注明出处，否则将追究版权责任。交流qq群：859640274**

# [本项目的 github 地址：MyTikTok](https://github.com/whenSunSet/MyTikTok)

>国庆快结束了，国庆中有六天都在写文章看代码还有比我苦逼的吗(买个惨，哈哈)。这几天为项目新增了五个模块，顺便看了看 kotlin 的语法并在项目中简单的实践了一下。本文中会讲解其中的两个模块，剩下的一些会在不久后发布的下一篇文章中进行讲解。

- 1.讨论——总结前两周评论中有意义的讨论并给予我的解答
- 2.app架构更新——随着开发的进行，发现第二篇文章中的架构有一些问题，所以在这里更新一下
- 3.网络层定制——基于 retrofit 和 okhttp3 定制一个网络请求层，中间会附加一些原理讲解 

## 一、讨论

**讨论1：zsh 对 bash 的支持并不是完全的，如果运行纯 bash 有时候会出问题建议不要在服务器上用。**

- 1.这个读者的建议非常好，上篇文章中我写了一个 unbunt 环境的初始化脚本，看来这个脚本只能自己在 linux 下开发的时候使用了

**讨论2：我以为 aop 是通过 aspectjrt 来实现的  原来是和 Butterknife 类似来实现的**

- 1.在我认知里面的 aop 可以简单的归纳成：通过注解的信息在某些方法的前后添加代码。
- 2.所以 aspectj 也是可以实现我在前篇文章中说的 aop 日志的。
- 3.如果读者了解 aspectj 的原理的话就会发现：他也是通过 gradle 插件来将代码插到注解的方法前后的，只不过这一部分不需要开发者来是实现。
- 4.而项目中自己实现一个这样的东西一个是为了可定制性，另一个就是为了能了解一些技术的原理而不是单单只会用。

**讨论3：建议以已完成某个功能模块或者某篇文章为版本，创建不同的tag，这样利于食用。(github 上面的 issue)**

- 1.这个读者的建议也非常好，我已经在每次更新文章的 commit 上面加上了 tag ，大家可以结合这个来看代码。


## 二、app架构更新
>我想看过本系列第二篇文章的同学都看过本项目的模块架构图。距离写下本项目的第一行代码到现在已经差不多三个月过去了，这个过程中项目中增加了很多模块,我对大的项目的把握程度也加深了许多，所以这一节我更新一下 app 的架构。

![图1：app 架构图.png](https://upload-images.jianshu.io/upload_images/2911038-4cb54b2cde58988f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
 
**我接下来就按照图1开始讲解，标了红色的小模块表示已经进行过开发的模块**

- 1.首先从最底层开始，这里是一些**二方库(自己开发的sdk)**、**三方库(开源的sdk)**。其他的所有模块都能依赖这里的库，当然都是**单向依赖(A 依赖 B，但是 B 不能依赖 A)**
- 2.再向上一层，这里有两个大模块，**generate-code** 和 **internal-base**。
    + 1.generate-code：这里放着生成代码的几个模块，比如用 apt 生成代码的 annotation-progress，又如用 gradle 的 transform 配合 javassist 生成代码的 invoker。
    + 2.internal-base：这里放着 app 中的所有的底层模块，例如负责网络请求的 http 模块，例如负责图片加载的 image 模块，例如复制数据库的 database 模块等等。
    + 3.**在这里 generate-code 与 internal-base 这两个大模块之间可以互相依赖(注意这里表示的不是类似 http 与 image 之间可以互相依赖，因为这样会产生循环依赖的错误)**
    + 4.这两个大模块都可以被更上层的大模块所依赖，**注意这里是单向依赖，是必须遵守的约定，因为没有代码层面的约束**
- 3.再向上看，左边是一个 **external-base** 大模块和一个 **core** 小模块组成的
    + 1.external-base：这个大模块里面目前还没有添加小模块，但是未来应该会添加进去，这里面装着的是外部侵入的代码的封装，比如 bugly 除了需要添加库的依赖还需要为其加一些另外的代码，又比如一些 android 厂商的 push 方案集成之后需要的适配代码
    + 2.core：这个是一个小模块，将其单独放在外面是因为其起一个承上启下的作用
        * 1.这里面装着更上层模块的公共代码，比如 app 进入时的初始化代码。
        * 2.解决一些底层小模块之间需要**互相引用**的问题，比如 **http** 需要和 **image** 之间**互相引用**，此时会造成**循环引用**的错误，此时就将这些代码放到 **core** 中进行处理。**暂定，在写下面的时候我发现这个特性可能会造成本模块依赖过多的问题，后面应该还会继续拆分这个小模块**
        * 3.沟通底层和上层的模块
    + 3.这里的两个模块可以被同层右边的 **app-plugins** 大模块所依赖，这里也是单向依赖
- 4.再看同层的右边，这一个大模块名为 **app-plugins**，里面的每一个小模块都能被编译成一个 app。然后其可以被最顶层的 **app-variants** 所依赖，最终构建出不同功能的 app。
- 5.最顶层就是 **app-variants**，这个大模块只能依赖 **app-plugins**，里面几乎不会有什么代码，有的就是一个个 gradle 配置，最终会生成不同功能的 app。

## 三、网络层定制
>现在 okhttp + retrofit，也许是一个新项目的标配了，但是很多人都只是在使用这两个库的最基本的功能，殊不知这两个库可以通过定制来实现更多的功能。这一节我就来讲讲如何基于这两个库来定制一个大项目的网络请求层。中间会穿插着一些原理的讲解。

### 1.网络层请求流程

![图2：网络层定制图.png](https://upload-images.jianshu.io/upload_images/2911038-1c840845f0470d84.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

**接下来我会按照图2开始讲解 okhttp + retrofit 整个请求流程，待读者对整个流程有所了解之后再讲定制的代码，这样会事半功倍。**

- 1.图中红色的框是开始部分，我们就从这里开始。**这里默认大家都会使用这两个框架，多余的东西就不再赘述了。**
- 2.首先我们在需要请求一个接口的时候会使用 Retrofit 对象调用其 create 方法创建一个 XXXService。我们看下图3的代码：
    + 1.可以看见这里就是简单的用了一下**动态代理**的方式将 XXXService 的每个接口交给特定的 ServiceMethod 来实现。
    + 2.这里的 ServiceMethod 怎么来的呢？看36行的 loadServiceMethod 方法，这首先为了性能会去 serviceMethodCache 中看看是否有 XXXService 某个接口对应的 ServiceMethod，如果没有的话就用 Builder 模式创建一个。

![图3：Retrofit#create.png](https://upload-images.jianshu.io/upload_images/2911038-555ecdc5a65f8261.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 3.回看图2，创建好了 XXXService 的实现类之后，我们一般会结合 Rxjava 调用某个接口，让其返回一个 Observable 对象。由前面的介绍，我们知道这里 Observable 其实是调用 ServiceMethod.adapt(OkhttpCall) 返回的(**可以看图3的21行**)，我们进入这个方法。
    + 1.这个方法里会将调用交给 CallAdapter.adapt(OkhttpCall)
    + 2.有些同学可能知道这个 CallAdapter 是在初始化 Retrofit 的时候被  Retrofit.Builder() 添加的 CallAdapterFactory 创建的。其有几个具体实现如图2。
    + 3.那么这里要选哪一个呢？选择 CallAdapter 的具体逻辑在 ServiceMethod.build 里面他会调用 ServiceMethod.createCallAdapter 这里最终会交给 Retrofit.callAdapter 来寻找合适的 CallAdapter。
    + 4.**那么3中的具体查找逻辑是什么呢？这里我总结一下：**
        * 1.会对 CallAdapterFactory 进行循环查找，一旦返回一个 CallAdapter 不为 null 那么就使用这个。
        * 2.具体是否为 null 的逻辑交给具体的 CallAdapterFactory 去实现。
        * 3.因为是顺序查找，所以如果列表中有多个匹配项，这里只取最开始的一个。
- 4.到这里我们先不看图2，**一般来说匹配上的 CallAdapterFactory 会是 RxJava2CallAdapterFactory**。我们先研究一下他是怎么产生一个 Observable 的。
    + 1.先看一下图4，我们直接看20行，这里解释了为什么一般会匹配到 RxJava2CallAdapterFactory 因为我们的 XXXService 定义接口的时候一般选择的返回值 都是 Observable 或者有关 Rxjava 的返回值。然后我们直接看55行，这里返回了一个 RxJava2CallAdapter，这个就是生成 Observable 的对象。
    + 2.接着我们看图5，还记得上面的**3.1**中我们说的吗？Observable 就是 CallAdapter.adapt(OkhttpCall) 产生的。这里就是具体实现。
        * 1.可以看见18行根据接口调用是同步还是异步会生成两种不同的 Observable。
        * 2.然后后面都是根据一些 flag，为 Observable 添加一些操作符。

![图4：RxJava2CallAdapterFactory#get.png](https://upload-images.jianshu.io/upload_images/2911038-dc334d1ab09fb791.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![图5：RxJava2CallAdapter#adapt.png](https://upload-images.jianshu.io/upload_images/2911038-cbe2dc023efbf350.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 5.再回到图2，现在我们已经有 Observable 了。这里我们先跳过图2中的几个步骤，直接来到黄色的框，从这里开始我们可以让得到的 Observable 开始运行。对 Rxjava 熟悉的同学应该知道，一个 Observable 会从操作符流的最顶部开始运行。所以这里会从我们前面讲到的 RxJava2CallAdapter.adapt 中定义的第一个 Observable 的 subscribe 开始运行。我们就默认这次接口调用是**同步**的这样简单点，所以会先进入 CallExecuteObservable 中。
    + 1.先看图6，第1行构造这个对象的时候会传入一个 Call 对象，其实现有很多我们在这里可以默认其为 OkhttpCall。
    + 2.图6的第5行，是 Observable 开始运行的时候最先调用的方法(**有兴趣的同学可以看看  Rxjava 的源码解析**)。这里我们可以看见13行，其将调用交给了 Okhttp.execute。
    + 3.我们可以看向图7的20行，这里调用了 createRawCall 创建了一个 okhttp3.Call 其具体实现是 RealCall(**我们直接使用 okhttp 的时候也是通过这个请求网络**)。
    + 4.在回到图2中，如图2所示当调用 RealCall.execute 的时候，就会进入 okhttp 的请求链。okhttp 使用了责任链模式，将请求穿过图2中的一个个**拦截器**，每个**拦截器**都负责一个功能。开发者可以在**拦截器链**的最开始插入自己的**拦截器**，以实现一些定制操作。
    + 5.再回到图7，okhttp 将数据请求完毕之后会返回一个 okhttp3.Response，这时候会在32行调用43行的 parseResponse 来将解析这个 Response。
    + 6.图7中后面有些代码看不见了，其实最终 Response 的解析会交给 ServiceMethod.toResponse。而其又会交给 Converter.coverter。这接口的实现类也很多，最常见的应该就是 GsonConverterFactory 提供的 GsonResponseBodyConverter 了。如图2，我们一般也是在创建 Retrofit 的时候添加一些 Converter 以供这里使用。**同样类似 CallAdapter，Converter 的选取也是一样的策略**
    + 7.**经过以上调用，我们就有了一个retrofit2.Respons，其内部有一个解析了 body 之后的对象。**

![图6：CallExecuteObservable#subscribeActual.png](https://upload-images.jianshu.io/upload_images/2911038-8d20537ca63352a2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![图7：OkHttpCall#execute.png](https://upload-images.jianshu.io/upload_images/2911038-0a9b2831caec2aba.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 6.CallExecuteObservable 中调用完毕之后，调用流程一般会交给 BodyObservable，这里面很简单，就是将 retrofit2.Respons 中的解析后的 body 交给下一个 Observable 操作符。就这样顺着操作符流最终我们在 XXXService 中定义的接口的返回值 Observable 的泛型对象就会被传入到 subscribe 中供外部调用者使用。如图2中的粉色框。

### 2.网络层定制代码
>所谓定制就是**在网络请求流程的各个主要节点中添加自己的代码实现以达到特殊的需求**。经过前面的讲解，我想读者应该对整个网络层的请求流程有了一个大致的了解。这时我们可以再看看图2，可以看见其中有几处我绿色的框，这几个地方就是我们可以添加定制代码的地方。接下来我就会按顺序讲解一下这几处的定制代码是如何实现的。

![图8：RetrofitFactory.png](https://upload-images.jianshu.io/upload_images/2911038-6f9030a36e354062.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![图9：DefaultRetrofitConfig.png](https://upload-images.jianshu.io/upload_images/2911038-cd9da5ff2c463226.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#### (1)retrofit2.Call的装饰
>我们按请求顺序可以在图2中首先看见的是 NewCall.execute 这个框，接下来我就来说说这个可以怎么定制。

- 1.按照我们前面的讲解，大家应该知道，如果不做任何定制的话这里的 NewCall 就是 OkhttpCall，其会返回一个 retrofit.Response。最终会在开发者的 subscribe 里面返回一个解析了 body 之后的数据结构(**这里就称为 ContentData**)。有时候我们会在 subscribe 里面需要更多的信息，比如在数据转化过程中**丢失的 head 的信息**。
- 2.此时我们就可以对 OkhttpCall 进行一个封装，首先我们可以定义一个我们自己的 DataContainer<T> 对象，其用于封装 **ContentData**，然后其还可以装数据转化中丢失的数据。如图10。

![图10：DataContainer.png](https://upload-images.jianshu.io/upload_images/2911038-ce94e7d386b8cd0c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 3.那么我们在定义 XXXService 的接口的返回值的时候就能这样定义：**Observable&lt;DataContainer&lt;ContentData&gt;&gt;**。
- 4.此时有人眼尖就会发现，不对啊这个 DataContainer 是被 Gson 反序列化过来的，里面的 okhttp3.Response 对象服务器又不知道是什么这样怎么序列化呢？
- 5.答案就在图8，图9中。大家可以看图8的第7行，这里我添加了一个自定义的 CallAdapterFactory。
- 6.在看图8的44、48、49行，根据前面我们描述的请求流程，44行的 CallAdapter 会用来生成 Observable。再看48行，这里的 call 就是 OkhttpCall 了。我们将其传入 buildCall 中返回了一个 NewCall，这里就是关键。
- 7.buildCall 的实现代码在图9，可以看38行。这里的实现非常简单直接就是将 OkhttpCall 封装 返回了一个 ContainerCall，如图11。

![图11：DataContainerCall.png](https://upload-images.jianshu.io/upload_images/2911038-073ed50e234aca3b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 8.DataContainerCall 里面的代码就不用我说了吧，就是给 DataContainer 传入一个 okhttp3.Response 对象。
- 9.大家是不是觉得就这样一个小东西很简单？其实我也觉得很简单，但是只要你会用了这一个小东西，那么更多实用的功能都能被这样实现。

#### (2)OkhttpClient定制
>按顺序下来，第二个定制的地方就是 OkhttpCall 调用 okhttp.RealCall 的地方了。

- 1.我们看图8的21行，这里给 Retrofit 添加了一个 OkhttpClient。之后的请求都是通过它来发送的。
- 2.这里插一下，大家可以看看3行，这里传的是一个 RetrofitConfig，它其实是一个接口，像图9的 DefaultRetrofitConfig 就是它的一个实现。当然我们还可以有不同的实现以实现不同的定制方式。
- 3.那么我们还是再看图9的6行，可以看见这个方法的返回值 Builder 中添加了一系列 Intercept。由我们前面的讲解可知，这些是**拦截器**，然后会按添加的顺序拦截请求和响应。
- 4.这里可以看见我实现了各种不同的功能：打印网络请求日志(**这个在上一篇文章中没实现，现在实现了**)、过滤过于频繁的请求(**防止ddos攻击**)、SSL认证(**当然现在没有后端还没实现**)、超时拦截、添加自定义的参数等等。
- 5.这里的定制比较简单，大家可以去看看各个拦截器中的实现。

#### (3)Converter定制

- 1.其实这个也很简单，大家可能都用过，就是图8的5、6两行，添加的数据转换器。
- 2.大家只要了解我前面讲解的 Converter 的执行策略就可以了。

#### (4)CallAdapter定制

- 1.大家可以回看 **(1)retrofit2.Call的装饰** 这一节，我们添加了一个 CustomAdapterFactory。
- 2.因为 CustomAdapterFactory 比 RxJava2CallAdapterFactory 先添加，所以其优先级比较高。再看图8的40行，这里获取了一个 delegate，其实就是 RxJava2CallAdapterFactory。所以我们可以在 RxJava2CallAdapter 返回的 Observable 上面添加一些统一的操作符。
- 3.具体的代码在图8的49行，然后转到图9的42行。可以看见我就只添加了一些简单的操作符：计数请求成功和失败次数、配合 ThrottlingInterceptor 进行频繁请求过滤。

#### (5)网络层定制代码总结
>**上面就是在网络请求的四个主要节点进行定制的方式。其实总结起来比较简单：1是扩展 Retrofit 返回的结果、2是扩展 okhttp 请求和返回、3是解析 okhttp 返回给 Retrofit 的结果、4是增强对 Retrofit 返回结果的处理。**

## 四、总结
>不知不觉已经写了这么多了，本来以为还可以写一节 Fresco 的定制，现在看来只能放在下一篇文章了。在这里预告一下：**从零开始仿写一个抖音App**这一系列的文章大概还有一到两篇 android 层面的文章，并且会在接下来的一周左右放出。

>这一阶段结束之后我的文章和学习重心将会转向音视频这块。**这几个月过来虽然有时候文章会 delay，但最终我也信守承诺没有弃坑。最后希望大家能持续关注本系列，毕竟我都已经这么努力了不是:)**。

## 连载文章
- [1.从零开始仿写一个抖音app——开始](https://www.jianshu.com/p/e92bd896ac35)
- [2.从零开始仿写一个抖音App——基本架构与MVPs](https://www.jianshu.com/p/3867f6cf4e82)
- [3.从零开始仿写一个抖音App——Apt代码生成技术、gradle插件开发与protocol协议](https://www.jianshu.com/p/f71cd4c91df8)
- [4.从零开始仿写一个抖音App——日志和埋点以及后端初步架构](https://juejin.im/post/5b9e9bf1e51d450e6b0dba92)


















