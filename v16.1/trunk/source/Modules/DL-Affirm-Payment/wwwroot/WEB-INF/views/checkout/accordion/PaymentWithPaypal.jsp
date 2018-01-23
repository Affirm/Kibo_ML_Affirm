<%@ include file="/WEB-INF/views/common/TagLibs.jsp" %>
<%@ page import="com.marketlive.biz.paypal.utility.PayPalConstants" %>
<%@ page import="com.marketlive.app.b2c.thirdparty.visa.VisaCheckoutConstants" %>
<%----------------------------------------------------------------------------------------------------------------------
  -    Description: Accordion Checkout payment page
  -  Storyboard ID:
  - Pre-conditions:
  -     Model/Form: com.marketlive.app.b2c.checkout.CheckoutForm
  - URLs Posted To: /checkout/accordioncheckout
  -   Current Tile: .tile.checkout.accordion.Payment
  - Tile Variables:
  -  Child Tile(s): addressForm
  -      Copyright: (c) 2014 MarketLive, Inc. All Rights Reserved.
  --------------------------------------------------------------------------------------------------------------------%>

<%--
    //(C) Copyright MarketLive. 2014. All rights reserved.
    //MarketLive is a trademark of MarketLive, Inc.
    //Warning: This computer program is protected by copyright law and international treaties.
    //Unauthorized reproduction or distribution of this program, or any portion of it, may result
    //in severe civil and criminal penalties, and will be prosecuted to the maximum extent
    //possible under the law.
--%>
<%--  Main Form --%>
<form:form method="post" modelAttribute="paymentForm" styleId="paymentForm" role="form">	
	<div class="ml-payment-wrapper">    
		<div ng-show="!model.payment.visaCheckoutOrder">	
			<div class="ml-payment-creditCard">
				<label>
					<div class="radio ml-payment-creditCard-input"><input type="radio" name="paypalCheckoutSelected" id="creditCard" value="<%=PayPalConstants.no%>" ng-model="model.payment.paypalCheckoutSelected"/></div>
					<span class="ml-payment-creditCard-label"><span class="ml-required-label"><fmt:message key="lbl.form.required" /></span> <fmt:message key="hdr.checkout.payment.creditCard" /></span>
				</label>
			</div>
			<%--  PayPal Button --%>
			<c:if test="${PaymentModel.payPalEnabled}" >
				<div ng-show="model.payment.showPayPal">
					<div class="ml-payment-payPal" >
						<label>
							<div class="ml-payment-payPal-image"><input type="radio" name="paypalCheckoutSelected" value="<%=PayPalConstants.yes%>" ng-model="model.payment.paypalCheckoutSelected" /> <img  src="https://www.paypal.com/en_US/i/logo/PayPal_mark_50x34.gif" alt="Acceptance Mark"></div>
							<div class="ml-payment-payPal-desc" onclick="javascript:window.open('https://www.paypal.com/cgi-bin/webscr?cmd=xpt/Marketing/popup/OLCWhatIsPayPal-outside','olcwhatispaypal','toolbar=no, location=no, directories=no, status=no, menubar=no, scrollbars=yes, resizable=yes, width=400, height=350');"><fmt:message key="msg.checkout.paypal.what"/></div>
							<div class="ml-payment-payPal-clearFix" ></div>
						</label>
					</div>
				</div>
			</c:if>	
						<tiles:insertDefinition name=".tile.checkout.payment.AccordionPaymentAffirmCheckout"/>	
	        <%-- Insert Payment Credit Cards --%>
	        <tiles:insertAttribute name="paymentCreditCard" />
		</div>
		<div ng-if="model.shipping.visaCheckoutOrder">
			<form:hidden path="paypalCheckoutSelected" value="<%=VisaCheckoutConstants.VISA_CHECKOUT_YES%>"/>
		</div>	
			<%-- Insert Payment Gift Certificates --%>
			<tiles:insertAttribute name="paymentGiftCertificates" />
			<div class="ml-payment-continue-button"><input id="btnContinuePaymentForm"
	                                               name="ContinuePaymentForm"
	                                               class="ml-button-submit-primary"
	                                               type="submit"
	                                               value='<fmt:message key="btn.accordion.continue" />'
													ng-click="submitStep($event, ${stepIndex})"/></div>
	</div>
</form:form>
