package com.is.cm.rest.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.is.cm.core.domain.ReportDataWrapper;
import com.is.cm.core.domain.VendorsuppOrderhistory;
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
	private static final Logger LOG = LoggerFactory
			.getLogger(ReportingQueriesController.class);
	@Autowired
	private ReportService reportService;

	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public ReportDataWrapper getMaxReportData(
			@RequestBody Map<String, Date> requestData) {
		LOG.debug("Getting Report Data ...");
		Date st = requestData.get("startDate");
		Date ed = requestData.get("endDate");
		MaxReportEvent event = reportService
				.getReport(new RequestMaxReportEvent(st, ed));
		return event.getEntity();
	}

	@RequestMapping(value = "/download/{reportType}/{o}/{a}", method = RequestMethod.GET)
	public HttpEntity<byte[]> downloadReport(@PathVariable String reportType,
			@PathVariable Long o, @PathVariable Long a) throws IOException {
		LOG.debug("Getting Report Data ...");
		Date st = new Date((Long) o);
		Date ed = new Date((Long) a);
		ReadEvent<String> downloadReportData = reportService
				.getDownloadReportData(new RequestDownloadReportEvent(st, ed,
						reportType));
		byte[] documentBody = downloadReportData.getEntity().getBytes();
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy");
		String fileName = reportType + "_" + sdf.format(st) + "_"
				+ sdf.format(ed) + ".csv";
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		header.set("Content-Disposition", "attachment; filename=" + fileName);
		header.setContentLength(documentBody.length);
		return new HttpEntity<byte[]>(documentBody, header);
	}

	@RequestMapping(value = "/{reportType}", method = RequestMethod.POST)
	@ResponseBody
	public ReportDataWrapper getReportData(@PathVariable String reportType,
			@RequestBody Map<String, Date> dateRange) {
		LOG.debug("Getting {} Report Data ...", reportType);
		Date st = dateRange.get("startDate");
		Date ed = dateRange.get("endDate");
		ReadEvent<ReportDataWrapper> event = reportService
				.getReportData(new RequestDownloadReportEvent(st, ed,
						reportType));
		return event.getEntity();
	}

	@RequestMapping(value = "/system/{reportType}", method = RequestMethod.POST)
	@ResponseBody
	public ReportDataWrapper getSystemReportData(
			@PathVariable String reportType,
			@RequestBody Map<String, Date> dateRange) {
		LOG.debug("Getting {} Report Data ...", reportType);
		Date st = dateRange.get("startDate");
		Date ed = dateRange.get("endDate");
		ReadEvent<ReportDataWrapper> event = reportService
				.getSystemReportData(new RequestDownloadReportEvent(st, ed,
						reportType));
		return event.getEntity();
	}

	@RequestMapping(value = "/system/vendor-supplier-history", method = RequestMethod.GET)
	@ResponseBody
	public List<VendorsuppOrderhistory> getVendorSupplierHistory() {
		LOG.debug("Getting vendor-supplier-history");
		ReadEvent<List<VendorsuppOrderhistory>> event = reportService
				.getVendorSupplierHistory(new PagedDataEvent<VendorsuppOrderhistory>(
						1, 100));
		return event.getEntity();
	}

	@RequestMapping(value = "/notifications", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Map> getNotifications() {
		ReadEvent<Map<String, Map>> event = reportService
				.getNotifications(new RequestReadEvent<Map<String, Map>>());
		return event.getEntity();
	}
}
