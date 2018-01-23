<%@ include file="/WEB-INF/views/common/TagLibs.jsp" %>

<%-- Service URL --%>
<tilesx:useAttribute name="serviceURL" id="serviceURL"/>
<tilesx:useAttribute name="cssRootClass" id="cssRootClass" ignore="true"/>
<tilesx:useAttribute name="thirdPartyTilesDef" id="thirdPartyTilesDef" ignore="true"/>
<tilesx:useAttribute name="affirmPaymentsJavascriptSDK" id="affirmPaymentsJavascriptSDK" ignore="true"/>

<c:if test="${!empty thirdPartyTilesDef}">
    <tiles:insertDefinition name="${thirdPartyTilesDef}" />
</c:if>

<c:if test="${!empty affirmPaymentsJavascriptSDK}">
    <tiles:insertDefinition name="${affirmPaymentsJavascriptSDK}" />
</c:if>

<c:if test="${!empty cssRootClass}">
    <c:set var="cssRootClass" value=" ${cssRootClass}" />
</c:if>

<%-- Fillslots --%>
<tiles:insertAttribute name="fillslots"/>

<div id="mlAccordionCheckoutApp" class="ml-accordion-checkout${cssRootClass}" data-ml-checkout-service-url="${serviceURL}">
    <div ng-controller="mlAccordionCheckoutCtrl">
        <div class="ml-accordion-loading"  ng-class="{'ml-accordion-loaded':isInitialized()}">
            <%-- Main Content --%>
            <div class="ml-step-container">
                <%-- Main Header --%>
                <h1><fmt:message key="hdr.checkout" /></h1>


<%-- BorderFree Error message handling --%>
<c:if test="${accordionCheckoutModel.borderFreeEnabled}">
    <tiles:insertAttribute name="borderfreeErrorMessage"/>
</c:if>

<%-- BorderFree Error message handling End --%>

                <%-- Steps --%>
                <tiles:insertAttribute name="steps"/>
            </div>

            <%-- Annotations (Fillslots, Basket, Summary, etc) --%>
            <div class="ml-accordion-annotations">
                <div class="ml-accordion-annotations-container">
                    <%-- Fillslot #2--%>
                    <div class="ml-accordion-fillslot"><c:out value="${accordionKickerSlot1}" escapeXml="false"/></div>

                    <%-- Basket --%>
                    <tiles:insertAttribute name="basket"/>

                    <%-- Summary --%>
                    <tiles:insertAttribute name="summary"/>

                    <%-- Fillslot #2--%>
                    <div class="ml-accordion-fillslot"><c:out value="${accordionKickerSlot2}" escapeXml="false"/></div>
                </div>
            </div>
        </div>
    </div>
</div>