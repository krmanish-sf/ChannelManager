package com.is.cm.core.persistance;

import java.util.List;
import java.util.Map;

import com.is.cm.core.domain.ChannelShippingMap;
import com.is.cm.core.domain.ShippingMethod;

public interface ShippingRepository {

  List<ShippingMethod> findShippingMethods();

  List<ChannelShippingMap> findRegExForChannel(int supportedChannelId, int shippingMethodId);

  ChannelShippingMap saveChannelShippingMap(int shippingMethodId, int supportedChannelId,
      String rexex);

  void deleteChannelShippingMapping(int id);

  String updateShippingMapping(int channelShippingMapId, Map<String, String> entity);

}
