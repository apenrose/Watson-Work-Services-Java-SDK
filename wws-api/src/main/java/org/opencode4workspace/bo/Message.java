package org.opencode4workspace.bo;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Christian Guedemann
 * @since 0.5.0
 * 
 *        Serializable object corresponding to a Message in a Conversation in a Watson Workspace space
 *
 */
public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	private String id;
	// TODO: Make this an enum
	private String contentType;
	private String content;
	private Date created;
	private Date updated;
	private Person createdBy;
	private Person updatedBy;

	/**
	 * @return String, id of the Message
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            String, id of the Message
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return String, content type of the Message
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @param contentType
	 *            String, content type of the Message
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * @return String, content of the Message
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content
	 *            String, content of the Message
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return Date the Message was written to the Conversation
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * @param created
	 *            Date the Message was written to the Conversation
	 */
	public void setCreated(Date created) {
		this.created = created;
	}

	/**
	 * @return Date the Message was last updated in the Conversation
	 */
	public Date getUpdated() {
		return updated;
	}

	/**
	 * @param updated
	 *            Date the Message was last updated in the Conversation
	 */
	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	/**
	 * @return Person who created the Message
	 */
	public Person getCreatedBy() {
		return createdBy;
	}

	/**
	 * @param createdBy
	 *            Person who created the Message
	 */
	public void setCreatedBy(Person createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return Person who last updated the Message
	 */
	public Person getUpdatedBy() {
		return updatedBy;
	}

	/**
	 * @param updatedBy
	 *            Person who last updated the Message
	 */
	public void setUpdatedBy(Person updatedBy) {
		this.updatedBy = updatedBy;
	}

}
