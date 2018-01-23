<%@ include file="/WEB-INF/views/common/TagLibs.jsp" %>
<%@ page import="com.marketlive.entity.currency.Amount"%>
<c:set var="ZERO" value="<%=Amount.ZERO %>" scope="request"/>

<jsp:useBean id="affirmWebUtil" class="com.deplabs.affirm.app.b2c.AffirmWebUtil"/>
<ct:call object="${affirmWebUtil}" method="isBorderFreeEnabledAndNotUSSelected"  fieldNameParam="${pageContext.request}" return="borderFreeEnabledAndNotUSSelected" />

<%-- Affirm Monthly Payment Price --%>
<c:if test="${!borderFreeEnabledAndNotUSSelected}">
<tiles:insertAttribute name="affirmMonthlyPaymentPrice"/>
</c:if>

<div class="ml-basket-message">
    <c:if test="${!empty reviewBasketChanged}">
        <div class="ml-basket-changed-review-message"><fmt:message key="err.checkout.review.basketchanged" /></div>
    </c:if>
    <c:if test="${BasketTableModel.showSaveCartMessage}">
        <c:if test="${BasketTableModel.showCartItemsChangedCartMssg}" >
            <div class="ml-basket-merge-message"><fmt:message key="msg.basket.saveCart.mergeBasket" /></div>
        </c:if>
        <c:if test="${BasketTableModel.showCartNewUserMssg}" >
            <div class="ml-basket-merge-message"><fmt:message key="msg.basket.saveCart.saveBasket" /></div>
        </c:if>
    </c:if>
</div>

<%--BORDER FREE pricing--%>
<c:set var="selectedCountryIsUS" value="${PricingModel.selectedCountryIsUS}" scope="request"/>
<c:set var="borderFreeEnable" value="${PricingModel.borderFreeIsEnable}" scope="request"/>
<c:set var="currency" value="${PricingModel.currentCurrency}" scope="request"/>
<c:set var="exchangeRate" value="${PricingModel.exchangeRate}" />
<c:set var="basketTotalMap" value="${PricingModel.basketTotalMap}" />
<c:choose>
     <c:when test ="${borderFreeEnable && !selectedCountryIsUS && !empty basketTotalMap}">
     <c:set var="exchangeRate" value="${PricingModel.noExchangeRate}" />
    <%--Border Free --%>
        <c:set var="merchandiseTotal" value="${basketTotalMap['totalSalePrice']}" />
        <c:if test ="${empty merchandiseTotal}">
            <c:set var="merchandiseTotal"  value="${ZERO}" />
        </c:if>

        <c:set var="basketSubTotal" value="${basketTotalMap['basketPageTotalPrice']}" />
        <c:if test ="${empty basketSubTotal}">
            <c:set var="basketSubTotal" value="${ZERO}" />
        </c:if>

        <c:set var="discountTotal" value="${basketTotalMap['totalOrderDiscount']}" />
        <c:if test ="${empty discountTotal}">
            <c:set var="discountTotal" value="${ZERO}" />
        </c:if>

		<c:set var="orderLevelDiscount" value="${basketTotalMap['orderDiscount']}" />
		<c:if test ="${empty orderLevelDiscount}">
            <c:set var="orderLevelDiscount" value="${ZERO}" />
        </c:if>
    </c:when>
    <c:otherwise>
    <c:set var="merchandiseTotal" value="${BasketTableModel.basket.merchandiseTotal}" />
    <c:set var="basketSubTotal" value="${BasketTableModel.subtotal}" />
    <c:set var="discountTotal" value="${BasketTableModel.discountTotal}" />
    </c:otherwise>
</c:choose>

<c:if test="${BasketTableModel.showDiscount}">
    <c:set var="showDiscount">true</c:set>
</c:if>

<c:set var="estTaxShippingEnabled">${BasketTableModel.estTaxShippingEnabled}</c:set>

<%--
<c:set var="showDiscount">false</c:set>
<c:set var="estTaxShippingEnabled">${false}</c:set>
--%>

<c:set var="classDiscount" value="ml-basket-item-discount-hide"></c:set>
<c:if test="${showDiscount}">
    <c:set var="classDiscount" value="ml-basket-item-discount-show"></c:set>
</c:if>

