> 思路来源:美团技术团队[如何优雅地记录操作日志？](https://mp.weixin.qq.com/s?__biz=MjM5NjQ5MTI5OA==&mid=2651764878&idx=1&sn=47d0a950bacdbd062e544251e4d2c4d5&chksm=bd1261c38a65e8d59395c62f565067c8c1dad2e71e9a864dd782b111a43234c24154799a9db5&scene=21#wechat_redirect)

## 项目介绍

基于Spring AOP和注解实现的异步操作日志记录框架。通过SpEL表达式大大提高日志内容的灵活性，并支持持久化到日志文件、数据库，同时封装为`Spring Boot Starter`，方便引入。

## 技术栈

Spring Boot,TransmittableThread Local

## 项目亮点

* 业务无侵入:基于注解和AOP，让操作日志和业务逻辑解耦,优雅地记录操作日志。
* 异步输出:采用线程池异步输出日志，降低对业务性能的影响。
* 自定义上下文：支持手动传递键值对，通过`SpEL`进行解析
* 更加灵活:使用SpEL来实现动态日志模板,让日志更加灵活简洁。
* 快速接入：封装为`Spring Boot Starter`，无需过多配置即可轻松整合。
* 可扩展性:用户可以自定义项目中使用的部分组件，如自定义SpEL函数。



