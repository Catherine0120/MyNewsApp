# MyNewsApp
程设小学期（Java + Android）大作业

-----------

`[Log 2022/8/30 23:36]`：实现上拉刷新。

`[Log 2022/8/31 11:04]`：实现下拉加载。

`[Log 2022/8/31 20:20]`：实现`DetailNewsActivity`

`[Log 2022/9/1 18:40]`：实现`SearchFragment `

`[Log 2022/9/1 21:18]`：完成`FavoritesFragment`基本策划

`[Log 2022/9/2 15:52]`：实现点赞/收藏功能，待完善部分基本只剩下本地存储

`[Log 2022/9/4 11:20]`：创建`Storage` branch，更改`shared preference`存储方式

`[Log 2022/9/5 11:25]`：`Storage` branch基本debug完毕

`[Log 2022/9/5 17:24]`：完成`SearchBar` 重绘

`[Log 2022/9/6]`：实现`ViewPager2`+`Fragment`

`[Log 2022/9/7 11:17]`：完成视频播放功能



-----------

待完善的部分：

1. ~~`BottomNavigation + ViewPager2 + Fragment`（包括`scroll`的时候`Navigation`收缩等）~~
2. ~~图片加载问题：`java.net.MalFormedURLException: no protocol`，`D/skia:---Failed to create image decoder with message 'unimplemented'`， `gif`动图之间的`[`&`]`格式问题~~
3. ~~视频播放~~
4. ~~`detailed page`中图片过大问题~~
5. ~~整体配色考虑？（蓝绿灰色系 or 加粉紫灰色系）——已解决：蓝绿灰色系+加载用粉紫色~~
6. ~~`DetailNewsActivity`的点赞收藏信息返回至`DiscoverFragment`，但应该要返回`DiscoverAdapter`处理，否则如果进行过`SCROLL_AND_LOAD`操作会导致`pos`越界——已解决：同步`DiscoverFragment`和`DiscoverAdapter`的`NewsList`~~
7. ~~看过的新闻边框变灰：若采用`notifyDataSetChanged`来调用`onBindView`开销太大，会造成明显卡顿。同样可能造成明显卡顿的包括频繁加载图片需要访问`url`。可能需要找到加载图片开销更小的更好方式~~
8. ~~`DiscoverAdapter`点击删除新闻`removeData`之后，`DiscoverFragment`的新闻列表尚不知晓，暂不清楚是否会导致两者的`pos`发生错位，同理也出现在`FavoritesFragment`和`FavoritesAdapter`之间
   【解决思路】：`closeBtn`触发后通知`Fragment`，同步`newsList`列表（可以在解决之前试一下`pos`错位现象是否存在）~~
9. ~~在`FavoritesFragement`收藏夹中进入新闻详情页点击取消收藏，收藏列表中仍能看到该新闻，且在此点击进入新闻报错：`You must ensure the ActivityResultLauncher is registered before calling launch().`（收藏操作在`DetailNewsActivity`中进行）
   【解决思路】：`DetailNewsActivity`增加取消收藏操作，如`if (!checked): remove news from favNewsList`（注意：只需要修改`newsList`数据，单条`news`中的状态记录`news.fav`是正确的（这也是报错原因）~~

