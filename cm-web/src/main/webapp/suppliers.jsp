<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="salesmachine.oim.api.OimConstants"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:basepage>
	<jsp:attribute name="maincontent">

  <div class="main-content">
      <div class="breadcrumbs" id="breadcrumbs"> 
   
        <ul class="breadcrumb">
          <li><a href="index.jsp"><i
							class="icon-home home-icon"></i>Home</a></li>
          <li class="active">Supplier</li>
        </ul>
      </div>
      <div class="page-content">
        <div class="page-header">
          <h1> Supplier <small> <i
							class="icon-double-angle-right"></i> overview &amp; stats </small> </h1>
        </div>
        <div class="row">
        <div class="col-xs-12">
              <div class="row">
              <div class="col-sm-12 inline">
              <div class="widget-container-span">
									<div class="widget-box">
										<div class="widget-header header-color-green">
											<h5>Tip:</h5>
											<div class="widget-toolbar">
												<a href="#" data-action="close"> <i
													class="icon-remove white"></i>
												</a>
											</div>
										</div>
										<div class="widget-body alert-success">
											<div class="widget-main">
												<p>To process orders with a supplier, you need to configure your supplier by providing your specific account details with the supplier.
If you have not yet configured your supplier, click "Add Supplier" to do it now. If your supplier is not in the list, write to us at <a
														href="mailto:support@inventorysource.com">support@inventorysource.com</a> and our support team will help you out with setting up your supplier.</p>
											</div>
										</div>
									</div>
								</div> 
                      <h4 class="lighter pull-left">
									<i class="icon-truck orange"></i> My Supplier </h4>
									
              <a data-toggle="modal" href="#mySupplieradd"
									id="addSupplier" class="btn btn-success pull-right">Add Supplier</a>
              </div>
						</div>
            <div class="space-2"></div>
            <div class="row">
              <div class="widget-box transparent">
            <div class="widget-main no-padding ">
              <table id="tableSupplier"
										class="table table-bordered table-striped table-responsive">
                <thead class="thin-border-bottom">
                  <tr>
                    <th><span class="hidden-xs">Supplier Name</span>
                    <i class="icon-user icon-2x blue visible-xs"></i>
					</th>
                    <%--<th><span class="hidden-xs visible-sm">Account</span>
					<i class="icon-barcode icon-2x blue visible-xs"></i>
					</th>
                     <th><i
													class="icon-plane icon-2x blue visible-xs"></i><span
													class="hidden-xs">Shipping Method</span></th> --%>
                    <th><span class="hidden-xs">Test Mode</span>
					<i class="icon-off icon-2x blue visible-xs"></i>
					</th>
                    <th class="hidden-xs">Order Push Details</th>
                    <th class="hidden-xs">Custom Supplier</th>
                    <th class="">Edit</th>
                    <th class=""></th>
                  </tr>
                </thead>
                <tbody>
                </tbody>
              </table>
              <!-- /widget-main --> 
            </div>
        
       </div>
         </div>
         </div>
        <div class="modal fade" id="mySupplieredit" tabindex="-1"
						role="dialog" aria-hidden="true">
              <div id="mysuppliereditdailog" class="modal-dialog">
                <div class="modal-content">
                  <div class="modal-header">
                    <button type="button" class="close"
										data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">Edit</h4>
                  </div>
                  <div class="modal-body ">
                    <div class="row">
                      <div class="container">
                        <form class="form-horizontal" role="form"
												action="/aggregators/suppliers" method="PUT"
												id="supplierform">
                         
                          <div class="form-group customSupplierNameDiv">
                              <label
														class="col-sm-5 control-label no-padding-right">Supplier Name</label>
                              <div class="col-sm-7">
                              <input class="width-70"
															name="name" type=text data-bind-vendorsupplier="oimSuppliers.supplierName" required />
                            </div>
												</div>
                         
                         
							<div class="form-group" id="account-number">
                              <label
														class="col-sm-5 control-label no-padding-right">Account</label>
                              <div class="col-sm-7">
                              <input class="width-70" name='accountno'
															type='text' data-bind-vendorsupplier="accountNumber"
															required="required" />
                            </div>
							</div>
							  <div class="form-group non-custom">
                              <label
														class="col-sm-5 control-label no-padding-right">Username</label>
                              <div class="col-sm-7">
                              <input class="width-70" name='login'
															type='text' data-bind-vendorsupplier="login"
															required="required" />
                            </div>
												</div>
                            <div class="form-group non-custom">
                            
                              <label
														class="col-sm-5 control-label no-padding-right">Password</label>
											
                              <div class="col-sm-7">
                              <input class="width-70" name='password'
															type='password' data-bind-vendorsupplier="password"
															required="required" />
                            </div>
												</div>
                            
                            <div class="form-group defShipCode"
													style="display: none;">
                              <label
														class="col-sm-5 control-label no-padding-right">Default Shipping Method Code</label>
                              <div class="col-sm-5">
                              <input class="width-100"
															name='defshippingmc' type='text'
															data-bind-vendorsupplier="defShippingMethodCode"
															required="required" />
                            </div>
												</div>
                        
                            <div class="form-group supplieremailDiv">
                              <label
														class="col-sm-5 control-label no-padding-right">Email</label>
                              <div class="col-sm-7">
                              <input class="width-70"
															required="required" name='supplieremail' type='email'
															data-bind-vendorsupplier="customMapper:oimSuppliers.oimSupplierMethodses[oimSupplierMethodNames.methodNameId=1].oimSupplierMethodattrValueses[oimSupplierMethodattrNames.attrId=1].attributeValue" />
                            </div>
												</div>
                            <div class="form-group"
													id="customSupplierFileFormatDiv">
                              <label
														class="col-sm-5 control-label no-padding-right">Order File Format</label>
                              <div class="col-sm-7">
                              <select class="width-70"
															name="customSupplierFileFormat"
															data-bind-vendorsupplier="customMapper:oimSuppliers.oimSupplierMethodses[oimSupplierMethodNames.methodNameId=1].oimSupplierMethodattrValueses[oimSupplierMethodattrNames.attrId=8].attributeValue">
                                <option value="1">CSV</option>
                                <option value="2">TSV</option>
                              </select>                  
                            </div>
												</div>
												
							 <div class="form-group" id="test-mode">
                              <label
														class="col-sm-5 control-label no-padding-right">Test Mode</label>
                              <div class="col-sm-7">
                              <select class="width-70" name="testmode"
															data-bind-vendorsupplier="testMode">
                                <option value="1" selected>Enabled</option>
                                <option value="0">Disabled</option>
                              </select>
                            </div>
												</div>
                            <div class="form-group" id="orderaction">
                              <label
														class="col-sm-5 control-label no-padding-right">Mark Earlier Orders</label>
                              <div class="col-sm-7">
                              <select class="width-70"
															name="updatewithstatus">
                                <option value="">No Action</option>
                                <option value="0">Unprocessed</option>
                                <option value="2">Processed</option>
                                <option value="3">Failed</option>
                                <option value="5">Manual</option>
                                <option value="6">Cancelled</option>
                              </select>
                            </div>
												</div>
                            <div class="form-group center">
                             <button class="btn btn-info btn-sm"
														id="update" type="button"> <i class="icon-save "></i>Update</button>
                            </div>
                         
                         
                        </form>
                        <!-- PAGE CONTENT BEGINS --> 
                        
                      </div>
                      
                    </div>
                  </div>
                </div>
                <!-- PAGE CONTENT ENDS --> 
              </div>
              <!-- /.col --> 
            </div>
            
             <div class="modal fade" id="mySupplierHGedit" tabindex="-1"
						role="dialog" aria-hidden="true">
              <div id="mysuppliereditdailog" class="modal-dialog">
                <div class="modal-content">
                  <div class="modal-header">
                    <button type="button"
										class="close hideHGSpecificDiv" data-dismiss="modal"
										aria-hidden="true">&times;</button>
                    <h4 class="modal-title">Edit</h4>
                  </div>
                  <div class="modal-body ">
                    <div class="row">
                      <div class="container">
                        <form class="form-horizontal" role="form"
												action="/aggregators/suppliers/updateHG" method="PUT"
												id="supplierformHG">
						<div id="accordion" class="accordion-style1 panel-group">
                           	<div
														class="panel panel-default honestGreen-settings hide">
									<div class="panel-heading">
										<h4 class="panel-title">
											<a class="accordion-toggle collapsed" data-toggle="collapse"
																	data-parent="#accordion" href="#collapseThree"> <i
																	class="icon-angle-right bigger-110"
																	data-icon-hide="icon-angle-down"
																	data-icon-show="icon-angle-right"></i> &nbsp;PHI Details
											</a>
										</h4>
									</div>
									<div class="panel-collapse collapse" id="collapseThree">
										<div class="panel-body">
										
											<div class="form-group">
							                              <label for="phi-ftp"
																		class="col-sm-5 control-label no-padding-right">FTP</label>
							                              <div class="col-sm-7">
							                                 <input class="width-70"
																			name="phi-ftp" minlength="2" type="text"
																			data-bind-vendorsupplier="customMapper:oimSuppliers.oimSupplierMethodses[oimSupplierMethodTypes.methodTypeId=3].oimSupplierMethodattrValueses[oimSupplierMethodattrNames.attrId=2].attributeValue" />
							                            </div>
											</div>
											 <div class="form-group">
				                            <div class="col-sm-5">
				                              <label
																			class="col-sm-offset-8 control-label no-padding-right">UserName</label>
																	</div>
				                              <div class="col-sm-7">
				                              <input class="width-70"
																			name="phi-login" type="text"
																			data-bind-vendorsupplier="customMapper:oimSuppliers.oimSupplierMethodses[oimSupplierMethodTypes.methodTypeId=3].oimSupplierMethodattrValueses[oimSupplierMethodattrNames.attrId=3].attributeValue" />
				                            </div>
																</div>
				                            <div class="form-group">
				                            <div class="col-sm-5">
				                              <label
																			class="col-sm-offset-8 control-label no-padding-right">Password</label>
																	</div>
				                              <div class="col-sm-7">
				                              <input class="width-70"
																			name="phi-password" type="password"
																			data-bind-vendorsupplier="customMapper:oimSuppliers.oimSupplierMethodses[oimSupplierMethodTypes.methodTypeId=3].oimSupplierMethodattrValueses[oimSupplierMethodattrNames.attrId=4].attributeValue" />
				                            </div>
																</div>
				                            <div class="form-group">
				                             <div class="col-sm-5">
				                              <label
																			class="col-sm-offset-8 control-label no-padding-right">Account</label>
											</div>
				                              <div class="col-sm-7">
				                              <input class="width-70"
																			name="phi-accountno" type="text"
																			data-bind-vendorsupplier="customMapper:oimSuppliers.oimSupplierMethodses[oimSupplierMethodTypes.methodTypeId=3].oimSupplierMethodattrValueses[oimSupplierMethodattrNames.attrId=9].attributeValue" />
				                            </div>
																</div>
										</div>
									</div>
									
					</div>
								
					<div class="panel panel-default honestGreen-settings hide">
							<div class="panel-heading">
								<h4 class="panel-title">
									<a class="accordion-toggle collapsed" data-toggle="collapse"
																	data-parent="#accordion" href="#collapseFour"> <i
																	class="icon-angle-right bigger-110"
																	data-icon-hide="icon-angle-down"
																	data-icon-show="icon-angle-right"></i> &nbsp;HVA Details
									</a>
								</h4>
							</div>
							<div class="panel-collapse collapse" id="collapseFour">
											<div class="panel-body">
										
											<div class="form-group">
							                            <div class="col-sm-5">
							                              <label for="hva-ftp"
																			class="col-sm-offset-8 control-label no-padding-right">FTP</label>
														</div>
							                              <div class="col-sm-7">
							                                 <input class="width-70"
																			name="hva-ftp" minlength="2" type="text"
																			data-bind-vendorsupplier="customMapper:oimSuppliers.oimSupplierMethodses[oimSupplierMethodTypes.methodTypeId=4].oimSupplierMethodattrValueses[oimSupplierMethodattrNames.attrId=2].attributeValue" />
							                            </div>
											</div>
											 <div class="form-group">
				                            <div class="col-sm-5">
				                              <label
																			class="col-sm-offset-8 control-label no-padding-right">UserName</label>
																	</div>
				                              <div class="col-sm-7">
				                              <input class="width-70"
																			name="hva-login" type="text"
																			data-bind-vendorsupplier="customMapper:oimSuppliers.oimSupplierMethodses[oimSupplierMethodTypes.methodTypeId=4].oimSupplierMethodattrValueses[oimSupplierMethodattrNames.attrId=3].attributeValue" />
				                            </div>
																</div>
				                            <div class="form-group">
				                            <div class="col-sm-5">
				                              <label
																			class="col-sm-offset-8 control-label no-padding-right">Password</label>
																	</div>
				                              <div class="col-sm-7">
				                              <input class="width-70"
																			name="hva-password" type="password"
																			data-bind-vendorsupplier="customMapper:oimSuppliers.oimSupplierMethodses[oimSupplierMethodTypes.methodTypeId=4].oimSupplierMethodattrValueses[oimSupplierMethodattrNames.attrId=4].attributeValue" />
				                            </div>
																</div>
				                            <div class="form-group">
				                             <div class="col-sm-5">
				                              <label
																			class="col-sm-offset-8 control-label no-padding-right">Account</label>
											</div>
				                              <div class="col-sm-7">
				                              <input class="width-70"
																			name="hva-accountno" type="text"
																			data-bind-vendorsupplier="customMapper:oimSuppliers.oimSupplierMethodses[oimSupplierMethodTypes.methodTypeId=4].oimSupplierMethodattrValueses[oimSupplierMethodattrNames.attrId=9].attributeValue" />
				                            </div>
																</div>
										</div>
							</div>
					</div>
					<br>
					 <div class="form-group">
                              <label
															class="col-sm-5 control-label no-padding-right">Test Mode</label>
                              <div class="col-sm-7">
                              <select class="width-70" name="testmode"
																data-bind-vendorsupplier="testMode">
                                <option value="1" selected>Enabled</option>
                                <option value="0">Disabled</option>
                              </select>
                            </div>
												</div>
                            <div class="form-group center">
                             <button
															class="btn btn-info btn-sm hideHGSpecificDiv"
															id="updateHG" type="button"> <i
																class="icon-save "></i>Update</button>
                            </div>
                         
                      </div>   
                        </form>
                        <!-- PAGE CONTENT BEGINS --> 
                        
                      </div>
                      
                    </div>
                  </div>
                </div>
                <!-- PAGE CONTENT ENDS --> 
              </div>
              <!-- /.col --> 
            </div>
            
