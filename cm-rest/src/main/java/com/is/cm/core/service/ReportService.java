package com.is.cm.core.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.is.cm.core.domain.DataTableCriterias;
import com.is.cm.core.domain.OrderBatch;
import com.is.cm.core.domain.ReportDataWrapper;
import com.is.cm.core.domain.VendorsuppOrderhistory;
import com.is.cm.core.event.PagedDataResultEvent;
import com.is.cm.core.event.ReadCollectionEvent;
import com.is.cm.core.event.ReadEvent;
import com.is.cm.core.event.RequestReadEvent;
import com.is.cm.core.event.reports.MaxReportEvent;
import com.is.cm.core.event.reports.PagedDataEvent;
import com.is.cm.core.event.reports.RequestDownloadReportEvent;
import com.is.cm.core.event.reports.RequestMaxReportEvent;

public interface ReportService {
	MaxReportEvent getReport(RequestMaxReportEvent event);

	ReadEvent<String> getDownloadReportData(RequestDownloadReportEvent requestDownloadReportEvent);

	ReadEvent<Map<String, Map>> getNotifications(RequestReadEvent<Map<String, Map>> requestReadEvent);

	ReadEvent<ReportDataWrapper> getReportData(RequestDownloadReportEvent event);

	ReadEvent<ReportDataWrapper> getSystemReportData(RequestDownloadReportEvent event);

	PagedDataResultEvent<VendorsuppOrderhistory> getVendorSupplierHistory(RequestReadEvent<DataTableCriterias> event);

	ReadCollectionEvent getAlerts(RequestReadEvent<Map<String, Map>> requestReadEvent);

	ReadCollectionEvent getChannelAlerts(RequestReadEvent requestReadEvent);

	PagedDataResultEvent<OrderBatch> getChannelPullHistory(RequestReadEvent<DataTableCriterias> event);

	ReadCollectionEvent getOrderHistory(RequestReadEvent requestReadEvent);

	ReadCollectionEvent fetchUnShippedOrders(int vendorID);

	ReadCollectionEvent fetchUnConfirmedOrders(int vendorID);

	ReadCollectionEvent trackOrderFileLocation(String vid, String poNumber, String location, String detailId);
}