<div class="ml-basket-row ml-basket-items ml-basket-item-header">
    <div class="ml-basket-column  ml-basket-items-container">
        <div class="ml-basket-row">
            <div class="ml-basket-column ml-basket-item-details-container ${classDiscount}">
                <div class="ml-basket-row">
                    <div class="ml-basket-column ml-basket-item-thumb"><fmt:message key="hdr.itemTable.itemNumber" /></div>
                    <div class="ml-basket-column ml-basket-item-product"></div>

                    <%-- thumb place holder for xs size --%>

                    <div class="ml-thumb-placeholder"></div>

                    <div class="ml-basket-item-row-wrapper">
                        <div class="ml-basket-column ml-basket-item-qty"><fmt:message key="hdr.itemTable.qty" /></div>
                        <div class="ml-basket-column ml-basket-item-totals-container">
                            <div class="ml-basket-row">
                                <div class="ml-basket-column ml-basket-item-price ml-basket-price">
                                    <div class="ml-basket-total-title"><fmt:message key="hdr.itemTable.priceEach" /></div>
                                </div>

                                <div class="ml-basket-column ml-basket-item-discount ml-basket-price">
                                    <c:if test="${showDiscount}">
                                        <div class="ml-basket-total-title"><fmt:message key="hdr.itemTable.discount" /></div>
                                    </c:if>
                                </div>

                                <div class="ml-basket-column ml-basket-item-total ml-basket-price">
                                    <div class="ml-basket-total-title"><fmt:message key="hdr.itemTable.totalPrice" /></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<c:set var="itemDataMap" value="${PricingModel.skuPriceQuoteMap}" />
<%-- Iterate over the basket items --%>
<c:forEach var="basketItem" varStatus="status" items="${BasketTableModel.sortedItems}">
    <c:set var="itemIndex" value="${status.index}" scope="request"/>
    <tiles:insertAttribute name="basketItemTableRow">
        <tiles:putAttribute name="basketItem" value="${basketItem}" type="org.marketlive.entity.cart.basket.IBasketItem"/>
        <tiles:putAttribute name="Sku" value="${basketItem.cartItemType}" type="org.marketlive.entity.sku.ISku"/>
        <tiles:putAttribute name="classDiscount" value="${classDiscount}"/>
        <tiles:putAttribute name="showDiscount" value="${showDiscount}"/>
        <tiles:putAttribute name="exchangeRate" value="${exchangeRate}"/>
        <tiles:putAttribute name="itemDataMap" value="${itemDataMap}"/>
    </tiles:insertAttribute>
</c:forEach>

