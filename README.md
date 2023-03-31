> 思路来源:美团技术团队[如何优雅地记录操作日志？](https://mp.weixin.qq.com/s?__biz=MjM5NjQ5MTI5OA==&mid=2651764878&idx=1&sn=47d0a950bacdbd062e544251e4d2c4d5&chksm=bd1261c38a65e8d59395c62f565067c8c1dad2e71e9a864dd782b111a43234c24154799a9db5&scene=21#wechat_redirect)

## 项目介绍

基于Spring AOP和注解实现的异步操作日志记录框架。通过SpEL表达式大大提高日志内容的灵活性，并支持持久化到日志文件、数据库，同时封装为`Spring Boot Starter`，方便引入。

## 技术栈

Spring Boot,TransmittableThread Local

## 项目亮点

- 业务无侵入:基于注解和AOP，让操作日志和业务逻辑解耦,优雅地记录操作日志。
- 异步输出:采用线程池异步输出日志，降低对业务性能的影响。
- 自定义上下文：支持手动传递键值对，通过`SpEL`进行解析
- 更加灵活:使用SpEL来实现动态日志模板,让日志更加灵活简洁。
- 快速接入：封装为`Spring Boot Starter`，无需过多配置即可轻松整合。
- 可扩展性:用户可以自定义项目中使用的部分组件，如自定义SpEL函数。

## 快速开始

### 导入依赖

暂未上传到git仓库,请clone后安装到本地仓库

```xml
        <dependency>
            <groupId>icu.ynu</groupId>
            <artifactId>log-record-spring-boot-starter</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
```

### 快速配置

无需配置即可立刻使用。可在配置文件中自定义持久化方式和线程池。默认开启文件持久化，关闭ELK持久化

如需需要到LogStash，只需要进行如下配置：

1.引入logstash依赖：

```xml
        <dependency>
            <groupId>net.logstash.logback</groupId>
            <artifactId>logstash-logback-encoder</artifactId>
            <version>7.2</version>
        </dependency>
```

2.配置host

一个示例配置如下所示：

```yml
log-record:
  enable: true
  # 线程池配置 
  pool:
    name: "我的自定义线程池"
    core-size: 3
    keep-alive-time: 30
    time-unit: seconds
    rejectedExecutionHandler: java.util.concurrent.ThreadPoolExecutor.DiscardPolicy
  # 持久化配置
  persistence:
    file:
      enabled: true
      infoLogFileName: logs/operation-info.%d.%i.log
      errorLogFileName: logs/operation-info.%d.%i.log
    elk:
      enabled: true
      host: 127.0.0.1
      project-name: demo

```

### 注解使用

加在任意Bean方法上即可，否则spring aop不生效

比如这样:

```java
    //修改章节
    @PostMapping("updateChapter")
    @LogRecord(bizId = "#getBizId()", bizType = "课程章节信息", content = "'课程:'+#getCourseName(#eduChapter.courseId) + '修改了课程章节信息,章节名称:' + #eduChapter.title")
    public R updateChapter(@RequestBody EduChapter eduChapter) {
        chapterService.updateById(eduChapter);
        return R.ok();
    }
```

### 操作员信息获取

需要使用IOperatorGetService,如:

```java
@Component
public class OperatorGetService implements IOperatorGetService {

    @Override
    public Operator getOperator() {
        JwtEntity user = UserUtil.getUser();
        return new Operator(user.getUserId(), user.getNickname());
    }
}
```

### 自定义函数

注意:

 1.需要使用注解@LogRecordFunctionBean标注类,否则Spring扫描不到!

 2.自定义的函数标注@LogRecordFunction方法,value为函数名称,为空时默认取方法名。必须是**静态方法!**

```java
/**
* 获取课程名称
*/
@LogRecordFunctionBean
public class CourseNameLogHelper implements InitializingBean {
    @Autowired
    EduCourseService ecs;
    static EduCourseService eduCourseService;

    @LogRecordFunction("getCourseName")
    public static String getCourseName(String courseId) {
        CourseInfoVo courseInfo = eduCourseService.getCourseInfo(courseId);
        return courseInfo.getTitle();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        eduCourseService = ecs;
    }
}
```



### 组件替换

需要替换什么组件,直接往容器放即可，如自定义持久化到数据库。

### 自定义持久化策略

实现`LogPersistenceStrategy`接口并注册为Bean即可。
