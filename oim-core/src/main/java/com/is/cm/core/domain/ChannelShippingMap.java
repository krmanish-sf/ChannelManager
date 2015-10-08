package com.is.cm.core.domain;

import java.io.Serializable;

import salesmachine.hibernatedb.OimChannelShippingMap;

public class ChannelShippingMap implements Serializable {
	private static final long serialVersionUID = 2519469217063741239L;
	private int id;
	// private OimSupportedChannels oimSupportedChannel;
	private String shippingRegEx;
	private ShippingCarrier oimShippingCarrier;
	private ShippingMethod oimShippingMethod;
	private Channel oimChannel;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getShippingRegEx() {
		return shippingRegEx;
	}

	public void setShippingRegEx(String shippingRegEx) {
		this.shippingRegEx = shippingRegEx;
	}

	public ShippingCarrier getOimShippingCarrier() {
		return oimShippingCarrier;
	}

	public void setOimShippingCarrier(ShippingCarrier oimShippingCarrier) {
		this.oimShippingCarrier = oimShippingCarrier;
	}

	public ShippingMethod getShippingMethod() {
		return oimShippingMethod;
	}

	public void setShippingMethod(ShippingMethod oimShippingMethod) {
		this.oimShippingMethod = oimShippingMethod;
	}

	public static ChannelShippingMap from(OimChannelShippingMap entity) {
		ChannelShippingMap channelShippingMap = new ChannelShippingMap();
		channelShippingMap.setId(entity.getId());
		channelShippingMap.setOimShippingCarrier(ShippingCarrier.from(entity
				.getOimShippingCarrier()));
		channelShippingMap.setShippingMethod(ShippingMethod.from(entity
				.getOimShippingMethod()));
		channelShippingMap.setShippingRegEx(entity.getShippingRegEx());
		channelShippingMap.setOimChannel(Channel.from(entity.getOimChannel()));
		
		return channelShippingMap;
	}

  public Channel getOimChannel() {
    return oimChannel;
  }

  public void setOimChannel(Channel oimChannel) {
    this.oimChannel = oimChannel;
  }
}