<%-- Start source code and tax shipping input--%>
<div class="ml-basket-row ml-basket-info-container">
    <div class="ml-basket-column ml-basket-additional">
        <div class="ml-basket-input-source-wrapper">
            <div class=sourcecodeBox>
                <div class="ETSBoxmsg ml-basket-input-source-title">
                    <div class="ml-basket-source-code-label"><fmt:message key="lbl.basket.sourcecode"/></div>
                    <div class="ml-basket-source-code-whatisit">
                        <c:set var="whatisthis"><fmt:message key="msg.basket.sourcecode.whatisthis"/></c:set>
                        <c:set var="whatisthisLabel"><fmt:message key="lbl.basket.sourcecode"/></c:set>
                        <a id="whatisthis" class="ml-popover-link" ml-title="${whatisthisLabel}" ml-content="${whatisthis}" data-placement="top">
                            <fmt:message key="lbl.basket.sourcecode.whatisthis"/>
                        </a>
                    </div>
                </div>
                <div class="ml-basket-input-source">
                    <div class="ml-basket-input-wrapper">
                        <input class="form-control couponCodeField" type="text" name="sourceCode" id="sourceCode" size="${site.FormFields.sourceCodeSize}" value="<c:out value="${basketForm.sourceCode}" />" maxlength="${site.FormFields.sourceCodeMaxLength}"/>
                    </div>
                    <div class="ml-basket-button-wrapper">
                        <input class="form_but sourceCodeApplyBtn ml-secondary-button" type="button" value="<fmt:message key='btn.basket.sourcecode.apply'/>"/>
                    </div>
                </div>
            </div>
        </div>
        <%-- Est tax shipping input --%>
        <c:if test="${estTaxShippingEnabled}">
            <div class="ml-est-tax-shipping">
                <tiles:insertAttribute name="esttaxshipinput">
                    <tiles:putAttribute name="showSubTotal" value="${showSubTotal}"/>
                    <tiles:putAttribute name="exchangeRate" value="${exchangeRate}"/>
                </tiles:insertAttribute>
            </div>
        </c:if>
    </div>

    <div class="ml-basket-column ml-basket-totals">
        <%-- Iterate over Order Discounts --%>
        <div class="ml-basket-row ml-basket-merchandise-subtotal">
            <div class="ml-basket-column ml-basket-total-label"><fmt:message key="msg.order.shipment.merchandiseSubtotal" /></div>
            <div class="ml-basket-column ml-basket-total-value"><mlamt:formatAmount currency ="${currency}" exchangeRate="${exchangeRate}" value="${merchandiseTotal}" /></div>
        </div>

        <div class="ml-basket-row  ml-basket-order-msg">
            <%-- if border free and not US--%>
            <%-- Iterate over Order Discounts --%>
            <c:forEach var="orderDiscount" varStatus="status" items="${BasketTableModel.basket.discounts}">
                <c:set var="showSubTotal" value="true"/>
                <c:set var="discountName" value="&nbsp;"/>
                <c:if test="${orderDiscount.showMessage}">
                    <c:set var="discountName" value="${orderDiscount.message}" />
                </c:if>
                <div class="ml-basket-row ml-basket-discount-order">
                       <div class="ml-basket-column ml-basket-total-label ml-discount-label">
                           <c:out value="${discountName}" escapeXml="false"/>
                       </div>
                       <c:choose>
                           <c:when test ="${borderFreeEnable && !selectedCountryIsUS}">
                               <c:if test ="${status.last && orderLevelDiscount > ZERO}">
                                   <div class="ml-basket-column ml-basket-total-value ml-discount-value">-<mlamt:formatAmount currency ="${currency}" exchangeRate="${exchangeRate}" value="${orderLevelDiscount}"/></div>
                               </c:if>
                           </c:when>
                           <c:otherwise>
                               <div class="ml-basket-column ml-basket-total-value ml-discount-value">-<mlamt:formatAmount currency ="${currency}" exchangeRate="${PricingModel.exchangeRate}" value="${orderDiscount.amount}"/></div>
                           </c:otherwise>
                       </c:choose>
                </div>
            </c:forEach>

            <%-- Oversized Shipping / Weight Surcharge Total Display --%>
            <c:if test="${BasketTableModel.basket.weightSurchargeTotal > ZERO }">
                <c:set var="showSubTotal" value="true"/>
                <div class="ml-basket-row ml-basket-shipping-overweight">
                    <div class="ml-basket-column ml-basket-total-label ml-overweight-label"><fmt:message key="msg.order.shipment.oversizedShipping" /></div>
                    <div class="ml-basket-column ml-basket-total-value ml-overweight-value"><mlamt:formatAmount currency ="${currency}" exchangeRate="${PricingModel.exchangeRate}" value="${BasketTableModel.basket.weightSurchargeTotal}" /></div>
                </div>
            </c:if>
		</div>

        <%-- SubTotal --%>
        <c:if test="${showSubTotal}">
            <div class="ml-basket-total-separator-wrapper"><div class="ml-basket-total-separator"></div></div>
            <div class="ml-basket-row ml-basket-subtotal">
                    <div class="ml-basket-column ml-basket-total-label"><fmt:message key="msg.order.shipment.subtotal" /></div>
                    <div class="ml-basket-column ml-basket-total-value"><mlamt:formatAmount currency ="${currency}" exchangeRate="${exchangeRate}" value="${basketSubTotal}" /></div>
            </div>
        </c:if>

        <c:if test="${estTaxShippingEnabled}">
            <div class="ml-est-tax-shipping">
                <tiles:insertAttribute name="esttaxshiptotals">
                    <tiles:putAttribute name="showSubTotal" value="${showSubTotal}"/>
                    <tiles:putAttribute name="currency" value="${currency}"/>
                    <tiles:putAttribute name="exchangeRate" value="${exchangeRate}"/>
                </tiles:insertAttribute>
            </div>
        </c:if>

        <%-- You Saved --%>
        <c:if test="${BasketTableModel.showYouSaved}">
            <div class="ml-basket-row ml-basket-you-saved">
                <div class="ml-basket-column ml-basket-total-label ml-basket-you-saved-label"><fmt:message key="msg.order.discount.youSaved" /></div>
                <div class="ml-basket-column ml-basket-total-value ml-basket-you-saved-value"><mlamt:formatAmount currency ="${currency}" exchangeRate="${exchangeRate}" value="${discountTotal}" /></div>
            </div>
        </c:if>
        
        <%-- Affirm Monthly Payment Price --%>
	    <c:if test="${MonthlyPaymentPriceModel.attributeMap['isAffirmMonthlyPaymentBasketEnabled'] && !borderFreeEnabledAndNotUSSelected}">
			<tiles:insertAttribute name="affirmMonthlyPaymentPriceBasketGlobalCartCartSummaryMessage">
				<tiles:putAttribute name="total" value="${EstTaxShipModel.basket.total}"/>
			</tiles:insertAttribute>
		</c:if>

        <%-- Iterate over Shipment Discounts --%>
        <c:forEach var="shipDiscount" varStatus="status" items="${BasketTableModel.shippingDiscounts}">
            <c:if test="${shipDiscount.showMessage}" >
                <c:if test="${status.index == 0}">
                    <div class="ml-basket-total-separator"></div>
                </c:if>
                <div class="ml-basket-row ml-discount-msg">
                    <c:out value="${shipDiscount.message}" escapeXml="false" />
                </div>
            </c:if>
        </c:forEach>

    </div>
</div>

<mlperf:script defer="true">
    jQuery(document).ready(function () {
        setTimeout(function () {
            var suc_redeemed = '${INVALID_SUC}';
            if ((suc_redeemed != null) && (suc_redeemed.length > 0)) {
                jQuery(".sourceCodeApplyBtn").click();
            }
        	jQuery('#sourceCode').keypress(function(event) {
        		var keycode = (event.keyCode ? event.keyCode : event.which);
        		if(keycode == '13') {
        			jQuery(".sourceCodeApplyBtn").click();
        			event.preventDefault();
        		}
        	});
        }, 10);
    });
</mlperf:script>
