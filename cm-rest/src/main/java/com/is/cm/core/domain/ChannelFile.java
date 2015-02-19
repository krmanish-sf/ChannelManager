package com.is.cm.core.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;

public class ChannelFile extends DomainBase implements java.io.Serializable {
	private static final long serialVersionUID = -8912336381172803352L;
	private Integer channelFileId;
	@JsonBackReference("ChannelFile-Filetype")
	private Filetype oimFiletypes;
	@JsonBackReference("ChannelFile-Channel")
	private Channel channel;
	private Date insertionTm;
	private Date deleteTm;

	public ChannelFile() {
	}

	public Integer getChannelFileId() {
		return this.channelFileId;
	}

	public void setChannelFileId(Integer channelFileId) {
		this.channelFileId = channelFileId;
	}

	public Filetype getOimFiletypes() {
		return this.oimFiletypes;
	}

	public void setOimFiletypes(Filetype oimFiletypes) {
		this.oimFiletypes = oimFiletypes;
	}

	public Channel getOimChannels() {
		return this.channel;
	}

	public void setOimChannels(Channel channel) {
		this.channel = channel;
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

}
