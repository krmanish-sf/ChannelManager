package salesmachine.hibernatedb;

import java.io.Serializable;

public class OimChannelShippingMap implements Serializable {
  private static final long serialVersionUID = 2519469217063741239L;
  private int id;
  private OimSupportedChannels oimSupportedChannel;
  private String shippingRegEx;
  private OimShippingCarrier oimShippingCarrier;
  private OimShippingMethod oimShippingMethod;
  private OimChannels oimChannel;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public OimSupportedChannels getOimSupportedChannel() {
    return oimSupportedChannel;
  }

  public void setOimSupportedChannel(OimSupportedChannels oimSupportedChannel) {
    this.oimSupportedChannel = oimSupportedChannel;
  }

  public String getShippingRegEx() {
    return shippingRegEx;
  }

  public void setShippingRegEx(String shippingRegEx) {
    this.shippingRegEx = shippingRegEx;
  }

  public OimShippingCarrier getOimShippingCarrier() {
    return oimShippingCarrier;
  }

  public void setOimShippingCarrier(OimShippingCarrier oimShippingCarrier) {
    this.oimShippingCarrier = oimShippingCarrier;
  }

  public OimShippingMethod getOimShippingMethod() {
    return oimShippingMethod;
  }

  public void setOimShippingMethod(OimShippingMethod oimShippingMethod) {
    this.oimShippingMethod = oimShippingMethod;
  }

  public OimChannels getOimChannel() {
    return oimChannel;
  }

  public void setOimChannel(OimChannels oimChannel) {
    this.oimChannel = oimChannel;
  }
}
