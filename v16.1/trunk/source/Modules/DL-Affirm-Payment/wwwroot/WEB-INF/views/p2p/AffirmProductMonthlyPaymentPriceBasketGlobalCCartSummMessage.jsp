<%@ include file="/WEB-INF/views/common/TagLibs.jsp" %>

<tiles:importAttribute name="total"/>
<div class="dl-affirm-monthly-payment-price">
		<a class="learn-more" style="visibility:hidden" href="#" data-affirmprice="${total.getAsString()}"></a>
</div>