<!--             moteng specific div (it can be used for those supplier which are FTP specific) -->

      <div class="modal fade" id="mySupplierFtpedit" tabindex="-1"
						role="dialog" aria-hidden="true">
              <div id="mysuppliereditdailog" class="modal-dialog">
                <div class="modal-content">
                  <div class="modal-header">
                    <button type="button" class="close"
										data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">Edit</h4>
                  </div>
                  <div class="modal-body ">
                    <div class="row">
                      <div class="container">
                        <form class="form-horizontal" role="form"
												action="/aggregators/suppliers" method="PUT"
												id="supplierformFtpBased">
                          <div class="form-group">
                              <label
														class="col-sm-5 control-label no-padding-right">Ftp Type</label>
                              <div class="col-sm-7">
                              <select class="width-70" name="ftpType"
															data-bind-vendorsupplier="customMapper:oimSuppliers.oimSupplierMethodses[oimSupplierMethodTypes.methodTypeId=1].oimSupplierMethodattrValueses[oimSupplierMethodattrNames.attrId=10].attributeValue"
															required="required">
                                <option value="FTP" selected>FTP</option>
                                <option value="SFTP">SFTP</option>
                              </select>
                            </div>
                            </div>
                         <div class="form-group">
                              <label
														class="col-sm-5 control-label no-padding-right">FTP</label>
                              <div class="col-sm-7">
                              <input class="width-70" name='ftpUrl'
															type='text'
															data-bind-vendorsupplier="customMapper:oimSuppliers.oimSupplierMethodses[oimSupplierMethodTypes.methodTypeId=1].oimSupplierMethodattrValueses[oimSupplierMethodattrNames.attrId=2].attributeValue"
															required="required" />
                            </div>
							</div>
							
							
							<div class="form-group">
                              <label
														class="col-sm-5 control-label no-padding-right">Account</label>
                              <div class="col-sm-7">
                              <input class="width-70" name='accountno'
															type='text'
															data-bind-vendorsupplier="customMapper:oimSuppliers.oimSupplierMethodses[oimSupplierMethodTypes.methodTypeId=1].oimSupplierMethodattrValueses[oimSupplierMethodattrNames.attrId=9].attributeValue"
															required="required" />
                            </div>
							</div>
							<div class="form-group baker-tayler-edit">
                              <label
														class="col-sm-5 control-label no-padding-right">Account Name</label>
                              <div class="col-sm-7">
                              <input class="width-70" name="accountname"
															type="text" 
															data-bind-vendorsupplier="customMapper:oimSuppliers.oimSupplierMethodses[oimSupplierMethodTypes.methodTypeId=1].oimSupplierMethodattrValueses[oimSupplierMethodattrNames.attrId=11].attributeValue" 
															required="required" />
                            </div>
												</div>
							  <div class="form-group">
                              <label
														class="col-sm-5 control-label no-padding-right">Username</label>
                              <div class="col-sm-7">
                              <input class="width-70" name='login'
															type='text'
															data-bind-vendorsupplier="customMapper:oimSuppliers.oimSupplierMethodses[oimSupplierMethodTypes.methodTypeId=1].oimSupplierMethodattrValueses[oimSupplierMethodattrNames.attrId=3].attributeValue"
															required="required" />
                            </div>
												</div>
                            <div class="form-group">
                            
                              <label
														class="col-sm-5 control-label no-padding-right">Password</label>
											
                              <div class="col-sm-7">
                              <input class="width-70" name='password'
															type='password'
															data-bind-vendorsupplier="customMapper:oimSuppliers.oimSupplierMethodses[oimSupplierMethodTypes.methodTypeId=1].oimSupplierMethodattrValueses[oimSupplierMethodattrNames.attrId=4].attributeValue"
															required="required" />
                            </div>
												</div>
                            
                            <div class="form-group">
                              <label
														class="col-sm-5 control-label no-padding-right">Test Mode</label>
                              <div class="col-sm-7">
                              <select class="width-70" name="testmode"
															data-bind-vendorsupplier="testMode">
                                <option value="1" selected>Enabled</option>
                                <option value="0">Disabled</option>
                              </select>
                            </div>
												</div>
                            <div class="form-group center">
                             <button class="btn btn-info btn-sm"
														id="updateFtpBasedSupplier" type="button"> <i
															class="icon-save "></i>Update</button>
                            </div>
                         
                         
                        </form>
                        <!-- PAGE CONTENT BEGINS --> 
                        
                      </div>
                      
                    </div>
                  </div>
                </div>
                <!-- PAGE CONTENT ENDS --> 
              </div>
              <!-- /.col --> 
            </div>
            
            <!-- Europa edit start -->
            
             <div class="modal fade" id="mySupplierEuropaEdit"
						tabindex="-1" role="dialog" aria-hidden="true">
              <div id="mysuppliereditdailog" class="modal-dialog">
                <div class="modal-content">
                  <div class="modal-header">
                    <button type="button" class="close"
										data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">Edit</h4>
                  </div>
                  <div class="modal-body ">
                    <div class="row">
                      <div class="container">
                        <form class="form-horizontal" role="form"
												action="/aggregators/suppliers" method="PUT"
												id="supplierEuropaform">
                         
							<div class="form-group">
                              <label
														class="col-sm-5 control-label no-padding-right">Customer Number</label>
                              <div class="col-sm-7">
                              <input class="width-70" name='customerNum'
															type='text' data-bind-vendorsupplier="accountNumber"
															required="required" />
                            </div>
							</div>
							  <div class="form-group">
                              <label
														class="col-sm-5 control-label no-padding-right">Business Name</label>
                              <div class="col-sm-7">
                              <input class="width-70"
															name='businessName' type='text'
															data-bind-vendorsupplier="login" required="required" />
                            </div>
												</div>
                          
                          
                            <div class="form-group">
                              <label
														class="col-sm-5 control-label no-padding-right">Test Mode</label>
                              <div class="col-sm-7">
                              <select class="width-70" name="testmode"
															data-bind-vendorsupplier="testMode">
                                <option value="1" selected>Enabled</option>
                                <option value="0">Disabled</option>
                              </select>
                            </div>
												</div>
                         
                            <div class="form-group center">
                             <button class="btn btn-info btn-sm"
														id="updateEuropa" type="button"> <i
															class="icon-save "></i>Update</button>
                            </div>
                         
                         
                        </form>
                        <!-- PAGE CONTENT BEGINS --> 
                        
                      </div>
                      
                    </div>
                  </div>
                </div>
                <!-- PAGE CONTENT ENDS --> 
              </div>
              <!-- /.col --> 
            </div>
            <!-- Europa edir end -->
    
    
	 <div class="modal fade" id="mySupplieradd" tabindex="-1" role="dialog"
						aria-hidden="true">
              <div id="mysupplieradddailog" class="modal-dialog">
                <div class="modal-content">
                  <div class="modal-header">
                    <button type="button" class="close"
										data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">Add</h4>
                  </div>
                  <div class="modal-body ">
                    <div class="row">
                      <div class="container">
                        <form class="form-horizontal" role="form"
												action="/aggregators/suppliers" method="PUT"
												id="supplierAddForm">
                        <div class="form-group">
                              <label for="suppliername"
														class="col-sm-5 control-label no-padding-right">Supplier</label>
                              <div class="col-sm-7">
                              <select class="width-70" id="Supplieradd"
															name="suppliername" required>
                                <option value="0">Custom</option>
                              </select>
                            </div>
						</div>
						<div id="accordion2"
													class="accordion-style1 panel-group honestGreen-settings hide">
						<div class="panel panel-default honestGreen-settings hide">
									<div class="panel-heading">
										<h4 class="panel-title">
											<a class="accordion-toggle collapsed" data-toggle="collapse"
																	data-parent="#accordion2" href="#collapseOne"> <i
																	class="icon-angle-right bigger-110"
																	data-icon-hide="icon-angle-down"
																	data-icon-show="icon-angle-right"></i> &nbsp;PHI Details
											</a>
										</h4>
									</div>
									<div class="panel-collapse collapse" id="collapseOne">
										<div class="panel-body">
										
											<div class="form-group">
							                              <label for="phi-ftp"
																		class="col-sm-5 control-label no-padding-right">FTP</label>
							                              <div class="col-sm-7">
							                                 <input class="width-70"
																			name="phi-ftp" minlength="2" type="text" value=""
																			required />
							                            </div>
											</div>
											<div class="form-group">
				                            
				                              <label
																		class="col-sm-5 control-label no-padding-right">Account</label>
										      <div class="col-sm-7">
				                              <input class="width-70"
																			name="phi-accountno" type="text" value="" required />
				                            </div>
																</div>
											 <div class="form-group">
				                         
				                              <label
																		class="col-sm-5 control-label no-padding-right">UserName</label>
																	
				                              <div class="col-sm-7">
				                              <input class="width-70"
																			name="phi-login" type="text" value="" required />
				                            </div>
																</div>
											
				                            <div class="form-group">
				                              <label
																		class="col-sm-5 control-label no-padding-right">Password</label>
										      <div class="col-sm-7">
				                              <input class="width-70"
																			name="phi-password" type="text" value="" required />
				                            </div>
																</div>
				                            
										</div>
									</div>
									
					</div>
								
					<div class="panel panel-default honestGreen-settings hide">
							<div class="panel-heading">
								<h4 class="panel-title">
									<a class="accordion-toggle collapsed" data-toggle="collapse"
																	data-parent="#accordion2" href="#collapseTwo"> <i
																	class="icon-angle-right bigger-110"
																	data-icon-hide="icon-angle-down"
																	data-icon-show="icon-angle-right"></i> &nbsp;HVA Details
									</a>
								</h4>
							</div>
							<div class="panel-collapse collapse" id="collapseTwo">
											<div class="panel-body">
											<div class="form-group">
							                              <label for="hva-ftp"
																		class="col-sm-5 control-label no-padding-right">FTP</label>
							                              <div class="col-sm-7">
							                                 <input class="width-70"
																			name="hva-ftp" minlength="2" type="text" value=""
																			required />
							                            </div>
											</div>
											 <div class="form-group">
				                              <label
																		class="col-sm-5 control-label no-padding-right">Account</label>
				                              <div class="col-sm-7">
				                              <input class="width-70"
																			name="hva-accountno" type="text" value="" required />
				                            </div>
																</div>
											 <div class="form-group">
				                              <label
																		class="col-sm-5 control-label no-padding-right">UserName</label>
				                              <div class="col-sm-7">
				                              <input class="width-70"
																			name="hva-login" type="text" value="" required />
				                            </div>
																</div>
											
				                            <div class="form-group">
				                              <label
																		class="col-sm-5 control-label no-padding-right">Password</label>
				                              <div class="col-sm-7">
				                              <input class="width-70"
																			name="hva-password" type="text" value="" required />
				                            </div>
																</div>
				                           
										</div>
							</div>
					</div>
						
					</div>	
 								 <div class="form-group supplier-class moteng-ftp">
                              <label
														class="col-sm-5 control-label no-padding-right">Ftp Type</label>
                              <div class="col-sm-7">
                              <select class="width-70" name="ftpType"
															required>
                                <option value="FTP" selected>FTP</option>
                                <option value="SFTP">SFTP</option>
                              </select>
                            </div>
												</div>
							 <div class="form-group supplier-class moteng-ftp">
                              <label
														class="col-sm-5 control-label no-padding-right">FTP</label>
                              <div class="col-sm-7">
                              <input class="width-70" name="moteng-ftp"
															type="text" value="" required />
                            </div>
												</div>
												
							 <div class="form-group supplier-class baker-tayler">
                              <label
														class="col-sm-5 control-label no-padding-right">Account Name</label>
                              <div class="col-sm-7">
                              <input class="width-70" name="accountname"
															type="text" value="" required />
                            </div>
												</div>
							  <div class="form-group supplier-class account-class">
                              <label
														class="col-sm-5 control-label no-padding-right">Account Number</label>
                              <div class="col-sm-7">
                              <input class="width-70" name="accountno"
															type="text" value="" required />
                            </div>
												</div>
												
							 <div class="form-group supplier-class">
                              <label
														class="col-sm-5 control-label no-padding-right">UserName</label>
                              <div class="col-sm-7">
                              <input class="width-70" name="login"
															type="text" value="" required />
                            </div>
												</div>
                            <div class="form-group supplier-class">
                              <label
														class="col-sm-5 control-label no-padding-right">Password</label>
                              <div class="col-sm-7">
                              <input class="width-70" name="password"
															type="text" value="" required />
                            </div>
												</div>
                          
