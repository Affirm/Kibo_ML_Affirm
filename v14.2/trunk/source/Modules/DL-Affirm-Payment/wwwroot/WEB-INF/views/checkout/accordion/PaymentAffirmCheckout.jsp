<%@ include file="/WEB-INF/views/common/TagLibs.jsp" %>

<%----------------------------------------------------------------------------------------------------------------------
  -    Description: Affirm Payment Button Tile
  -  Storyboard ID: 
  - Pre-conditions:
  -     Model/Form: 
  - URLs Posted To: 
  -   Current Tile: .tile.thirdparty.affirm.button
  - Tile Variables:
  -  Child Tile(s): 
  -      Copyright: (c) 2015 MarketLive, Inc. All Rights Reserved.
  --------------------------------------------------------------------------------------------------------------------%>

<jsp:useBean id="affirmWebUtil" class="com.deplabs.affirm.app.b2c.AffirmWebUtil"/>
<ct:call object="${affirmWebUtil}" method="isBorderFreeEnabledAndNotUSSelected"  fieldNameParam="${pageContext.request}" return="borderFreeEnabledAndNotUSSelected" />

<c:if test="${affirmEnabled && !borderFreeEnabledAndNotUSSelected}" >
	<div>
		<div class="ml-payment-payPal dl-payment-affirm">
			<label>
				<div class="dl-payment-affirm-image">
					<input type="radio" name="paypalCheckoutSelected" value="AFFIRM_YES" ng-model="model.payment.paypalCheckoutSelected">
					<span class="dl-payment-affirm-btn-1">
						<fmt:message key="lbl.resource.affirmpayment_checkout_payment_btn_1"/>
					</span>
					<img src="<fmt:message key="lnk.resource.affirmpayment_checkout_affirm_image_path"/>" alt="<fmt:message key="lbl.resource.affirmpayment_checkout_payment_method"/>" width="100px"/>
					<span class="dl-payment-affirm-btn-2">
						<fmt:message key="lbl.resource.affirmpayment_checkout_payment_btn_2"/>
					</span>
				</div>
			</label>
			
			<div class="dl-payment-affirm-description" ng-show="model.payment.paypalCheckoutSelected == 'AFFIRM_YES'">
				<fmt:message key="msg.resource.affirmpayment_checkout_payment_description"/>
				<div class="dl-payment-affirm-what-is">
					<a class="affirm-site-modal"><fmt:message key="lnk.resource.affirmpayment_checkout_what"/></a>
				</div>
			</div>
		</div>
	</div>
</c:if>