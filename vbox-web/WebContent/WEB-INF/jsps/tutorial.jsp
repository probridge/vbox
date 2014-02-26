<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:template>
<jsp:body>
<div class="row">
<h2 class="text-center">虚拟实验室 使用帮助</h2>
<hr>
	<ol>
		<li>
		<p>打开您计算机上的浏览器，进入如下网址：</p>
		<div class="well">
		<h4 class="text-center text-success">http://202.120.22.10/</h4>
		</div>
		<p>交大用户请使用统一认证jAccount账户登录，校外用户可以通过右上角的“登录”选项选择“其他用户”登录。</p>
		<img src="imgs/tutorial-1.png">
		<div class="bs-callout bs-callout-info">
	    	<h4>虚拟实验室</h4>
	    	<p>本系统如无特殊情况，每天24小时无休开放。目前支持登录的平台包括计算机桌面操作系统Windows, Linux以及移动设备操作系统如iOS、Android以及Windows等，因此您可以使用台式机、笔记本、Apple iPad、Android 4.0以上系统的手机或平板在任何地点登录使用。</p>
  		</div>
		<hr>
		</li>
		<li>
		<p>登录成功，进入如下界面。首次点击“申请一个vBox”，申请空间和个人vBox。</p>
		<img src="imgs/tutorial-2.png">
		<div class="bs-callout bs-callout-info">
	    	<h4>vBox</h4>
	    	<p>本虚拟实验室以开放远程虚拟机形式提供给有相应需求的师生使用，每一个虚拟机都称之为一个vBox。理论上每个账户可同时申请数个vBox，但是每次只能连接使用其中的一个。
	    	每个vBox都有有效期，具体有效期，后台会根据用户的实际需求酌情调整，超过有效期的vBox将无法访问。</p>
  		</div>
		<hr>
		</li>
		<li>
		<p>第一次申请vBox的时候，可以按个人所需，选择所需空间的大小。</p>
		<p>如果您还知道有某一门课程并且想参加，可以输入课程代码以及备注相关信息。点击“提交申请”后，等待后台审核。</p>
		<p>只有在向实验中心以邮件等形式反馈使用需求后，后台才会开始审核。</p>
		<img src="imgs/tutorial-3.png">
		<div class="bs-callout bs-callout-info">
	    	<h4>用户</h4>
	    	<p>每一个通过后台审核批准的jAccount帐号或者普通账号都称之为一个用户，这个用户拥有独立数据存储空间供其所有vBox中共享使用。</p>
	    	<p>按用户身份的不同，实验中心会给每个用户设置不同的有效期：教师的教学或科研用途，这个空间将可以长期使用；
	    	学生的实验研究，通常会设置有效时间段，到期自动注销该用户（今后需要做实验可再行申请），特殊需求情况下经其导师确认，也可分配能长期使用的空间。</p>
  		</div>
		<hr>
		</li>			
		<li>
		<p>后台审核通过后，您需重新登录虚拟实验室。系统会自动开始初始化vBox。</p>
		<p>空间申请得到批准后，“我的vBox”则已经可以使用，“我的vBox”中只安装有微软Office软件、Adobe PDF软件等常用软件。</p>
		<p>如需使用特殊软件，则需将特殊软件名称反馈给实验中心，后台工作人员会安装好相应软件，以课程的形式开放，并以邮件等形式通知您课程代码，根据该课程代码提交申请即可。</p>
		<p>实验中心现有正版物流软件Demo3D、仿真软件LocalSolver、统计软件SAS等，如果需要使用其余特殊软件，您可以自行提供安装文件，由工作人员后台帮您安装。</p>
		<img src="imgs/tutorial-4.png">
		<div class="bs-callout bs-callout-info">
	    	<h4>个人vBox</h4>
			<p>每个用户审核通过后即拥有个人vBox, 该环境内包含了基本预装环境，可以进行Office文档编辑、论文阅读、网络浏览、数据共享等功能。每个用户的个人vBox为私有空间完全独立。</p>
  		</div>
		<div class="bs-callout bs-callout-info">
	    	<h4>课程vBox</h4>
	    	<p>实验中心针对对特殊软件、特殊虚拟环境有要求的实验或课程建立的vBox，其中根据具体需求预装相应课程实验软件。用户可以根据课程代码申请使用该课程，每个用户的课程vBox和个人vBox共享个人数据，但各用户之间会彼此独立使用。</p>
  		</div>
		<hr>
		</li>
		<li>
		<p>新申请的vBox第一次启动会进行额外的初始化工作，请耐心等待3-5分钟，直至系统就绪完成再进行操作。</p>
		<p>待vBox的状态为绿色“开始”图标时，点击“开始”即可使用。如果想切换至其他vBox使用，点击相应的“使用”按钮，等待切换完毕绿色按钮“开始”出现后即可使用。</p>
		<img src="imgs/tutorial-5.png">
		<div class="bs-callout bs-callout-info">
	    	<h4>关于母盘</h4>
	    	<p>每个用户或者课程的vBox在创建时分配独立的母盘副本作为系统启动盘。母盘为服务器中预装的一系列操作系统镜像，管理员里面根据不同需求为不同的课程安装特定软件。</p>
	    	<p>母盘作为模板由管理员定期维护，用户的操作不会影响到母盘：个人文件、数据等资料会统一保存在自己申请的空间D盘内。</p>
			<p>同一用户使用的各个vBox之间系统独立、数据共享。在虚拟实验室界面上，可以点击“我的文件”查看自己的所有资料。</p>
  		</div>
		<div class="bs-callout bs-callout-danger">
	    	<h4>注意</h4>
	    	<p>请不要将文件数据保存在C盘中，系统盘由母盘管理，可能会因为管理员的不定期的操作系统维护被覆盖清空。</p>
  		</div>
		<hr>
		</li>
		<li>
		<p>绿色图标出现后，此时可以点击进入“我的文件”，您可以看到自己留存的所有文件，并且可以通过“上传文件拖到这里”，实现本地文件上传至vBox中。所有文件保存的位置，都在您所申请的空间的D盘中。</p>
		<img src="imgs/tutorial-6.png">
		<div class="bs-callout bs-callout-info">
	    	<h4>访问文件</h4>
	    	<p>vBox中的用户文件通常可以保存在“桌面”上或者“我的文档”中，在这两处保存的文件均可以在ACEUser目录下的Desktop或者My Documents目录中找到。您也可以在直接在D盘的根目录下建立文件和文件夹，这些文件直接可以通过“我的文件”直接访问。</p>
  		</div>
		<hr>
		</li>
		<li>
		<p>点击“开始”进入，连接成功，会显示如下画面时，您就可正式使用虚拟实验室了。</p>
		<p>如果您在中院内网使用Internet Explorer浏览器访问，可以通过点击右上角“内网访问”选项进行直连，提高使用流畅度体验。</p>
		<p>使用过程中还可以使用“放大”按钮切换全屏模式。</p>
		<img src="imgs/tutorial-7.png">
		<div class="bs-callout bs-callout-info">
	    	<h4>访问体验</h4>
	    	<p>vBox的流畅访问依赖于您的网络接入速度和所在地区接入交通大学的校园网入口的带宽，对于vBox桌面的访问建议至少需要512Kb的带宽。注意如果使用无线网络，网络信号的强弱、同一网络内的用户数以及无线路由器的性能也会影响访问速度。</p>
  		</div>
		<hr>
		</li>
		<li>
		<p>如果看到如下红色提示，意味着您使用了版本过低的浏览器。这些浏览器已经是多年前的版本，技术和功能上已经远远落后并且在访问包括vBox的大部分网站时都会带来很差的体验和安全隐患，强烈建议升级至新版。</p>
		<p>我们建议使用Internet Explorer 9或者谷歌浏览器Chrome 29以上版本。</p>
		<img src="imgs/tutorial-8.png">
		<div class="bs-callout bs-callout-info">
	    	<h4>浏览器支持</h4>
	    	<p>请使用表中亮绿色版本的浏览器以获得最佳体验。</p>
	    	<img src="imgs/browser-support.png">
  		</div>		
		<hr>
		</li>
	</ol>
</div>
</jsp:body>
</t:template>