<!--                             <div class="form-group supplier-class" -->
<!-- 													style="display: none;"> -->
<!--                               <label -->
<!-- 														class="col-sm-5 control-label no-padding-right">Default Shipping Method Code</label> -->
<!--                               <div class="col-sm-7"> -->
<!--                               <input class="width-70" -->
<!-- 															name="defshippingmc" type="text" value="" required /> -->
<!--                             </div> -->
<!-- 												</div> -->
							
							 <div class="form-group europa">
                              <label
														class="col-sm-5 control-label no-padding-right">Business Name</label>
                              <div class="col-sm-7">
                              <input class="width-70"
															name="europa-business-name" type="text" value="" required />
                            </div>
												</div>
							 <div class="form-group europa">
                              <label
														class="col-sm-5 control-label no-padding-right">Customer Number</label>
                              <div class="col-sm-7">
                              <input class="width-70"
															name="europa-customer-num" type="text" value="" required />
                            </div>
							</div>
							<!-- Custom Supplier code start -->
							 <div class="form-group customSupplierNameDiv">
                              <label
														class="col-sm-5 control-label no-padding-right">Supplier Name</label>
                              <div class="col-sm-7">
                              <input class="width-70"
															name="name" type=text value="" required />
                            </div>
												</div>
                            <div class="form-group supplieremailDiv">
                              <label
														class="col-sm-5 control-label no-padding-right">Email</label>
                              <div class="col-sm-7">
                              <input class="width-70"
															name="supplieremail" type="email" value="" required />
                            </div>
												</div>
                            <div class="form-group customfileformatDiv">
                              <label
														class="col-sm-5 control-label no-padding-right">Order File Format</label>
                              <div class="col-sm-7">
                              <select class="width-70"
															name="customSupplierFileFormat" required>
                                <option value="1" SELECTED>CSV</option>
                                <option value="2">TSV</option>
                              </select>
                            </div>
							</div>
							<div class="form-group supplierDefaultShippingCodeDiv">
                              <label
														class="col-sm-5 control-label no-padding-right">Default Shipping Code</label>
                              <div class="col-sm-7">
                              <input class="width-70"
															name="defshippingmc" type="text" value="" required />
                            </div>
												</div>
							<!-- Custom Supplier code end -->
                            <div class="form-group">
                              <label
														class="col-sm-5 control-label no-padding-right">Test Mode</label>
                              <div class="col-sm-7">
                              <select class="width-70" name="testmode">
                                <option value="1" selected>Enabled</option>
                                <option value="0">Disabled</option>
                              </select>
                            </div>
												</div>
                             <div class="form-group center">
                             <button class="btn btn-info btn-sm"
														id="savesupplier" type="button"> <i
															class="icon-save "></i>Save</button>
                            </div>
                           
                         
                         
                        </form>
                        <!-- PAGE CONTENT BEGINS --> 
                        
                      </div>
                      
                    </div>
                  </div>
                </div>
                <!-- PAGE CONTENT ENDS --> 
              </div>
              <!-- /.col --> 
            </div>
            
            <div class="modal fade" id="editshippingmethods"
						tabindex="-1" role="dialog" aria-hidden="true">
              <div id="mysupplieradddailog" class="modal-dialog">
                <div class="modal-content">
                  <div class="modal-header">
                    <button type="button" class="close"
										data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">Shipping Methods</h4>
                  </div>
                  <div class="modal-body ">
                    <div class="row">
                      <div class="container">
                      <div class="col-sm-12">
							<table id="tableShippingMap"
													class="table table-bordered table-striped table-responsive">
								<thead class="thin-border-bottom">
									<tr>
										<th><i class="icon-barcode icon-2x blue visible-xs"></i><span
																class="hidden-xs visible-sm">Shipping Carrier</span></th>
										<th><i class="icon-plane icon-2x blue visible-xs"></i><span
																class="hidden-xs">Shipping Method</span></th>
										<th><i class="icon-user icon-2x blue visible-xs"></i> <span
																class="hidden-xs">Supplier Shipping Carrier</span></th>
										<th><i class="icon-user icon-2x blue visible-xs"></i> <span
																class="hidden-xs">Supplier Shipping Method</span></th>
									</tr>
								</thead>
								<tbody>
								</tbody>
							</table>
						</div>
                        
                      </div>
                    </div>
                  </div>
                </div>
			</div>
            </div>
      </div> 
    </div>
  </div>
