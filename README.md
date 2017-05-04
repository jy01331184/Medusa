<div align=center><img src="https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1487937880420&di=eac044d2ea0a05eafb9c8b1603e93fb8&imgtype=0&src=http%3A%2F%2Fimg1.cache.netease.com%2Fcatchpic%2FD%2FDE%2FDE30E317FFFACFBD21FFD9D09E08DC90.jpg">
</div>
<div align=center>
  <h1>Medusa --美杜莎 基于gradle构建的Android插件化实现</h1>
</div>

- [x] 无侵入式支持activity、service、broadcast、contenprovider等原生组件
- [x] 支持跨bundle的资源调用（图片，布局，样式等）和类函数调用（非反射）
- [x] 支持bundle异步加载，懒加载，优先级加载等加载方式
- [x] 基于gradle plugin的一键式构建，无需任何额外代码或过程

###相关模块说明

<table>
  <thead>
    <tr>
      <th style="width:15%">module</th>
      <th style="width:20%">usage</th>
      <th style="width:65%">remark</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>app</td>
      <td>主工程</td>
      <td>依赖medusa sdk</td>
    </tr>
    <tr>
      <td>bundle1</td>
      <td>一号bundle</td>
      <td>引用bundle2 class、引用bundle3 resource.包含activity、service、receiver、style</td>
    </tr>
    <tr>
      <td>bundle2</td>
      <td>二号bundle</td>
      <td>包含activity、自定义view</td>
    </tr>
    <tr>
      <td>bundle3</td>
      <td>三号bundle</td>
      <td>包含activity、receiver、图片（lazyload）</td>
    </tr>
    <tr>
      <td>medusa sdk</td>
      <td>sdk</td>
      <td>包含bundle、classloader等一系列插件化功能支撑</td>
    </tr>
    <tr>
      <td>medusa plugin</td>
      <td>构建plugin</td>
      <td>定义gradle plugin 完成整个medusa构建</td>
    </tr>
    <tr>
      <td>medusa studio plugin</td>
      <td>android studio/idea 可视化构建工具</td>
      <td>为IDE提供可视化按钮，一键式构建</td>
    </tr>
  </tbody>
</table>

---------------

#### 相关概念和使用方法详见[wiki](https://github.com/jy01331184/Medusa/wiki) 

