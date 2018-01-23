<%@ include file="/WEB-INF/views/common/TagLibs.jsp" %>
<%@ page import="com.marketlive.system.site.ActiveSite, com.marketlive.system.locale.ActiveLanguage"%>

<tiles:importAttribute name="context" ignore="true" />
	
	<c:set var="currencySymbol" value="<%= ActiveSite.getSite().getCurrency().getSymbol(ActiveLanguage.getUserLocale())%>" />
	
	<c:set var="affirmLogo"><fmt:message key="msg.resource.affirmMonthlyPaymentLogo"></fmt:message></c:set>
	<c:set var="affirmPricingMessage"><fmt:message key="msg.resource.affirmPricingMessage"><fmt:param value="${affirmLogo}"/></fmt:message></c:set>

	<mlperf:script defer="true" type="text/javascript">MarketLive.P2P.Affirm.onMonthlyPaymentReady(${MonthlyPaymentPriceModel.aprLoan},
											${MonthlyPaymentPriceModel.months},'${affirmPricingMessage}', ${MonthlyPaymentPriceModel.minRangePrice},
											${MonthlyPaymentPriceModel.maxRangePrice}, '${currencySymbol}');</mlperf:script>