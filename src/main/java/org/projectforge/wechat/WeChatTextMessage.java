/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.wechat;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
public class WeChatTextMessage {

	private String toUserName;
	private String fromUserName;
	private String createTime;
	private String messageType;//text
	private String content;
	private String msgId;
	/**
	 * @return the toUserName
	 */
	public String getToUserName() {
		return toUserName;
	}
	/**
	 * @param toUserName the toUserName to set
	 * @return this for chaining.
	 */
	public void setToUserName(final String toUserName) {
		this.toUserName = toUserName;
		return;
	}
	/**
	 * @return the fromUserName
	 */
	public String getFromUserName() {
		return fromUserName;
	}
	/**
	 * @param fromUserName the fromUserName to set
	 * @return this for chaining.
	 */
	public void setFromUserName(final String fromUserName) {
		this.fromUserName = fromUserName;
		return;
	}
	/**
	 * @return the createTime
	 */
	public String getCreateTime() {
		return createTime;
	}
	/**
	 * @param createTime the createTime to set
	 * @return this for chaining.
	 */
	public void setCreateTime(final String createTime) {
		this.createTime = createTime;
		return;
	}
	/**
	 * @return the messageType
	 */
	public String getMessageType() {
		return messageType;
	}
	/**
	 * @param messageType the messageType to set
	 * @return this for chaining.
	 */
	public void setMessageType(final String messageType) {
		this.messageType = messageType;
		return;
	}
	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 * @return this for chaining.
	 */
	public void setContent(final String content) {
		this.content = content;
		return;
	}
	/**
	 * @return the msgId
	 */
	public String getMsgId() {
		return msgId;
	}
	/**
	 * @param msgId the msgId to set
	 * @return this for chaining.
	 */
	public void setMsgId(final String msgId) {
		this.msgId = msgId;
		return;
	}



}
