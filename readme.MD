# logback-extra

Create multiple similar loggers in logback.

see `sample.MD`

---

## 起源

在类似于`SaaS`的场景下，需要服务很多的【客户】，将他们的日志分开存放是很实用的。

+ `logback`不支持`for`语法，无法直接支持这样的场景。
+ 好在他有`newRule`方式来引入自定义扩展。 于是，自己做了个`ForAction`，可以实现这个功能。
+ 然而，`logback`底下的配置引擎`joran`，在设计上做的很烂，实现上也有重大缺陷（参考下面的吐槽）。扩展出来的`ForAction`有很多问题。

我以为他们会意识到问题，并且修复。
毕竟`logback`是Java日志里面的No.1，而且当年团队也是生猛无比的。敢于自我怀疑自我颠覆。

于是等他们的升级，好几年。

种一棵树，最好的时机是十年前，第二好的时机就是现在。

等待完美，就是错过了太多。

## 吐槽`joran`的垃圾设计和实现

### 实现代码

#### Path Selector
`*/x/y`这样的`Selector`可以match`/a/y`这样的Path，这样的结果，去哪里也说不通。
但是就发生在`SimpleRuleStore`里面。

而且：`joran`里面的`RuleStore`是以接口形式体现，也就是说：是策略模式的设计。
但是在启动时候，hard coding了`SimpleRuleStore`，都没有个地方去灵活设置。
当然，可以用语法技巧突破这个限制，以一个错误行为纠正另外一个错误行为。

### 设计问题

#### 背离扩展的设计

https://logback.qos.ch/manual/onJoran.html

As apparent in previous chapters, logback relies on Joran, a mature, flexible and powerful configuration framework.
Many of the capabilities offered by logback modules are only possible on account of Joran.
This chapter focuses on Joran, its basic design and its salient features.

`joran`自称是成熟、灵活、强大，诸多美好的形容词。但是它在根本的设计逻辑上，完全背离了扩展。

先描述一下，一个扩展性的配置引擎（或者说，一个简单的语言解释器）如何做。

+ Engine获取到一个节点，
+ Engine询问是否有注册的Action能处理这个节点，如果找到了就让它来处理，没找到就用默认的处理方式（可能是处理，或者放弃）。
+ 节点包括了StartNode、Body、EndNode。Body里面可能包含有更多的子节点，这些有的可能是当前Action想处理的，有些对它是未知的，它就扔回去给Engine，Engine再去找。
+ 在Engine授权给Action的时候，顺便也要把上下文环境发给它，Action在这个基础上做一个自己的环境，可以包含Local变量。这个在学校的编程课里面叫压栈。

每个环节，有自己的职责和权限，不能超越！！！

但是`joran`是把所有的节点都遍历完了，然后一个个的去找Action。这个就导致了，它的配置规则里面，有很多的`NOPAction`
，因为这些都是不符合预期的规则匹配。
规则和规则之间打架的太多。

——从组织单元管理的角度看，`joran`就像极了极权国家。什么都想管，然后什么都管不好，一团糟。

——他们的法律体系，也是属于补丁法系，不断的打补丁，自我矛盾，自我搏斗。

——组织单元也是这样，一个自我感觉是圣人或者智者的领导就会把什么都抓手里，什么都要指导。

因为错误的设计理念，就会有错乱的关系，代码实现里面的耦合就太多。`joran`的代码实现里面大量的相互引用，Field基本上都是public的。










