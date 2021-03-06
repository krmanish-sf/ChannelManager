package com.is.cm.core.domain;

import java.util.Date;

public class ChannelFileId extends DomainBase implements java.io.Serializable {
	private static final long serialVersionUID = -9048684820035903874L;
	private Integer channelId;
	private Integer fileTypeId;
	private Date insertionTm;
	private Date deleteTm;

	public ChannelFileId() {
	}

	public Integer getChannelId() {
		return this.channelId;
	}

	public void setChannelId(Integer channelId) {
		this.channelId = channelId;
	}

	public Integer getFileTypeId() {
		return this.fileTypeId;
	}

	public void setFileTypeId(Integer fileTypeId) {
		this.fileTypeId = fileTypeId;
	}

	public Date getInsertionTm() {
		return this.insertionTm;
	}

	public void setInsertionTm(Date insertionTm) {
		this.insertionTm = insertionTm;
	}

	public Date getDeleteTm() {
		return this.deleteTm;
	}

	public void setDeleteTm(Date deleteTm) {
		this.deleteTm = deleteTm;
	}

	@Override
	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof ChannelFileId))
			return false;
		ChannelFileId castOther = (ChannelFileId) other;

		return ((this.getChannelId() == castOther.getChannelId()) || (this
				.getChannelId() != null && castOther.getChannelId() != null && this
				.getChannelId().equals(castOther.getChannelId())))
				&& ((this.getFileTypeId() == castOther.getFileTypeId()) || (this
						.getFileTypeId() != null
						&& castOther.getFileTypeId() != null && this
						.getFileTypeId().equals(castOther.getFileTypeId())))
				&& ((this.getInsertionTm() == castOther.getInsertionTm()) || (this
						.getInsertionTm() != null
						&& castOther.getInsertionTm() != null && this
						.getInsertionTm().equals(castOther.getInsertionTm())))
				&& ((this.getDeleteTm() == castOther.getDeleteTm()) || (this
						.getDeleteTm() != null
						&& castOther.getDeleteTm() != null && this
						.getDeleteTm().equals(castOther.getDeleteTm())));
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getChannelId() == null ? 0 : this.getChannelId().hashCode());
		result = 37
				* result
				+ (getFileTypeId() == null ? 0 : this.getFileTypeId()
						.hashCode());
		result = 37
				* result
				+ (getInsertionTm() == null ? 0 : this.getInsertionTm()
						.hashCode());
		result = 37 * result
				+ (getDeleteTm() == null ? 0 : this.getDeleteTm().hashCode());
		return result;
	}

}
