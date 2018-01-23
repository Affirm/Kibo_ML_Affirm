<%@ include file="/WEB-INF/views/common/TagLibs.jsp" %>

<%----------------------------------------------------------------------------------------------------------------------
  -    Description: Affirm Payment JavaScript SDK
  -  Storyboard ID: 
  - Pre-conditions:
  -     Model/Form: com.deplabs.app.b2c.thirdparty.affirm.payments.common
  - URLs Posted To: 
  -   Current Tile: .tile.thirdparty.affirm.payments.common.javaScriptSDK
  - Tile Variables:
  -  Child Tile(s): 
  -      Copyright: (c) 2014 MarketLive, Inc. All Rights Reserved.
  --------------------------------------------------------------------------------------------------------------------%>

<c:set var="affirmApiKey" value="${AffirmJavaScriptSDKModel.affirmApiKey}" scope="request" />
<c:set var="affirmEnabled" value="${AffirmJavaScriptSDKModel.affirmEnabled}" scope="request" />
<c:set var="affirmJsUrl" value="${AffirmJavaScriptSDKModel.affirmJSUrl}/js/v2/affirm.js" scope="request" />  <%--https://cdn1-sandbox.affirm.com/js/v2/affirm.js --%>
<c:set var="whatIsAffirmURL" value="${AffirmJavaScriptSDKModel.whatIsAffirmURL}" scope="request" />

<c:if test="${affirmEnabled}">
  <script type='text/javascript'>
  		var _affirm_config = {
		    public_api_key:  "${affirmApiKey}",
		    script:          "${affirmJsUrl}"
		  };
  </script>

  <script type="text/javascript" src="${affirmJsUrl}"></script>
</c:if>
