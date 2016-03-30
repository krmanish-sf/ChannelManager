package com.is.cm.rest.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.is.cm.core.domain.Channel;
import com.is.cm.core.domain.DataTableCriterias;
import com.is.cm.core.domain.Order;
import com.is.cm.core.domain.OrderBatch;
import com.is.cm.core.domain.PagedDataResult;
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
import com.is.cm.core.service.ReportService;

@Controller
@RequestMapping("/aggregators/reports")
public class ReportingQueriesController {
	private static final Logger LOG = LoggerFactory.getLogger(ReportingQueriesController.class);
	@Autowired
	private ReportService reportService;

	private static final DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
	private static final Logger log = LoggerFactory.getLogger(ReportingQueriesController.class);

	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public ReportDataWrapper getMaxReportData(@RequestBody Map<String, String> requestData) {
		LOG.debug("Getting Report Data ...");
		String st = requestData.get("startDate");
		String ed = requestData.get("endDate");
		Date startDate, endDate;
		try {
			startDate = df.parse(st);
			endDate = df.parse(ed);
			endDate.setHours(23);
			endDate.setMinutes(59);
			endDate.setSeconds(59);
			MaxReportEvent event = reportService.getReport(new RequestMaxReportEvent(startDate, endDate));
			return event.getEntity();
		} catch (ParseException e) {
			log.error("Error in parsing the provided date string", e.getMessage());
			throw new RuntimeException("Error in parsing the provided date string");
		}
	}

	@RequestMapping(value = "/download/{reportType}/{o}/{a}", method = RequestMethod.GET)
	public HttpEntity<byte[]> downloadReport(@PathVariable String reportType, @PathVariable Long o, @PathVariable Long a) throws IOException {
		LOG.debug("Getting Report Data ...");
		Date st = new Date((Long) o);
		Date ed = new Date((Long) a);
		ReadEvent<String> downloadReportData = reportService.getDownloadReportData(new RequestDownloadReportEvent(st, ed, reportType));
		byte[] documentBody = downloadReportData.getEntity().getBytes();
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy");
		String fileName = reportType + "_" + sdf.format(st) + "_" + sdf.format(ed) + ".csv";
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		header.set("Content-Disposition", "attachment; filename=" + fileName);
		header.setContentLength(documentBody.length);
		return new HttpEntity<byte[]>(documentBody, header);
	}

	@RequestMapping(value = "/{reportType}", method = RequestMethod.POST)
	@ResponseBody
	public ReportDataWrapper getReportData(@PathVariable String reportType, @RequestBody Map<String, String> dateRange) {
		LOG.debug("Getting {} Report Data ...", reportType);
		String st = dateRange.get("startDate");
		String ed = dateRange.get("endDate");
		Date startDate, endDate;
		try {
			startDate = df.parse(st);
			endDate = df.parse(ed);
			endDate.setHours(23);
			endDate.setMinutes(59);
			endDate.setSeconds(59);
			ReadEvent<ReportDataWrapper> event = reportService.getReportData(new RequestDownloadReportEvent(startDate, endDate, reportType));
			return event.getEntity();
		} catch (ParseException e) {
			log.error("Error in parsing the provided date string", e.getMessage());
			throw new RuntimeException("Error in parsing the provided date string");
		}
	}

	@RequestMapping(value = "/system/{reportType}", method = RequestMethod.POST)
	@ResponseBody
	public ReportDataWrapper getSystemReportData(@PathVariable String reportType, @RequestBody Map<String, String> dateRange) {
		LOG.debug("Getting {} Report Data ...", reportType);
		String st = dateRange.get("startDate");
		String ed = dateRange.get("endDate");
		Date startDate, endDate;
		try {
			startDate = df.parse(st);
			endDate = df.parse(ed);
			endDate.setHours(23);
			endDate.setMinutes(59);
			endDate.setSeconds(59);
			ReadEvent<ReportDataWrapper> event = reportService.getSystemReportData(new RequestDownloadReportEvent(startDate, endDate, reportType));
			return event.getEntity();
		} catch (ParseException e) {
			log.error("Error in parsing the provided date string", e.getMessage());
			throw new RuntimeException("Error in parsing the provided date string");
		}
	}

