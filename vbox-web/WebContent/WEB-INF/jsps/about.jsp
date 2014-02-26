<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:template>
<jsp:body>
		<div class="row">
			<div class="col-lg-4 col-lg-offset-1 col-md-4 col-md-offset-1 col-sm-5 hidden-xs"> 
	            <img src="imgs/login.png">
			</div>
			<div class="col-lg-6 col-lg-offset-0 col-md-6 col-md-offset-0 col-sm-7 col-sm-offset-0 col-xs-12"> 
              <ul class="nav nav-tabs" style="margin-bottom: 15px;">
                <li class="active"><a href="#about" data-toggle="tab">关于虚拟实验室</a></li>
                <li><a href="#solution" data-toggle="tab">技术方案</a></li>
              </ul>
              <div id="myTabContent" class="tab-content">
                <div class="tab-pane fade active in" id="about">
<p>　　本虚拟实验室(vBox)是由安泰实验中心从2013年7月开始准备，以实验中心的服务器设备、网络设施等硬件为支撑，为了给有所需要的师生提供良好的实验教学、实验研究条件，自主建设的一个虚拟实验平台。目前已开发的功能主要为个人实验研究、教师实验教学、各类数据存储等方面。支持跨平台登录，笔记本、平板电脑、智能手机等多类设备随时登录使用。</p>
<p><strong>个人实验研究</strong></p>
<p>　　主要解决对需要运行专业软件、对计算机运行效率要求较高、数据存储调用便捷等实验环境有特殊要求的情况。由师生自行反馈使用需求至实验中心，工作人员会视师生的个人实际的软件需求、空间需求、运行效率需求等情况来分配资源。</p>
<p><strong>教师实验教学</strong></p>
<p>　　主要针对教学中需要使用到软件等方式授课的情况。不需要固定的装有相关软件的计算机，只要有能上网的笔记本、平板电脑等设备，通过虚拟实验室就可以实现相应的软件运算与实验教学。</p>
<p>　　不论个人实验研究还是实验教学，在审核通过后，工作人员都将在虚拟实验室平台以课程的形式开放，通过给相应的老师和学生登录此门课程放开权限控制使用资格。课程需要用到的资料、软件等内容都可以交由后台工作人员上传和安装准备。<p>
<p>　　各类数据存储，师生使用虚拟实验室的申请通过后，每个人都会分配到一个空间，此空间有时效限制。根据大家的不同实际需求，所申请的空间的使用时限将会被设置为永久使用、某段时间内使用等。教师教学或科研用途，这个空间将可以长期使用；学生的实验研究，通常会设置有效时间段，到期自动注销该用户（今后需要做实验可再行申请），特殊需求情况下经其导师确认，也可分配能长期使用的空间。</p>
<p>　　每个用户的所有个人实验数据、参与的实验课程数据、自己上传的数据资料等内容，都会自行保存在存储系统中，直至达到相应的使用时间上限。此外，本系统的数据导出，有两种方式，一种是通过在vBox中将文件发送至邮箱或者ftp等途径实现，另一种是直接通过虚拟实验室首页的“我的文件”单击下载至本地电脑磁盘。</p>
                </div>
                <div class="tab-pane fade" id="solution">
<img src="imgs/vdi.png" class="center">
<p>　　当前瞬息万变的业务环境要求基础架构能够快速满足员工不断变化的桌面、应用和数据接入需求。不管您的企业正通过并购迅速壮大，还是只希望吸引并留住优秀人才，企业主管都非常清楚，企业必须提供比以往任何时候都更强大的接入功能，同时确保有效的控制和数据安全性。</p>
<p>　　在传统的办公室环境中，设备刷新、更换、打补丁和更新都会导致员工生产率下降，即使在管理最有效的环境中也不例外。此外，随着当代员工越来越多地希望使用自己的智能电话、平板电脑和笔记本来开展自由灵活办公，办公室和家之间的界限变得日益模糊。</p>
<p>　　ProBridge领先的桌面虚拟化解决方案使企业可以将Windows桌面、应用和数据转变为可从任何地点上通过任何设备访问的云服务。</p>                              
<h3>虚拟桌面管理</h3>
<img src="imgs/server-virtualization.png" class="center">
<p>　　以集中化服务的形式管理桌面、应用和数据，从而加强对它们的控制。 通过一个集中式控制台应用策略并快速分配和管理用户。</p>
<p>　　桌面映象管理支持迅速配置需要新应用软件和安全补丁，多映象支持为不同用户群体个性化的桌面应用。</p>
<div class="clearfix"></div>
<h3>成本节约</h3>
<img src="imgs/linkedclone.jpg" class="center">
<p>　　方案使用差分磁盘技术进行虚拟映像管理，通过差分磁盘的方案，可以大幅度降低管理成本以及存储成本。通过将用户配置数据隔离保存，做到秒级个性化桌面交付和简化的系统维护管理。</p>
<p>　　通过对服务器资源的高度虚拟化和共享，每台服务器可以做到高达100个并发桌面用户，支持的总用户数则不受限制。资源动态分配和智能回收将大大提高服务器的利用率，拒绝闲置资源，将每一份IT花费效果最大化。</p>
<div class="clearfix"></div>
<h3>灵活的接入方式</h3>
<img src="imgs/html5-rdp.png" class="center">
<p>　　通过使用HTML5技术在浏览器端实现RDP协议，用户可以直接在任何计算机终端进行云桌面的访问，而无需安装任何软件和插件，大大降部署和使用的难度。同时这种方式可以使得各类移动终端的直接访问支持，无需安装任何APP。</p>
<p>　　使用现有的基于 Windows、Mac 或 Linux 的笔记本电脑或台式机、瘦客户端、零客户端或移动设备从家里或办公室访问虚拟桌面。</p>                              
<div class="clearfix"></div>
<h3>安全优势</h3>
<p>　　封禁USB端口，限制网络是否让您的用户怨声载道？通过云桌面部署方式超越以往所有技术安全管理手段，不需要对终端设备进行限制改造，真正实现系统安全、防病毒和信息安全的全方位保护。所有数据永远留在云端，保证企业资产不会意外流失。</p>                              
<div class="clearfix"></div>
<p class="pull-right" style="color: #CCCCCC"><small><a href="http://www.probridge.com.cn" target="_blank">Powered by ProBridge Technology</a></small></p>
                </div>
              </div>
		</div>
	</div>
</jsp:body>
</t:template>