</jsp:attribute>
	<jsp:attribute name="pagejs">
<script type="text/javascript">
	$(document).on('click', '.hideHGSpecificDiv', function(e) {
		$('.honestGreen-settings').removeClass('show');
		$('.honestGreen-settings').addClass('hide');

	});
	var table_vendorSuppliers;
	function edit(e) {
		var vendorSupplier = table_vendorSuppliers.row(e[0]).data();
		var vendorSupplierTemp = JSON.parse(JSON.stringify(vendorSupplier));
		//var rowIndex = table_vendorSuppliers.fnGetPosition(e[0]);
		GenericBinder('vendorsupplier', vendorSupplierTemp);
		if (vendorSupplier.oimSuppliers.isCustom) {
			$('#customSupplierFileFormatDiv').show();
			$('.supplieremailDiv').show();
			$('.non-custom').hide();
			$('.customSupplierNameDiv').show();
			$('.defShipCode').show();
		} else {
			if(vendorSupplier.oimSuppliers.supplierId == 2224 || vendorSupplier.oimSuppliers.supplierId == 2225){
				$('#customSupplierFileFormatDiv').hide();
				$('.supplieremailDiv').hide();
				$('.customSupplierNameDiv').hide();
				$('.defShipCode').hide();
				$('.non-custom').hide();
				$('#orderaction').hide();
				$('#account-number').hide();
			}else{
				$('#customSupplierFileFormatDiv').hide();
				$('.supplieremailDiv').hide();
				$('.customSupplierNameDiv').hide();
				$('.defShipCode').hide();
				$('.non-custom').show();
			}
			
		}

		$('#saveshippingmapping').off('click').on(
				'click',
				vendorSupplier,
				function(e) {
					var data = {};
					$('#shippingmethodsForm :input').each(function() {
						if (this.name) {
							data[this.name] = this.value;
						}
					});
					$(this).CRUD(
							{
								url : "aggregators/suppliers/"
										+ e.data.oimSuppliers.supplierId
										+ '/shippingmapping',
								method : "PUT",
								data : JSON.stringify(data)
							});
				});
		$('#supplierform').validate();
		$('#supplierform').prop('action',
				'aggregators/suppliers/' + vendorSupplier.vendorSupplierId);
		$('#update').off("click").on("click", function(e) {
			$('#supplierform').submit();
		});
	}

	function editHG(e) {
		$('.honestGreen-settings').removeClass('hide');
		$('.honestGreen-settings').addClass('show');
		var vendorSupplier = table_vendorSuppliers.row(e[0]).data();

		var vendorSupplierTemp = JSON.parse(JSON.stringify(vendorSupplier));
		console.log(vendorSupplierTemp);
		//console.log(vendorSupplierTemp.oimSuppliers.oimSupplierMethodses[vendorSupplierTemp.oimSuppliers.oimSupplierMethodses.oimSupplierMethodTypes.methodTypeId=3].oimSupplierMethodattrValueses[oimSupplierMethodattrNames.attrId=2].attributeValue);
		//var rowIndex = table_vendorSuppliers.fnGetPosition(e[0]);
		GenericBinder('vendorsupplier', vendorSupplierTemp);
		if (vendorSupplier.oimSuppliers.isCustom) {
			$('#customSupplierFileFormatDiv').show();
			$('.supplieremailDiv').show();
		} else {
			$('#customSupplierFileFormatDiv').hide();
			$('.supplieremailDiv').hide();
		}

		$('#saveshippingmapping').off('click').on(
				'click',
				vendorSupplier,
				function(e) {
					var data = {};
					$('#shippingmethodsForm :input').each(function() {
						if (this.name) {
							data[this.name] = this.value;
						}
					});
					$(this).CRUD(
							{
								url : "aggregators/suppliers/"
										+ e.data.oimSuppliers.supplierId
										+ '/shippingmapping',
								method : "PUT",
								data : JSON.stringify(data)
							});
				});
		$('#supplierformHG').validate();
		$('#supplierformHG').prop(
				'action',
				'aggregators/suppliers/updateHG/'
						+ vendorSupplier.vendorSupplierId);
		$('#supplierformHG').prop('method', 'PUT');
		$('#updateHG').off("click").on("click", function(e) {
			$('#supplierformHG').submit();
		});
	}

	//editFtpBasedSupplier
	function editFtpBasedSupplier(e) {
		var vendorSupplier = table_vendorSuppliers.row(e[0]).data();
		var vendorSupplierTemp = JSON.parse(JSON.stringify(vendorSupplier));
		console.log(vendorSupplierTemp);
		//var rowIndex = table_vendorSuppliers.fnGetPosition(e[0]);
		GenericBinder('vendorsupplier', vendorSupplierTemp);
		if (vendorSupplier.oimSuppliers.isCustom) {
			$('#customSupplierFileFormatDiv').show();
			$('.supplieremailDiv').show();
		} else {
			$('#customSupplierFileFormatDiv').hide();
			$('.supplieremailDiv').hide();
			if(vendorSupplier.oimSuppliers.supplierId==2202){
				$('.baker-tayler-edit').removeClass("hide");
				$('.baker-tayler-edit').addClass("show");
			}else{
				$('.baker-tayler-edit').removeClass("show");
				$('.baker-tayler-edit').addClass("hide");
			}
		}

		$('#saveshippingmapping').off('click').on(
				'click',
				vendorSupplier,
				function(e) {
					var data = {};
					$('#shippingmethodsForm :input').each(function() {
						if (this.name) {
							data[this.name] = this.value;
						}
					});
					$(this).CRUD(
							{
								url : "aggregators/suppliers/"
										+ e.data.oimSuppliers.supplierId
										+ '/shippingmapping',
								method : "PUT",
								data : JSON.stringify(data)
							});
				});
		$('#supplierformFtpBased').validate();
		$('#supplierformFtpBased').prop('action',
				'aggregators/suppliers/' + vendorSupplier.vendorSupplierId);
		$('#updateFtpBasedSupplier').off("click").on("click", function(e) {
			$('#supplierformFtpBased').submit();
		});
	}
	//editEuropaSupplier
	function editEuropaSupplier(e) {
		var vendorSupplier = table_vendorSuppliers.row(e[0]).data();
		var vendorSupplierTemp = JSON.parse(JSON.stringify(vendorSupplier));
		console.log(vendorSupplierTemp);
		//var rowIndex = table_vendorSuppliers.fnGetPosition(e[0]);
		GenericBinder('vendorsupplier', vendorSupplierTemp);
		if (vendorSupplier.oimSuppliers.isCustom) {
			$('#customSupplierFileFormatDiv').show();
			$('.supplieremailDiv').show();
		} else {
			$('#customSupplierFileFormatDiv').hide();
			$('.supplieremailDiv').hide();
		}

		$('#saveshippingmapping').off('click').on(
				'click',
				vendorSupplier,
				function(e) {
					var data = {};
					$('#shippingmethodsForm :input').each(function() {
						if (this.name) {
							data[this.name] = this.value;
						}
					});
					$(this).CRUD(
							{
								url : "aggregators/suppliers/"
										+ e.data.oimSuppliers.supplierId
										+ '/shippingmapping',
								method : "PUT",
								data : JSON.stringify(data)
							});
				});
		$('#supplierEuropaform').validate();
		$('#supplierEuropaform').prop('action',
				'aggregators/suppliers/' + vendorSupplier.vendorSupplierId);
		$('#updateEuropa').off("click").on("click", function(e) {
			$('#supplierEuropaform').submit();
		});
	}

	function del(e) {
		bootbox
				.confirm(
						'This will remove this supplier record.  Click <b>OK</b> to remove it fully.',
						function(b) {
							if (b) {
								var vendorSupplier = table_vendorSuppliers.row(
										e[0]).data();
								$(e[0])
										.CRUD(
												{
													method : "DELETE",
													url : 'aggregators/suppliers/subscriptions/'
															+ vendorSupplier.vendorSupplierId,
													data : JSON
															.stringify(vendorSupplier),
													message : {
														title : 'Delete Supplier',
														text : 'Supplier Deleted successfully.'
													},
													success : function(data,
															textStatus, jqXHR) {
														table_vendorSuppliers
																.row(e[0])
																.remove();
														table_vendorSuppliers
																.draw();
														getAlerts();
													}
												});
							}
						});
	}

	jQuery(function($) {
		table_vendorSuppliers = $('#tableSupplier')
				.DataTable(
						{
							"bPaginate" : false,
							"bLengthChange" : false,
							"sAjaxSource" : 'aggregators/suppliers',
							"fnServerData" : function(sSource, aoData,
									fnCallback, oSettings) {
								oSettings.jqXHR = $(this).CRUD({
									type : "GET",
									url : sSource,
									data : aoData,
									cache : false,
									success : fnCallback
								});
							},
							"sAjaxDataProp" : '',
							"aoColumns" : [

									{
										"mData" : function(row) {

											return '<a class="btn btn-default icon-info-sign btn-xs visible-xs addresspop" data-toggle="popover" data-container="body"  data-placement="bottom" data-content="'+row.oimSuppliers.supplierName+'" data-original-title="Supplier Name"></a><div class="hidden-xs">'
													+ row.oimSuppliers.supplierName
													+ '</div>';
										}
									},
									/*{
										"mData" : "accountNumber"
									},
									 {
										"mData" : function(vendorSupplier) {
											return '<a class="btn btn-info btn-sm hidden-xs icon-exchange" href="#editshippingmethods"'
													+ ' data-toggle="modal" onclick="$.CM.viewSupplierShippingMap($(this).parent().parent());"></a><a class="btn btn-info btn-xs icon-info visible-xs btn-xs"'
													+ ' href="#editshippingmethods" data-toggle="modal" onclick="$.CM.viewSupplierShippingMap($(this).parent().parent());"></a>';
										}
									}, */
									{
										"mData" : function(vendorSupplier) {
											return vendorSupplier.testMode ? "Enabled"
													: "Disabled";
										}
									},
									{
										"sClass" : "hidden-xs",
										"mData" : function(o) {
											var ret = '';

											if (o.oimSuppliers.supplierId == 1822
													|| o.oimSuppliers.supplierId == 221 || o.oimSuppliers.supplierId == 2161 || o.oimSuppliers.supplierId ==2202) {
												for (var i = 0; i < o.oimSuppliers.oimSupplierMethodses.length; i++) {
													var oimSupplierMethod = o.oimSuppliers.oimSupplierMethodses[i];
													if (oimSupplierMethod.oimSupplierMethodNames
															&& o.oimSuppliers.oimSupplierMethodses[i].oimVendors.vendorId == o.vendors.vendorId) {
														ret += "<strong>";
														ret += oimSupplierMethod.oimSupplierMethodTypes.methodTypeName;
														ret += "</strong>: ";
														ret += oimSupplierMethod.oimSupplierMethodNames.methodName;
														ret += "<br/>";
													}
													if (oimSupplierMethod.oimSupplierMethodattrValueses
															&& o.oimSuppliers.oimSupplierMethodses[i].oimVendors.vendorId == o.vendors.vendorId) {
														for (var j = 0; j < oimSupplierMethod.oimSupplierMethodattrValueses.length; j++) {
															var attrVal = oimSupplierMethod.oimSupplierMethodattrValueses[j];
															ret += "<strong>";
															ret += attrVal.oimSupplierMethodattrNames.attrName;
															ret += "</strong> : ";
															if (attrVal.oimSupplierMethodattrNames.attrId == 8) {
																switch (attrVal.attributeValue) {
																case "1":
																	ret += "CSV";
																	break;
																case "2":
																	ret += "XML";
																	break;
																case "3":
																	ret += "Plain Text";
																	break;
																}
															} else {
																ret += attrVal.attributeValue;
															}

															ret += "<br/>";
														}
													}
												}
											} else {
												if (o.oimSuppliers.supplierId == 2002) {
													ret += "<strong>Business Name</strong>: "
															+ o.accountNumber;
													ret += "<br> <strong>Customer Number</strong>: "
															+ o.login;
												}
												else if(o.oimSuppliers.isCustom){
													ret += "<strong>Account</strong>: "
														+ o.accountNumber;
												}
												else if(o.oimSuppliers.supplierId == 2224 || o.oimSuppliers.supplierId == 2225){
													ret = "<span></span>";
												}
												else {
													ret += "<strong>Account</strong>: "
															+ o.accountNumber;
													ret += "<br> <strong>Login</strong>: "
															+ o.login;
													ret += "<br> <strong>Password</strong>: "
															+ o.password;
												}
											}
											return ret;
										}
									},
									{
										"sClass" : "hidden-xs",
										"mData" : function(vendorSupplier) {
											if (vendorSupplier.oimSuppliers.isCustom)
												return "Yes";
											return "No";
										}
									},
									{
										"bSortable" : false,
										"mData" : function(vendorSupplier) {
											if (vendorSupplier.oimSuppliers.supplierId == 1822)
												return '<a class="btn btn-info btn-sm hidden-xs icon-pencil" href="#mySupplierHGedit" data-toggle="modal" onclick="editHG((($(this)).parent()).parent())"></a><a class="btn btn-info btn-xs icon-pencil visible-xs btn-xs" href="#mySupplierHGedit" data-toggle="modal" onclick="editHG((($(this)).parent()).parent())"></a>';
											else if (vendorSupplier.oimSuppliers.supplierId == 221 || vendorSupplier.oimSuppliers.supplierId == 2161 || vendorSupplier.oimSuppliers.supplierId == 2202) {
												return '<a class="btn btn-info btn-sm hidden-xs icon-pencil" href="#mySupplierFtpedit" data-toggle="modal" onclick="editFtpBasedSupplier((($(this)).parent()).parent())"></a><a class="btn btn-info btn-xs icon-pencil visible-xs btn-xs" href="#mySupplierFtpedit" data-toggle="modal" onclick="editFtpBasedSupplier((($(this)).parent()).parent())"></a>';
											} else if (vendorSupplier.oimSuppliers.supplierId == 2002) {
												//mySupplierHGedit
												return '<a class="btn btn-info btn-sm hidden-xs icon-pencil" href="#mySupplierEuropaEdit" data-toggle="modal" onclick="editEuropaSupplier((($(this)).parent()).parent())"></a><a class="btn btn-info btn-xs icon-pencil visible-xs btn-xs" href="#mySupplierEuropaEdit" data-toggle="modal" onclick="editEuropaSupplier((($(this)).parent()).parent())"></a>';
											} else {
												return '<a class="btn btn-info btn-sm hidden-xs icon-pencil" href="#mySupplieredit" data-toggle="modal" onclick="edit((($(this)).parent()).parent())"></a><a class="btn btn-info btn-xs icon-pencil visible-xs btn-xs" href="#mySupplieredit" data-toggle="modal" onclick="edit((($(this)).parent()).parent())"></a>';
											}
										}
									},
									{
										"bSortable" : false,
										"mData" : function(vendorSupplier) {
											return '<a class="btn btn-danger btn-sm hidden-xs" onclick="del((($(this)).parent()).parent())"><i class="icon-trash"></i>Delete</a><a class="btn btn-danger visible-xs btn-xs icon-trash" onclick="del((($(this)).parent()).parent())"></a>';
										}
									} ]
						});

		$('#Supplieradd').on('change', function() {
			var a = $(this).val();
			if (a == '0') {
				$('.customfileformatDiv').show();
				$('.supplieremailDiv').show();
				$('.honestGreen-settings').removeClass("show");
				$('.honestGreen-settings').addClass("hide");
				$('.supplier-class').removeClass("show");
				$('.supplier-class').addClass("hide");
				$('.europa').removeClass("show");
				$('.europa').addClass("hide");
				$('.customSupplierNameDiv').removeClass("hide");
				$('.customSupplierNameDiv').addClass("show");
				$('.account-class').removeClass("hide");
				$('.account-class').addClass("show");
				$('.supplierDefaultShippingCodeDiv').removeClass("hide");
				$('.supplierDefaultShippingCodeDiv').addClass("show");
				$('.baker-tayler').removeClass("show");
				$('.baker-tayler').addClass("hide");
			} else {
				if (a == '1822') {
					$('.honestGreen-settings').removeClass("hide");
					$('.honestGreen-settings').addClass("show");
					$('.supplier-class').removeClass("show");
					$('.supplier-class').addClass("hide");
					$('.customSupplierNameDiv').removeClass("show");
					$('.customSupplierNameDiv').addClass("hide");
				} else {
					$('.honestGreen-settings').removeClass("show");
					$('.honestGreen-settings').addClass("hide");
					$('.supplier-class').removeClass("hide");
					$('.supplier-class').addClass("show");
					$('.moteng-ftp').removeClass("show");
					$('.moteng-ftp').addClass("hide");
					$('.baker-tayler').removeClass("show");
					$('.baker-tayler').addClass("hide");
					$('.europa').removeClass("show");
					$('.europa').addClass('hide');
					$('.customSupplierNameDiv').removeClass("show");
					$('.customSupplierNameDiv').addClass("hide");
					if (a == '221' || a=='2161') {
						$('.moteng-ftp').removeClass("hide");
						$('.moteng-ftp').addClass("show");
					} else if (a == '2002') {
						$('.europa').removeClass("hide");
						$('.europa').addClass("show");
						$('.supplier-class').removeClass("show");
						$('.supplier-class').addClass("hide");
					}
					else if(a=='2202'){
						$('.moteng-ftp').removeClass("hide");
						$('.moteng-ftp').addClass("show");
						$('.baker-tayler').removeClass("hide");
						$('.baker-tayler').addClass("show");	
					}
					else if(a == '2224' || a == '2225'){ // Rotcho or Fox
						$('.supplier-class').removeClass("show");
						$('.supplier-class').addClass("hide");
					}
					
				}
				$('.customfileformatDiv').hide();
				$('.supplieremailDiv').hide();
				$('.supplierDefaultShippingCodeDiv').removeClass("show");
				$('.supplierDefaultShippingCodeDiv').addClass("hide");
			}
		});

		$('#addSupplier').on(
				'click',
				function() {
					$('#supplierAddForm :input').each(function() {
						this.value = '';
					});
					$('#Supplieradd').empty().append(
							"<option>Please wait...</option>");
					$(this).CRUD(
							{
								url : "aggregators/suppliers/unsubscribed",
								method : "GET",
								success : function(data) {
									$('#Supplieradd').empty();
									$("<option/>").val("").html(
											"Select Supplier").appendTo(
											$('#Supplieradd'));
									$.each(data, function() {
										$("<option/>").val(this.supplierId)
												.html(this.supplierName)
												.appendTo($('#Supplieradd'));
									});
									$("<option/>").val(0).html("Custom")
											.appendTo($('#Supplieradd'));
								}
							});
				});
		$('#supplierform')
				.validate(
						{
							invalidHandler : function(event, validator) {
								// 'this' refers to the form
								var errors = validator.numberOfInvalids();
								if (errors) {
									var message = errors == 1 ? 'You missed 1 field. It has been highlighted'
											: 'You missed '
													+ errors
													+ ' fields. They have been highlighted';
// 									$.gritter.add({
// 										title : "Supplier information",
// 										text : message
// 									});
									errorAlert('Supplier information',message);
								}
							},
							submitHandler : function(form) {
								var formArray = $(form).serializeArray();
								var formObject = {};
								$.each(formArray, function(i, v) {
									formObject[v.name] = v.value;
								});
								$(this)
										.CRUD(
												{
													url : $(form)
															.attr('action'),
													method : "PUT",
													data : JSON
															.stringify(formObject),
													success : function(a, b, c) {
														$('.modal').modal(
																'hide');

// 														$.gritter
// 																.add({
// 																	title : 'Update Supplier',
// 																	text : 'Supplier updated successfully.'
// 																});
														successAlert('Update Supplier','Supplier updated successfully.');
														table_vendorSuppliers.ajax
																.reload();
													}
												});
							},
							highlight : function(e) {
								$(e).closest('.form-group').removeClass(
										'has-info').addClass('has-error');
							},
							success : function(e) {
								$(e).closest('.form-group').removeClass(
										'has-error').addClass('has-info');
								$(e).remove();
							}
						});

		$('#supplierformHG')
				.validate(
						{
							invalidHandler : function(event, validator) {
								// 'this' refers to the form
								var errors = validator.numberOfInvalids();
								if (errors) {
									var message = errors == 1 ? 'You missed 1 field. It has been highlighted'
											: 'You missed '
													+ errors
													+ ' fields. They have been highlighted';
// 									$.gritter.add({
// 										title : "Supplier information",
// 										text : message
// 									});
									errorAlert('Supplier information',message);
								}
							},
							submitHandler : function(form) {
								var formArray = $(form).serializeArray();
								var formObject = {};
								$.each(formArray, function(i, v) {
									formObject[v.name] = v.value;
								});
								$(this)
										.CRUD(
												{
													url : $(form)
															.attr('action'),
													method : "PUT",
													data : JSON
															.stringify(formObject),
													success : function(a, b, c) {
														$('.modal').modal(
																'hide');

// 														$.gritter
// 																.add({
// 																	title : 'Update Supplier',
// 																	text : 'Supplier updated successfully.'
// 																});
														successAlert('Update Supplier','Supplier updated successfully.');
														table_vendorSuppliers.ajax
																.reload();
													}
												});
							},
							highlight : function(e) {
								$(e).closest('.form-group').removeClass(
										'has-info').addClass('has-error');
							},
							success : function(e) {
								$(e).closest('.form-group').removeClass(
										'has-error').addClass('has-info');
								$(e).remove();
							}
						});

		$('#supplierformFtpBased')
				.validate(
						{
							invalidHandler : function(event, validator) {
								// 'this' refers to the form
								var errors = validator.numberOfInvalids();
								if (errors) {
									var message = errors == 1 ? 'You missed 1 field. It has been highlighted'
											: 'You missed '
													+ errors
													+ ' fields. They have been highlighted';
// 									$.gritter.add({
// 										title : "Supplier information",
// 										text : message
// 									});
									errorAlert('Supplier information',message);
								}
							},
							submitHandler : function(form) {
								var formArray = $(form).serializeArray();
								var formObject = {};
								$.each(formArray, function(i, v) {
									formObject[v.name] = v.value;
								});
								$(this)
										.CRUD(
												{
													url : $(form)
															.attr('action'),
													method : "PUT",
													data : JSON
															.stringify(formObject),
													success : function(a, b, c) {
														$('.modal').modal(
																'hide');

// 														$.gritter
// 																.add({
// 																	title : 'Update Supplier',
// 																	text : 'Supplier updated successfully.'
// 																});
														successAlert('Update Supplier','Supplier updated successfully.');
														table_vendorSuppliers.ajax
																.reload();
													}
												});
							},
							highlight : function(e) {
								$(e).closest('.form-group').removeClass(
										'has-info').addClass('has-error');
							},
							success : function(e) {
								$(e).closest('.form-group').removeClass(
										'has-error').addClass('has-info');
								$(e).remove();
							}
						});

		//supplierEuropaform
		$('#supplierEuropaform')
				.validate(
						{
							invalidHandler : function(event, validator) {
								// 'this' refers to the form
								var errors = validator.numberOfInvalids();
								if (errors) {
									var message = errors == 1 ? 'You missed 1 field. It has been highlighted'
											: 'You missed '
													+ errors
													+ ' fields. They have been highlighted';
// 									$.gritter.add({
// 										title : "Supplier information",
// 										text : message
// 									});
									errorAlert('Supplier information',message);
								}
							},
							submitHandler : function(form) {
								var formArray = $(form).serializeArray();
								var formObject = {};
								$.each(formArray, function(i, v) {
									formObject[v.name] = v.value;
								});
								$(this)
										.CRUD(
												{
													url : $(form)
															.attr('action'),
													method : "PUT",
													data : JSON
															.stringify(formObject),
													success : function(a, b, c) {
														$('.modal').modal(
																'hide');

// 														$.gritter
// 																.add({
// 																	title : 'Update Supplier',
// 																	text : 'Supplier updated successfully.'
// 																});
														successAlert('Update Supplier','Supplier updated successfully.');
														table_vendorSuppliers.ajax
																.reload();
													}
												});
							},
							highlight : function(e) {
								$(e).closest('.form-group').removeClass(
										'has-info').addClass('has-error');
							},
							success : function(e) {
								$(e).closest('.form-group').removeClass(
										'has-error').addClass('has-info');
								$(e).remove();
							}
						});

		$('#supplierAddForm')
				.validate(
						{
							invalidHandler : function(event, validator) {
								// 'this' refers to the form
								var errors = validator.numberOfInvalids();
								if (errors) {
									var message = errors == 1 ? 'You missed 1 field. It has been highlighted'
											: 'You missed '
													+ errors
													+ ' fields. They have been highlighted';

// 									$.gritter.add({
// 										title : "Supplier information",
// 										text : message
// 									});
									errorAlert('Supplier information',message);
								}
							},
							submitHandler : function(form) {
								var formArray = $(form).serializeArray();
								var formObject = {};
								$.each(formArray, function(i, v) {
									formObject[v.name] = v.value;
								});
								$(this)
										.CRUD(
												{
													url : "aggregators/suppliers",
													method : "PUT",
													data : JSON
															.stringify(formObject),

													success : function() {
// 														$.gritter
// 																.add({
// 																	title : 'Add Supplier',
// 																	text : 'Supplier added successfully.'
// 																});
														successAlert('Add Supplier','Supplier added successfully.');
														$('#mySupplieradd')
																.modal('hide');
														table_vendorSuppliers.ajax
																.reload();

													}
												});
							},
							highlight : function(e) {
								$(e).closest('.form-group').removeClass(
										'has-info').addClass('has-error');
							},
							success : function(e) {
								$(e).closest('.form-group').removeClass(
										'has-error').addClass('has-info');
								$(e).remove();
							}
						});
		$('#savesupplier').off('click').on('click', function() {
			$('#supplierAddForm').submit();
		});
		$(document).on('click', ".deleteparent", function() {
			$(this).parent().parent().remove();
		});

		$("#addshipping")
				.on(
						'click',
						function() {
							var time = (new Date()).getTime();
							var html = '<div class="form-group">'
									+ '<div class="col-sm-5 no-padding-left"><input type="text" class="width-100" name="shipping_text_'
									+ time
									+ '" /></div>'
									+ '<div class="col-sm-6 no-padding-left"><input type="text" class="width-100" name="shipping_method_text_'+time+'" /></div><div class="col-sm-1 no-padding-left"><button class="btn btn-info btn-sm deleteparent" type="button">'
									+ '<i class="icon-trash"></i></button></div></div>';
							$(this).parent().parent().prepend($(html));
						});

	});
</script>
</jsp:attribute>
</t:basepage>
