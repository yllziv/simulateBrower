模拟浏览器行为（高级爬虫）
===================================
###用途
模拟人使用浏览器，包括点击按钮、自动填写表单并提交、搜索等行为，爬取web信息

### 目录说明
- lib 保存使用到的jar包
- src 保存java源码
    - getPhoneNumber 通过系统命令调用python程序，识别58同城手机号
    - urlPictureDataBase 将网页中图片的URL转换为二进制图片，保存到postgres数据库中；将二进制图片从数据库中读出来，保存为本地图片
    - Main 爬取豆瓣同城活动的所有信息，并保存到postgres数据库中。
- rectangle 保存识别58同城手机号的python程序

### 环境
- java 1.7
- python 2.7

   


