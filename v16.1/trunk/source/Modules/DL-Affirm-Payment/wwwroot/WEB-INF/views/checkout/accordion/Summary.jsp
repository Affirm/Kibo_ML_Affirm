<%@ include file="/WEB-INF/views/common/TagLibs.jsp" %>

<jsp:useBean id="affirmWebUtil" class="com.deplabs.affirm.app.b2c.AffirmWebUtil"/>
<ct:call object="${affirmWebUtil}" method="isBorderFreeEnabledAndNotUSSelected"  fieldNameParam="${pageContext.request}" return="borderFreeEnabledAndNotUSSelected" />

<%-- Affirm Monthly Payment Price --%>
<c:if test="${!borderFreeEnabledAndNotUSSelected}">
	<tiles:insertAttribute name="affirmMonthlyPaymentPrice"/>
</c:if>

<div class="ml-accordion-summary">
    <h2><span><fmt:message key="hdr.checkout.summary"/></span></h2>
    <div class="ml-accordion-summary-group">
        <div class="ml-accordion-summary-item" ng-class="{'ml-summary-message': total.special}" ng-repeat="total in model.summary.subTotals">
            <div class="ml-accordion-summary-item-label">{{total.label}}</div>
            <div class="ml-accordion-summary-item-value">{{total.value}}</div>
        </div>
    </div>
    <div class="ml-accordion-summary-group">
        <div class="ml-accordion-summary-item" ng-class="{'ml-summary-total': !total.special, 'ml-summary-message': total.special}" ng-repeat="total in model.summary.orderTotals">
            <div class="ml-accordion-summary-item-label">{{total.label}}</div>
            <div class="ml-accordion-summary-item-value">{{total.value}}</div>
        </div>
    </div>
	    <c:if test="${MonthlyPaymentPriceModel.attributeMap['isAffirmMonthlyPaymentCartSummaryEnabled'] && !borderFreeEnabledAndNotUSSelected}">
		    <div style="float: right">
			     <tiles:insertAttribute name="affirmMonthlyPaymentPriceBasketGlobalCartCartSummaryMessage">
						<tiles:putAttribute name="total" value="${MonthlyPaymentPriceModel.attributeMap['summaryAmount']}"/>
				 </tiles:insertAttribute>
	 		</div>
	</c:if>
</div>