/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.wechat;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
public class HelloWeChat extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public HelloWeChat() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	@Override
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html;charset=UTF-8");
		final PrintWriter pw = response.getWriter();
		String echo = request.getParameter("echostr");
		echo = new String(echo.getBytes("ISO-8859-1"),"UTF-8");
		pw.println(echo);
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html;charset=UTF-8");
		final PrintWriter pw = response.getWriter();
		final String wxMsgXml = IOUtils.toString(request.getInputStream(),"utf-8");
		WeChatTextMessage textMsg = null;
		try {
			textMsg = getWeChatTextMessage(wxMsgXml);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		final StringBuffer replyMsg = new StringBuffer();
		if(textMsg != null){
			//增加你所需要的处理逻辑，这里只是简单重复消息
			replyMsg.append("您给我的消息是：");
			replyMsg.append(textMsg.getContent());
		}
		else{
			replyMsg.append(":)不是文本的消息，我暂时看不懂");
		}
		final String returnXml = getReplyTextMessage(replyMsg.toString(), textMsg.getFromUserName());
		pw.println(returnXml);
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	@Override
	public void init() throws ServletException {
		// Put your code here
	}

	private WeChatTextMessage getWeChatTextMessage(final String xml){
		final XStream xstream = new XStream(new DomDriver());
		xstream.alias("xml", WeChatTextMessage.class);
		xstream.aliasField("ToUserName", WeChatTextMessage.class, "toUserName");
		xstream.aliasField("FromUserName", WeChatTextMessage.class, "fromUserName");
		xstream.aliasField("CreateTime", WeChatTextMessage.class, "createTime");
		xstream.aliasField("MsgType", WeChatTextMessage.class, "messageType");
		xstream.aliasField("Content", WeChatTextMessage.class, "content");
		xstream.aliasField("MsgId", WeChatTextMessage.class, "msgId");
		final WeChatTextMessage wechatTextMessage = (WeChatTextMessage)xstream.fromXML(xml);
		return wechatTextMessage;
	}

	private String getReplyTextMessage(final String content, final String weChatUser){
		final WeChatReplyTextMessage we = new WeChatReplyTextMessage();
		we.setMessageType("text");
		we.setFuncFlag("0");
		we.setCreateTime(new Long(new Date().getTime()).toString());
		we.setContent(content);
		we.setToUserName(weChatUser);
		we.setFromUserName("shanghaiweather");//TODO 浣犵殑鍏紬甯愬彿寰俊鍙?
		final XStream xstream = new XStream(new DomDriver());
		xstream.alias("xml", WeChatReplyTextMessage.class);
		xstream.aliasField("ToUserName", WeChatReplyTextMessage.class, "toUserName");
		xstream.aliasField("FromUserName", WeChatReplyTextMessage.class, "fromUserName");
		xstream.aliasField("CreateTime", WeChatReplyTextMessage.class, "createTime");
		xstream.aliasField("MsgType", WeChatReplyTextMessage.class, "messageType");
		xstream.aliasField("Content", WeChatReplyTextMessage.class, "content");
		xstream.aliasField("FuncFlag", WeChatReplyTextMessage.class, "funcFlag");
		final String xml =xstream.toXML(we);
		return xml;
	}

}
