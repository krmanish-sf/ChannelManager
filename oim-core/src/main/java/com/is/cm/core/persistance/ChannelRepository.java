package com.is.cm.core.persistance;

import java.util.List;
import java.util.Map;

import com.is.cm.core.domain.Channel;
import com.is.cm.core.domain.ChannelShippingMap;
import com.is.cm.core.domain.Filetype;
import com.is.cm.core.domain.SupportedChannel;
import com.is.cm.core.domain.UploadedFile;

import salesmachine.hibernatedb.OimChannels;

public interface ChannelRepository {
  Channel save(int channelId, Map<String, String> channelDetails);

  void delete(int id);

  OimChannels findById(int id);

  List<Channel> findAll();

  List<Filetype> getFileTypes(int channelId);

  List<UploadedFile> getOimUploadedFiles(int channelId);

  Channel findByName(String name);

  List<ChannelShippingMap> findShippingMapping(Integer entity);

  List<SupportedChannel> findSupportedChannels();

  Map<String, String> findBigcommerceAuthDetailsByUrl(String url);

  Map<String, String> findShopifyAuthDetailsByUrl(String entity);
}
