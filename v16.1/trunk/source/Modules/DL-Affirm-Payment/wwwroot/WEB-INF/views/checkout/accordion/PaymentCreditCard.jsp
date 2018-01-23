<%@ include file="/WEB-INF/views/common/TagLibs.jsp" %>

<%----------------------------------------------------------------------------------------------------------------------
  -    Description: Accordion Checkout Payment Credit Card page
  -  Storyboard ID:
  - Pre-conditions:
  -     Model/Form: com.marketlive.app.b2c.checkout.CheckoutForm
  - URLs Posted To: /checkout/accordioncheckout
  -   Current Tile: .tile.checkout.accordion.Payment.CreditCard
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
<div ng-hide="model.payment.paypalCheckoutSelected == 'YES'">
<div ng-hide="model.payment.paypalCheckoutSelected == 'AFFIRM_YES'">
    <%--  New Credit Card --%>
    <div class="ml-payment-form-label"></div>
    <div class="ml-payment-form-content ml-payment-credit-cards">
        <%--<form:hidden path="creditCardType" id="creditCardType" value=""/>--%>
        <input type="hidden" id="ccRequiredForOrder" value="${ccRequiredForOrder}"/>
        <input type="hidden" name="creditCardType" ng-model="model.payment.creditCards.selectedOption" data-ml-sync-hidden/>
        <span data-ml-credit-cards data-ml-cards="model.payment.creditCards.options"
              data-ml-card-number="model.payment.creditCardNumber"
              data-ml-sync-to="model.payment.creditCards.selectedOption"
              data-ml-image-root="${siteImages.imagePath}"></span>
    </div>

    <%--  Credit Card Number --%>
    <div class="ml-payment-form-label"><span class="ml-required-label"><fmt:message key="lbl.form.required" /></span> <fmt:message key="lbl.form.creditCardNumber" /></div>
    <div class="form-group ml-payment-form-content"><input type="text" name="creditCardNumber" id="creditCardNumber" size="${site.FormFields.creditCardNumSize}" maxlength="${site.FormFields.creditCardNumMaxLength}" class="form-control" ng-model="model.payment.creditCardNumber"/></div>
    <%--  Expiration Date --%>
    <div class="ml-payment-form-label"><span class="ml-required-label"><fmt:message key="lbl.form.required" /></span> <fmt:message key="lbl.form.creditCardExpires" /></div>
    <div class="form-group ml-payment-form-content">
        <div class="ml-payment-card-month">
            <select class="form-control"
                    id = creditCardExpiryMonth
                    name="creditCardExpiryMonth"
                    ng-model="model.payment.creditCardExpiryMonth.selectedOption">
                <option value=""><fmt:message key="sel.form.creditCardMonth" /></option>
                <option ng-repeat="option in model.payment.creditCardExpiryMonth.options" value="{{option.value}}">{{option.label}}</option>
            </select>
        </div>
        <div class="ml-payment-card-year">
            <select class="form-control"
                    name="creditCardExpiryYear"
                    ng-model="model.payment.creditCardExpiryYear.selectedOption">
                <option value=""><fmt:message key="sel.form.creditCardYear" /></option>
                <option ng-repeat="option in model.payment.creditCardExpiryYear.options" value="{{option.value}}">{{option.label}}</option>
            </select>
        </div>
    </div>


    <%-- CVV2 Module Addition Start--%>
    <c:if test="${PaymentModel.siteSupportsCvv2}" >
        <div class="ml-payment-form-label"><span class="ml-required-label"><fmt:message key="lbl.form.required" /></span> <fmt:message key="lbl.form.cvv2.cardIdNumber" /></div>
        <div class="ml-payment-form-content">
            <div class="form-group ml-payment-card-cvv2">
                <input type="text" name="cvv2" id="cvv2" class="form-control" size="${PaymentModel.cvv2MaxLength}" maxlength="${PaymentModel.cvv2MaxLength}" autocomplete="off" ng-model="model.payment.cvv2" />
            </div>
            <span class="ml-payment-cvv2-desc">
                <c:url var="modalURL" value="${mlnav:urlWrapper('/checkout/cvv2.do', pageContext)}" />
                <fmt:message var="modalLinkText"  key="lnk.cvv2.howToFind" />
                <ml:modal id="cvv2Modal" styleClass="ml-cvv2-modal" url="${modalURL}" text='${modalLinkText}' />
            </span>
        </div>
    </c:if>
    </div>
</div>
<%-- CVV2 Module Addition  End--%>