	@RequestMapping(value = "/system/vendor-supplier-history", method = RequestMethod.POST)
	@ResponseBody
	public PagedDataResult<VendorsuppOrderhistory> getVendorSupplierHistory(@RequestBody DataTableCriterias criteria) {
		LOG.debug("Getting vendor-supplier-history");
		PagedDataResultEvent<VendorsuppOrderhistory> event = reportService
				.getVendorSupplierHistory(new RequestReadEvent<DataTableCriterias>(criteria));
		event.getEntity().setDraw(criteria.getDraw());
		return event.getEntity();

	}

	@RequestMapping(value = "/system/channel-pull-history", method = RequestMethod.POST)
	@ResponseBody
	public PagedDataResult<OrderBatch> getChannelPullHistory(@RequestBody DataTableCriterias criteria) {
		LOG.debug("Getting channel-pull-history");
		/*
		 * Map<String, Date> dateRange1 = new HashMap<String, Date>(); startDate
		 * = df.parse(st); endDate = df.parse(ed); endDate.setHours(23);
		 * endDate.setMinutes(59); endDate.setSeconds(59);
		 * dateRange1.put("startDate", startDate); dateRange1.put("endDate",
		 * endDate);
		 */
		PagedDataResultEvent<OrderBatch> event = reportService.getChannelPullHistory(new RequestReadEvent<DataTableCriterias>(criteria));
		event.getEntity().setDraw(criteria.getDraw());
		return event.getEntity();
	}

	@RequestMapping(value = "/notifications", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Map> getNotifications() {
		ReadEvent<Map<String, Map>> event = reportService.getNotifications(new RequestReadEvent<Map<String, Map>>());
		return event.getEntity();
	}

	@RequestMapping(value = "/system/alerts", method = RequestMethod.GET)
	@ResponseBody
	public List getAlerts() {
		ReadCollectionEvent event = reportService.getAlerts(new RequestReadEvent());
		return event.getEntity();
	}

	@RequestMapping(value = "/system/channel-alerts", method = RequestMethod.GET)
	@ResponseBody
	public List getChannelAlerts() {
		ReadCollectionEvent event = reportService.getChannelAlerts(new RequestReadEvent());
		return event.getEntity();
	}

	@RequestMapping(value = "/system/order-history", method = RequestMethod.GET)
	@ResponseBody
	public List getOrderHistory() {
		ReadCollectionEvent event = reportService.getOrderHistory(new RequestReadEvent());
		return event.getEntity();
	}

	// '/cm-rest/aggregators/reports/system/fetchUnShippedOrders/'+vendorID,
	@RequestMapping(value = "/system/fetchUnShippedOrders/{vendorID}", method = RequestMethod.GET)
	@ResponseBody
	public List fetchUnShippedOrders(@PathVariable int vendorID) {
		ReadCollectionEvent event = reportService.fetchUnShippedOrders(vendorID);
		return event.getEntity();
	}

	@RequestMapping(value = "/system/fetchUnConfirmedOrders/{vendorID}", method = RequestMethod.GET)
	@ResponseBody
	public List fetchUnConfirmedOrders(@PathVariable int vendorID) {
		ReadCollectionEvent event = reportService.fetchUnConfirmedOrders(vendorID);
		return event.getEntity();
	}

	@RequestMapping(value = "/system/trackOrderFileLocation/{vid}/PO/{poNumber}/location/{location}/detailId/{detailId}", method = RequestMethod.GET)
	@ResponseBody
	public List trackOrderFileLocation(@PathVariable("vid") String vid, @PathVariable("poNumber") String poNumber,
			@PathVariable("location") String location, @PathVariable("detailId") String detailId) {
		ReadCollectionEvent event = reportService.trackOrderFileLocation(vid, poNumber, location,detailId);
		return event.getEntity();
	}

}
